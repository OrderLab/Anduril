package feedback.parser.grammar

import feedback.log.exception.{MsgExceptionRecord, NestedException, PlainExceptionRecord, StackTrace}

import scala.collection.mutable

sealed trait Trunk {
  val begin: Int
  val end: Int

  final def length: Int = end - begin
}

sealed trait ExceptionTrunk extends Trunk {
  val exception: String
  require(exception != null && exception.nonEmpty)

  val msg: Option[String]

  final override val end: Int = begin + 1
}

final case class MsgTrunk(override val begin: Int, override val end: Int) extends Trunk

final case class HeaderExceptionTrunk(override val begin: Int,
                                      override val exception: String,
                                      override val msg: Option[String]) extends ExceptionTrunk

final case class NestedExceptionTrunk(override val begin: Int,
                                      override val exception: String,
                                      override val msg: Option[String]) extends ExceptionTrunk

final case class StackTraceTrunk(override val begin: Int,
                                 override val end: Int,
                                 stacktrace: StackTrace) extends Trunk {
  require(stacktrace != null)
}

// use context-free grammar and dynamic programming to maximize the number of nested exceptions
object TrunkGrammar {
  def calculate(trunks: Array[Trunk]): ((Int, Int), Array[(Int, Int)]) = {

    def update(i: Int, withMsg: Boolean, op: (Int, Int) => Unit): Unit = {
      if (withMsg) {
        (i + 1 until trunks.length) foreach { j =>
          trunks(j) match {
            case StackTraceTrunk(_, _, _) =>
              op(j + 1, i)
            case _ =>
          }
        }
      } else {
        if (i + 1 < trunks.length) {
          trunks(i + 1) match {
            case StackTraceTrunk(_, _, _) =>
              op(i + 2, i)
            case _ =>
          }
        }
      }
    }

    val head = Array.fill(trunks.length + 1) { -1 }
    trunks.indices foreach { i =>
      trunks(i) match {
        case HeaderExceptionTrunk(_, _, msg) =>
          update(i, msg.nonEmpty, (next, current) => {
            if (head(next) == -1) {
              head(next) = current
            }
          })
        case _ =>
      }
    }
    if (head exists { _ != -1 }) {
      val weight = head.map(w => if (w == -1) 0 else 1).toArray
      trunks.indices foreach { i =>
        //  weight(i) > 0 means there exists a head exception trunk
        if (weight(i) > 0) {
          trunks(i) match {
            case NestedExceptionTrunk(_, _, msg) =>
              update(i, msg.nonEmpty, (next, current) => {
                if (weight(next) < weight(current) + 1) {
                  weight(next) = weight(current) + 1
                  head(next) = current
                }
              })
            case _ =>
          }
        }
      }
      if (head.last == -1) ((0, trunks.length), Array()) else {
        val exceptions = mutable.ArrayBuffer.empty[(Int, Int)]
        var i = head.length - 1
        var msg = (0, 0)
        while (i != -1) {
          val range = if (head(i) == -1) (0, i) else (head(i), i)
          if (weight(i) == 0) {
            msg = range
          } else {
            exceptions += range
          }
          i = head(i)
        }
        (msg, exceptions.reverse.toArray)
      }
    } else ((0, trunks.length), Array())
  }

  def calculate(text: Array[String],
                trunks: Array[Trunk],
                t: ((Int, Int), Array[(Int, Int)])): (Option[String], Option[NestedException]) = {

    def aggregate(init: String, begin: Int, end: Int): String =
      if (begin == end) init else {
        require(begin < end)
        val builder = new mutable.StringBuilder(init)
        (begin until end) foreach { builder append text(_) }
        builder.toString
      }

    t match {
      case ((begin, end), exceptions) =>
        (if (begin == end) None else Some(aggregate("", trunks(begin).begin, trunks(end - 1).end)),
          if (exceptions.isEmpty) None else Some(new NestedException((exceptions map { case (begin, end) =>
            trunks(end - 1) match {
              case StackTraceTrunk(_, _, stacktrace) =>
                (trunks(begin) match {
                  case HeaderExceptionTrunk(_, exception, msg) =>
                    (exception, msg)
                  case NestedExceptionTrunk(_, exception, msg) =>
                    (exception, msg)
                }) match {
                  case (exception, Some(msg)) =>
                    require(begin + 1 < end)
                    val finalMsg = aggregate(msg, trunks(begin).begin + 1, trunks(end - 2).end)
                    MsgExceptionRecord(exception, finalMsg, stacktrace)
                  case (exception, None) =>
                    PlainExceptionRecord(exception, stacktrace)
                }
            }
          }).toArray)))
    }
  }

  def calculate(text: Array[String], trunks: Array[Trunk]): (Option[String], Option[NestedException]) =
    calculate(text, trunks, calculate(trunks))
}
