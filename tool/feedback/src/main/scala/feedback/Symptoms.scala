package feedback

import feedback.parser.{DistributedLog, LogType, Parser, TestFail}
import feedback.time.Timing

import javax.json.JsonObject

private[feedback] object Symptoms {
  private def checkDistributedTrial(trial: DistributedLog): Unit = {
    require(trial.distributed)
    require(trial.dirs.zipWithIndex.forall {
      case (dir, i) => i == Parser.parseLogDirId(dir.getName)
    })
  }

  def isSymptomLogged(spec: JsonObject): Boolean = {
    val bug = spec.getString("case")
    bug match {
      case "hdfs-4233" => true
      case "zookeeper-3006" => false
      case _ => throw new Exception(s"can't recognize bug case $bug")
    }
  }

  private def singletonMap(node: java.lang.Integer, timing: Option[Timing]): Option[java.util.Map[Integer, java.util.List[Timing]]] =
    timing.map(e => java.util.Collections.singletonMap(node, java.util.Collections.singletonList(e)))

  private def findSymptom(trial: DistributedLog, spec: JsonObject): Option[java.util.Map[Integer, java.util.List[Timing]]] = {
    val bug = spec.getString("case")
    bug match {
      case "zookeeper-3006" =>
        require(!trial.distributed)
        val testResult = trial.logs(0).testResult
        val symptom = testResult match {
          case TestFail(_, _, testMethod, testClass, exceptions) =>
            if (testMethod.equals("testAbsentRecentSnapshot") &&
              testClass.equals("org.apache.zookeeper.test.ZkDatabaseCorruptionTest")) {
              val exception = exceptions(0)
              if (exception.exception.equals("java.lang.NullPointerException") && exception.exceptionMsg.isEmpty)
                Some(testResult) else None
            } else None
          case _ => None
        }
        singletonMap(-1, symptom)
      case "hdfs-4233" =>
        checkDistributedTrial(trial)
        val node = 1
        singletonMap(node, trial.logs(node).entries.find(entry =>
          if (entry.file.equals("Server$Handler") && entry.fileLogLine == 1538 && entry.logType == LogType.INFO) {
            if (entry.exceptions.length == 1) {
              entry.exceptions(0).stacktrace.contains("org.apache.hadoop.hdfs.server.namenode.FSEditLog.startLogSegment(FSEditLog.java:835)")
            } else false
          } else false))
      case _ => throw new Exception(s"can't recognize bug case $bug")
    }
  }

  def checkSymptom(trial: DistributedLog, spec: JsonObject): Boolean = findSymptom(trial, spec).nonEmpty

  def getSymptom(trial: DistributedLog, spec: JsonObject): java.util.Map[java.lang.Integer, java.util.List[Timing]] =
    findSymptom(trial, spec).get
}
