import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MainMenu {

    public MainMenu() {
        // Create the main frame
        JFrame frame = new JFrame("Project1");
        frame.setSize(350, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Create a label
        JLabel label = new JLabel("Hello, Librarian!");

        // Create a button
        JButton button1 = new JButton("Search Book");
        JButton button2 = new JButton("Check Out By ISBN");
        JButton button3 = new JButton("Check In Book");
        JButton button4 = new JButton("Add Borrower");
        JButton button5 = new JButton("Fines");

        // Set maximum size for all buttons to ensure they have the same width
        Dimension buttonSize = new Dimension(Short.MAX_VALUE, button1.getPreferredSize().width);
        button1.setMaximumSize(buttonSize);
        button2.setMaximumSize(buttonSize);
        button3.setMaximumSize(buttonSize);
        button4.setMaximumSize(buttonSize);
        button5.setMaximumSize(buttonSize);

        // Set center alignment for the panel elements
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        button1.setAlignmentX(Component.CENTER_ALIGNMENT);
        button2.setAlignmentX(Component.CENTER_ALIGNMENT);
        button3.setAlignmentX(Component.CENTER_ALIGNMENT);
        button4.setAlignmentX(Component.CENTER_ALIGNMENT);
        button5.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add ActionListener to handle button clicks
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!SearchBook.isOpen)
                    SearchBook.searchBook();
            }
        });

        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!CheckOutBook.isbnSearchIsOpen)
                    CheckOutBook.checkOutISBN();
            }
        });

        button3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!CheckInBook.isOpen) {
                    CheckInBook.checkIn();
                }
            }
        });

        button4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!AddBorrower.isOpen) {
                    AddBorrower.addBorrower();
                }
            }
        });
        
        button5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!Fines.isOpen) {
                    Fines.fines();
                }
            }
        });

        // Add components to the panel
        panel.add(label);
        panel.add(button1);
        panel.add(button2);
        panel.add(button3);
        panel.add(button4);
        panel.add(button5);

        // Add the panel to the content pane
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new GridBagLayout());

        // Create GridBagConstraints for centering the panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        // Add the panel to the content pane
        contentPane.add(panel);

        // Center the frame on the screen
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);

        // Set the frame visibility
        frame.setVisible(true);
    }
    public static void main(String[] args) {
        // Load the MySQL JDBC driver
        try {
            DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "project1");
        }
        catch(SQLException ex) {
            JFrame frame = new JFrame("Error");
            frame.setSize(400, 200);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JOptionPane.showMessageDialog(frame, "Error in connection.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Create and run the Swing GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainMenu();
            }
        });
    }
}