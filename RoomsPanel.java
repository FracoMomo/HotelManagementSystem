import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class RoomsPanel extends VBox {
    private final Main main;
    private final TableView<HotelData.Room> table = Ui.table("rooms-table");

    public RoomsPanel(Main main) {
        this.main = main;
        Ui.page(this, "Room Availability and Management");
        table.getColumns().add(Ui.column("Room Number", 150, room -> room.number));
        table.getColumns().add(Ui.column("Room Type", 200, room -> room.type));
        table.getColumns().add(Ui.moneyColumn("Rate", 170, room -> room.rate));
        table.getColumns().add(Ui.statusColumn("Status", 180, room -> room.status));
        getChildren().addAll(table, Ui.actions(
                Ui.button("Add Room", "add-room", true, this::addRoom),
                Ui.button("Update Selected Rate", "update-rate", false, this::updateRate)));
        refreshTable();
    }

    public void refreshTable() {
        Ui.refresh(table, HotelData.rooms);
    }

    private void addRoom() {
        try {
            var numberText = Ui.input(this, "Room number:");
            if (numberText.isEmpty()) return;
            int number = Integer.parseInt(numberText.get().trim());
            if (HotelData.findRoom(number) != null) {
                Ui.message(this, "Room number already exists.");
                return;
            }
            var type = Ui.input(this, "Room type (Single, Double, Deluxe, Suite):");
            if (type.isEmpty() || type.get().trim().isEmpty()) return;
            var rateText = Ui.input(this, "Room rate:");
            if (rateText.isEmpty()) return;
            double rate = Double.parseDouble(rateText.get().trim());
            if (!Double.isFinite(rate) || rate < 0) throw new NumberFormatException();

            HotelData.rooms.add(new HotelData.Room(number, type.get().trim(), rate, "Available"));
            main.refreshAll();
            Ui.message(this, "Room added successfully.");
        } catch (NumberFormatException ex) {
            Ui.message(this, "Please enter a valid room number and rate.");
        }
    }

    private void updateRate() {
        HotelData.Room room = table.getSelectionModel().getSelectedItem();
        if (room == null) {
            Ui.message(this, "Please select a room.");
            return;
        }
        var value = Ui.input(this, "Enter new room rate:");
        if (value.isEmpty()) return;
        try {
            double rate = Double.parseDouble(value.get().trim());
            if (!Double.isFinite(rate) || rate < 0) throw new NumberFormatException();
            room.rate = rate;
            main.refreshAll();
            Ui.message(this, "Room rate updated.");
        } catch (NumberFormatException ex) {
            Ui.message(this, "Enter a valid amount.");
        }
    }
}
