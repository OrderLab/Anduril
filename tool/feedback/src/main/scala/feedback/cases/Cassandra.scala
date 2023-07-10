package feedback.cases

import scala.annotation.unused

@unused
final class Cassandra_17663 extends UnitTestWorkload {
  override val ok_is_good: Boolean = false
}

@unused
final class Cassandra_6415 extends DistributedWorkload {
  override val targetNode: Int = 0
}


