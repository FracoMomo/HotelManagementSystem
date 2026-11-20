import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;

/** The original standalone room-table component (not a separate navigation page). */
public class RoomTable extends VBox {
    public RoomTable() {
        Ui.page(this, "Room Information");
        TableView<String[]> table = Ui.table("room-information-table");
        table.setEditable(true);
        String[] columns = {"Room No.", "Room Type", "Price", "Status"};
        for (int i = 0; i < columns.length; i++) {
            final int index = i;
            TableColumn<String[], String> column = Ui.column(columns[i], 160, row -> row[index]);
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            column.setOnEditCommit(event -> event.getRowValue()[index] = event.getNewValue());
            table.getColumns().add(column);
        }
        table.setItems(FXCollections.observableArrayList(
                new String[]{"101", "Single", "₱1,500", "Available"},
                new String[]{"102", "Double", "₱2,000", "Occupied"},
                new String[]{"103", "Deluxe", "₱3,500", "Available"},
                new String[]{"104", "Suite", "₱5,000", "Occupied"},
                new String[]{"105", "Single", "₱1,500", "Available"}));
        getChildren().add(table);
    }
}
