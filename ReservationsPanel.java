import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class ReservationsPanel extends VBox {
    private final Main main;
    private final TableView<HotelData.Reservation> table = Ui.table("reservations-table");

    public ReservationsPanel(Main main) {
        this.main = main;
        Ui.page(this, "Manage Reservations");
        table.getColumns().add(Ui.column("ID", 65, r -> r.id));
        table.getColumns().add(Ui.column("Guest", 135, r -> r.guest));
        table.getColumns().add(Ui.column("Contact", 135, r -> r.contact));
        table.getColumns().add(Ui.column("Room", 65, r -> r.roomNumber));
        table.getColumns().add(Ui.column("Check-in", 105, r -> r.checkIn));
        table.getColumns().add(Ui.column("Check-out", 105, r -> r.checkOut));
        table.getColumns().add(Ui.statusColumn("Status", 115, r -> r.status));
        getChildren().addAll(table, Ui.actions(
                Ui.button("Book Reservation", "book-reservation", true, this::bookReservation),
                Ui.button("Cancel Selected", "cancel-reservation", false, this::cancelReservation)));
        refreshTable();
    }

    public void refreshTable() {
        Ui.refresh(table, HotelData.reservations);
    }

    private void bookReservation() {
        TextField guest = new TextField();
        TextField contact = new TextField();
        TextField room = new TextField();
        TextField checkIn = new TextField();
        TextField checkOut = new TextField();
        GridPane form = Ui.form();
        Ui.field(form, 0, "Guest name:", guest, "guest-name");
        Ui.field(form, 1, "Contact:", contact, "guest-contact");
        Ui.field(form, 2, "Room number:", room, "reservation-room");
        Ui.field(form, 3, "Check-in date:", checkIn, "check-in-date");
        Ui.field(form, 4, "Check-out date:", checkOut, "check-out-date");
        if (!Ui.showForm(this, "New Reservation", form)) return;

        try {
            if (guest.getText().trim().isEmpty()) throw new IllegalArgumentException();
            int roomNumber = Integer.parseInt(room.getText().trim());
            HotelData.Room selectedRoom = HotelData.findRoom(roomNumber);
            if (selectedRoom == null || !selectedRoom.status.equals("Available")) {
                Ui.message(this, "That room is not available.");
                return;
            }
            if (checkIn.getText().trim().isEmpty() || checkOut.getText().trim().isEmpty()) {
                throw new IllegalArgumentException();
            }
            HotelData.Reservation reservation = new HotelData.Reservation(
                    HotelData.nextReservationId++, guest.getText().trim(), contact.getText().trim(),
                    roomNumber, checkIn.getText().trim(), checkOut.getText().trim());
            HotelData.reservations.add(reservation);
            selectedRoom.status = "Reserved";
            main.refreshAll();
            Ui.message(this, "Reservation created. ID: " + reservation.id);
        } catch (IllegalArgumentException ex) {
            Ui.message(this, "Please enter valid reservation details.");
        }
    }

    private void cancelReservation() {
        HotelData.Reservation reservation = table.getSelectionModel().getSelectedItem();
        if (reservation == null) {
            Ui.message(this, "Select a reservation first.");
            return;
        }
        if (reservation.status.equals("Checked In") || reservation.status.equals("Checked Out")) {
            Ui.message(this, "This reservation can no longer be cancelled.");
            return;
        }
        reservation.status = "Cancelled";
        HotelData.Room room = HotelData.findRoom(reservation.roomNumber);
        if (room != null && room.status.equals("Reserved")) room.status = "Available";
        main.refreshAll();
        Ui.message(this, "Reservation cancelled.");
    }
}
