package startup.guva;

import com.google.common.util.concurrent.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Description TODO
 * @Author Noah
 * @Date 2018-12-18
 * @Version V1.0
 */
public class AsynCollection {

    private static final Logger logger = LoggerFactory.getLogger(AsynCollection.class);

    private Map<String, InputStream> ceateDocx() {
        File file = new File("");
        long start = System.currentTimeMillis();
        Map<String, InputStream> allMap = new HashMap<>();
        int count = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(count);
        ListeningExecutorService service = MoreExecutors.listeningDecorator(executorService);
        try {
            for (int i = 0; i < count; i++) {
                final ListenableFuture<Map<String, InputStream>> listenableFuture = service.submit(() -> {
                    Map<String, InputStream> inputStreamMap = new HashMap<>();
                    // 自定义实现方法
                    latch.countDown();
                    return inputStreamMap;
                });
                Futures.addCallback(listenableFuture, new FutureCallback<Map<String, InputStream>>() {
                    @Override
                    public void onSuccess(@Nullable Map<String, InputStream> map) {
                        allMap.putAll(map);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {

                    }
                }, executorService);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        try {
            latch.await();
            executorService.shutdown();
        } catch (Exception e) {
            logger.error("", e);
        }
        System.out.println(String.format("%d---->%d", count, TimeUnit.MILLISECONDS.toSeconds(end - start)));
        return allMap;
    }
}
