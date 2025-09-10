package mr.demonid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * Тест на реакцию от сервера.
 */
public class CrptApiMockHttpTest {


    @DisplayName("Успешное создание документа")
    @Test
    void testCreateDocumentSuccess() throws Exception {

        HttpClient mockClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        UUID fakeUuid = UUID.randomUUID();

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"value\":\"" + fakeUuid + "\"}");
        when(mockClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(mockResponse);

        CrptApi api = new CrptApi(TimeUnit.SECONDS, 1, 5, mockClient);

        CrptApi.Document doc = new CrptApi.Document();
        doc.doc_type = "LP_INTRODUCE_GOODS";

        UUID result = api.createDocument(
                doc,
                CrptApi.ProductGroup.SHOES,
                CrptApi.DocumentFormat.MANUAL,
                "fake-signature",
                "fake-token"
        );

        // Проверяем
        assertEquals(fakeUuid, result);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockClient).send(requestCaptor.capture(), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());

        // Проверим, что Authorization попал в заголовки
        HttpRequest sentRequest = requestCaptor.getValue();
        assertEquals("Bearer fake-token", sentRequest.headers().firstValue("Authorization").orElse(null));
        assertEquals(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create?pg=shoes"), sentRequest.uri());
    }


    @DisplayName("Ошибка от сервера")
    @Test
    void testCreateDocumentHttpError() throws Exception {

        HttpClient mockClient = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("{\"error\":\"Internal Server Error\"}");
        when(mockClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(mockResponse);

        CrptApi api = new CrptApi(TimeUnit.SECONDS, 1, 5, mockClient);

        CrptApi.Document doc = new CrptApi.Document();
        doc.doc_type = "LP_INTRODUCE_GOODS";

        // Должно выброситься исключение
        RuntimeException e = assertThrows(RuntimeException.class, () -> api.createDocument(
                doc,
                CrptApi.ProductGroup.SHOES,
                CrptApi.DocumentFormat.MANUAL,
                "sig",
                "token"
                )
        );
        assertTrue(e.getMessage().contains("HTTP error: 500"));
    }
}
