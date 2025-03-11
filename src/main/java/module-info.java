module com.example.zpoif_projekt {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires java.compiler;

    opens pl.pw.edu.mini.zpoif to javafx.fxml;
    exports pl.pw.edu.mini.zpoif.Api;
    exports pl.pw.edu.mini.zpoif.Application;
    exports pl.pw.edu.mini.zpoif.Data.plotData;
    opens pl.pw.edu.mini.zpoif.Application to javafx.fxml;
    exports pl.pw.edu.mini.zpoif.Data.tableData;
    exports pl.pw.edu.mini.zpoif.Data.rateData;
}