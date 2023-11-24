import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AddBorrower {

    public static boolean isOpen;

    public static void addBorrower() {

        isOpen = true;

        // Make the Add Borrower frame
        JFrame frame = new JFrame("Add Borrower");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2));
        JLabel ssnLabel = new JLabel("* SSN (Format = 123-45-6789):");
        JLabel nameLabel = new JLabel("* First and Last Name:");
        JLabel addressLabel = new JLabel("* Address:");
        JLabel phoneLabel = new JLabel("Phone (Format = (123) 456-7890):");
        JLabel required = new JLabel("* = required");

        // Create a JTextFields for the info
        JTextField ssnField = new JTextField(20);
        JTextField nameField = new JTextField(20);
        JTextField addressField = new JTextField(20);
        JTextField phoneField = new JTextField(20);

        // Create a JButton for triggering the search
        JButton addButton = new JButton("Add Borrower");

        panel.add(ssnLabel);
        panel.add(ssnField);
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(addressLabel);
        panel.add(addressField);
        panel.add(phoneLabel);
        panel.add(phoneField);
        panel.add(required);
        panel.add(addButton);
        frame.add(panel);

        // Add a window listener to detect when the new frame is closed
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                isOpen = false;
            }
        });

        frame.setVisible(true);

        // Add ActionListener to the button
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Check if any text field is empty
                if (ssnField.getText().isEmpty() || nameField.getText().isEmpty() ||
                    addressField.getText().isEmpty() || ssnField.getText().length() != 11 || ssnField.getText().charAt(3) != '-' || ssnField.getText().charAt(6) != '-' || (!phoneField.getText().isEmpty() && phoneField.getText().length() != 14)) {
                    JOptionPane.showMessageDialog(frame, "Please fill in all required fields and check input format.",
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                } 
                else {
                    try {
                        // Create a connection to the local MySQL server
                        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "project1");

                        // Create a Statement object 
                        Statement stmt = conn.createStatement();

                        // Set the current database, if not already set in the getConnection
                        stmt.execute("USE Library;");

                        // Store what the user inputs in the text fields
                        String ssn = ssnField.getText();
                        String name = nameField.getText();
                        String address = addressField.getText();
                        String phone = phoneField.getText();
                        
                        String query = "SELECT MAX(CAST(SUBSTRING(Card_id, 3) AS SIGNED)) AS last_card_id FROM BORROWER";
                        try (PreparedStatement preparedStatement = conn.prepareStatement((query))) {
                            ResultSet resultSet = preparedStatement.executeQuery();
                            if(resultSet.next()) {
                                String last_card_id = resultSet.getString("last_card_id");
                                StringBuilder cardID = new StringBuilder("ID");
                                for(int i = last_card_id.length() + 2; i < 8; i++) {
                                    cardID.append("0");
                                }
                                int id = Integer.parseInt(last_card_id);
                                id++;
                                cardID.append(String.valueOf(id));
                                String query2 = "INSERT INTO BORROWER (Card_id, Ssn, Bname, Address, Phone) VALUES (?, ?, ?, ?, ?)";
                                try (PreparedStatement preparedStatement2 = conn.prepareStatement((query2))) {
                                    preparedStatement2.setString(1, cardID.toString());
                                    preparedStatement2.setString(2, ssn);
                                    preparedStatement2.setString(3, name);
                                    preparedStatement2.setString(4, address);
                                    preparedStatement2.setString(5, phone);
                                    // Execute the SQL statement
                                    preparedStatement2.executeUpdate();
                                    JOptionPane.showMessageDialog(frame, "Successfully added '" + name + "' with Card ID: '" + cardID + "'", "Check In Successful!", JOptionPane.INFORMATION_MESSAGE);
                                }
                                catch(SQLException ex) { 
                                    String errorMessage = ex.getLocalizedMessage();
                                    if (errorMessage.contains("Duplicate")) {
                                        JOptionPane.showMessageDialog(frame, "SSN '" + ssn + "' is already in the system.", "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                    else {
                                        JOptionPane.showMessageDialog(frame, "Error in connection or query - check input formats.", "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            }
                        }
                        // Close the connection
                        conn.close();
                    }
                    catch(SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Error in connection or query - check input formats.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }
}