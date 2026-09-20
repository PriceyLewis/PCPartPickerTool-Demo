import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

public class Main {
    public static Map<String, Object> user_details = null;
    public static final DefaultListModel<String> basketModel = new DefaultListModel<>();
    private static final String APP_TITLE = "PC Part Picker";
    private static final String BASKET_SEPARATOR = " | ";
    private static final boolean BROWSER_DEMO = Boolean.getBoolean("portfolio.browser");
    private static final boolean COMPACT_BROWSER = BROWSER_DEMO && Boolean.getBoolean("portfolio.compact");

    public static void main(String[] args) {
        if (BROWSER_DEMO) {
            BrowserBridge.signalReady("main-start");
        }

        SwingUtilities.invokeLater(() -> {
            configureLookAndFeel();
            GUI gui = new GUI();
            DatabaseAccess database = new DatabaseAccess();
            if (BROWSER_DEMO) {
                BrowserBridge.signalReady("database-ready");
                user_details = DatabaseAccess.browserDemoUser();
                homeScreen(gui, database);
            } else {
                showWelcomeScreen(gui, database);
            }
        });
    }

    private static int browserWidth(int desktopWidth) {
        return COMPACT_BROWSER ? Math.min(360, desktopWidth) : desktopWidth;
    }

    private static void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Default look and feel applied.");
        }
        AppTheme.install();
    }

    private static void showWelcomeScreen(GUI gui, DatabaseAccess database) {
        JFrame frame = gui.Addframe(browserWidth(600), 420, APP_TITLE + " - Welcome");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = gui.Addpanel(frame, new GridLayout(4, 1, 12, 12));
        JLabel heading = new JLabel("Build and compare PC parts locally", SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(heading, 0);

        JButton signUpButton = gui.Addbutton("Sign Up", panel);
        JButton loginButton = gui.Addbutton("Login", panel);
        JButton exitButton = gui.Addbutton("Exit", panel);

        ImageIcon icon = loadLogoIcon();
        if (icon != null) {
            frame.setIconImage(icon.getImage());
        }

        signUpButton.addActionListener(user_SignUp(gui, database));
        loginButton.addActionListener(user_LoginIn(gui, database));
        exitButton.addActionListener(e -> System.exit(0));

        frame.setVisible(true);
    }

    private static ImageIcon loadLogoIcon() {
        URL resource = Main.class.getResource("/Assets/logo.png");
        if (resource != null) {
            return new ImageIcon(resource);
        }

        File logo = findFile(new File(System.getProperty("user.dir")), "logo.png");
        return logo != null ? new ImageIcon(logo.getAbsolutePath()) : null;
    }

    private static File findFile(File dir, String targetName) {
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                File result = findFile(file, targetName);
                if (result != null) {
                    return result;
                }
            } else if (file.getName().equalsIgnoreCase(targetName)) {
                return file;
            }
        }

        return null;
    }

    public static void showTableFromResultSet(ResultSet rs, String title) {
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            String[] columnNames = new String[columnCount];

            for (int i = 1; i <= columnCount; i++) {
                columnNames[i - 1] = metaData.getColumnName(i);
            }

            DefaultTableModel model = new DefaultTableModel(columnNames, 0);
            while (rs.next()) {
                Object[] rowData = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    rowData[i - 1] = rs.getObject(i);
                }
                model.addRow(rowData);
            }

            JTable table = new JTable(model);
            AppTheme.styleTable(table);
            JFrame frame = new JFrame(title);
            frame.setSize(700, 420);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new JScrollPane(table));
            frame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error displaying table: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void homeScreen(GUI gui, DatabaseAccess database) {
        JFrame frame = gui.Addframe(browserWidth(760), 560, APP_TITLE + " - Dashboard");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel page = gui.Addpanel(frame, new BorderLayout(0, 20));
        String userName = user_details == null ? "Builder" : String.valueOf(user_details.getOrDefault("Username", "Builder"));
        page.add(AppTheme.pageHeader(
            "PC PART PICKER",
            "Welcome back, " + userName,
            "Compare demo inventory, build a basket and get a tailored recommendation."
        ), BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(COMPACT_BROWSER ? 8 : 4, COMPACT_BROWSER ? 1 : 2, 12, 12));
        actions.setOpaque(false);
        JButton browseParts = dashboardButton("Browse inventory", "Compare available parts across stores");
        JButton searchButton = dashboardButton("Advanced search", "Filter by keyword, brand and budget");
        JButton recommendationsButton = dashboardButton("Smart recommendations", "Ask the local assistant for a match");
        JButton basketButton = dashboardButton("Basket · " + basketModel.size() + " item(s)", "Review selections and order summary");
        JButton reviewButton = dashboardButton("Leave a store review", "Share a rating with other builders");
        JButton viewReviewsButton = dashboardButton("Read store reviews", "Compare recent customer feedback");
        JButton historyButton = dashboardButton("Search history", "Revisit your recent component searches");
        JButton exitButton = dashboardButton("Close application", "Return to your desktop or browser");
        actions.add(browseParts);
        actions.add(searchButton);
        actions.add(recommendationsButton);
        actions.add(basketButton);
        actions.add(reviewButton);
        actions.add(viewReviewsButton);
        actions.add(historyButton);
        actions.add(exitButton);
        page.add(actions, BorderLayout.CENTER);

        JLabel status = new JLabel("●  Demo inventory connected   ·   Local recommendation engine ready");
        status.setFont(AppTheme.SUBTITLE);
        status.setForeground(AppTheme.SUCCESS);
        page.add(status, BorderLayout.SOUTH);

        browseParts.addActionListener(e -> partBrowserScreen(gui, database));
        searchButton.addActionListener(e -> searchPartsScreen(gui, database));
        recommendationsButton.addActionListener(e -> ai_GUI(gui, database));
        basketButton.addActionListener(e -> showBasket(gui, database));
        reviewButton.addActionListener(user_LeaveReview(gui, database));
        viewReviewsButton.addActionListener(e -> viewReviewsScreen(gui, database));
        historyButton.addActionListener(e -> viewHistoryScreen(gui, database));
        exitButton.addActionListener(e -> gui.Close(frame));

        frame.setVisible(true);
        if (BROWSER_DEMO) {
            BrowserBridge.signalReady("home");
        }
    }

    private static JButton dashboardButton(String title, String description) {
        JButton button = AppTheme.button("<html><div style='text-align:left'><b>" + title +
            "</b><br><span style='font-size:10px;color:#cbd5e1'>" + description + "</span></div></html>", false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setVerticalAlignment(SwingConstants.CENTER);
        return button;
    }

    public static void managePartsScreen(GUI gui, DatabaseAccess db) {
        JFrame frame = gui.Addframe(browserWidth(760), 440, "Manage Parts");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        String[] columns = {"PartID", "PartName", "Brand"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        AppTheme.styleTable(table);

        Runnable reloadTable = () -> {
            model.setRowCount(0);
            try {
                ResultSet rs = db.executeQuery("SELECT PartID, PartName, Brand FROM Parts ORDER BY PartName");
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("PartID"),
                        rs.getString("PartName"),
                        rs.getString("Brand")
                    });
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Could not load parts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField nameField = new JTextField(16);
        JTextField brandField = new JTextField(12);
        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete Selected");
        JButton refreshButton = new JButton("Refresh");

        controlPanel.add(new JLabel("Name"));
        controlPanel.add(nameField);
        controlPanel.add(new JLabel("Brand"));
        controlPanel.add(brandField);
        controlPanel.add(addButton);
        controlPanel.add(deleteButton);
        controlPanel.add(refreshButton);

        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String brand = brandField.getText().trim();
            if (name.isEmpty() || brand.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Name and brand are required.");
                return;
            }

            db.executeUpdate("INSERT INTO Parts (PartName, Brand) VALUES (?, ?)", name, brand);
            nameField.setText("");
            brandField.setText("");
            reloadTable.run();
        });

        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Select a part first.");
                return;
            }

            Object partId = model.getValueAt(row, 0);
            db.executeUpdate("DELETE FROM Parts WHERE PartID = ?", partId);
            reloadTable.run();
        });

        refreshButton.addActionListener(e -> reloadTable.run());

        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);
        frame.add(panel);
        reloadTable.run();
        frame.setVisible(true);
    }

    private static ActionListener user_LeaveReview(GUI gui, DatabaseAccess database) {
        return e -> {
            if (user_details == null) {
                JOptionPane.showMessageDialog(null, "Log in before leaving a review.");
                return;
            }

            String storeName = gui.AddOptionPane("Store name");
            if (storeName == null || storeName.isBlank()) {
                return;
            }

            String review = gui.AddOptionPane("Review for " + storeName);
            if (review == null || review.isBlank()) {
                return;
            }

            String ratingText = gui.AddOptionPane("Rating between 1 and 5");
            if (ratingText == null) {
                return;
            }

            try {
                int rating = Integer.parseInt(ratingText.trim());
                if (rating < 1 || rating > 5) {
                    throw new NumberFormatException();
                }

                String storeId = database.retrieve_StoreID(storeName.trim());
                if (storeId.isBlank()) {
                    JOptionPane.showMessageDialog(null, "Store not found.");
                    return;
                }

                database.insert_Review(storeId, String.valueOf(user_details.get("UserID")), review.trim(), rating);
                JOptionPane.showMessageDialog(null, "Review submitted.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Rating must be a whole number from 1 to 5.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Could not submit review: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };
    }

    private static ActionListener user_SignUp(GUI gui, DatabaseAccess database) {
        return e -> {
            JFrame frame = gui.Addframe(browserWidth(420), 260, APP_TITLE + " - Sign Up");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel panel = gui.Addpanel(frame, new GridLayout(4, 2, 10, 10));
            JTextField usernameField = new JTextField();
            JTextField preferencesField = new JTextField();
            JTextField locationField = new JTextField();
            JButton signUpButton = new JButton("Create Account");
            JButton cancelButton = new JButton("Cancel");

            panel.add(new JLabel("Username"));
            panel.add(usernameField);
            panel.add(new JLabel("Preferences"));
            panel.add(preferencesField);
            panel.add(new JLabel("Location"));
            panel.add(locationField);
            panel.add(signUpButton);
            panel.add(cancelButton);

            signUpButton.addActionListener(ev -> {
                String username = usernameField.getText().trim();
                String preferences = preferencesField.getText().trim();
                String location = locationField.getText().trim();

                if (username.isEmpty() || preferences.isEmpty() || location.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "All fields are required.");
                    return;
                }

                try {
                    user_details = DatabaseAccess.add_User(username, preferences, location, database.getURL());
                    gui.Close(frame);
                    homeScreen(gui, database);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Sign-up failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            cancelButton.addActionListener(ev -> gui.Close(frame));
            frame.setVisible(true);
        };
    }

    private static ActionListener user_LoginIn(GUI gui, DatabaseAccess database) {
        return e -> {
            JFrame frame = gui.Addframe(browserWidth(380), 180, APP_TITLE + " - Login");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel panel = gui.Addpanel(frame, new GridLayout(2, 2, 10, 10));
            JTextField userIdField = new JTextField();
            JButton loginButton = new JButton("Login");
            JButton cancelButton = new JButton("Cancel");

            panel.add(new JLabel("Enter your User ID"));
            panel.add(userIdField);
            panel.add(loginButton);
            panel.add(cancelButton);

            loginButton.addActionListener(ev -> {
                String userId = userIdField.getText().trim();
                if (userId.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "User ID is required.");
                    return;
                }

                try {
                user_details = DatabaseAccess.retrieve_User(userId, database.getURL());
                    if (user_details == null) {
                        JOptionPane.showMessageDialog(frame, "No user found for ID " + userId + ".");
                        return;
                    }

                    gui.Close(frame);
                    homeScreen(gui, database);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Login failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            cancelButton.addActionListener(ev -> gui.Close(frame));
            frame.setVisible(true);
        };
    }

    public static void ai_GUI(GUI gui, DatabaseAccess database) {
        JFrame frame = gui.Addframe(browserWidth(700), 460, APP_TITLE + " - Recommendations");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setText("Local assistant: ask for a recommendation, budget advice, or a store review.\n\n");

        JTextField inputField = new JTextField();
        JButton sendButton = new JButton("Send");
        JButton closeButton = new JButton("Close");
        JButton basketButton = new JButton("View Basket");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(sendButton);
        buttonPanel.add(basketButton);
        buttonPanel.add(closeButton);

        JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        mainPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        frame.add(mainPanel);

        PCPartAI assistant = new PCPartAI(database);
        ActionListener sendAction = e -> {
            String prompt = inputField.getText().trim();
            if (prompt.isEmpty()) {
                return;
            }

            inputField.setText("");
            chatArea.append("You: " + prompt + "\n");

            PCPartAI.AssistantReply reply = assistant.respond(prompt, user_details);
            chatArea.append("Assistant: " + reply.getMessage() + "\n\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());

            if (reply.hasRecommendation()) {
                int choice = JOptionPane.showConfirmDialog(
                    frame,
                    "Add " + reply.getPartName() + " from " + reply.getStoreName() + " to the basket?",
                    "Add Recommendation",
                    JOptionPane.YES_NO_OPTION
                );
                if (choice == JOptionPane.YES_OPTION) {
                    addItemToBasket(reply.getPartName(), reply.getStoreName(), reply.getPrice(), frame);
                    if (user_details != null) {
                        database.add_Log(reply.getPartName(), user_details, database.getURL());
                    }
                }
            }
        };

        sendButton.addActionListener(sendAction);
        inputField.addActionListener(sendAction);
        basketButton.addActionListener(e -> showBasket(gui, database));
        closeButton.addActionListener(e -> gui.Close(frame));

        frame.setVisible(true);
    }

    public static void adminScreen(GUI gui, DatabaseAccess database) {
        JFrame frame = gui.Addframe(browserWidth(620), 480, "Admin Dashboard");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = gui.Addpanel(frame, new GridLayout(6, 1, 10, 10));
        JButton viewUsers = gui.Addbutton("View Users", panel);
        JButton viewSearches = gui.Addbutton("View Search Logs", panel);
        JButton viewLowStock = gui.Addbutton("View Low Stock", panel);
        JButton manageParts = gui.Addbutton("Manage Parts", panel);
        JButton viewAnalytics = gui.Addbutton("View Analytics", panel);
        JButton closeButton = gui.Addbutton("Close", panel);

        viewUsers.addActionListener(e -> showTableFromResultSet(database.executeQuery("SELECT UserID, Username, Location, Preferences FROM Users ORDER BY UserID"), "Users"));
        viewSearches.addActionListener(e -> showTableFromResultSet(database.executeQuery(
            "SELECT TOP 50 UserID, SearchedPart, SearchDate, UserLocation FROM UserSearches ORDER BY SearchDate DESC"
        ), "Search Logs"));
        viewLowStock.addActionListener(e -> showTableFromResultSet(database.executeQuery(
            "SELECT Parts.PartName, Stores.StoreName, Inventory.StockLevel " +
                "FROM (Inventory INNER JOIN Parts ON Inventory.PartID = Parts.PartID) " +
                "INNER JOIN Stores ON Inventory.StoreID = Stores.StoreID " +
                "WHERE Inventory.StockLevel < 5 ORDER BY Inventory.StockLevel ASC"
        ), "Low Stock"));
        manageParts.addActionListener(e -> managePartsScreen(gui, database));
        viewAnalytics.addActionListener(e -> showAnalytics(frame, database));
        closeButton.addActionListener(e -> gui.Close(frame));

        frame.setVisible(true);
    }

    private static void showAnalytics(JFrame parent, DatabaseAccess database) {
        try {
            ResultSet users = database.executeQuery("SELECT COUNT(*) AS TotalUsers FROM Users");
            ResultSet topSearch = database.executeQuery(
                "SELECT TOP 1 SearchedPart, COUNT(*) AS SearchCount FROM UserSearches GROUP BY SearchedPart ORDER BY COUNT(*) DESC"
            );
            ResultSet lowStock = database.executeQuery("SELECT COUNT(*) AS LowStockCount FROM Inventory WHERE StockLevel < 5");

            StringBuilder summary = new StringBuilder("Admin analytics summary\n\n");
            if (users.next()) {
                summary.append("Total users: ").append(users.getInt("TotalUsers")).append('\n');
            }
            if (topSearch.next()) {
                summary.append("Most searched part: ")
                    .append(topSearch.getString("SearchedPart"))
                    .append(" (")
                    .append(topSearch.getInt("SearchCount"))
                    .append(" searches)\n");
            }
            if (lowStock.next()) {
                summary.append("Low stock inventory lines: ").append(lowStock.getInt("LowStockCount"));
            }

            JOptionPane.showMessageDialog(parent, summary.toString(), "Analytics", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parent, "Could not load analytics: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void searchPartsScreen(GUI gui, DatabaseAccess db) {
        JFrame frame = gui.Addframe(browserWidth(760), 480, "Search Parts");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = gui.Addpanel(frame, new BorderLayout(10, 10));
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(18);
        JTextField minPriceField = new JTextField(6);
        JTextField maxPriceField = new JTextField(6);
        JComboBox<String> brandFilter = new JComboBox<>(new String[]{"All", "Intel", "AMD", "NVIDIA", "Corsair", "MSI", "ASUS"});

        filters.add(new JLabel("Keyword"));
        filters.add(searchField);
        filters.add(new JLabel("Brand"));
        filters.add(brandFilter);
        filters.add(new JLabel("Min Price"));
        filters.add(minPriceField);
        filters.add(new JLabel("Max Price"));
        filters.add(maxPriceField);

        String[] columns = {"Part Name", "Store", "Price", "Brand"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable resultTable = new JTable(tableModel);
        AppTheme.styleTable(resultTable);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton searchButton = new JButton("Search");
        JButton addToBasketButton = new JButton("Add Selected");
        JButton viewBasketButton = new JButton("View Basket");
        bottomPanel.add(searchButton);
        bottomPanel.add(addToBasketButton);
        bottomPanel.add(viewBasketButton);

        ActionListener searchAction = e -> {
            StringBuilder sql = new StringBuilder(
                "SELECT Parts.PartName, Stores.StoreName, Inventory.Price, Parts.Brand " +
                    "FROM (Parts INNER JOIN Inventory ON Parts.PartID = Inventory.PartID) " +
                    "INNER JOIN Stores ON Inventory.StoreID = Stores.StoreID WHERE Inventory.StockLevel > 0"
            );

            java.util.List<Object> params = new java.util.ArrayList<>();
            String keyword = searchField.getText().trim();
            String brand = String.valueOf(brandFilter.getSelectedItem());
            String minPrice = minPriceField.getText().trim();
            String maxPrice = maxPriceField.getText().trim();

            if (!keyword.isEmpty()) {
                sql.append(" AND Parts.PartName LIKE ?");
                params.add("%" + keyword + "%");
            }
            if (!"All".equalsIgnoreCase(brand)) {
                sql.append(" AND Parts.Brand = ?");
                params.add(brand);
            }
            Double minPriceValue;
            Double maxPriceValue;
            try {
                minPriceValue = parseOptionalPriceFilter(minPrice);
                maxPriceValue = parseOptionalPriceFilter(maxPrice);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
                return;
            }

            if (minPriceValue != null && maxPriceValue != null && minPriceValue > maxPriceValue) {
                JOptionPane.showMessageDialog(frame, "Min price cannot be greater than max price.");
                return;
            }

            if (minPriceValue != null) {
                sql.append(" AND Inventory.Price >= ?");
                params.add(minPriceValue);
            }
            if (maxPriceValue != null) {
                sql.append(" AND Inventory.Price <= ?");
                params.add(maxPriceValue);
            }

            sql.append(" ORDER BY Inventory.Price ASC");

            try {
                ResultSet rs = db.executeQuery(sql.toString(), params.toArray());
                tableModel.setRowCount(0);
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getString("PartName"),
                        rs.getString("StoreName"),
                        rs.getDouble("Price"),
                        rs.getString("Brand")
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Search failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        addToBasketButton.addActionListener(e -> {
            int row = resultTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Select a part first.");
                return;
            }

            addItemToBasket(
                String.valueOf(resultTable.getValueAt(row, 0)),
                String.valueOf(resultTable.getValueAt(row, 1)),
                parsePriceValue(resultTable.getValueAt(row, 2)),
                frame
            );
        });

        searchButton.addActionListener(searchAction);
        viewBasketButton.addActionListener(e -> showBasket(gui, db));

        panel.add(filters, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public static void viewReviewsScreen(GUI gui, DatabaseAccess db) {
        JFrame frame = gui.Addframe(browserWidth(640), 420, "Store Reviews");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = gui.Addpanel(frame, new BorderLayout(10, 10));
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        JTextField storeField = new JTextField();
        JButton searchButton = new JButton("Find Reviews");
        JTextArea reviewsArea = new JTextArea();
        reviewsArea.setEditable(false);
        reviewsArea.setLineWrap(true);
        reviewsArea.setWrapStyleWord(true);

        topPanel.add(new JLabel("Store name"), BorderLayout.WEST);
        topPanel.add(storeField, BorderLayout.CENTER);
        topPanel.add(searchButton, BorderLayout.EAST);

        searchButton.addActionListener(e -> {
            String storeName = storeField.getText().trim();
            if (storeName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Enter a store name.");
                return;
            }

            try {
                String storeId = db.retrieve_StoreID(storeName);
                if (storeId.isBlank()) {
                    reviewsArea.setText("Store not found.");
                    return;
                }

                ResultSet rs = db.executeQuery(
                    "SELECT TOP 10 [Rating(1-5)] AS Rating, Comment, ReviewDate FROM StoreReviews WHERE StoreID = ? ORDER BY ReviewDate DESC",
                    storeId
                );

                StringBuilder builder = new StringBuilder();
                while (rs.next()) {
                    int rating = rs.getInt("Rating");
                    builder.append(starString(rating))
                        .append("  ")
                        .append(rs.getString("Comment"))
                        .append(" (")
                        .append(rs.getString("ReviewDate"))
                        .append(")\n\n");
                }

                reviewsArea.setText(builder.length() == 0 ? "No reviews yet for this store." : builder.toString());
            } catch (SQLException ex) {
                reviewsArea.setText("Could not load reviews: " + ex.getMessage());
            }
        });

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(reviewsArea), BorderLayout.CENTER);
        frame.setVisible(true);
    }

    public static void viewHistoryScreen(GUI gui, DatabaseAccess db) {
        if (user_details == null) {
            JOptionPane.showMessageDialog(null, "Log in to view history.");
            return;
        }

        JFrame frame = gui.Addframe(browserWidth(600), 380, "Your History");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Searched Part", "Date", "Location"}, 0);
        JTable table = new JTable(model);
        AppTheme.styleTable(table);

        try {
            ResultSet rs = db.executeQuery(
                "SELECT TOP 25 SearchedPart, SearchDate, UserLocation FROM UserSearches WHERE UserID = ? ORDER BY SearchDate DESC",
                user_details.get("UserID")
            );

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("SearchedPart"),
                    rs.getString("SearchDate"),
                    rs.getString("UserLocation")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Could not load history: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        frame.add(new JScrollPane(table));
        frame.setVisible(true);
    }

    public static void showBasket(GUI gui, DatabaseAccess db) {
        JFrame frame = gui.Addframe(browserWidth(480), 420, "Your Basket");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JList<String> basketList = new JList<>(basketModel);
        JLabel totalLabel = new JLabel("", SwingConstants.CENTER);
        JButton removeButton = new JButton("Remove Selected");
        JButton clearButton = new JButton("Clear Basket");
        JButton checkoutButton = new JButton("Create Order Summary");

        Runnable updateTotal = () -> totalLabel.setText(String.format(Locale.UK, "Total: GBP %.2f", basketTotal()));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(removeButton);
        buttons.add(clearButton);
        buttons.add(checkoutButton);

        removeButton.addActionListener(e -> {
            int selectedIndex = basketList.getSelectedIndex();
            if (selectedIndex >= 0) {
                basketModel.remove(selectedIndex);
                updateTotal.run();
            }
        });

        clearButton.addActionListener(e -> {
            basketModel.clear();
            updateTotal.run();
        });

        checkoutButton.addActionListener(e -> {
            if (basketModel.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Your basket is empty.");
                return;
            }

            StringBuilder receipt = new StringBuilder("Receipt\n\n");
            for (int i = 0; i < basketModel.size(); i++) {
                receipt.append("- ").append(basketModel.getElementAt(i)).append('\n');
            }
            receipt.append("\n").append(totalLabel.getText());
            receipt.append("\nDate: ").append(java.time.LocalDate.now());
            JOptionPane.showMessageDialog(frame, receipt.toString(), "Demo Order Summary", JOptionPane.INFORMATION_MESSAGE);
            basketModel.clear();
            updateTotal.run();
        });

        panel.add(totalLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(basketList), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        frame.add(panel);
        updateTotal.run();
        frame.setVisible(true);
    }

    public static void partBrowserScreen(GUI gui, DatabaseAccess database) {
        JFrame frame = gui.Addframe(browserWidth(820), 500, "Browse Parts");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Part Name", "Store", "Price", "Action"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        JTable table = new JTable(model);
        AppTheme.styleTable(table);
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox(), model));

        JComboBox<String> brandFilter = new JComboBox<>(new String[]{"All Brands", "Intel", "AMD", "NVIDIA", "Corsair", "MSI", "ASUS"});
        JComboBox<String> sortFilter = new JComboBox<>(new String[]{"Default", "Price Low to High", "Price High to Low"});
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(new JLabel("Brand"));
        filters.add(brandFilter);
        filters.add(new JLabel("Sort"));
        filters.add(sortFilter);

        ActionListener loadParts = e -> {
            StringBuilder sql = new StringBuilder(
                "SELECT Parts.PartName, Stores.StoreName, Inventory.Price " +
                    "FROM (Parts INNER JOIN Inventory ON Parts.PartID = Inventory.PartID) " +
                    "INNER JOIN Stores ON Inventory.StoreID = Stores.StoreID WHERE Inventory.StockLevel > 0"
            );
            java.util.List<Object> params = new java.util.ArrayList<>();

            if (!"All Brands".equals(brandFilter.getSelectedItem())) {
                sql.append(" AND Parts.Brand = ?");
                params.add(String.valueOf(brandFilter.getSelectedItem()));
            }

            String sort = String.valueOf(sortFilter.getSelectedItem());
            if ("Price Low to High".equals(sort)) {
                sql.append(" ORDER BY Inventory.Price ASC");
            } else if ("Price High to Low".equals(sort)) {
                sql.append(" ORDER BY Inventory.Price DESC");
            } else {
                sql.append(" ORDER BY Parts.PartName");
            }

            try {
                ResultSet rs = database.executeQuery(sql.toString(), params.toArray());
                model.setRowCount(0);
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("PartName"),
                        rs.getString("StoreName"),
                        rs.getDouble("Price"),
                        "Add to Basket"
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Could not load parts: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        brandFilter.addActionListener(loadParts);
        sortFilter.addActionListener(loadParts);

        panel.add(filters, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.add(panel);

        loadParts.actionPerformed(null);
        frame.setVisible(true);
    }

    public static void addItemToBasket(String partName, String storeName, double price, Component parent) {
        String item = partName + BASKET_SEPARATOR + storeName + BASKET_SEPARATOR + String.format(Locale.UK, "GBP %.2f", price);
        basketModel.addElement(item);
        JOptionPane.showMessageDialog(parent, partName + " added to basket.");
    }

    static Double parseOptionalPriceFilter(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        try {
            double value = Double.parseDouble(text.trim());
            if (!Double.isFinite(value) || value < 0) {
                throw new NumberFormatException("Price must be finite and non-negative");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Price must be a finite number zero or above.");
        }
    }

    public static double parsePriceValue(Object value) {
        if (value == null) {
            return 0;
        }

        double parsed;
        if (value instanceof Number number) {
            parsed = number.doubleValue();
        } else {
            String text = value.toString()
                .replace("GBP", "")
                .replace("£", "")
                .replace("$", "")
                .trim();
            parsed = Double.parseDouble(text);
        }

        if (!Double.isFinite(parsed) || parsed < 0) {
            throw new IllegalArgumentException("Price must be a finite number zero or above.");
        }
        return parsed;
    }

    private static double basketTotal() {
        double total = 0;
        for (int i = 0; i < basketModel.size(); i++) {
            String[] parts = basketModel.get(i).split("\\Q" + BASKET_SEPARATOR + "\\E");
            if (parts.length >= 3) {
                total += parsePriceValue(parts[2]);
            }
        }
        return total;
    }

    private static String starString(int rating) {
        return "★".repeat(Math.max(0, rating)) + "☆".repeat(Math.max(0, 5 - rating));
    }
}
