package feedback.parser

import java.io.File

import feedback.common.Env
import org.joda.time.DateTime

import scala.collection.mutable

final case class InjectionInTrace(val pid: Int, val id: Int, val occurrence: Int,
                                 val time: DateTime,
                                 val thread: String)  {

}

object InjectionRecordsReader {

  def readRecordCSVs (rootDir: File): Array[Array[InjectionInTrace]] = {
    if (rootDir.isDirectory) {
      val rootPath = rootDir.toPath
      Env.parallel(rootDir.listFiles().toSeq
        .flatMap(f => TextParser.parseInjectionRecordFileId(f.getName))
        .sorted.zipWithIndex.map {
        case (id, index) =>
          require(id == index)
          val files = rootPath.resolve(TextParser.getRecordFile(id)).toFile
      }, (file: File) => readSingleRecordCSV(file)).get.map{
        case (injectionArray, None) => injectionArray
      }.toArray
    } else {
      var z = new Array[Array[InjectionInTrace]](1)
      z(0) = readSingleRecordCSV(rootDir).toArray
      z
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
