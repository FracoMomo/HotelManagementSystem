import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main extends Application {
    private BorderPane root;
    private Sidebar sidebar;
    private Role role;
    private final Map<String, Node> pages = new LinkedHashMap<>();

    @Override public void start(Stage stage) {
        root = new BorderPane();
        Scene scene = new Scene(root, 1100, 740);
        Ui.applyStyles(scene);
        stage.setTitle("Hotel Management System");
        stage.setMinWidth(1000);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            if (!Ui.confirm(root, "Exit", "Are you sure you want to exit?")) event.consume();
        });
        showLogin();
        stage.show();
        stage.centerOnScreen();
    }

    public void showLogin() {
        role = null;
        pages.clear();
        root.setLeft(null);
        root.setCenter(new LoginPanel(this));
    }

    // Local demo routing only. Real authentication and authorization come later.
    public void enterWorkspace(Role selected) {
        role = selected;
        pages.clear();
        if (role == Role.CUSTOMER) {
            pages.put("Rooms", new GuestRoomsPanel(this));
            pages.put("Book", scroll(new GuestBookingPanel()));
            pages.put("MyBooking", scroll(new GuestManagePanel()));
        } else if (role == Role.ADMIN) {
            pages.put("Dashboard", new Dashboard(this));
            pages.put("Rooms", new RoomsPanel(this));
            pages.put("Reports", new ReportsPanel(this));
        } else {
            pages.put("Dashboard", scroll(new ReceptionHome(this)));
            pages.put("Rooms", new GuestRoomsPanel(this));
            pages.put("Reservations", new ReservationsPanel(this));
            pages.put("Reception", new ReceptionPanel(this));
        }
        sidebar = new Sidebar(this, role);
        root.setLeft(sidebar);
        showPage(role == Role.CUSTOMER ? "Rooms" : "Dashboard");
    }

    private ScrollPane scroll(Node content) {
        ScrollPane pane = new ScrollPane(content);
        pane.setFitToWidth(true);
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pane.getStyleClass().add("workspace-scroll");
        return pane;
    }

    public Role getRole() { return role; }
    public void showPage(String page) {
        if (role == null || !pages.containsKey(page)) return;
        refreshAll();
        root.setCenter(pages.get(page));
        sidebar.selectPage(page);
    }
    public void refreshAll() {
        for (Node page : pages.values()) {
            if (page instanceof Dashboard p) p.refreshData();
            if (page instanceof RoomsPanel p) p.refreshTable();
            if (page instanceof ReservationsPanel p) p.refreshTable();
            if (page instanceof ReceptionPanel p) p.refreshTable();
            if (page instanceof ReportsPanel p) p.refreshData();
            if (page instanceof GuestRoomsPanel p) p.refreshData();
        }
    }
    public static void main(String[] args) { launch(args); }
}
