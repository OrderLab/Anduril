package feedback.parser

import feedback.common.Env
import feedback.log.{DistributedWorkloadLog, Log, UnitTestLog}
import org.slf4j.LoggerFactory

import java.io.File
import java.nio.file.Path

object LogParser {
  private val LOG = LoggerFactory.getLogger(getClass)

  def parseLog(rootDir: File): Log = {
    if (rootDir.isDirectory) {
      val rootPath = rootDir.toPath
      DistributedWorkloadLog(Env.parallel(rootDir.listFiles(dir => dir.isDirectory).toSeq
        .flatMap(dir => TextParser.parseLogDirId(dir.getName))
        .sorted.zipWithIndex.map {
        case (id, index) =>
          require(id == index)
          val files = rootPath.resolve(TextParser.getLogDir(id)).toFile.listFiles
            .filter(file => !file.isDirectory && file.getName.endsWith(".log"))
          require(files.length == 1)
          files(0)
      }, (file: File) => LogFileParser.parseLogFile(file)).get.map{
        case (log, None) => log
      }.toArray)
    } else {
      LogFileParser.parseLogFile(rootDir) match {
        case (log, Some(result)) => UnitTestLog(log, result)
        case (log, None) =>
          val begin = log.entries(0).showtime
          val end = log.entries.last.showtime
          LOG.warn("Found unit test log without test result, duration = {} ms", end.getMillis - begin.getMillis)
          throw new RuntimeException("No test result")
      }
    }
  }

  def parseLog(path: Path): Log = parseLog(path.toFile)

  def parseLog(path: String): Log = parseLog(new File(path))
}
