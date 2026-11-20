import javafx.scene.control.*;
import javafx.scene.layout.*;

public class GuestManagePanel extends VBox {
    public GuestManagePanel() {
        Ui.page(this, "Manage my booking");
        Label note = new Label("Use your booking reference and the email provided when you booked. No customer account needed.\nBooking lookup and changes will be connected later.");
        note.setWrapText(true); note.getStyleClass().add("subtitle");
        GridPane form = Ui.form();
        Ui.field(form, 0, "Booking reference", new TextField(), "lookup-reference");
        Ui.field(form, 1, "Email address", new TextField(), "lookup-email");
        Label feedback = new Label(); feedback.setWrapText(true);
        Button find = Ui.button("Find my booking", "find-booking", true,
                () -> feedback.setText("Booking lookup is a UI preview. Contact reception for reservation changes."));
        Label heading = new Label("Once your booking is retrieved"); heading.getStyleClass().add("section-title");
        Button edit = Ui.button("Update booking", "guest-edit", false, () -> {});
        Button extend = Ui.button("Request extra nights", "guest-extend", false, () -> {});
        Button cancel = Ui.button("Cancel booking", "guest-cancel", false, () -> {});
        edit.setDisable(true); extend.setDisable(true); cancel.setDisable(true);
        getChildren().addAll(note, form, find, feedback, heading, Ui.actions(edit, extend, cancel),
                new Label("Your booking details will appear here after lookup is connected."));
    }
}
