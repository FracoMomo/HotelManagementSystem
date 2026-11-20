import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/** Shared JavaFX presentation helpers; hotel records remain in HotelData. */
public final class Ui {
    private Ui() { }

    public static String stylesheet() {
        return Objects.requireNonNull(Ui.class.getResource("/hotel.css"),
                "Missing hotel.css resource").toExternalForm();
    }

    public static void applyStyles(Scene scene) {
        scene.getStylesheets().add(stylesheet());
    }

    public static VBox header(String title, String subtitle) {
        Label heading = new Label(title);
        heading.getStyleClass().add("page-title");
        heading.setWrapText(true);
        VBox header = new VBox(6, heading);
        if (subtitle != null) {
            Label description = new Label(subtitle);
            description.getStyleClass().add("subtitle");
            header.getChildren().add(description);
        }
        return header;
    }

    public static void page(VBox page, String title) {
        page.getStyleClass().add("page");
        page.setSpacing(24);
        page.getChildren().add(header(title, null));
    }

    public static Button button(String text, String id, boolean primary, Runnable action) {
        Button button = new Button(text);
        button.setId(id);
        if (primary) button.getStyleClass().add("primary-button");
        button.setOnAction(event -> action.run());
        return button;
    }

    public static FlowPane actions(Button... buttons) {
        FlowPane actions = new FlowPane(10, 10);
        actions.getChildren().addAll(buttons);
        return actions;
    }

    public static <T> TableView<T> table(String id) {
        TableView<T> table = new TableView<>();
        table.setId(id);
        table.setEditable(false);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.setPlaceholder(new Label("No records to display."));
        VBox.setVgrow(table, Priority.ALWAYS);
        return table;
    }

    public static <T, V> TableColumn<T, V> column(
            String title, double width, Function<T, V> value) {
        TableColumn<T, V> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setMinWidth(Math.min(width, 80));
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(value.apply(cell.getValue())));
        return column;
    }

    public static <T> TableColumn<T, Double> moneyColumn(
            String title, double width, Function<T, Double> value) {
        TableColumn<T, Double> column = column(title, width, value);
        column.setCellFactory(ignored -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : money(amount));
                setGraphic(null);
            }
        });
        return column;
    }

    public static <T> TableColumn<T, String> statusColumn(
            String title, double width, Function<T, String> value) {
        TableColumn<T, String> column = column(title, width, value);
        column.setMinWidth(132);
        column.setCellFactory(ignored -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                setText(null);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                badge.setText(status);
                String style = switch (status) {
                    case "Available", "Checked In" -> "status-green";
                    case "Reserved" -> "status-amber";
                    case "Occupied" -> "status-blue";
                    default -> "status-neutral";
                };
                badge.getStyleClass().setAll("status-badge", style);
                setGraphic(badge);
            }
        });
        return column;
    }

    /** Retain object selection and the user's sort order when refreshing records. */
    public static <T> void refresh(TableView<T> table, java.util.Collection<T> records) {
        T selected = table.getSelectionModel().getSelectedItem();
        table.getItems().setAll(records);
        table.sort();
        if (selected != null && records.contains(selected)) {
            table.getSelectionModel().select(selected);
        }
    }

    public static String money(double amount) {
        return String.format("₱%,.2f", amount);
    }

    public static void prepareDialog(Dialog<?> dialog, Node owner) {
        if (owner.getScene() != null && owner.getScene().getWindow() != null) {
            dialog.initOwner(owner.getScene().getWindow());
        }
        dialog.getDialogPane().getStylesheets().add(stylesheet());
        dialog.getDialogPane().getStyleClass().add("hotel-dialog");
        dialog.setHeaderText(null);
    }

    public static void message(Node owner, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Hotel Management System");
        prepareDialog(alert, owner);
        alert.showAndWait();
    }

    public static boolean confirm(Node owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        prepareDialog(alert, owner);
        return alert.showAndWait().filter(ButtonType.YES::equals).isPresent();
    }

    public static Optional<String> input(Node owner, String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Hotel Management System");
        dialog.setContentText(prompt);
        prepareDialog(dialog, owner);
        return dialog.showAndWait();
    }

    public static GridPane form() {
        GridPane form = new GridPane();
        form.setHgap(18);
        form.setVgap(14);
        form.getStyleClass().add("form");
        return form;
    }

    public static void field(GridPane form, int row, String label, TextField field, String id) {
        field.setId(id);
        field.setPrefColumnCount(22);
        Label heading = new Label(label);
        heading.setLabelFor(field);
        form.addRow(row, heading, field);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    public static boolean showForm(Node owner, String title, GridPane form) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        prepareDialog(dialog, owner);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        return dialog.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }
}
