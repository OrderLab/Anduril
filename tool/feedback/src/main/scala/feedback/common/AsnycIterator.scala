package feedback.common

import java.util.concurrent.LinkedBlockingQueue

final class AsyncIterator[T](val task: (T => Unit) => Unit) extends Iterator[T] {
  private val queue = new LinkedBlockingQueue[Option[T]]

  private val future = Env.submit((() => try {
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