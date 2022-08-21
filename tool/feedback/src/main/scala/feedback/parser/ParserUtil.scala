package feedback.parser

import feedback.{AsyncIterator, ThreadUtil}

import java.io.File
import java.nio.file.{Files, Path}
import scala.io.Source
import scala.util.{Success, Using}

object ParserUtil {
  private val MAX_FILE_SIZE = 200_000_000L  // in bytes

  def getFileLines(file: File): Array[String] = {
    require(Files.size(file.toPath) < MAX_FILE_SIZE)
    Using(Source.fromFile(file)) {_.getLines().toArray} match {
      case Success(lines) => lines
    }
  }

  def getFileLines(path: Path): Array[String] = getFileLines(path.toFile)

  def getFileLines(path: String): Array[String] = getFileLines(new File(path))

  def getFileLinesAsync(file: File): Iterator[String] = {
    require(Files.size(file.toPath) < MAX_FILE_SIZE)
    new AsyncIterator[String](add => Using(Source.fromFile(file)) { file =>
      val iterator = file.getLines()
      while (iterator.hasNext) {
        add(iterator.next())
      }
    } match {
      case Success(_) =>
    })
  }

  def getFileLinesAsync(path: Path): Iterator[String] = getFileLinesAsync(path.toFile)
}
