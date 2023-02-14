package com.nr.instrumentation.kafka.streams;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.TransactionNamePriority;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public class StreamsUtil {
    private StreamsUtil() {}

    public static void initTransaction() {
        LoopState.LOCAL.set(new LoopState());
        NewRelic.getAgent().getTransaction().setTransactionName(TransactionNamePriority.FRAMEWORK_LOW, false, "KafkaStreams",
                "StreamsThread/Loop");
    }

    // Records number of records poll to loop state
    public static void recordPolledToLoopState(ConsumerRecords<?, ?> records) {
        LoopState state = LoopState.LOCAL.get();
        if (state != null) {
            int polled = records == null ? 0 : records.count();
            state.incRecordsPolled(polled);
        }
    }

    public static void updateTotalProcessedToLoopState(double processed) {
        LoopState state = LoopState.LOCAL.get();
        if (state != null) {
            state.incTotalProcessed(processed);
        }

    }

    public static void endTransaction() {
        LoopState state = LoopState.LOCAL.get();
        if (state != null && state.getRecordsPolled() == 0 && state.getTotalProcessed() == 0) {
            NewRelic.getAgent().getTransaction().ignore();
        }
        LoopState.LOCAL.remove();
    }
}
