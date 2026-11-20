import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public final class PreviewForms {
    private PreviewForms() { }
    public static void open(Node owner, String title, HotelData.Reservation reservation, boolean fine) {
        if (reservation == null) { Ui.message(owner, "Select a reservation first."); return; }
        GridPane form = Ui.form();
        form.add(new Label("Reservation #" + reservation.id + " · " + reservation.guest), 0, 0, 2, 1);
        if (fine) {
            Ui.field(form, 1, "Damage description", new TextField(), "fine-description");
            Ui.field(form, 2, "Fine amount", new TextField(), "fine-amount");
        } else {
            Ui.field(form, 1, "Guest name", new TextField(reservation.guest), "edit-guest");
            Ui.field(form, 2, "Check-in date", new TextField(reservation.checkIn), "edit-arrival");
            Ui.field(form, 3, "Check-out date", new TextField(reservation.checkOut), "edit-departure");
        }
        Label note = new Label("UI preview · changes are not saved yet.");
        form.add(note, 0, fine ? 3 : 4, 2, 1);
        Dialog<Void> dialog = new Dialog<>(); dialog.setTitle(title); Ui.prepareDialog(dialog, owner);
        dialog.getDialogPane().setContent(form); dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}
