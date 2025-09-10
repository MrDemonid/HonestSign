package mr.demonid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тест метода создания документа.
 * Поскольку токена у нас нет, то можем только протестировать, что сервер вернет ошибку.
 */
class CrptApiTest {

    @Test
    void testCreateDocumentThrowsOnInvalidToken() throws Exception {
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 1, 5);

        CrptApi.Document doc = new CrptApi.Document();
        doc.doc_type = "LP_INTRODUCE_GOODS";        // обязательный параметр для создания DTO запроса.
        doc.doc_id = UUID.fromString("0e85d8b5-28cc-447d-ba1d-d8e63c7459f9");
        doc.doc_status = "NEW";

        // Передаем пустой токен, ожидаем RuntimeException
        RuntimeException e = assertThrows(RuntimeException.class, () ->
                api.createDocument(doc,
                        CrptApi.ProductGroup.SHOES,
                        CrptApi.DocumentFormat.MANUAL,
                        "fake-signature",
                        "bad-token" // пустой токен
                )
        );

        System.out.println(e.getMessage());
        // Проверяем, что сообщение содержит ожидаемую строку
        assert e.getMessage().contains("HTTP error: ");
    }

    /**
     * Проверка реакции на null-параметры.
     */
    @Test
    void testCreateDocumentThrowsOnNullParameters() {
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 1, 5);
        CrptApi.Document doc = new CrptApi.Document();
        doc.doc_type = "LP_INTRODUCE_GOODS";

        assertThrows(NullPointerException.class, () ->
                api.createDocument(null, CrptApi.ProductGroup.SHOES, CrptApi.DocumentFormat.MANUAL, "sig", "token")
        );

        assertThrows(NullPointerException.class, () ->
                api.createDocument(doc, null, CrptApi.DocumentFormat.MANUAL, "sig", "token")
        );

        assertThrows(NullPointerException.class, () ->
                api.createDocument(doc, CrptApi.ProductGroup.SHOES, null, "sig", "token")
        );

        assertThrows(NullPointerException.class, () ->
                api.createDocument(doc, CrptApi.ProductGroup.SHOES, CrptApi.DocumentFormat.MANUAL, null, "token")
        );

        assertThrows(NullPointerException.class, () ->
                api.createDocument(doc, CrptApi.ProductGroup.SHOES, CrptApi.DocumentFormat.MANUAL, "sig", null)
        );
    }

    /**
     * Проверка на недопустимость пустых строк (сигнатуры и токена).
     */
    @Test
    void testCreateDocumentThrowsOnEmptyTokenOrSignature() {
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 1, 5);
        CrptApi.Document doc = new CrptApi.Document();
        doc.doc_type = "LP_INTRODUCE_GOODS";

        assertThrows(IllegalArgumentException.class, () ->
                api.createDocument(doc, CrptApi.ProductGroup.SHOES, CrptApi.DocumentFormat.MANUAL, "", "token")
        );

        assertThrows(IllegalArgumentException.class, () ->
                api.createDocument(doc, CrptApi.ProductGroup.SHOES, CrptApi.DocumentFormat.MANUAL, "sig", "")
        );
    }

    /**
     * Проверка на сереализацию/десереализацию документа.
     */
    @Test
    void testDocumentSerialization() throws Exception {
        CrptApi.Document doc = new CrptApi.Document();
        doc.doc_type = "LP_INTRODUCE_GOODS";
        doc.doc_id = UUID.randomUUID();

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String json = mapper.writeValueAsString(doc);
        String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));

        byte[] decoded = Base64.getDecoder().decode(base64);
        CrptApi.Document decodedDoc = mapper.readValue(decoded, CrptApi.Document.class);
        assertEquals(doc.doc_type, decodedDoc.doc_type);
        assertEquals(doc.doc_id, decodedDoc.doc_id);
    }
}