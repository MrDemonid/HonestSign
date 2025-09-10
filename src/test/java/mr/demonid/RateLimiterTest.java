package mr.demonid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Тест лимита запросов.
 */
class RateLimiterTest {

    @DisplayName("Проверка лимита запросов")
    @Test
    void testRateLimiterLimitsRequests() throws InterruptedException {
        // создаем лимитер: 2 запроса в секунду
        CrptApi.RateLimiter limiter = new CrptApi.RateLimiter(2, 1, TimeUnit.SECONDS);

        // замеряем время начала теста
        long start = System.currentTimeMillis();

        // делаем три запроса подряд
        limiter.acquire();
        limiter.acquire();
        limiter.acquire(); // третий должен "задержать" выполнение
        limiter.acquire();
        limiter.acquire(); // пятый должен "задержать" выполнение

        // узнаем, сколько по времени занял тест
        long elapsed = System.currentTimeMillis() - start;
        // должно занять более 2-х секунд, потому что третий и пятый вызовы блокируется
        assert (elapsed >= 2000) : "RateLimiter не ограничивает запросы правильно, прошло " + elapsed + " мс";
    }

    @DisplayName("Некорректные параметры")
    @Test
    void testRateLimiterConstructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new CrptApi.RateLimiter(0, 1, TimeUnit.SECONDS));
        assertThrows(IllegalArgumentException.class, () -> new CrptApi.RateLimiter(5, 0, TimeUnit.SECONDS));
        assertThrows(NullPointerException.class, () -> new CrptApi.RateLimiter(5, 1, null));
    }

}
