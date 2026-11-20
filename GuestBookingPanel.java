import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.File;

public class GuestBookingPanel extends VBox {
    public GuestBookingPanel() {
        Ui.page(this, "Book a room");
        Label note = new Label("No account needed. Provide your contact email and an ID for your booking.\nPreview only: no booking or ID is submitted yet.");
        note.setWrapText(true); note.setMinHeight(USE_PREF_SIZE); note.getStyleClass().add("subtitle");
        GridPane form = Ui.form();
        TextField name = new TextField(), email = new TextField(), phone = new TextField();
        Ui.field(form, 0, "Full name *", name, "booking-name");
        Ui.field(form, 1, "Email address *", email, "booking-email");
        email.setPromptText("For reservation updates from the hotel");
        Ui.field(form, 2, "Phone (optional)", phone, "booking-phone");
        ComboBox<String> room = new ComboBox<>(); room.setId("booking-room"); room.setPromptText("Select a room");
        HotelData.rooms.forEach(r -> room.getItems().add(r.number + " · " + r.type + " · " + Ui.money(r.rate) + " / night"));
        room.setMaxWidth(Double.MAX_VALUE);
        form.addRow(3, new Label("Room *"), room);
        DatePicker arrival = new DatePicker(), departure = new DatePicker();
        arrival.setId("booking-arrival"); departure.setId("booking-departure");
        arrival.setPromptText("Choose arrival date"); departure.setPromptText("Choose departure date");
        form.addRow(4, new Label("Check-in *"), arrival);
        form.addRow(5, new Label("Check-out *"), departure);
        TextField id = new TextField(); id.setEditable(false); id.setPromptText("Select an ID image or PDF"); id.setId("booking-id");
        Button browse = Ui.button("Choose ID", "choose-id", false, () -> {
            FileChooser chooser = new FileChooser(); chooser.setTitle("Select identification");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ID image or PDF", "*.png", "*.jpg", "*.jpeg", "*.pdf"));
            File file = chooser.showOpenDialog(getScene().getWindow());
            if (file != null) { id.setText(file.getName()); id.setUserData(file); }
        });
        HBox identity = new HBox(8, id, browse); HBox.setHgrow(id, Priority.ALWAYS);
        form.addRow(6, new Label("Identification *"), identity);
        CheckBox consent = new CheckBox("I agree to share my contact details and ID with the hotel."); consent.setId("booking-consent");
        Label feedback = new Label(); feedback.setWrapText(true); feedback.setMinHeight(USE_PREF_SIZE); feedback.setId("booking-feedback");
        Button preview = Ui.button("Review booking", "review-booking", true, () -> {
            feedback.getStyleClass().setAll("form-error");
            if (name.getText().isBlank() || !email.getText().trim().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")
                    || room.getValue() == null || arrival.getValue() == null || departure.getValue() == null
                    || id.getUserData() == null || !consent.isSelected()) {
                feedback.setText("Complete the required fields, choose an ID, and accept the consent checkbox."); return;
            }
            if (!departure.getValue().isAfter(arrival.getValue())) {
                feedback.setText("Check-out must be after check-in."); return;
            }
            feedback.getStyleClass().setAll("success-note");
            feedback.setText("Booking preview ready for " + name.getText().trim() + ". Hotel updates will go to " + email.getText().trim()
                    + ". No reservation or ID has been submitted.");
        });
        getChildren().addAll(note, form, consent, preview, feedback);
    }
}
