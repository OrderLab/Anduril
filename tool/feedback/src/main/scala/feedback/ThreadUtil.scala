package feedback

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{Callable, ExecutorService, Executors, Future, LinkedBlockingQueue}

final class AsyncIterator[T](val task: (T => Unit) => Unit) extends Iterator[T] {
  private val queue = new LinkedBlockingQueue[Option[T]]

  private val future = ThreadUtil.submit(() => try {
    task { element =>
      require(queue.add(Some(element)))
    }
  } finally {
    queue.add(None)
  })

  private val current = new AtomicReference[Option[T]](queue.take())

  override def hasNext: Boolean = this.synchronized {
    val v = current.get().nonEmpty
    if (!v) {
      future.get()
    }
    v
  }

  override def next(): T = this.synchronized {
    val v = current.get().get
    current.set(queue.take())
    v
  }
}

object ThreadUtil {
  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run(): Unit = shutdown()
  })

  private def createNewPool: Option[ExecutorService] = Some(Executors.newCachedThreadPool)

  @volatile private var executor: Option[ExecutorService] = None

  def submit[T](future: Callable[T]): Future[T] = this.synchronized {
    if (executor.isEmpty) {
      executor = createNewPool
    }
    executor.map(_.submit(future)).get
  }

  def submit[T](task: () => T): Future[T] = this.synchronized {
      submit(new Callable[T] {
        override def call(): T = task()
      })
  }

  def shutdown(): Unit = this.synchronized {
    executor foreach { _.shutdown() }
    executor = None
  }
}
