package feedback.time

import org.joda.time.DateTime

trait Timing extends Ordered[Timing] with Serializable {
  val showtime: DateTime
  // TODO: require showtime != null

  override def compare(that: Timing): Int = this.showtime.compareTo(that.showtime)
}
