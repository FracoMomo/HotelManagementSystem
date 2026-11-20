import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;

public class Main extends Application {
    private BorderPane root;
    private Sidebar sidebar;
    private final Map<String, javafx.scene.Node> pages = new LinkedHashMap<>();
    private Dashboard dashboard;
    private RoomsPanel roomsPanel;
    private ReservationsPanel reservationsPanel;
    private ReceptionPanel receptionPanel;
    private ReportsPanel reportsPanel;

    @Override
    public void start(Stage stage) {
        dashboard = new Dashboard(this);
        roomsPanel = new RoomsPanel(this);
        reservationsPanel = new ReservationsPanel(this);
        receptionPanel = new ReceptionPanel(this);
        reportsPanel = new ReportsPanel(this);

        pages.put("Dashboard", dashboard);
        pages.put("Rooms", roomsPanel);
        pages.put("Reservations", reservationsPanel);
        pages.put("Reception", receptionPanel);
        pages.put("Reports", reportsPanel);

        sidebar = new Sidebar(this);
        root = new BorderPane();
        root.setLeft(sidebar);
        Scene scene = new Scene(root, 1100, 700);
        Ui.applyStyles(scene);
        stage.setTitle("Hotel Management System");
        stage.setMinWidth(1000);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            if (!Ui.confirm(root, "Exit", "Are you sure you want to exit?")) event.consume();
        });
        showPage("Dashboard");
        stage.show();
        stage.centerOnScreen();
    }

    public void showPage(String page) {
        if (!pages.containsKey(page)) return;
        refreshAll();
        root.setCenter(pages.get(page));
        sidebar.selectPage(page);
    }

    public void refreshAll() {
        dashboard.refreshData();
        roomsPanel.refreshTable();
        reservationsPanel.refreshTable();
        receptionPanel.refreshTable();
        reportsPanel.refreshData();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
