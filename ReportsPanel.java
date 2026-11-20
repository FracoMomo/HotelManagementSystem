import javax.swing.*;
import java.awt.*;

public class ReportsPanel extends JPanel {

    private JLabel total = new JLabel();
    private JLabel available = new JLabel();
    private JLabel reserved = new JLabel();
    private JLabel occupied = new JLabel();
    private JLabel revenue = new JLabel();

    public ReportsPanel(Main main) {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Occupancy and Revenue Reports");
        title.setFont(new Font("Arial", Font.BOLD, 25));
        add(title, BorderLayout.NORTH);

        JPanel report = new JPanel(new GridLayout(5, 1, 10, 10));
        report.setBackground(Color.WHITE);
        report.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        report.add(total);
        report.add(available);
        report.add(reserved);
        report.add(occupied);
        report.add(revenue);

        add(report, BorderLayout.CENTER);
        refreshData();
    }

    public void refreshData() {
        total.setText("Total Rooms: " + HotelData.rooms.size());
        available.setText("Available Rooms: " + HotelData.countRooms("Available"));
        reserved.setText("Reserved Rooms: " + HotelData.countRooms("Reserved"));
        occupied.setText("Occupied Rooms: " + HotelData.countRooms("Occupied"));
        revenue.setText(String.format("Recorded Revenue: ₱%,.2f", HotelData.getRevenue()));
    }
}
