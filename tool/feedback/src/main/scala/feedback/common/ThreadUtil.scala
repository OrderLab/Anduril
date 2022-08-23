package feedback.common

import java.util.concurrent.{Callable, ExecutorService, Executors, Future, LinkedBlockingQueue}
import scala.jdk.CollectionConverters.IteratorHasAsScala

final class AsyncIterator[T](val task: (T => Unit) => Unit) extends Iterator[T] {
  private val queue = new LinkedBlockingQueue[Option[T]]

  private val future = ThreadUtil.submit((() => try {
    task { element =>
      require(queue.add(Some(element)))
    }
  } finally {
    queue.add(None)
  }): RunMayThrow)

  @volatile private var current = queue.take

  override def hasNext: Boolean = this.synchronized {
    val v = current.nonEmpty
    if (!v) {
      future.get
    }
    v
  }

  override def next(): T = this.synchronized {
    val v = current.get
    current = queue.take
    v
  }
}

object ThreadUtil {
  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run(): Unit = shutdown()
  })

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

  def shutdown(): Unit = this.synchronized {
    executor foreach { _.shutdown() }
    executor = None
  }
}
