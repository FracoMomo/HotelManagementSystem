import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ReceptionHome extends VBox {
    public ReceptionHome(Main main) {
        Ui.page(this, "Reception desk");
        Label note = new Label("Welcome back. Manage the guest journey from reservation to departure.");
        note.setWrapText(true); note.getStyleClass().add("subtitle");
        getChildren().addAll(note,
                section("01  Reservations", "Create bookings, update guest details, and arrange extra nights.", "Manage Reservations", () -> main.showPage("Reservations")),
                section("02  Arrivals & departures", "Find a reservation and handle guest check-in or check-out.", "Open Reception", () -> main.showPage("Reception")),
                section("03  Payments & fines", "Record payments and prepare room damage charges at checkout.", "View Payments", () -> main.showPage("Reception")));
    }
    private VBox section(String title, String text, String action, Runnable run) {
        VBox box = new VBox(10, Ui.header(title, text), Ui.button(action, null, false, run));
        box.getStyleClass().add("access-section"); return box;
    }
}
