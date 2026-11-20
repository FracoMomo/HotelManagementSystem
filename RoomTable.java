
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RoomTable extends JPanel {

    public RoomTable() {

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        setBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        JLabel title = new JLabel("Room Information");
        title.setFont(new Font("Arial", Font.BOLD, 19));

        String[] columns = {
            "Room No.",
            "Room Type",
            "Price",
            "Status"
        };

        Object[][] data = {
            {"101", "Single", "₱1,500", "Available"},
            {"102", "Double", "₱2,000", "Occupied"},
            {"103", "Deluxe", "₱3,500", "Available"},
            {"104", "Suite", "₱5,000", "Occupied"},
            {"105", "Single", "₱1,500", "Available"}
        };

        DefaultTableModel model =
            new DefaultTableModel(data, columns);

        JTable table = new JTable(model);

        table.setRowHeight(35);
        table.setFont(new Font("Arial", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(table);

        add(title, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
}
