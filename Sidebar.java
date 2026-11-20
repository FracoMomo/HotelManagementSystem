import javax.swing.*;
import java.awt.*;

public class Sidebar extends JPanel {

    public Sidebar(Main main) {
        setPreferredSize(new Dimension(220, 700));
        setBackground(new Color(35, 45, 60));
        setLayout(new GridLayout(8, 1, 5, 8));
        setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JLabel logo = new JLabel("HOTEL SYSTEM", SwingConstants.CENTER);
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Arial", Font.BOLD, 20));
        add(logo);

        addMenuButton("Dashboard", "Dashboard", main);
        addMenuButton("Room Availability", "Rooms", main);
        addMenuButton("Reservations", "Reservations", main);
        addMenuButton("Check In / Check Out", "Reception", main);
        addMenuButton("Payments", "Reception", main);
        addMenuButton("Reports", "Reports", main);

        JButton exit = new JButton("Exit");
        style(exit);
        exit.addActionListener(e -> {
            int answer = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to exit?", "Exit",
                JOptionPane.YES_NO_OPTION
            );
            if (answer == JOptionPane.YES_OPTION) System.exit(0);
        });
        add(exit);
    }

    private void addMenuButton(String text, String page, Main main) {
        JButton button = new JButton(text);
        style(button);
        button.addActionListener(e -> main.showPage(page));
        add(button);
    }

    private void style(JButton button) {
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(55, 70, 90));
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
    }
}
