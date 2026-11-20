import javafx.scene.control.*;
import javafx.scene.layout.*;

public class GuestRoomsPanel extends VBox {
    private final TableView<HotelData.Room> table = Ui.table("guest-rooms-table");
    public GuestRoomsPanel(Main main) {
        Ui.page(this, main.getRole() == Role.CUSTOMER ? "Find your next stay" : "Room Availability");
        Label note = new Label("Explore room types and nightly rates. Date-based availability will be connected later.");
        note.setWrapText(true); note.getStyleClass().add("subtitle");
        DatePicker arrival = new DatePicker(); arrival.setPromptText("Check-in date"); arrival.setId("browse-arrival");
        DatePicker departure = new DatePicker(); departure.setPromptText("Check-out date"); departure.setId("browse-departure");
        table.getColumns().add(Ui.column("Room", 100, r -> r.number));
        table.getColumns().add(Ui.column("Room type", 180, r -> r.type));
        table.getColumns().add(Ui.moneyColumn("Per night", 160, r -> r.rate));
        table.getColumns().add(Ui.statusColumn("Current status", 150, r -> r.status));
        getChildren().addAll(note, new HBox(12, arrival, departure), table);
        getChildren().add(Ui.button(main.getRole() == Role.CUSTOMER ? "Book a Room" : "Create Reservation",
                "room-book", true, () -> main.showPage(main.getRole() == Role.CUSTOMER ? "Book" : "Reservations")));
        refreshData();
    }
    public void refreshData() { Ui.refresh(table, HotelData.rooms); }
}
