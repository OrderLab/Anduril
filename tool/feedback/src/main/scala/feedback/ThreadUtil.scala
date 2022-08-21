package feedback

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{Callable, Executors, Future, LinkedBlockingQueue}

final class AsyncIterator[T](val task: (T => Unit) => Unit) extends Iterator[T] {
  private val queue = new LinkedBlockingQueue[Option[T]]

  val future = ThreadUtil.submit(() => try {
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
  private val executor = Executors.newCachedThreadPool

  def submit[T](future: Callable[T]): Future[T] = executor.submit(future)

  def submit[T](task: () => T): Future[T] = submit(new Callable[T] {
    override def call(): T = task()
  })
}
