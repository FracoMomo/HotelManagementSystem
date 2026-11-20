import javax.swing.*;
import java.awt.*;

public class Dashboard extends JPanel {

    private JLabel totalRooms = new JLabel();
    private JLabel availableRooms = new JLabel();
    private JLabel occupiedRooms = new JLabel();
    private JLabel revenue = new JLabel();

    public Dashboard(Main main) {
        setLayout(new BorderLayout(15, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel subtitle = new JLabel("Hotel Management Overview");
        subtitle.setForeground(Color.GRAY);

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        header.add(title);
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 15));
        cards.setOpaque(false);
        cards.add(createCard("Total Rooms", totalRooms));
        cards.add(createCard("Available Rooms", availableRooms));
        cards.add(createCard("Occupied Rooms", occupiedRooms));
        cards.add(createCard("Recorded Revenue", revenue));
        add(cards, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createCard(String title, JLabel value) {
        JPanel card = new JPanel(new GridLayout(2, 1, 5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel heading = new JLabel(title);
        heading.setForeground(Color.GRAY);
        value.setFont(new Font("Arial", Font.BOLD, 23));

        card.add(heading);
        card.add(value);
        return card;
    }

    public void refreshData() {
        totalRooms.setText(String.valueOf(HotelData.rooms.size()));
        availableRooms.setText(String.valueOf(HotelData.countRooms("Available")));
        occupiedRooms.setText(String.valueOf(HotelData.countRooms("Occupied")));
        revenue.setText(String.format("₱%,.2f", HotelData.getRevenue()));
    }
}
