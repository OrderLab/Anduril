package feedback.common

import java.lang.management.ManagementFactory
import java.util.concurrent.{Callable, ExecutorService, Executors, Future}
import scala.jdk.CollectionConverters.IteratorHasAsScala

object Env {
  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run(): Unit = shutdown()
  })

  val pid: Int = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt

  private def createNewPool: Option[ExecutorService] = Some(Executors.newCachedThreadPool)

  @volatile private var executor: Option[ExecutorService] = None

  private def submit[V](callable: Callable[V]): Future[V] = this.synchronized {
    if (executor.isEmpty) {
      executor = createNewPool
    }
    executor.map(_.submit(callable)).get
  }

  def submit[V](callable: CallMayThrow[V]): Future[V] = submit(callable: Callable[V])

  def submit(callable: RunMayThrow): Future[Void] = submit(callable: Callable[Void])

  private def submit[T, R](t: T, function: FunctionMayThrow[T, R]): Future[R] =
    submit(new CallMayThrow[R] {
      override def callMayThrow(): R = function.apply(t)
    })

  def submit[T](t: T, action: ActionMayThrow[T]): Future[Void] =
    submit(new RunMayThrow {
      override def runMayThrow(): Unit = action.accept(t)
    })

  private def parallel[T](iterator: Iterator[T], action: ActionMayThrow[T]): Future[Void] = {
    val tasks = iterator.map((item => submit(item, action))).toSeq
    submit(() => tasks foreach { _.get })
  }

  def parallel[T](iterator: java.util.Iterator[T], action: ActionMayThrow[T]): Future[Void] =
    parallel(iterator.asScala, action)

  def parallel[T](iterable: java.lang.Iterable[T], action: ActionMayThrow[T]): Future[Void] =
    parallel(iterable.iterator, action)

  def parallel[T](iterable: Iterable[T], action: ActionMayThrow[T]): Future[Void] =
    parallel(iterable.iterator, action)

  def parallel[T](begin: Integer, end: Integer, action: ActionMayThrow[Integer]): Future[Void] =
    parallel((begin.toInt until end.toInt).iterator map Integer.valueOf, action)

  def parallel[T, R](iterator: Iterator[T], function: FunctionMayThrow[T, R]): Future[Iterable[R]] = {
    val tasks = iterator.map((item => submit(item, function))).toSeq
    submit(() => tasks map { _.get })
  }

  def parallel[T, R](iterator: java.util.Iterator[T], function: FunctionMayThrow[T, R]): Future[Iterable[R]] =
    parallel(iterator.asScala, function)

  def parallel[T, R](iterable: java.lang.Iterable[T], function: FunctionMayThrow[T, R]): Future[Iterable[R]] =
    parallel(iterable.iterator, function)

  def parallel[T, R](iterable: Iterable[T], function: FunctionMayThrow[T, R]): Future[Iterable[R]] =
    parallel(iterable.iterator, function)

  def parallel[R](begin: Integer, end: Integer, function: FunctionMayThrow[Integer, R]): Future[Iterable[R]] =
    parallel((begin.toInt until end.toInt).iterator map Integer.valueOf, function)

  @volatile private var count = 0

  def enter(): Unit = this.synchronized {
    count += 1
  }

  def exit(): Unit = this.synchronized {
    require(count > 0)
    count -= 1
    if (count == 0) {
      shutdown()
    }
  }

  private def shutdown(): Unit = this.synchronized {
    executor foreach { _.shutdown() }
    executor = None
  }
}
