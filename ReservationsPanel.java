import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ReservationsPanel extends JPanel {

    private DefaultTableModel model;

    public ReservationsPanel(Main main) {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Manage Reservations");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        String[] columns = {"ID", "Guest", "Contact", "Room", "Check-in", "Check-out", "Status"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(28);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton book = new JButton("Book Reservation");
        JButton cancel = new JButton("Cancel Selected");
        buttons.add(book);
        buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);

        book.addActionListener(e -> bookReservation());
        cancel.addActionListener(e -> cancelReservation(table));
        refreshTable();
    }

    public void refreshTable() {
        model.setRowCount(0);
        for (HotelData.Reservation r : HotelData.reservations) {
            model.addRow(new Object[]{
                r.id, r.guest, r.contact, r.roomNumber,
                r.checkIn, r.checkOut, r.status
            });
        }
    }

    private void bookReservation() {
        JTextField guest = new JTextField();
        JTextField contact = new JTextField();
        JTextField room = new JTextField();
        JTextField checkIn = new JTextField();
        JTextField checkOut = new JTextField();

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.add(new JLabel("Guest name:")); form.add(guest);
        form.add(new JLabel("Contact:")); form.add(contact);
        form.add(new JLabel("Room number:")); form.add(room);
        form.add(new JLabel("Check-in date:")); form.add(checkIn);
        form.add(new JLabel("Check-out date:")); form.add(checkOut);

        int result = JOptionPane.showConfirmDialog(
            this, form, "New Reservation", JOptionPane.OK_CANCEL_OPTION
        );
        if (result != JOptionPane.OK_OPTION) return;

        try {
            if (guest.getText().trim().isEmpty()) throw new IllegalArgumentException();
            int roomNumber = Integer.parseInt(room.getText().trim());
            HotelData.Room selectedRoom = HotelData.findRoom(roomNumber);

            if (selectedRoom == null || !selectedRoom.status.equals("Available")) {
                JOptionPane.showMessageDialog(this, "That room is not available.");
                return;
            }
            if (checkIn.getText().trim().isEmpty() || checkOut.getText().trim().isEmpty()) {
                throw new IllegalArgumentException();
            }

            HotelData.Reservation reservation = new HotelData.Reservation(
                HotelData.nextReservationId++,
                guest.getText().trim(),
                contact.getText().trim(),
                roomNumber,
                checkIn.getText().trim(),
                checkOut.getText().trim()
            );

            HotelData.reservations.add(reservation);
            selectedRoom.status = "Reserved";
            refreshTable();
            JOptionPane.showMessageDialog(this, "Reservation created. ID: " + reservation.id);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid reservation details.");
        }
    }

    private void cancelReservation(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a reservation first.");
            return;
        }

        int id = (int) model.getValueAt(row, 0);
        HotelData.Reservation r = HotelData.findReservation(id);

        if (r.status.equals("Checked In") || r.status.equals("Checked Out")) {
            JOptionPane.showMessageDialog(this, "This reservation can no longer be cancelled.");
            return;
        }

        r.status = "Cancelled";
        HotelData.Room room = HotelData.findRoom(r.roomNumber);
        if (room != null && room.status.equals("Reserved")) room.status = "Available";

        refreshTable();
        JOptionPane.showMessageDialog(this, "Reservation cancelled.");
    }
}
