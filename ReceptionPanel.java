import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class ReceptionPanel extends VBox {
    private final Main main;
    private final TableView<HotelData.Reservation> table = Ui.table("reception-table");

    public ReceptionPanel(Main main) {
        this.main = main;
        Ui.page(this, "Reception: Check-in, Check-out and Payments");
        table.getColumns().add(Ui.column("ID", 80, r -> r.id));
        table.getColumns().add(Ui.column("Guest", 220, r -> r.guest));
        table.getColumns().add(Ui.column("Room", 90, r -> r.roomNumber));
        table.getColumns().add(Ui.statusColumn("Status", 170, r -> r.status));
        table.getColumns().add(Ui.moneyColumn("Paid", 160, r -> r.payment));
        getChildren().addAll(table, Ui.actions(
                Ui.button("Check In", "check-in", true, () -> updateStatus("Check In")),
                Ui.button("Check Out", "check-out", false, () -> updateStatus("Check Out")),
                Ui.button("Record Payment", "record-payment", false, this::recordPayment),
                Ui.button("Apply Damage Fine", "damage-fine", false, () -> PreviewForms.open(this, "Apply Damage Fine", table.getSelectionModel().getSelectedItem(), true))));
        refreshTable();
    }

    public void refreshTable() {
        Ui.refresh(table, HotelData.reservations);
    }

    private HotelData.Reservation selectedReservation() {
        HotelData.Reservation reservation = table.getSelectionModel().getSelectedItem();
        if (reservation == null) Ui.message(this, "Please select a reservation.");
        return reservation;
    }

    private void updateStatus(String action) {
        HotelData.Reservation reservation = selectedReservation();
        if (reservation == null) return;
        HotelData.Room room = HotelData.findRoom(reservation.roomNumber);
        if (action.equals("Check In")) {
            if (!reservation.status.equals("Reserved")) {
                Ui.message(this, "Only reserved guests can check in.");
                return;
            }
            reservation.status = "Checked In";
            room.status = "Occupied";
        } else {
            if (!reservation.status.equals("Checked In")) {
                Ui.message(this, "Only checked-in guests can check out.");
                return;
            }
            reservation.status = "Checked Out";
            room.status = "Available";
        }
        main.refreshAll();
        Ui.message(this, action + " completed.");
    }

    private void recordPayment() {
        HotelData.Reservation reservation = selectedReservation();
        if (reservation == null) return;
        if (reservation.status.equals("Cancelled")) {
            Ui.message(this, "Cannot record payment for a cancelled reservation.");
            return;
        }
        var value = Ui.input(this, "Enter payment amount:");
        if (value.isEmpty()) return;
        try {
            double amount = Double.parseDouble(value.get().trim());
            if (!Double.isFinite(amount) || amount <= 0
                    || !Double.isFinite(reservation.payment + amount)) throw new NumberFormatException();
            reservation.payment += amount;
            main.refreshAll();
            Ui.message(this, "Payment recorded successfully.");
        } catch (NumberFormatException ex) {
            Ui.message(this, "Enter a valid payment amount.");
        }
    }
}
