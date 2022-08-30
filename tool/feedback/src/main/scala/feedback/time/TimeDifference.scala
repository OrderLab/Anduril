package feedback.time

import feedback.log.{DistributedWorkloadLog, Log, LogFile, UnitTestLog}
import org.joda.time.DateTime

final class TimeDifference(val good: DateTime, val bad: DateTime) {
  val difference: Long = bad.getMillis - good.getMillis

  def this(pair: (DateTime, DateTime)) = this(pair._1, pair._2)
  def this(good: LogFile, bad: LogFile) = this(good.showtime, bad.showtime)

  def this(good: Log, bad: Log) = this((good, bad) match {
    case (UnitTestLog(good, _), UnitTestLog(bad, _)) =>
      (good.showtime, bad.showtime)
    case (DistributedWorkloadLog(good), DistributedWorkloadLog(bad)) =>
      require(good.length == bad.length)
      good.map(_.showtime).zip(bad.map(_.showtime)).min
  })

  def good2bad(good: DateTime): DateTime = good.plus(difference)
}
