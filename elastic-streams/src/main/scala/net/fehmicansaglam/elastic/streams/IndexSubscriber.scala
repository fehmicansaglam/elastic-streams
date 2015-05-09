package net.fehmicansaglam.elastic.streams

import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}

import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.client.Client
import org.reactivestreams.{Subscriber, Subscription}

class IndexSubscriber(client: Client, index: String, typ: String, max: Long) extends Subscriber[String] {
  private[this] var subscription: Subscription = _
  private[this] val canceled = new AtomicBoolean(false)

  private[this] val available = new AtomicLong(max)
  private[this] val inProgress = new AtomicLong(0)

  private[this] val builder = client.prepareIndex(index, typ)

  private[this] val listener: ActionListener[IndexResponse] = new ActionListener[IndexResponse] {
    override def onFailure(t: Throwable): Unit = {
      done()
      onError(t)
    }

    override def onResponse(response: IndexResponse): Unit = {
      val demand = available.get() - inProgress.decrementAndGet()
      if (demand >= batchSize) {
        available.addAndGet(-demand)
        subscription.request(demand)
//        println(s"Requested $demand inProgress: ${inProgress.get()} available: ${available.get()}")
      }
    }
  }

  override def onError(t: Throwable): Unit = {
    if (t == null) throw null
    // Here we are not allowed to call any methods on the `Subscription` or the `Publisher`, as per rule 2.3
    // And anyway, the `Subscription` is considered to be cancelled if this method gets called, as per rule 2.4
  }

  override def onSubscribe(s: Subscription): Unit = {
    // As per rule 2.13, we need to throw a `java.lang.NullPointerException` if the `Subscription` is `null`
    if (s == null) throw null

    if (subscription != null) {
      s.cancel()
    } else {
      // We have to assign it locally before we use it, if we want to be a synchronous `Subscriber`
      // Because according to rule 3.10, the Subscription is allowed to call `onNext` synchronously from within `request`
      subscription = s
      s.request(available.getAndSet(0))
    }
  }

  override def onComplete(): Unit = {
    // Here we are not allowed to call any methods on the `Subscription` or the `Publisher`, as per rule 2.3
    // And anyway, the `Subscription` is considered to be cancelled if this method gets called, as per rule 2.4
  }

  override def onNext(element: String): Unit = {
    // As per rule 2.13, we need to throw a `java.lang.NullPointerException` if the `element` is `null`
    if (element == null) throw null

    if (!canceled.get()) {
      // If we aren't already done
      available.incrementAndGet()
      inProgress.incrementAndGet()
      builder.setSource(element).execute(listener)
    }
  }

  protected def batchSize: Long = 10

  // Showcases a convenience method to idempotently marking the Subscriber as "done", so we don't want to process more
  // elements herefor we also need to cancel our `Subscription`.
  private[this] def done(): Unit = {
    canceled.set(true)
    subscription.cancel()
  }

}
