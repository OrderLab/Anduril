package feedback.parser

import feedback.ThreadUtil
import feedback.log.{DistributedWorkloadLog, Log, LogFile, TestResult, UnitTestLog}

import java.io.File
import java.nio.file.Path
import java.util.concurrent.Callable

object LogParser {
  def parseLog(rootDir: File): Log = {
    if (rootDir.isDirectory) {
      val rootPath = rootDir.toPath
      val logs = rootDir.listFiles(dir => dir.isDirectory)
        .flatMap(dir => TextParser.parseLogDirId(dir.getName))
        .sorted.zipWithIndex.map {
        case (id, index) =>
          require(id == index)
          val files = rootPath.resolve(TextParser.getLogDir(id)).toFile.listFiles
            .filter(file => !file.isDirectory && file.getName.endsWith(".log"))
          require(files.length == 1)
          ThreadUtil.submit(() => LogFileParser.parseLogFile(files(0)))
      }
      DistributedWorkloadLog(logs.map(_.get).map {
          case (log, None) => log
      })
    } else {
      LogFileParser.parseLogFile(rootDir) match {
        case (log, Some(result)) => UnitTestLog(log, result)
      }
    }
  }

  def parseLog(path: Path): Log = parseLog(path.toFile)

  def parseLog(path: String): Log = parseLog(new File(path))
}
