import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ReceptionPanel extends JPanel {

    private DefaultTableModel model;

    public ReceptionPanel(Main main) {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Reception: Check-in, Check-out and Payments");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        String[] columns = {"ID", "Guest", "Room", "Status", "Paid"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(30);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton checkIn = new JButton("Check In");
        JButton checkOut = new JButton("Check Out");
        JButton payment = new JButton("Record Payment");
        buttons.add(checkIn);
        buttons.add(checkOut);
        buttons.add(payment);
        add(buttons, BorderLayout.SOUTH);

        checkIn.addActionListener(e -> updateStatus(table, "Check In"));
        checkOut.addActionListener(e -> updateStatus(table, "Check Out"));
        payment.addActionListener(e -> recordPayment(table));
        refreshTable();
    }

    public void refreshTable() {
        model.setRowCount(0);
        for (HotelData.Reservation r : HotelData.reservations) {
            model.addRow(new Object[]{
                r.id, r.guest, r.roomNumber, r.status,
                String.format("₱%,.2f", r.payment)
            });
        }
    }

    private HotelData.Reservation selectedReservation(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reservation.");
            return null;
        }
        int id = (int) model.getValueAt(row, 0);
        return HotelData.findReservation(id);
    }

    private void updateStatus(JTable table, String action) {
        HotelData.Reservation r = selectedReservation(table);
        if (r == null) return;

        HotelData.Room room = HotelData.findRoom(r.roomNumber);

        if (action.equals("Check In")) {
            if (!r.status.equals("Reserved")) {
                JOptionPane.showMessageDialog(this, "Only reserved guests can check in.");
                return;
            }
            r.status = "Checked In";
            room.status = "Occupied";
        } else {
            if (!r.status.equals("Checked In")) {
                JOptionPane.showMessageDialog(this, "Only checked-in guests can check out.");
                return;
            }
            r.status = "Checked Out";
            room.status = "Available";
        }

        refreshTable();
        JOptionPane.showMessageDialog(this, action + " completed.");
    }

    private void recordPayment(JTable table) {
        HotelData.Reservation r = selectedReservation(table);
        if (r == null) return;

        if (r.status.equals("Cancelled")) {
            JOptionPane.showMessageDialog(this, "Cannot record payment for a cancelled reservation.");
            return;
        }

        String value = JOptionPane.showInputDialog(this, "Enter payment amount:");
        if (value == null) return;

        try {
            double amount = Double.parseDouble(value.trim());
            if (amount <= 0) throw new NumberFormatException();

            r.payment += amount;
            refreshTable();
            JOptionPane.showMessageDialog(this, "Payment recorded successfully.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid payment amount.");
        }
    }
}
