import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RoomsPanel extends JPanel {

    private DefaultTableModel model;

    public RoomsPanel(Main main) {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Room Availability and Management");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Room Number", "Room Type", "Rate", "Status"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(30);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addRoom = new JButton("Add Room");
        JButton changeRate = new JButton("Update Selected Rate");
        buttons.add(addRoom);
        buttons.add(changeRate);
        add(buttons, BorderLayout.SOUTH);

        addRoom.addActionListener(e -> addRoom());
        changeRate.addActionListener(e -> updateRate(table));
        refreshTable();
    }

    public void refreshTable() {
        model.setRowCount(0);
        for (HotelData.Room room : HotelData.rooms) {
            model.addRow(new Object[]{
                room.number, room.type, String.format("₱%,.2f", room.rate), room.status
            });
        }
    }

    private void addRoom() {
        try {
            String numberText = JOptionPane.showInputDialog(this, "Room number:");
            if (numberText == null) return;
            int number = Integer.parseInt(numberText.trim());

            if (HotelData.findRoom(number) != null) {
                JOptionPane.showMessageDialog(this, "Room number already exists.");
                return;
            }

            String type = JOptionPane.showInputDialog(this, "Room type (Single, Double, Deluxe, Suite):");
            if (type == null || type.trim().isEmpty()) return;

            String rateText = JOptionPane.showInputDialog(this, "Room rate:");
            if (rateText == null) return;
            double rate = Double.parseDouble(rateText.trim());

            if (rate < 0) throw new NumberFormatException();

            HotelData.rooms.add(new HotelData.Room(number, type.trim(), rate, "Available"));
            refreshTable();
            JOptionPane.showMessageDialog(this, "Room added successfully.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid room number and rate.");
        }
    }

    private void updateRate(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room.");
            return;
        }

        int roomNumber = (int) model.getValueAt(row, 0);
        String value = JOptionPane.showInputDialog(this, "Enter new room rate:");
        if (value == null) return;

        try {
            double rate = Double.parseDouble(value.trim());
            if (rate < 0) throw new NumberFormatException();
            HotelData.findRoom(roomNumber).rate = rate;
            refreshTable();
            JOptionPane.showMessageDialog(this, "Room rate updated.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid amount.");
        }
    }
}
