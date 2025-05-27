module GFP.DeepseekPrototype {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires org.apache.poi.ooxml;

    opens CashFlowy.controller to javafx.fxml;
    exports CashFlowy.controller;



    //exports CashFlowy.main;

    exports CashFlowy;
    opens CashFlowy to javafx.fxml;
    exports CashFlowy.persistence.model;
    opens CashFlowy.persistence.model to javafx.fxml;

}
