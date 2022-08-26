package feedback.log.exception

// stack trace record

sealed trait StackTraceRecord extends Serializable {
  val className: String
  require(className != null && className.nonEmpty)

  val methodName: String
  require(methodName != null && methodName.nonEmpty)

  val literal: String
}

sealed trait JUnit5StackTraceRecord extends StackTraceRecord

// stack trace element

final case class NormalStackTraceElement(override val className: String,
                                         override val methodName: String,
                                         fileName: String,
                                         lineNumber: Int) extends StackTraceRecord {
  require(fileName != null && fileName.nonEmpty)
  require(lineNumber > 0)  // TODO: check -1

  override val literal = s"$className.$methodName($fileName:$lineNumber)"
}

final case class NativeStackTraceElement(override val className: String,
                                         override val methodName: String) extends StackTraceRecord {
  override val literal = s"$className.$methodName(Native Method)"
}

final case class UnknownStackTraceElement(override val className: String,
                                          override val methodName: String) extends StackTraceRecord {
  override val literal = s"$className.$methodName(Unknown Source)"
}

final case class JUnit5NormalStackTraceElement(override val className: String,
                                               override val methodName: String,
                                               fileName: String,
                                               lineNumber: Int) extends JUnit5StackTraceRecord {
  require(fileName != null && fileName.nonEmpty)
  require(lineNumber > 0) // TODO: check -1

  override val literal = s"$className.$methodName($fileName:$lineNumber)"
}

final case class JUnit5NativeStackTraceElement(override val className: String,
                                               override val methodName: String) extends JUnit5StackTraceRecord {
  override val literal = s"$className.$methodName(Native Method)"
}

final case class JUnit5UnknownStackTraceElement(override val className: String,
                                                override val methodName: String) extends JUnit5StackTraceRecord {
  override val literal = s"$className.$methodName(Unknown Source)"
}

// stack trace

final class CallStack(val stack: Array[StackTraceRecord]) extends Serializable {
  require(stack != null && stack.nonEmpty && !stack.contains(null))
  override def toString: String = stack.mkString("CallStack(", ", ", ")")
}

sealed trait StackTrace extends Serializable {
  val stack: CallStack
  require(stack != null)

  def matchPrefix(sites: String *): Boolean =
    if (stack.stack.length < sites.length) false
    else !sites.to(LazyList).zip(stack.stack).exists {
      case (string, stacktrace) => !(string equals stacktrace.literal)
    }
}

final case class NormalStackTrace(override val stack: CallStack) extends StackTrace

final case class MoreStackTrace(override val stack: CallStack, more: Int) extends StackTrace {
  require(more > 0)
}

sealed trait JUnit5StackTrace extends StackTrace

final case class JUnit5NormalStackTrace(override val stack: CallStack) extends JUnit5StackTrace

final case class JUnit5MoreStackTrace(override val stack: CallStack) extends JUnit5StackTrace

// exception

sealed trait ExceptionRecord extends Serializable {
  val exception: String
  require(exception != null &&
    (exception.endsWith("Exception") || exception.endsWith("Error") || exception.endsWith("Throwable")))

  val stacktrace: StackTrace
  require(stacktrace != null)
}

final case class PlainExceptionRecord(override val exception: String,
                                      override val stacktrace: StackTrace) extends ExceptionRecord

final case class MsgExceptionRecord(override val exception: String,
                                    msg: String,
                                    override val stacktrace: StackTrace) extends ExceptionRecord {
  require(msg != null && msg.nonEmpty)
}

final class NestedException(val exceptions: Array[ExceptionRecord]) extends Serializable {
  require(exceptions != null && exceptions.nonEmpty && !exceptions.contains(null))
  override def toString: String = exceptions.mkString("NestedException(", "\n", ")")
}
