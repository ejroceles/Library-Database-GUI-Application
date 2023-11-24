import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CheckOutBook {

    public static boolean checkOutIsOpen;
    public static boolean isbnSearchIsOpen;

    public static void checkOut(List<String> isbns) {
        
        checkOutIsOpen = true;

        JFrame frame = new JFrame("Check Out");
        frame.setSize(350, 150);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        JLabel label = new JLabel("Card ID of borrower checking out:");
        JTextField cardIDField = new JTextField(20);
        JButton checkOutButton = new JButton("Check Out");

        panel.add(label);
        panel.add(cardIDField);
        panel.add(checkOutButton);
        frame.add(panel);

        // Add a window listener to detect when the new frame is closed
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                checkOutIsOpen = false;
            }
        });

        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(((screenSize.width - frame.getWidth()) / 2), ((screenSize.height - frame.getHeight()) / 2) - 300);
        frame.setVisible(true);

        checkOutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // Create a connection to the local MySQL server
                    Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "project1");

                    // Create a Statement object 
                    Statement stmt = conn.createStatement();

                    // Set the current database, if not already set in the getConnection
                    stmt.execute("USE Library;");

                    // Store what the user inputs in the Card ID bar
                    String cardID = cardIDField.getText();

                    // Check if borrower exists in the database
                    String query = "SELECT * FROM BORROWER WHERE Card_id = ?";
                    try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                        preparedStatement.setString(1, cardID);
                        ResultSet resultSet = preparedStatement.executeQuery();
                        if(resultSet.next()) {
                            // Insert into BOOK_LOANS
                            String query2 = "SELECT COUNT(*) AS num_loans FROM BOOK_LOANS WHERE Card_id = ? AND Date_in IS NULL";
                            try (PreparedStatement preparedStatement2 = conn.prepareStatement((query2))) {
                                preparedStatement2.setString(1, cardID);
                                ResultSet resultSet2 = preparedStatement2.executeQuery();
                                if(resultSet2.next()) {
                                    if(resultSet2.getInt("num_loans") == 3) {
                                        JOptionPane.showMessageDialog(frame, "Borrower '" + cardID + "' already has 3 books loaned out", "Borrower Cannot Check Out", JOptionPane.INFORMATION_MESSAGE);
                                    }
                                    else if(resultSet2.getInt("num_loans") + isbns.size() > 3) {
                                        JOptionPane.showMessageDialog(frame, "Check out cannot be completed since Borrower '" + cardID + "' will have more than 3 books loaned out", "Borrower Cannot Check Out", JOptionPane.INFORMATION_MESSAGE);
                                    }
                                    else {
                                        for(String isbn : isbns) {
                                            String query3 = "INSERT INTO BOOK_LOANS (Isbn, Card_id, Date_out, Due_date, Date_in) VALUES (?, ?, ?, ?, ?)";
                                            try (PreparedStatement preparedStatement3 = conn.prepareStatement(query3)) {
                                                preparedStatement3.setString(1, isbn);
                                                preparedStatement3.setString(2, cardID);
                                                // Set Date_out to today's date
                                                LocalDate currentDate = LocalDate.now();
                                                preparedStatement3.setString(3, currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                                                // Set Due_date to 14 days from today
                                                LocalDate dueDate = currentDate.plusDays(14);
                                                preparedStatement3.setString(4, dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                                                preparedStatement3.setNull(5, java.sql.Types.DATE); // Date_in is null (book not returned)
                                                // Execute the SQL statement
                                                preparedStatement3.executeUpdate();
                                                Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                                                frame.setLocation(((screenSize.width - frame.getWidth()) / 2), (screenSize.height - frame.getHeight()) / 2);
                                                JOptionPane.showMessageDialog(frame, "ISBN '" + isbn + "' has been loaned out to borrower '" + cardID + "'", "Loan Successful!", JOptionPane.INFORMATION_MESSAGE);
                                                frame.dispose();
                                                checkOutIsOpen = false;
                                                SearchBook.searchButton.doClick();  
                                                CheckInBook.searchButton.doClick();
                                            }
                                        }
                                    }
                                }
                            }                          
                        }
                        else {
                            frame.dispose();
                            checkOutIsOpen = false;
                            JOptionPane.showMessageDialog(frame, "Borrower '" + cardID + "' is not in the system", "Borrower Not Found", JOptionPane.INFORMATION_MESSAGE);
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
    }

    public static void checkOutISBN() {

        isbnSearchIsOpen = true;

        JFrame frame = new JFrame("Check Out by ISBN");
        frame.setSize(350, 150);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        JLabel label = new JLabel("ISBN to Check Out:");
        JTextField searchField = new JTextField(20);
        JButton checkOutButton = new JButton("Check Out");

        panel.add(label);
        panel.add(searchField);
        panel.add(checkOutButton);
        frame.add(panel);

        // Add a window listener to detect when the new frame is closed
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                isbnSearchIsOpen = false;
            }
        });

        // Center the frame on the screen
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(((screenSize.width - frame.getWidth()) / 2) + 300, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);

        checkOutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // Create a connection to the local MySQL server
                    Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "project1");

                    // Create a Statement object 
                    Statement stmt = conn.createStatement();

                    // Set the current database, if not already set in the getConnection
                    stmt.execute("USE Library;");

                    // Store what the user inputs in the search bar
                    String searchText = searchField.getText();

                    // Search for book
                    String query = "SELECT * FROM BOOK WHERE Isbn = ?";
                    try (PreparedStatement preparedStatement = conn.prepareStatement((query))) {
                        preparedStatement.setString(1, searchText);
                        ResultSet resultSet = preparedStatement.executeQuery();
                        if(resultSet.next()) {
                            String isbn10 = resultSet.getString("Isbn");
                            // For book availability
                            String query2 = "SELECT * FROM BOOK_LOANS WHERE Isbn = ? AND Date_in IS NULL";     
                            try (PreparedStatement preparedStatement2 = conn.prepareStatement((query2))) {
                                preparedStatement2.setString(1, isbn10);
                                ResultSet resultSet2 = preparedStatement2.executeQuery();
                                if(resultSet2.next()) {
                                    JOptionPane.showMessageDialog(frame, "ISBN " + isbn10 +" is currently checked out", "Book Already Checked Out", JOptionPane.INFORMATION_MESSAGE);
                                }
                                else {
                                    List<String> isbns = new ArrayList<>();
                                    isbns.add(isbn10);
                                    if(!checkOutIsOpen) {
                                        checkOut(isbns);
                                        frame.dispose();
                                        isbnSearchIsOpen = false;
                                    }
                                }
                            }                
                        }
                        else {
                            frame.dispose();
                            isbnSearchIsOpen = false;
                            JOptionPane.showMessageDialog(frame, "ISBN '" + searchText + "' is invalid or not in library", "Search Failed!", JOptionPane.INFORMATION_MESSAGE);
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
    }
}