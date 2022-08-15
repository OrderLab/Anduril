package feedback.time

private[feedback] object LogType extends Enumeration {
  val TRIAL: Value = Value("Trial-Run")
  val GOOD: Value = Value("Good-Run")
  val BAD: Value = Value("Bad-Run")
}
