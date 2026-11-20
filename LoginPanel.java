import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LoginPanel extends HBox {
    public LoginPanel(Main main) {
        setId("login-screen");
        VBox welcome = new VBox(24);
        welcome.getStyleClass().add("login-welcome");
        welcome.setPrefWidth(410);
        welcome.setMinWidth(340);
        Label brand = new Label("HOTEL MANAGEMENT SYSTEM");
        brand.getStyleClass().add("eyebrow");
        Label title = new Label("A welcoming stay.\nA well-run hotel.");
        title.getStyleClass().add("login-title");
        title.setWrapText(true);
        Label intro = new Label("Find your room, manage arrivals, or oversee the hotel. Start with the access that fits your visit.");
        intro.setWrapText(true);
        Region space = new Region();
        VBox.setVgrow(space, Priority.ALWAYS);
        Label note = new Label("SENIOR DEVELOPERS  /  CSIT228\nInterface preview · sample data");
        note.setWrapText(true);
        welcome.getChildren().addAll(brand, title, intro, space, note);

        VBox access = new VBox(12);
        access.setMinHeight(USE_PREF_SIZE);
        access.getStyleClass().add("login-access");
        access.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(access, Priority.ALWAYS);
        access.getChildren().add(Ui.header("Welcome", "Choose guest access or sign in to your staff workspace."));
        VBox guest = new VBox(10);
        guest.getStyleClass().add("access-section");
        guest.getChildren().addAll(Ui.header("Staying with us?", "Customer access · no account or password required"),
                new Label("Have your email address and an ID ready when booking."),
                Ui.button("Continue as Customer", "guest-access", true, () -> main.enterWorkspace(Role.CUSTOMER)));

        ComboBox<String> role = new ComboBox<>();
        role.getItems().addAll("Receptionist", "Hotel Administrator");
        role.getSelectionModel().selectFirst();
        role.setId("login-role");
        role.setMaxWidth(Double.MAX_VALUE);
        TextField username = new TextField();
        PasswordField password = new PasswordField();
        Label credentials = new Label();
        credentials.setWrapText(true);
        credentials.setMinHeight(USE_PREF_SIZE);
        credentials.setId("demo-credentials");
        credentials.getStyleClass().add("credential-note");
        Runnable update = () -> {
            credentials.setText("Demo logins\nReceptionist: receptionist / reception123\nHotel Administrator: admin / admin123");
            username.clear(); password.clear();
        };
        role.setOnAction(e -> update.run()); update.run();
        GridPane fields = Ui.form();
        Label roleLabel = new Label("Staff role"); roleLabel.setLabelFor(role);
        fields.addRow(0, roleLabel, role);
        Ui.field(fields, 1, "Username", username, "login-username");
        Ui.field(fields, 2, "Password", password, "login-password");
        Label error = new Label(); error.setId("login-error"); error.getStyleClass().add("form-error");
        Button signIn = Ui.button("Sign in to workspace", "staff-login", true, () -> {
            boolean admin = role.getSelectionModel().getSelectedIndex() == 1;
            if (username.getText().trim().equals(admin ? "admin" : "receptionist")
                    && password.getText().equals(admin ? "admin123" : "reception123")) {
                main.enterWorkspace(admin ? Role.ADMIN : Role.RECEPTIONIST);
            } else error.setText("Use the demo username and password shown above.");
        });
        signIn.setDefaultButton(true);
        access.getChildren().addAll(guest, Ui.header("Staff sign in", null), fields, credentials, error, signIn);
        ScrollPane accessScroll = new ScrollPane(access);
        accessScroll.setFitToWidth(true);
        accessScroll.setFitToHeight(true);
        accessScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        accessScroll.getStyleClass().add("workspace-scroll");
        HBox.setHgrow(accessScroll, Priority.ALWAYS);
        getChildren().addAll(welcome, accessScroll);
    }
}
