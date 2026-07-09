package org.Core.Scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.*;
import java.util.Map;
import java.util.concurrent.*;

@Component
@Slf4j
public class TimeOutSchedulingService {

    private final Map<String, ScheduledFuture<?>> scheduledFutureMap=new ConcurrentHashMap<>();
    private final ScheduledExecutorService  executorService= Executors.newScheduledThreadPool(8);

    public void schedule(String gameId,long delay, Runnable job) {

        ScheduledFuture<?> future =
                executorService.schedule(job, delay, TimeUnit.MILLISECONDS);

        ScheduledFuture<?> old =
                scheduledFutureMap.put(gameId, future);

        if (old != null) {
            old.cancel(false);
            log.info("Replaced timeout for {}", gameId);
        } else {
            log.info("Created timeout for {}", gameId);
        }
    }
    public void cancel(String gameId){
        ScheduledFuture<?> old=scheduledFutureMap.get(gameId);
        if (old!=null) old.cancel(true);
    }

}
