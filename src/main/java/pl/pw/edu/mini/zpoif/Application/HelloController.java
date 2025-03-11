package pl.pw.edu.mini.zpoif.Application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.WorldMapView;
import pl.pw.edu.mini.zpoif.Api.Api;
import pl.pw.edu.mini.zpoif.Data.rateData.CurrencyRate;
import pl.pw.edu.mini.zpoif.Data.rateData.Rate;
import pl.pw.edu.mini.zpoif.Data.tableData.Table;
import pl.pw.edu.mini.zpoif.Data.plotData.PlotData;
import pl.pw.edu.mini.zpoif.Data.plotData.RatePlot;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

public class HelloController implements Initializable {
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final double wartoscDomyslnaMonet = 100;
    private final double wartoscDomyslnaChleb = 4;
    private final double wartoscDomyslnaCzekolada = 6;
    private final double wartoscDomyslnaZapalki = 0.5;
    private final double wartoscDomyslnaJajko = 1;
    private final double wartoscDomyslnaPiwo = 3;
    private final Map<String , Double> produkty = new HashMap<String, Double>() {{
        put("Chleb", wartoscDomyslnaChleb);
        put("Czekolada", wartoscDomyslnaCzekolada);
        put("Zapałki", wartoscDomyslnaZapalki);
        put("Jajko", wartoscDomyslnaJajko);
        put("Piwo", wartoscDomyslnaPiwo);
    }};
    @FXML
    private TableView<Table> table;
    @FXML
    private TableColumn<Table, String> currency;
    @FXML
    private TableColumn<Table, Date> date;
    @FXML
    private TableColumn<Table, Double> rate;
    @FXML
    private Button buttonPorownaj;
    @FXML
    private DatePicker endDateButton;
    @FXML
    private DatePicker startDateButton;
    @FXML
    private CheckComboBox<Rate> currencyChoiceBox;
    @FXML
    private LineChart<String, Number> wykresPorownanie;
    @FXML
    private ChoiceBox<Rate> ileCzegoCheckBox;
    @FXML
    private BarChart<String, Number> ileCzegoPlot;
    @FXML
    private Button ileCzegoButton;
    @FXML
    private WorldMapView worldMap;
    @FXML
    private Label krajLabel;
    @FXML
    private Label mapLabel;
    @FXML
    private Label mapValue;
    private final HttpClient httpClient = HttpClient.newBuilder().build();

    public HelloController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        CurrencyRate[] data = getCurrencyRatesData();

        //////////////// tworzenie tabelki //////////
        makeTable(data);

        //////////////// tworzenie porownania walut //////////
        makePorownaniaWalut(data);

        //////////////// tworzenie ileCzego //////////
        makeIleCzego(data);

        //////////////// tworzenie mapy swiata //////////
        makeWorldMap(data);

    }

    private static CurrencyRate[] getCurrencyRatesData() {
        HttpClient httpClient = HttpClient.newBuilder().build();
        Api api = new Api();
        CurrencyRate[] data = api.getApiData(httpClient, "http://api.nbp.pl/api/exchangerates/tables/A/", "http://api.nbp.pl/api/exchangerates/tables/B/");
        return data;
    }

    private void makeWorldMap(CurrencyRate[] data) {
        worldMap.setOnMouseClicked(mouseEvent -> {
            ObservableList<WorldMapView.Country> countries = worldMap.getSelectedCountries();
            if (countries.isEmpty()) {
                krajLabel.setText("Wybierz kraj z mapy");
                mapLabel.setText("");
                mapValue.setText("");
            } else {
                setWorldMapLabels(data, countries);
            }
        });

        makeWorldMapScrollable();
    }

    private void setWorldMapLabels(CurrencyRate[] data, ObservableList<WorldMapView.Country> countries) {
        Locale locale = new Locale("", countries.get(0).name());
        krajLabel.setText(countries.get(0).getLocale().getDisplayCountry());
        mapLabel.setText(Currency.getInstance(locale).getDisplayName(Locale.getDefault()));
        String currencyCode = Currency.getInstance(locale).getCurrencyCode();
        Optional<Double> rateValue = Optional.empty();
        List<Rate> rates = data[0].getRates().stream().filter(r -> Objects.equals(r.getCode(), currencyCode)).toList();
        if (!rates.isEmpty()) rateValue = Optional.ofNullable(rates.get(0).getMid());
        List<Rate> rates2 = data[1].getRates().stream().filter(r -> Objects.equals(r.getCode(), currencyCode)).toList();
        if (!rates2.isEmpty()) rateValue = Optional.ofNullable(rates2.get(0).getMid());
        if (rateValue.isEmpty()) {
            mapValue.setText("1,00");
        } else {
            mapValue.setText(String.format("%.2f", rateValue.get()));
        }
    }

    private void makeWorldMapScrollable() {
        worldMap.setOnScroll(scrollEvent -> {
            double delta = scrollEvent.getDeltaY();
            if (delta < 0) {
                worldMap.setZoomFactor(worldMap.getZoomFactor() - 0.5);
            } else {
                worldMap.setZoomFactor(worldMap.getZoomFactor() + 0.5);
            }
            scrollEvent.consume();
        });
    }

    private void makeIleCzego(CurrencyRate[] data) {
        setCheckBoxProperties(data);
        ileCzegoButton.setText("Przelicz");
        ileCzegoButton.setOnAction(actionEvent -> {
            Rate rate = ileCzegoCheckBox.getValue();
            //if (!validateRates(rates)) return;
            ileCzegoPlot.getData().clear();
            ileCzegoPlot.setTitle("Ile wybranych produktów jesteśmy w stanie kupić za 100 " + rate.getCode());
            for (Map.Entry<String, Double> entry : produkty.entrySet()) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName(entry.getKey());
                series.getData().add(new XYChart.Data<>(entry.getKey(), round(wartoscDomyslnaMonet * rate.getMid() / entry.getValue())));
                ileCzegoPlot.getData().add(series);
            }
            ileCzegoPlot.setAnimated(false);
            ileCzegoPlot.setBarGap(-120);
            ileCzegoPlot.setVisible(true);
        });
    }

    private void makePorownaniaWalut(CurrencyRate[] data) {
        setChoiceBoxProperties(data);
        buttonPorownaj.setOnAction(actionEvent -> {
            ObservableList<Rate> rates = currencyChoiceBox.getCheckModel().getCheckedItems();
            if (!isRateCorrect(rates)) return;
            LocalDate endDate = endDateButton.getValue();
            LocalDate startDate = startDateButton.getValue();
            if (!isDateCorrect(startDate, endDate)) return;
            wykresPorownanie.getData().clear();
            wykresPorownanie.setTitle("Kursy wybranych walut między " + startDate.format(dateTimeFormatter) + " a "
                    + endDate.format(dateTimeFormatter));
            for (Rate selectedRate : rates) {
                boolean isARate = data[0].getRates().contains(selectedRate);
                PlotData plotData = getPlotData(startDate, endDate, selectedRate, isARate);
                if (plotData == null) return;
                XYChart.Series<String, Number> series = processPlotData(plotData);
                wykresPorownanie.getData().add(series);
            }
            wykresPorownanie.setVisible(true);
            wykresPorownanie.setAnimated(false);
            buttonPorownaj.setText("Porównaj waluty");
        });
    }

    private void makeTable(CurrencyRate[] data) {
        ObservableList<Table> table2 = FXCollections.observableArrayList();
        getTableData(data, table2);
        setTable(table2);
    }


    private XYChart.Series<String, Number> processPlotData(PlotData chartData) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(String.format("%s (%s)", chartData.getCurrency(), chartData.getCode()));
        for (RatePlot rate : chartData.getRates()) {
            series.getData().add(new XYChart.Data<>(rate.getEffectiveDate(), rate.getMid()));
        }
        series.getData().sort(Comparator.comparing(XYChart.Data::getXValue));
        return series;
    }

    private PlotData getPlotData(LocalDate startDate, LocalDate endDate, Rate selectedRate, boolean isARate) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Błąd");
        String link = String.format("http://api.nbp.pl/api/exchangerates/rates/%s/%s/%s/%s/",
                isARate ? "A" : "B",
                selectedRate.getCode(),
                startDate.format(dateTimeFormatter),
                endDate.format(dateTimeFormatter));

        HttpRequest chartRequest = HttpRequest.newBuilder()
                .header("Accept", "application/json")
                .uri(URI.create(link))
                .build();
        HttpResponse<String> chartResponse;
        buttonPorownaj.setText("Tworzenie wykresu...");
        try {
            chartResponse = httpClient.send(chartRequest, HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException ex) {
            alert.setHeaderText("Brak internetu");
            alert.setContentText("Sprawdź połączenie internetowe");
            alert.showAndWait();
            buttonPorownaj.setText("Porównaj waluty");
            return null;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        PlotData plotData = getPlotData(chartResponse);
        return plotData;
    }

    private static PlotData getPlotData(HttpResponse<String> chartResponse) {
        ObjectMapper chartMapper = new ObjectMapper();
        PlotData plotData;
        try {
            plotData = chartMapper.readValue(chartResponse.body(), PlotData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return plotData;
    }

    private void getTableData(CurrencyRate[] currencyRates, ObservableList<Table> tableData) {
        for(int num=0; num<currencyRates.length; num++){
            List<Rate> rates = currencyRates[num].getRates();
            for (int i = 0; i < rates.size(); i++) {
                Rate rate2 = currencyRates[num].getRates().get(i);
                double rate = round(rate2.getMid());
                String name = rate2.getCurrency();
                Date date = currencyRates[num].getEffectiveDate();
                String formattedDate = formatDate(date);
                tableData.add(new Table(name, formattedDate, rate));
            }
        }
    }

    private double round(double d) {
        return Math.round(d * 10000.0) / 10000.0;
    }

    private String formatDate(Date date) {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }

    private void setTable(ObservableList<Table> table2) {
        currency.prefWidthProperty().bind(table.widthProperty().multiply(0.334));
        date.prefWidthProperty().bind(table.widthProperty().multiply(0.333));
        rate.prefWidthProperty().bind(table.widthProperty().multiply(0.333));
        currency.setCellValueFactory(new PropertyValueFactory<>("currency"));
        date.setCellValueFactory(new PropertyValueFactory<>("date"));
        rate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        table.setItems(table2);
    }

    private void setChoiceBoxProperties(CurrencyRate[] currencyRates) {
        setBox(currencyRates, currencyChoiceBox.getItems());

    }

    private void setCheckBoxProperties(CurrencyRate[] currencyRates) {
        setBox(currencyRates, ileCzegoCheckBox.getItems());
    }

    private void setBox(CurrencyRate[] currencyRates, ObservableList<Rate> items) {
        if (currencyRates != null) {
            items.addAll(FXCollections.observableArrayList(currencyRates[0].getRates()));
            items.addAll(FXCollections.observableArrayList(currencyRates[1].getRates()));
            Rate defaultRate = new Rate();
            defaultRate.setCode("PLN");
            defaultRate.setCurrency("złoty polski");
            defaultRate.setMid(1.00);
            items.add(defaultRate);
        }
    }

    private boolean isRateCorrect(ObservableList<Rate> rates) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Error!");
        if (rates == null
                || rates.isEmpty()
                || rates.stream().allMatch(Objects::isNull)) {
            alert.setHeaderText("Wybierz walutę");
            alert.setContentText("Wybierz walutę do wykresu");
            alert.showAndWait();
            return false;
        }
        return true;
    }

    private boolean isDateCorrect(LocalDate startDate, LocalDate endDate) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Error!");
        LocalDate now = LocalDate.now();
        LocalDate earliestDate = LocalDate.of(2002, 1, 2);
        if (startDate == null
                || endDate == null
                || startDate.isBefore(earliestDate)
                || endDate.isBefore(earliestDate)
                || startDate.isAfter(now)
                || endDate.isAfter(now)
                || startDate.isAfter(endDate)
                || DAYS.between(startDate, endDate) > 366) {
            alert.setHeaderText("Podaj poprawną datę");
            alert.setContentText("Daty nie mogą się różnić o więcej niż rok (limit API NBP)! " +
                    "Data musi być z przedziału od 2 stycznia 2002 r. do dziś.");
            alert.showAndWait();
            return false;
        }
        return true;
    }
}