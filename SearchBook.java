import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SearchBook {

    public static boolean isOpen;
    // Create a JButton for triggering the search
    public static JButton searchButton = new JButton("Search");

    public static void searchBook() {

        isOpen = true;

        // Make the Book Search frame
        JFrame frame = new JFrame("Book Search");
        frame.setSize(700, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Search for a book:");

        // Create a JTextField for the search input
        JTextField searchField = new JTextField(20);
        
        // Create a JButton for triggering the search
        JButton checkOutButton = new JButton("Check Out Selection");

        // Create a JList to show the results
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> resultList = new JList<>(listModel);
        resultList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        frame.setLayout(new BorderLayout());
        panel.add(label);
        panel.add(searchField);
        panel.add(searchButton);
        panel.add(checkOutButton);
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

                    // Perform the search action with the searchText
                    StringBuilder query = new StringBuilder("SELECT BOOK.Isbn, BOOK.Title, GROUP_CONCAT(AUTHORS.Name SEPARATOR ', ') AS Authors FROM BOOK JOIN BOOK_AUTHORS ON BOOK.Isbn = BOOK_AUTHORS.Isbn JOIN AUTHORS ON BOOK_AUTHORS.Author_id = AUTHORS.Author_id WHERE");
                    // Append conditions for each search term
                    for (int i = 0; i < searchWords.length; i++) {
                        query.append(" BOOK.Isbn LIKE ? OR BOOK.Title LIKE ? OR AUTHORS.Name LIKE ?");
                        if (i < searchWords.length - 1) {
                            query.append(" OR");
                        }
                    }
                    query.append(" GROUP BY BOOK.Isbn, BOOK.Title");
                    try (PreparedStatement preparedStatement = conn.prepareStatement(query.toString())) {
                        
                        for (int i = 0; i < searchWords.length; i++) {
                            String likePattern = "%" + searchWords[i] + "%";
                            preparedStatement.setString(i * 3 + 1, likePattern); // ISBN10
                            preparedStatement.setString(i * 3 + 2, likePattern); // Title
                            preparedStatement.setString(i * 3 + 3, likePattern); // AuthorName
                        }
            
                        // Process the results
                        ResultSet resultSet = preparedStatement.executeQuery();
                        listModel.clear(); // Clear previous results

                        while (resultSet.next()) {
                            // Display the results in the ScrollPane
                            String isbn10 = resultSet.getString("Isbn");
                            String title = resultSet.getString("Title");
                            String authors = resultSet.getString("Authors");
                            
                            // For book availability
                            String query2 = "SELECT * FROM BOOK_LOANS WHERE Isbn = ? AND Date_in IS NULL";
                            try (PreparedStatement preparedStatement2 = conn.prepareStatement((query2))) {
                                preparedStatement2.setString(1, isbn10);
                                ResultSet resultSet2 = preparedStatement2.executeQuery();
                                if(resultSet2.next()) {                     
                                    // Add the result to the listModel
                                    listModel.addElement("ISBN10: " + isbn10 + " | Title: " + title + " | Author(s): " + authors + " | Unavailable");
                                }
                                else {
                                    listModel.addElement("ISBN10: " + isbn10 + " | Title: " + title + " | Author(s): " + authors + " | Available");
                                }
                            }
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

        checkOutButton.addActionListener(new ActionListener() {
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

                    boolean canCheckOut = false;

                    // Check if any item is selected
                    if (selectedIndices.length > 0) {
                        // Check if more than 3 items selected
                        if(selectedIndices.length > 3) {
                            JOptionPane.showMessageDialog(frame, "Cannot checkout more than 3 books", "Too Many Books Selected", JOptionPane.INFORMATION_MESSAGE);
                        }
                        else {
                            List<String> isbns = new ArrayList<>();
                            for (int index : selectedIndices) {
                                String isbn = listModel.getElementAt(index).substring(8,18);
                                isbns.add(isbn);
                                // For book availability
                                String query = "SELECT * FROM BOOK_LOANS WHERE Isbn = ? AND Date_in IS NULL";
                                try (PreparedStatement preparedStatement = conn.prepareStatement((query))) {
                                    preparedStatement.setString(1, isbn);
                                    ResultSet resultSet = preparedStatement.executeQuery();
                                    if(!resultSet.next()) {
                                        canCheckOut = true;
                                    }
                                    else {
                                        canCheckOut = false;
                                        break;
                                    }
                                }
                            }
                            if(canCheckOut) {
                                if(!CheckOutBook.checkOutIsOpen) {
                                    CheckOutBook.checkOut(isbns);
                                }
                            }
                            else {
                                JOptionPane.showMessageDialog(frame, "One or more of your selections is unavailable", "Selection Unavailable", JOptionPane.INFORMATION_MESSAGE);
                            }
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