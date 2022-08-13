package feedback

import feedback.parser.{DistributedLog, LogEntry}

import javax.json.JsonObject

private[feedback] object Symptoms {
  private def error = throw new Exception("can't recognize the bug case")

  def isSymptomLogged(spec: JsonObject): Boolean = spec.getString("case") match {
    case "hdfs-4233" => true
    case "zookeeper-3006" => false
    case _ => error
  }

  private def singletonMap(t: (java.lang.Integer, Option[LogEntry])) = t match {
    case (node, entry) => entry.map(e => java.util.Collections.singletonMap(node, java.util.Arrays.asList(e)))
  }

  private def findSymptom(trial: DistributedLog, spec: JsonObject) = spec.getString("case") match {
    case "zookeeper-3006" =>
      require(!trial.distributed)
      singletonMap (-1, trial.logs(0).entries.find(_.msg.contains(
        "\n1) testAbsentRecentSnapshot(org.apache.zookeeper.test.ZkDatabaseCorruptionTest)\njava.lang.NullPointerException\n")))
    case "hdfs-4233" =>
      require(trial.distributed)
      require(trial.dirs.zipWithIndex.forall{
        case (dir, i) => dir.getName.equals(s"logs-$i")
      })
      val node = 1
      singletonMap (node, trial.logs(node).entries.find(entry =>
        entry.file.equals("Server$Handler") &&
          entry.fileLogLine == 1538 &&
          entry.`type`.equals("INFO") &&
          entry.msg.contains("at org.apache.hadoop.hdfs.server.namenode.FSEditLog.startLogSegment(FSEditLog.java:835)\n")))
    case _ => error
  }

  def checkSymptom(trial: DistributedLog, spec: JsonObject): Boolean = findSymptom(trial, spec).nonEmpty

  def getSymptom(trial: DistributedLog, spec: JsonObject): java.util.Map[java.lang.Integer, java.util.List[LogEntry]] =
    findSymptom(trial, spec).get
}
