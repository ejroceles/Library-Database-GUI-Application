import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CheckInBook {

    public static boolean isOpen;

    public static JButton searchButton = new JButton("Search");


    public static void checkIn() {

        isOpen = true;

        // Make the Book Search frame
        JFrame frame = new JFrame("Check In");
        frame.setSize(700, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Check in:");

        // Create a JTextField for the search input
        JTextField searchField = new JTextField(20);
        
        // Create a JButton for triggering the search
        JButton checkInButton = new JButton("Check In Selection");

        // Create a JList to show the results
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> resultList = new JList<>(listModel);
        resultList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);


        frame.setLayout(new BorderLayout());
        panel.add(label);
        panel.add(searchField);
        panel.add(searchButton);
        panel.add(checkInButton);
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
        
        // Add an ActionListener to the search button to search the library 
        searchButton.addActionListener(new ActionListener() {
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
                    String[] searchWords = searchText.split(" ");
                    listModel.clear(); // Clear previous results

                    // Perform the search action with the searchText
                    StringBuilder query = new StringBuilder("SELECT BOOK_LOANS.Isbn, BOOK_LOANS.Card_id, BORROWER.Bname FROM BORROWER JOIN BOOK_LOANS ON BORROWER.Card_id = BOOK_LOANS.Card_id WHERE (");
                    // Append conditions for each search term
                    for (int i = 0; i < searchWords.length; i++) {
                        query.append(" BOOK_LOANS.Isbn LIKE ? OR BOOK_LOANS.Card_id LIKE ? OR BORROWER.Bname LIKE ?");
                        if (i < searchWords.length - 1) {
                            query.append(" OR");
                        }
                    }
                    query.append(") AND Date_in IS NULL");
                    try (PreparedStatement preparedStatement = conn.prepareStatement(query.toString())) {
                        
                        for (int i = 0; i < searchWords.length; i++) {
                            String likePattern = "%" + searchWords[i] + "%";
                            preparedStatement.setString(i * 3 + 1, likePattern); // ISBN10
                            preparedStatement.setString(i * 3 + 2, likePattern); // Title
                            preparedStatement.setString(i * 3 + 3, likePattern); // AuthorName
                        }
                        // Process the results
                        ResultSet resultSet = preparedStatement.executeQuery();

                        while (resultSet.next()) {
                            // Display the results in the ScrollPane
                            String isbn10 = resultSet.getString("Isbn");
                            String cardID = resultSet.getString("Card_id");
                            String borrower = resultSet.getString("Bname");
                            listModel.addElement("ISBN10: " + isbn10 + " | Borrower ID: '" + cardID + "' | Borrower Name: " + borrower);
                        }
                    }
                    // Close the connection
                    conn.close();
                }
                catch(SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "Error in connection or query.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        checkInButton.addActionListener(new ActionListener() {
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
                    if (selectedIndices.length > 0) {
                        for (int index : selectedIndices) {
                            String isbn = listModel.getElementAt(index).substring(8,18);
                            // For book availability
                            String query = "UPDATE BOOK_LOANS SET Date_in = ? WHERE Isbn = ?";
                            try (PreparedStatement preparedStatement = conn.prepareStatement((query))) {
                                LocalDate currentDate = LocalDate.now();
                                preparedStatement.setString(1, currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                                preparedStatement.setString(2, isbn);

                                int rowsUpdated = preparedStatement.executeUpdate();
                                if (rowsUpdated > 0) {
                                    JOptionPane.showMessageDialog(frame, "Successfully checked in '" + isbn + "'", "Check In Successful!", JOptionPane.INFORMATION_MESSAGE);
                                }
                                else {
                                    JOptionPane.showMessageDialog(frame, "Error in checking in selection.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                        searchButton.doClick();
                        SearchBook.searchButton.doClick();
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
