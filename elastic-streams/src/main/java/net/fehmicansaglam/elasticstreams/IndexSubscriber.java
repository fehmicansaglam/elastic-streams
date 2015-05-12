package net.fehmicansaglam.elasticstreams;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class IndexSubscriber implements Subscriber<String> {

    private final ESLogger logger = ESLoggerFactory.getLogger("elastic-streams");
    private final IndexRequestBuilder builder;

    private Subscription subscription; // Obeying rule 3.1, we make this private!
    private final AtomicLong available;
    private final AtomicLong inProgress;
    private final AtomicBoolean canceled;
    protected long batchSize = 5;


    public IndexSubscriber(Client client, String index, String type, long max) {
        this.builder = client.prepareIndex(index, type);
        this.available = new AtomicLong(max);
        this.inProgress = new AtomicLong(0);
        this.canceled = new AtomicBoolean(false);
    }

    @Override
    public void onSubscribe(Subscription s) {
        // As per rule 2.13, we need to throw a `java.lang.NullPointerException` if the `Subscription` is `null`
        if (s == null) throw null;

        if (subscription != null) {
            // If someone has made a mistake and added this Subscriber multiple times, let's handle it gracefully
            s.cancel(); // Cancel the additional subscription
        } else {
            // We have to assign it locally before we use it, if we want to be a synchronous `Subscriber`
            // Because according to rule 3.10, the Subscription is allowed to call `onNext` synchronously from within `request`
            subscription = s;
            // If we want elements, according to rule 2.1 we need to call `request`
            // And, according to rule 3.2 we are allowed to call this synchronously from within the `onSubscribe` method
            s.request(available.getAndSet(0));
        }
    }

    @Override
    public void onNext(String element) {
        // As per rule 2.13, we need to throw a `java.lang.NullPointerException` if the `element` is `null`
        if (element == null) throw null;

        if (!canceled.get()) {
            // If we aren't already done
            available.incrementAndGet();
            inProgress.incrementAndGet();
            builder.setSource(element).execute(new ActionListener<IndexResponse>() {
                @Override
                public void onResponse(IndexResponse indexResponse) {
                    long demand = available.get() - inProgress.decrementAndGet();
                    if (demand >= batchSize) {
                        available.addAndGet(-demand);
                        subscription.request(demand);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    done();
                    onError(t);
                }
            });
        }
    }

    @Override
    public void onError(Throwable t) {
        if (t == null) throw null;
        // Here we are not allowed to call any methods on the `Subscription` or the `Publisher`, as per rule 2.3
        // And anyway, the `Subscription` is considered to be cancelled if this method gets called, as per rule 2.4
        logger.error("Errored: {}", t, t.getMessage());
    }

    @Override
    public void onComplete() {
        // Here we are not allowed to call any methods on the `Subscription` or the `Publisher`, as per rule 2.3
        // And anyway, the `Subscription` is considered to be cancelled if this method gets called, as per rule 2.4
    }

    // Showcases a convenience method to idempotently marking the Subscriber as "done", so we don't want to process more
    // elements herefor we also need to cancel our `Subscription`.
    private void done() {
        canceled.set(true);
        subscription.cancel();
    }
}
