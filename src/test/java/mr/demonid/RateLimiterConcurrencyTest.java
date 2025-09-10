package mr.demonid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Тест многопоточности RaceLimiter.
 */
public class RateLimiterConcurrencyTest {

    static final int NUM_THREADS = 10;


    @DisplayName("Многопоточный лимит")
    @Test
    void testRateLimiterMultiThread() throws InterruptedException {
        // Создаем RateLimiter: максимум 5 запросов в 1 секунду
        CrptApi.RateLimiter limiter = new CrptApi.RateLimiter(5, 1, TimeUnit.SECONDS);

        // Создаем пул потоков и атомарную переменную для теста
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        AtomicInteger passedRequests = new AtomicInteger(0);

        // Собственно поток
        Runnable task = () -> {
            try {
                limiter.acquire();
                passedRequests.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        long startTime = System.currentTimeMillis();

        // Запускаем все потоки почти одновременно
        for (int i = 0; i < NUM_THREADS; i++) {
            executor.submit(task);
        }

        // Завершаем работу пула
        executor.shutdown();
        try {
            boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);     // дожидаемся завершения потоков
            if (!finished) {
                // Прерываем все задачи. Видимо в коде где-то ошибка!
                executor.shutdownNow();
                fail("Пул потоков не завершился вовремя");
            }
        } catch (InterruptedException e) {
            // Главный поток прервался, завершаем наши тестовые потоки.
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            fail("Принудительное завершение программы");
        }

        // Засекаем время выполнения потоков
        long durationMillis = System.currentTimeMillis() - startTime;

        // Все запросы должны пройти
        assertEquals(NUM_THREADS, passedRequests.get(), "Не все запросы прошли");

        // Так как лимит 5/сек, 10 запросов должны занять примерно >=2 секунды
        assertTrue(durationMillis >= 1000, "RateLimiter не соблюдает ограничение по времени");
    }
}
