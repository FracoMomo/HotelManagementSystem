import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ReportsPanel extends VBox {
    private final Label total = new Label();
    private final Label available = new Label();
    private final Label reserved = new Label();
    private final Label occupied = new Label();
    private final Label revenue = new Label();

    public ReportsPanel(Main main) {
        Ui.page(this, "Occupancy and Revenue Reports");
        VBox report = new VBox();
        report.getStyleClass().add("report");
        report.getChildren().addAll(total, available, reserved, occupied, revenue);
        for (javafx.scene.Node row : report.getChildren()) {
            row.getStyleClass().add("report-row");
            ((Label) row).setMaxWidth(Double.MAX_VALUE);
        }
        revenue.getStyleClass().add("report-revenue");
        getChildren().add(report);
        refreshData();
    }

    public void refreshData() {
        total.setText("Total Rooms: " + HotelData.rooms.size());
        available.setText("Available Rooms: " + HotelData.countRooms("Available"));
        reserved.setText("Reserved Rooms: " + HotelData.countRooms("Reserved"));
        occupied.setText("Occupied Rooms: " + HotelData.countRooms("Occupied"));
        revenue.setText("Recorded Revenue: " + Ui.money(HotelData.getRevenue()));
    }
}
