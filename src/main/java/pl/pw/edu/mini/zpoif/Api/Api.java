package pl.pw.edu.mini.zpoif.Api;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.pw.edu.mini.zpoif.Data.rateData.CurrencyRate;

public class Api {
    private String link1;
    private String link2;

    public static CurrencyRate[] getApiData(HttpClient httpClient, String link1, String link2) {
        CurrencyRate[] tableA = importTableData(link1, httpClient);
        CurrencyRate[] tableB = importTableData(link2, httpClient);
        return mergeTables(tableA, tableB);
    }

    private static CurrencyRate[] mergeTables(CurrencyRate[] tableA, CurrencyRate[] tableB) {
        return Stream.concat(Arrays.stream(tableA), Arrays.stream(tableB)).toArray(CurrencyRate[]::new);
    }

    private static CurrencyRate[] importTableData(String url, HttpClient httpClient) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Accept", "application/json")
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return readResponseAndMap(response);
        } catch (ConnectException ex) {
            Alert noConnectionAlert = new Alert(Alert.AlertType.ERROR);
            noConnectionAlert.setHeaderText("Nie można połączyć się z serwerem.");
            noConnectionAlert.setContentText("Sprawdź połączenie internetowe i uruchom ponownie aplikację.");
            noConnectionAlert.showAndWait();
            Platform.exit();
            return null;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private static CurrencyRate[] readResponseAndMap(HttpResponse<String> response){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response.body(), CurrencyRate[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
