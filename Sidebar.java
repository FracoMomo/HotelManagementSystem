import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

public class Sidebar extends VBox {
    private static final PseudoClass ACTIVE = PseudoClass.getPseudoClass("active");
    private final Map<Button, String> destinations = new LinkedHashMap<>();

    public Sidebar(Main main, Role role) {
        getStyleClass().add("sidebar");
        setPrefWidth(238);
        setMinWidth(238);
        setSpacing(8);

        Label logo = new Label("HOTEL SYSTEM");
        logo.getStyleClass().add("brand");
        getChildren().add(logo);

        Label identity = new Label(role.label + (role == Role.CUSTOMER ? " · Guest" : " · Demo"));
        identity.getStyleClass().add("role-label");
        getChildren().add(identity);
        if (role != Role.CUSTOMER) addMenuButton("Overview", "Dashboard", "nav-dashboard", main);
        addMenuButton(role == Role.ADMIN ? "Manage Rooms & Rates" : "Room Availability", "Rooms", "nav-rooms", main);
        if (role == Role.CUSTOMER) {
            addMenuButton("Book a Room", "Book", "nav-book", main);
            addMenuButton("Manage My Booking", "MyBooking", "nav-my-booking", main);
        } else if (role == Role.RECEPTIONIST) {
            addMenuButton("Reservations", "Reservations", "nav-reservations", main);
            addMenuButton("Check In / Check Out", "Reception", "nav-reception", main);
            addMenuButton("Payments & Damage Fines", "Reception", "nav-payments", main);
        } else {
            addMenuButton("Occupancy & Revenue", "Reports", "nav-reports", main);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Button exit = Ui.button("Exit", "nav-exit", false, () -> {
            if (Ui.confirm(this, "Exit", "Are you sure you want to exit?")) Platform.exit();
        });
        exit.getStyleClass().add("nav-button");
        exit.setMaxWidth(Double.MAX_VALUE);
        Button logout = Ui.button(role == Role.CUSTOMER ? "Back to welcome" : "Sign out", "nav-logout", false, main::showLogin);
        logout.getStyleClass().add("nav-button");
        logout.setMaxWidth(Double.MAX_VALUE);
        getChildren().addAll(spacer, logout, exit);
    }

    private void addMenuButton(String text, String page, String id, Main main) {
        Button button = new Button(text);
        button.setId(id);
        button.setOnAction(event -> {
            main.showPage(page);
            // Payments and check-in/out intentionally share the existing reception screen.
            destinations.keySet().forEach(item -> item.pseudoClassStateChanged(ACTIVE, item == button));
        });
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        destinations.put(button, page);
        getChildren().add(button);
    }

    public void selectPage(String page) {
        boolean selected = false;
        for (Map.Entry<Button, String> entry : destinations.entrySet()) {
            boolean active = !selected && entry.getValue().equals(page);
            entry.getKey().pseudoClassStateChanged(ACTIVE, active);
            selected |= active;
        }
    }
}
