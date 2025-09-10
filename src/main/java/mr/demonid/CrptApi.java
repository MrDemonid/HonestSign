package mr.demonid;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Класс для работы с API Честного знака.
 * Поддерживает ограничение количества запросов в заданный интервал времени (thread-safe).
 * Все внутренние классы объявлены как static, поскольку по сути являются независимыми классами,
 * просто, в силу задания, вложенными внутрь CrptApi.
 */
public class CrptApi {

    // Базовый URL API Честного знака (можно менять при расширении, или вынести в файл настроек)
    private static final String BASE_URL = "https://ismp.crpt.ru/api";
    private static final String API_VERSION = "/v3";
    private static final String CREATE_FUNCTION = "/lk/documents/create";

    /**
     * Форматы документов.
     */
    public enum DocumentFormat {
        MANUAL("MANUAL"),
        XML("XML"),
        CSV("CSV");

        private final String value;

        DocumentFormat(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    /**
     * Товарная группа.
     */
    public enum ProductGroup {
        CLOTHES("clothes"),
        SHOES("shoes"),
        TOBACCO("tobacco"),
        PERFUMERY("perfumery"),
        TIRES("tires"),
        ELECTRONICS("electronics"),
        PHARMA("pharma"),
        MILK("milk"),
        BICYCLE("bicycle"),
        WHEELCHAIRS("wheelchairs");

        private final String value;

        ProductGroup(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }


    private final ObjectMapper objectMapper;
    private final RateLimiter rateLimiter;
    private final HttpClient httpClient;

    private final Logger log = LogManager.getLogger(getClass().getName());

    /**
     * Класс для работы с API Честного знака.
     *
     * @param timeUnit     Единица времени (секунды, минуты и тд.).
     * @param interval     Количество единиц времени (например: 5 секунд, за которые должно быть не белее requestLimit запросов).
     * @param requestLimit Максимальное кол-во запросов за интервал.
     */
    public CrptApi(TimeUnit timeUnit, int interval, int requestLimit) {
        this.rateLimiter = new RateLimiter(requestLimit, interval, timeUnit);
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        httpClient = HttpClient.newHttpClient();
    }

    // тестовый конструктор
    protected CrptApi(TimeUnit timeUnit, int interval, int requestLimit, HttpClient httpClient) {
        this.rateLimiter = new RateLimiter(requestLimit, interval, timeUnit);
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.httpClient = httpClient;
    }


    /**
     * Создает документ в ИС МП и возвращает уникальный идентификатор документа в ИС МП.
     *
     * @param doc            Объект Document.
     * @param productGroup   Код товарной группы, например "milk", "shoes", ...
     * @param documentFormat формат документа: "MANUAL", "XML", "CSV".
     * @param signature      открепленная подпись в base64.
     * @param token          токен Bearer для авторизации.
     * @return UUID созданного документа.
     * @throws Exception При ошибках HTTP или сериализации
     */
    public UUID createDocument(Document doc, ProductGroup productGroup, DocumentFormat documentFormat, String signature, String token) throws Exception {

        // Соблюдаем лимит на запросы
        rateLimiter.acquire();

        // Убеждаемся, что параметры не null и не пустые строки.
        checkParameters(doc, productGroup, documentFormat, signature, token);

        // Формируем DTO запроса
        CreateDocumentRequest request = buildCreateDocumentRequest(doc, productGroup, documentFormat, signature);

        // Сериализация DTO в JSON
        String requestBody = objectMapper.writeValueAsString(request);

        // Формирование HTTP-запроса
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + API_VERSION + CREATE_FUNCTION + "?pg=" + productGroup.getValue()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Отправка запроса
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        checkStatus(response);
        // Извлекаем UUID документа
        UUID res = UUID.fromString(objectMapper.readTree(response.body()).get("value").asText());
        log.info("Created '{}' document", res);
        return res;
    }


    /**
     * Проверка ответа от сервера.
     */
    private void checkStatus(HttpResponse<?> response) throws RuntimeException {
        int code = response.statusCode();
        if (code < 200 || code >= 300) {
            log.error("HTTP error: {}, body: {}", code, response.body());
            throw new RuntimeException("HTTP error: " + code + ", body: " + response.body());
        }
    }

    /**
     * Проверка корректности параметров для createDocument().
     * Поскольку у нас только одна сервисная функция, то все проверки сведены в один метод.
     *
     * @throws IllegalArgumentException Если строки пустые.
     * @throws NullPointerException     Если какой-то из параметров равен null.
     */
    private void checkParameters(Document doc, ProductGroup productGroup, DocumentFormat documentFormat, String signature, String token) throws IllegalArgumentException, NullPointerException {
        Objects.requireNonNull(doc, "Document must not be null");
        Objects.requireNonNull(productGroup, "ProductGroup must not be null");
        Objects.requireNonNull(documentFormat, "DocumentFormat must not be null");
        Objects.requireNonNull(signature, "Signature must not be null");
        Objects.requireNonNull(token, "Token must not be null");
        if (token.isBlank())
            throw new IllegalArgumentException("Token cannot be empty");
        if (signature.isBlank())
            throw new IllegalArgumentException("Signature cannot be empty");
    }

    /**
     * Формирует DTO запроса для создания документа.
     */
    private CreateDocumentRequest buildCreateDocumentRequest(Document doc, ProductGroup productGroup, DocumentFormat documentFormat, String signature) throws IllegalArgumentException {
        try {
            // Сериализуем Document в JSON и кодируем в Base64
            String docJson = objectMapper.writeValueAsString(doc);
            String base64Doc = Base64.getEncoder().encodeToString(docJson.getBytes(StandardCharsets.UTF_8));
            return new CreateDocumentRequest(
                    documentFormat.getValue(),
                    base64Doc,
                    productGroup.getValue(),
                    signature,
                    doc.doc_type
            );
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to serialize document", e);
        }
    }


    /**
     * DTO-класс для документа.
     * Для тестового задания оставлен простым. В реальности структура берётся из документации API.
     */
    public static class Document {
        public Description description;
        public UUID doc_id;
        public String doc_status;
        public String doc_type;
        public boolean importRequest;
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        public LocalDate production_date;
        public String production_type;
        public Product[] products;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        public LocalDate reg_date;
        public String reg_number;

        public static class Description {
            public String participantInn;
        }

        public static class Product {
            public String certificate_document;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            public LocalDate certificate_document_date;
            public String certificate_document_number;
            public String owner_inn;
            public String producer_inn;
            public String production_date;
            public String tnved_code;
            public String uit_code;
            public String uitu_code;
        }
    }


    /**
     * DTO для запроса на создание документа.
     * Использована аннотация Jackson, для более удобного именования полей.
     */
    public class CreateDocumentRequest {
        @JsonProperty("document_format")
        public String documentFormat;
        @JsonProperty("product_document")
        public String productDocument; // base64 JSON
        @JsonProperty("product_group")
        public String productGroup;
        @JsonProperty("signature")
        public String signature;
        @JsonProperty("type")
        public String type;

        public CreateDocumentRequest() {
        }

        public CreateDocumentRequest(String documentFormat, String productDocument, String productGroup, String signature, String type) {
            this.documentFormat = documentFormat;
            this.productDocument = productDocument;
            this.productGroup = productGroup;
            this.signature = signature;
            this.type = type;
        }
    }


    /**
     * Потокобезопасный rate limiter на основе очереди временных меток.
     */
    static class RateLimiter {
        final Logger log = LogManager.getLogger(getClass().getName());

        private final int limit;
        private final long intervalMillis;
        private final Deque<Long> timestamps = new ArrayDeque<>();


        RateLimiter(int limit, int interval, TimeUnit unit) throws IllegalArgumentException, NullPointerException {
            if (limit <= 0 || interval <= 0) {
                throw new IllegalArgumentException("Limit and interval must be positive");
            }
            this.limit = limit;
            this.intervalMillis = Objects.requireNonNull(unit).toMillis(interval);
        }

        /**
         * Запрос на доступ к нашему API.
         */
        synchronized void acquire() throws InterruptedException {
            while (true) {
                long now = System.currentTimeMillis();

                // убираем устаревшие записи
                while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= intervalMillis) {
                    timestamps.pollFirst();
                }

                // если есть место, то сразу добавляем запрос
                if (timestamps.size() < limit) {
                    timestamps.addLast(now);
                    return;
                }

                // блокируем поток запроса, пока не освободится место.
                Long oldest = timestamps.peekFirst();
                if (oldest == null) {
                    // странная ситуация, попробуем снова после паузы.
                    log.error("Unexpected situation: first order is NULL");
                    wait(intervalMillis);
                    continue;
                }
                long sleepTime = intervalMillis - (now - oldest);
                if (sleepTime > 0) {
                    wait(sleepTime);
                }
            }
        }

    }
}
