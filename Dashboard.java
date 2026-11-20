import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class Dashboard extends VBox {
    private final Label totalRooms = new Label();
    private final Label availableRooms = new Label();
    private final Label occupiedRooms = new Label();
    private final Label revenue = new Label();

    public Dashboard(Main main) {
        getStyleClass().add("page");
        setSpacing(28);
        getChildren().add(Ui.header("Dashboard", "Hotel Management Overview"));

        GridPane cards = new GridPane();
        cards.setHgap(16);
        cards.setVgap(16);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(50);
            cards.getColumnConstraints().add(column);
        }
        cards.add(createCard("Total Rooms", totalRooms, "total-rooms"), 0, 0);
        cards.add(createCard("Available Rooms", availableRooms, "available-rooms"), 1, 0);
        cards.add(createCard("Occupied Rooms", occupiedRooms, "occupied-rooms"), 0, 1);
        cards.add(createCard("Recorded Revenue", revenue, "revenue"), 1, 1);
        getChildren().add(cards);
        refreshData();
    }

    private VBox createCard(String title, Label value, String id) {
        Label heading = new Label(title);
        heading.getStyleClass().add("metric-heading");
        value.setId(id);
        value.getStyleClass().add("metric-value");
        VBox card = new VBox(18, heading, value);
        card.getStyleClass().add("metric-card");
        card.setMinWidth(0);
        return card;
    }

    public void refreshData() {
        totalRooms.setText(String.valueOf(HotelData.rooms.size()));
        availableRooms.setText(String.valueOf(HotelData.countRooms("Available")));
        occupiedRooms.setText(String.valueOf(HotelData.countRooms("Occupied")));
        revenue.setText(Ui.money(HotelData.getRevenue()));
    }
}
