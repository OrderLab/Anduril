package feedback.parser

import java.io.File

import feedback.common.Env
import org.joda.time.DateTime

import scala.collection.mutable

final case class InjectionPoint(val pid: Int, val id: Int, val occurrence: Int,
                                 val time: DateTime,
                                 val thread: String)  {

}

sealed trait InjectionTrace

final case class UnitTestInjectionTrace(trace:Array[InjectionPoint]) extends  InjectionTrace {

}

final case class DistributedInjectionTraces(traces:Array[Array[InjectionPoint]]) extends InjectionTrace {

}

object InjectionRecordsReader {

  def readRecordCSVs (rootDir: File): InjectionTrace = {
    if (rootDir.isDirectory) {
      val rootPath = rootDir.toPath
      DistributedInjectionTraces(Env.parallel(rootDir.listFiles().toSeq
        .flatMap(f => TextParser.parseInjectionRecordFileId(f.getName))
        .sorted.zipWithIndex.map {
        case (id, index) =>
          require(id == index)
          val file = rootPath.resolve(TextParser.getRecordFile(id)).toFile
          file
      }, (file: File) => readSingleRecordCSV(file)).get.toArray)
    } else {
      UnitTestInjectionTrace(readSingleRecordCSV(rootDir))
    }
  }

  def readSingleRecordCSV (f : File): Array[InjectionPoint] = {
    val text = ParserUtil.getFileLinesAsync(f)
    var line = text.next()
    val injections = mutable.ArrayBuffer.empty[InjectionPoint]
    while (text.hasNext) {
      line = text.next()
      //val Array(pid, id, occurrence, time, millies,thread) = line.split(",").map(_.trim)
      val array = line.split(",")
      val pid = array(0)
      val id = array(1)
      val occurrence = array(2)
      val time = array(3)
      val millies = array(4)
      var thread = array(5)
      if (array.size >= 7) {
        for (i <- 6 until array.size) {
          thread = thread+","+array(i)
        }
      }
      injections += InjectionPoint(pid.toInt, id.toInt, occurrence.toInt, LogFileParser.parseDatetime(time+","+millies), thread)
    }
    injections.toArray
  }

  def readRecordCSVs(path: String): Option[InjectionTrace] = Some(readRecordCSVs(new File(path)))
}
