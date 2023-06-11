package feedback.parser

import java.io.File

import feedback.common.Env
import org.joda.time.DateTime

import scala.collection.mutable

final case class InjectionInTrace(val pid: Int, val id: Int, val occurrence: Int,
                                 val time: DateTime,
                                 val thread: String)  {

}

sealed trait InjectionTrace

final case class UnitTestInjectionTrace(trace:Array[InjectionInTrace]) extends  InjectionTrace {

}

final case class DistributedInjectionTraces(traces:Array[Array[InjectionInTrace]]) extends InjectionTrace {

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

  def readSingleRecordCSV (f : File): Array[InjectionInTrace] = {
    val text = ParserUtil.getFileLinesAsync(f)
    var line = text.next()
    val injections = mutable.ArrayBuffer.empty[InjectionInTrace]
    while (text.hasNext) {
      line = text.next()
      val Array(id, occurrence, time, thread) = line.split(",").map(_.trim)
      injections += InjectionInTrace(-1, id.toInt, occurrence.toInt, LogFileParser.parseDatetime(time), thread)
    }
    injections.toArray
  }
}
