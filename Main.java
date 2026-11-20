import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    private CardLayout cardLayout;
    private JPanel content;

    private Dashboard dashboard;
    private RoomsPanel roomsPanel;
    private ReservationsPanel reservationsPanel;
    private ReceptionPanel receptionPanel;
    private ReportsPanel reportsPanel;

    public Main() {
        setTitle("Hotel Management System");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        content = new JPanel(cardLayout);

        dashboard = new Dashboard(this);
        roomsPanel = new RoomsPanel(this);
        reservationsPanel = new ReservationsPanel(this);
        receptionPanel = new ReceptionPanel(this);
        reportsPanel = new ReportsPanel(this);

        content.add(dashboard, "Dashboard");
        content.add(roomsPanel, "Rooms");
        content.add(reservationsPanel, "Reservations");
        content.add(receptionPanel, "Reception");
        content.add(reportsPanel, "Reports");

        add(new Sidebar(this), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        showPage("Dashboard");
    }

    public void showPage(String page) {
        refreshAll();
        cardLayout.show(content, page);
    }

    public void refreshAll() {
        dashboard.refreshData();
        roomsPanel.refreshTable();
        reservationsPanel.refreshTable();
        receptionPanel.refreshTable();
        reportsPanel.refreshData();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
