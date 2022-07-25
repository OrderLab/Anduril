package feedback

import feedback.parser.DistributedLog

import javax.json.JsonObject

object Symptoms {
  private def error = throw new Exception("can't recognize the bug case")

  def isSymptomLogged(spec: JsonObject): Boolean = spec.getString("case") match {
    case "hdfs-4233" => true
    case "zookeeper-3006" => false
    case _ => error
  }

  def checkSymptom(trial: DistributedLog, spec: JsonObject): Boolean = spec.getString("case") match {
    case "zookeeper-3006" =>
      require(!trial.distributed)
      trial.logs(0).entries.exists(_.msg.contains(
        "\n1) testAbsentRecentSnapshot(org.apache.zookeeper.test.ZkDatabaseCorruptionTest)\njava.lang.NullPointerException\n"))
    case "hdfs-4233" =>
      require(trial.distributed)
      require(trial.dirs.zipWithIndex.forall{
        case (dir, i) => dir.getName.equals(s"logs-$i")
      })
      trial.logs(1).entries.exists(entry =>
        entry.file.equals("Server$Handler") &&
          entry.fileLogLine == 1538 &&
          entry.`type`.equals("INFO") &&
          entry.msg.contains("at org.apache.hadoop.hdfs.server.namenode.FSEditLog.startLogSegment(FSEditLog.java:835)\n"))
    case _ => error
  }
}
