import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Fines {

    public static boolean isOpen;
    public static boolean payFineIsOpen;
    private static JButton displayFines = new JButton("Display Fines");

    public static void fines() {

        isOpen = true;

        JFrame frame = new JFrame("Fine Management");
        frame.setSize(700, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel label = new JLabel("List of Unpaid Fines:");
        displayFines.setSize(0,0);
        JButton refreshFines = new JButton("Refresh Fines");
        JButton payFines = new JButton("Pay Fine Selection");

        panel.add(refreshFines, BorderLayout.LINE_START);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);
        panel.add(payFines, BorderLayout.LINE_END);

        // Create a JList to show the results
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> resultList = new JList<>(listModel);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        frame.add(displayFines);
        frame.add(panel, BorderLayout.NORTH);
        frame.add(new JScrollPane(resultList), BorderLayout.CENTER);

        // Add a window listener to detect when the new frame is closed
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                isOpen = false;
            }
        });

        frame.setVisible(true);

        displayFines.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // Create a connection to the local MySQL server
                    Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "project1");

                    // Create a Statement object 
                    Statement stmt = conn.createStatement();

                    // Set the current database, if not already set in the getConnection
                    stmt.execute("USE Library;");

                    // Display fines
                    String query = "SELECT BL.Card_id, SUM(F.Fine_amt) AS TotalFineAmount FROM BOOK_LOANS AS BL JOIN FINES AS F ON BL.Loan_id = F.Loan_id WHERE F.Paid = 0 GROUP BY BL.Card_id";
                    try (PreparedStatement preparedStatement = conn.prepareStatement((query))) {
                        ResultSet resultSet = preparedStatement.executeQuery();
                        listModel.clear();
                        if(resultSet.next()) {
                            String cardID = resultSet.getString("Card_id");
                            String totalFineString = resultSet.getString("TotalFineAmount");
                            BigDecimal totalFine = new BigDecimal(totalFineString);
                            listModel.addElement("Card ID: " + cardID + " | Total Fine Amount: $" + totalFine);
                        }
                    }
                    // Close the connection
                    conn.close();
                }
                catch(SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "Error in connection.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        displayFines.doClick();

        refreshFines.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // Create a connection to the local MySQL server
                    Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "project1");

                    // Create a Statement object 
                    Statement stmt = conn.createStatement();

                    // Set the current database, if not already set in the getConnection
                    stmt.execute("USE Library;");

                    LocalDate currentDate = LocalDate.now();
                    String query = "SELECT * FROM BOOK_LOANS WHERE Due_date < ?";
                    try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                        preparedStatement.setString(1, currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)); 

                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            while (resultSet.next()) {
                                int loanId = resultSet.getInt("Loan_id");
                                Date dueDate = resultSet.getDate("Due_date");
                                Date dateIn = resultSet.getDate("Date_in");
                                BigDecimal totalFine = new BigDecimal(".00");
                                if(dateIn != null) {
                                    long daysDifference = ChronoUnit.DAYS.between(dueDate.toLocalDate(), dateIn.toLocalDate());
                                    BigDecimal fineAmt = new BigDecimal(String.valueOf(daysDifference * .25));
                                    totalFine = totalFine.add(fineAmt.setScale(2, RoundingMode.HALF_UP));
                                }
                                else {
                                    long daysDifference = ChronoUnit.DAYS.between(dueDate.toLocalDate(), currentDate);
                                    BigDecimal fineAmt = new BigDecimal(String.valueOf(daysDifference * .25));
                                    totalFine = totalFine.add(fineAmt.setScale(2, RoundingMode.HALF_UP));
                                }
                                
                                String query2 = "INSERT INTO FINES VALUES (?, ?, ?)";
                                try (PreparedStatement preparedStatement2 = conn.prepareStatement((query2))) {
                                    preparedStatement2.setInt(1, loanId);
                                    preparedStatement2.setString(2, totalFine.toString());
                                    preparedStatement2.setInt(3, 0);
                                    preparedStatement2.executeUpdate();
                                }
                                catch(SQLException ex) { 
                                    String errorMessage = ex.getLocalizedMessage();
                                    if (errorMessage.contains("Duplicate")) {
                                        String query3 = "UPDATE FINES SET Fine_amt = ? WHERE Loan_id = ? AND Paid = 0";
                                        try (PreparedStatement preparedStatement3 = conn.prepareStatement((query3))) {
                                            preparedStatement3.setString(1, totalFine.toString());
                                            preparedStatement3.setInt(2, loanId);
                                            preparedStatement3.executeUpdate();
                                        }
                                    }
                                    else {
                                        JOptionPane.showMessageDialog(frame, "Error in connection or query.", "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                }                               
                            }
                        }
                        displayFines.doClick();
                    }
                    // Close the connection
                    conn.close();                        
                }
                catch(SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "Error in connection.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        payFines.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // Create a connection to the local MySQL server
                    Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "project1");

                    // Create a Statement object 
                    Statement stmt = conn.createStatement();

                    // Set the current database, if not already set in the getConnection
                    stmt.execute("USE Library;");

                    // Get selected indices
                    int[] selectedIndices = resultList.getSelectedIndices();

                    // Check if any item is selected
                    String cardID = "";
                    String fineAmtString = "";
                    if (selectedIndices.length > 0) {
                        for (int index : selectedIndices) {
                            cardID = listModel.getElementAt(index).substring(9,17);
                            fineAmtString = listModel.getElementAt(index).substring(40);
                        }
                    
                        String query = "SELECT * FROM BOOK_LOANS WHERE Card_id = ? AND Due_date < ? AND Date_in IS NULL";
                        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                            preparedStatement.setString(1, cardID);
                            LocalDate currentDate = LocalDate.now(); 
                            preparedStatement.setString(2, currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)); 
                            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                                if (resultSet.next()) {
                                    JOptionPane.showMessageDialog(frame, "Cannot pay fines until all overdue books are returned.", "Error", JOptionPane.ERROR_MESSAGE);                              
                                }
                                else {
                                    if(!payFineIsOpen) {
                                        payFine(cardID, fineAmtString);
                                    }
                                }
                            }
                        }
                        // Close the connection
                        conn.close();
                    }                        
                }
                catch(SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "Error in connection or query.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });  
    }
    
    private static void payFine(String cardID, String fineAmtString) {

        payFineIsOpen = true;

        JFrame frame = new JFrame("Pay Fine");
        frame.setSize(400, 100);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        JLabel label = new JLabel("Enter payment $:");
        JTextField paymentField = new JTextField(20);
        JButton payButton = new JButton("Pay");

        panel.add(label);
        panel.add(paymentField);
        panel.add(payButton);
        frame.add(panel);
        
        // Add a window listener to detect when the new frame is closed
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                payFineIsOpen = false;
            }
        });

        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(((screenSize.width - frame.getWidth()) / 2), ((screenSize.height - frame.getHeight()) / 2) - 300);
        frame.setVisible(true);


        payButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // Create a connection to the local MySQL server
                    Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "project1");

                    // Create a Statement object 
                    Statement stmt = conn.createStatement();

                    // Set the current database, if not already set in the getConnection
                    stmt.execute("USE Library;");

                    String paymentString = paymentField.getText();
                    try {
                        BigDecimal fineAmt = new BigDecimal(fineAmtString);
                        BigDecimal payment = new BigDecimal(paymentString);
                        if(fineAmt.subtract(payment).compareTo(BigDecimal.ZERO) == 0) {
                            String query2 = "SELECT * FROM FINES JOIN BOOK_LOANS ON FINES.Loan_id = BOOK_LOANS.Loan_id";
                            try (PreparedStatement preparedStatement2 = conn.prepareStatement((query2))) {
                                ResultSet resultSet2 = preparedStatement2.executeQuery();
                                while(resultSet2.next()) {
                                    int loanID = resultSet2.getInt("Loan_id");
                                    String cardID2 = resultSet2.getString("Card_id");
                                    if(cardID2.equals(cardID)) {
                                        String query3 = "UPDATE FINES SET Paid = 1 WHERE Loan_id = ?";
                                        try (PreparedStatement preparedStatement3 = conn.prepareStatement((query3))) {
                                            preparedStatement3.setInt(1, loanID);
                                            preparedStatement3.executeUpdate();
                                            JOptionPane.showMessageDialog(frame, "Payment Successful!.", "Payment Successful", JOptionPane.INFORMATION_MESSAGE);
                                        }
                                    }
                                }
                            }
                            frame.dispose();
                            displayFines.doClick();
                        }
                        else {
                            JOptionPane.showMessageDialog(frame, "Must pay total fine amount.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    catch (NumberFormatException notNumber) {
                        // If an exception is caught, the string is not a valid number
                        JOptionPane.showMessageDialog(frame, "Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                catch(SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "Error in connection or query.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}