package feedback.symptom

import feedback.cases.BugCase
import feedback.log.Log
import feedback.log.entry.LogEntry
import feedback.time.Timing
import org.joda.time.DateTime

import javax.json.JsonObject

trait SymptomEvent extends Timing

final case class UnitTestLogEvent(logEntry: LogEntry) extends SymptomEvent {
  require(logEntry != null)

  override val showtime: DateTime = logEntry.showtime
}

final case class DistributedWorkloadLogEvent(node: Int, logEntry: LogEntry) extends SymptomEvent {
  require(node > 0)
  require(logEntry != null)

  override val showtime: DateTime = logEntry.showtime
}

object Symptoms {
  def isResultEventLogged(bug: String): Boolean = BugCase.cases.get(bug).exists(_.isResultEventLogged)

  def isResultEventLogged(spec: JsonObject): Boolean = isResultEventLogged(spec.getString("case"))

  def findSymptom(log: Log, bug: String): Option[List[Timing]] =
    BugCase.cases.get(bug).flatMap { _.findSymptom(log) }

  def findResultEvent(log: Log, bug: String): Option[Timing] =
    BugCase.cases.get(bug).flatMap { _.findResultEvent(log) }

  def findResultEvent(log: Log, spec: JsonObject): Option[Timing] =
    findResultEvent(log, spec.getString("case"))

  def hasResultEvent(log: Log, spec: JsonObject): Boolean =
    findResultEvent(log, spec.getString("case")).isDefined

}
