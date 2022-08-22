package feedback

private[feedback] object JavaTestUtil {
  def assertMismatch(task: () => Any): Unit =
    ScalaTestUtil.assertMismatch(new java.util.concurrent.Callable[Unit]() {
      override def call(): Unit = task()
    })
}
