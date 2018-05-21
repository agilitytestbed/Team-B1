package nl.utwente.ing.transaction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


public class DatabaseCommunication {
	private static final String FILENAME = "data.db";
	private static final String URL = "jdbc:sqlite:" + FILENAME;

	/**
	 * Connects to the database.
	 * @return
	 * 		Connection object
	 */
	private static Connection connect() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(URL);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return conn;
	}

	/**
	 * Takes the given sql and executes it.
	 * @param sql
	 * 			The sql message as a string
	 */
	private static void executeSQL(String sql) {
		try (Connection conn = connect();
			 Statement stmt = conn.createStatement()) {
			stmt.execute(sql);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

    /**
     * Generates all tables in the database.
     */
    public static void generateTables() {
        String sql = "CREATE TABLE IF NOT EXISTS categoryRules (" +
                "id integer PRIMARY KEY," +
                "description text," +
                "IBAN text," +
                "type text," +
                "categoryId text," +
                "applyOnHistory boolean" +
                ");";
        executeSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS categoryRuleSessions (" +
                "session integer," +
                "categoryRule integer" +
                ");";
        executeSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id integer PRIMARY KEY," +
                "date text," +
                "amount real," +
                "externalIBAN text NOT NULL," +
                "type text NOT NULL," +
				"description text," +
                "categoryID integer" +
                ")";
        executeSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS categories (" +
                "id integer PRIMARY KEY," +
                "name text" +
                ")";
        executeSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS transactionSessions (" +
                "session integer," +
                "transactions text" +
                ")";
        executeSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS categorySessions (" +
                "session integer," +
                "categories text" +
                ")";
        executeSQL(sql);

    }

	/*
	 * -------------------- Code for handling sessions --------------------
	 */

    /**
     * Adds a new transaction in the transactionSessions table.
     * Before doing this it checks if the transaction is already in the table with the given sessionID.
     * @param sessionID id of the session.
     * @param id id of the transaction.
     */
	public static void addTransactionId(int sessionID, int id) {
        String sql = "SELECT * FROM transactionSessions WHERE session = ? AND transactions = ?;";
        boolean add = false;
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionID);
            pstmt.setInt(2, id);
            ResultSet result = pstmt.executeQuery();
            if (!result.next()) {
                add = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (add) {
            sql = "INSERT INTO transactionSessions VALUES (?, ?)";

            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, sessionID);
                pstmt.setInt(2, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
	}

    /**
     * Adds a new category in the categorySessions table.
     * Before doing this it checks if the category is already in the table with the given sessionID.
     * @param sessionID id of the session.
     * @param id id of the category.
     */
	public static void addCategoryId(int sessionID, int id) {
        String sql = "SELECT * FROM categorySessions WHERE session = ? AND categories = ?;";
        boolean add = false;
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionID);
            pstmt.setInt(2, id);
            ResultSet result = pstmt.executeQuery();
            if (!result.next()) {
                add = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (add) {
            sql = "INSERT INTO categorySessions VALUES (?, ?)";

            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, sessionID);
                pstmt.setInt(2, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
	}

    /**
     * Adds a new categoryRule in the categoryRuleSessions table.
     * Before doing this it checks if the categoryRule is already in the table with the given sessionID.
     * @param sessionID id of the session.
     * @param id id of the categoryRule.
     */
	public static void addCategoryRulesId(int sessionID, int id) {
		String sql = "SELECT * FROM categoryRuleSessions WHERE session = ? AND categoryRule = ?;";
		boolean add = false;
		try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
		    pstmt.setInt(1, sessionID);
		    pstmt.setInt(2, id);
		    ResultSet result = pstmt.executeQuery();
		    if (!result.next()) {
		        add = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (add) {
            sql = "INSERT INTO categoryRuleSessions VALUES (?, ?)";

            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, sessionID);
                pstmt.setInt(2, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Adds a new session id to every session table.
     * @param sessionID id of the session.
     */
	public static void addSession(int sessionID) {
		// Add transaction session
		String sql = "INSERT INTO transactionSessions(session) VALUES(?)";
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, sessionID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		
		// Add category session
		sql = "INSERT INTO categorySessions(session) VALUES(?)";
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, sessionID);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }

	    //Add categoryRule session
        sql = "INSERT INTO categoryRuleSessions(session) VALUES(?)";
		try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
		    pstmt.setInt(1, sessionID);
		    pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Gets the maximum session id.
     * @return maximum sessionID or -1 if the table is empty.
     */
    public static int getMaxSessionId() {
        String sql = "SELECT max(session) from transactionSessions";
        try (Connection conn = connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            if (rs.next()) {
                return rs.getInt("max(session)");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    /**
     * Checks if the sessionID is valid or not.
     * @param sessionID id of the session.
     * @return true if the session is valid, false otherwise.
     */
	public static boolean validSessionId(int sessionID) {
		String sql = "SELECT * FROM transactionSessions " +
                    "WHERE session == ?;";
		try (Connection conn = connect();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, sessionID);
			ResultSet rs  = pstmt.executeQuery();
            return rs.next();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return false;
	}

    /**
     * Deletes a transactions from the transactionSessions table
     * @param id of the transaction
     * @param sessionID id of the session
     */
	public static void deleteTransactionId(int sessionID, int id) {
		String sql = "DELETE FROM transactionSessions WHERE session = ? AND transactions = ?;";
		
		try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
			// Substring to remove the first comma
            pstmt.setInt(1, sessionID);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}

    /**
     * Deletes a category from the categorySessions table
     * @param id of the category
     * @param sessionID id of the session
     */
	public static void deleteCategoryId(int sessionID, int id) {
		String sql = "DELETE FROM categorySessions WHERE categories = ? AND session = ?";
		
		try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
			// Substring to remove the first comma
            pstmt.setInt(1, id);
            pstmt.setInt(2, sessionID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}

    /**
     * Deletes a categoryRule from the categoryRuleSessions table
     * @param id of the categoryRule
     * @param sessionID id of the session
     */
	public static void deleteCategoryRulesId(int id, int sessionID) {
	    String sql = "DELETE FROM categoryRuleSessions WHERE categoryRule = ? AND session = ?;";

	    try (Connection conn = connect();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, id);
	        stmt.setInt(2, sessionID);
	        stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Gets a list with all transactions.
     * @return List with transactions.
     */
    public static List<Transaction> getTransactions() {
	    List<Transaction> t = new ArrayList<>();
	    String sql = "SELECT * FROM transactions;";
	    try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        ResultSet result = pstmt.executeQuery();
	        Map<Integer, String> c = getCategories();
	        while(result.next()) {
	            int id = result.getInt("id");
	            String date = result.getString("date");
	            double amount = result.getDouble("amount");
	            String iBAN = result.getString("externalIBAN");
	            String type = result.getString("type");
	            String description = result.getString("description");
	            int categoryId = result.getInt("categoryID");
	            Transaction transaction = new Transaction(id, date, amount, iBAN, type, description);
	            if (c.containsKey(categoryId)) {
                    transaction.setCategory(new Category(categoryId, c.get(categoryId)));
                }
	            t.add(transaction);
            }
            return t;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return t;
    }

    /**
     * Returns a Map with all the categories.
     * Each name is mapped to an id.
     * @return Map with all categories.
     */
    public static Map<Integer, String> getCategories() {
	    Map<Integer, String> c = new HashMap<>();
	    String sql = "SELECT * FROM categories;";
	    try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        ResultSet result = pstmt.executeQuery();
	        while (result.next()) {
	            int id = result.getInt("id");
	            String description = result.getString("name");
	            c.put(id, description);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return c;
    }

    /**
     * Returns a list with all the categoryRules.
     * @return categoryRules list.
     */
    public static List<CategoryRule> getCategoryRules() {
	    List<CategoryRule> cr = new ArrayList<>();
	    String sql = "SELECT * FROM categoryRules;";
	    try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        ResultSet result = pstmt.executeQuery();
	        while (result.next()) {
	            int id = result.getInt("id");
	            String description = result.getString("description");
	            String iBAN = result.getString("IBAN");
	            String type = result.getString("type");
	            int categoryId = result.getInt("categoryId");
	            boolean applyOnHistory = result.getBoolean("applyOnHistory");
	            CategoryRule rule = new CategoryRule(id, description, iBAN, type, categoryId);
	            rule.setApplyOnHistory(applyOnHistory);
	            cr.add(rule);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cr;
    }

	/*
	 * -------------------- Code for normal data --------------------
	 */

    /**
     * Queries the database for the largest category index.
     * @return
     * 		int representing the largest category index or -1 if there are no entries
     */
    public static int getLastCategoryIndex() {
        String sql = "SELECT c.id FROM categories AS c WHERE NOT EXISTS " +
                "(SELECT id FROM categories WHERE id = c.id + 1) LIMIT 0,1";
        try (Connection conn = connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    /**
     * Queries the database for the largest transaction index.
     * @return
     * 		int representing the largest transaction index or -1 if there are no entries
     */
    public static int getLastTransactionIndex() {
        String sql = "SELECT t.id FROM transactions AS t WHERE NOT EXISTS " +
                "(SELECT id FROM transactions WHERE id = t.id + 1) LIMIT 0,1";
        try (Connection conn = connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }
	
	/**
	 * Gets the transaction from the database with a specific id.
	 * @param id
	 * 			Id of the transaction
	 * @return
	 * 			Transaction object from the database
	 */
	public static Transaction getTransaction(int id, int sessionID) {
		String sql = "SELECT * FROM transactions WHERE id == ? AND id IN " +
                "(SELECT transactions FROM transactionSessions WHERE session = ?)";
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, id);
	        pstmt.setInt(2, sessionID);
	        ResultSet rs  = pstmt.executeQuery();
	            
	        if (rs.next()) {
	            int categoryId = rs.getInt("categoryID");
	        	Transaction t = new Transaction(rs.getInt("id"), rs.getString("date"),
						rs.getDouble("amount"), rs.getString("externalIBAN"), rs.getString("type"),
						rs.getString("description"));
                Map<Integer, String> c = getCategories();
                if (c.containsKey(categoryId)) {
                    t.setCategory(new Category(categoryId, c.get(categoryId)));
                }
	        	return t;
	        }
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
	}
	
	/**
	 * Gets all the transactions in the db by filtering with optional parameters.
	 * @return
	 * 			List of transactions
	 */
	public static List<Transaction> getAllTransactions(int offset, int limit,
			int categoryID, int sessionId) {
		List<Transaction> transactions = new ArrayList<>();
		
		
		String sql = "SELECT * FROM transactions WHERE ";
		String sql1 = " id IN (SELECT transactions FROM transactionSessions WHERE session = ?)";
		if (categoryID != -1) {
			sql += "categoryID = ? AND";
		}
		sql += sql1;
		sql += " LIMIT ?,?;";
		try (Connection conn = connect();
		     PreparedStatement pstmt  = conn.prepareStatement(sql)) {
			if (categoryID != -1) {
				pstmt.setInt(1, categoryID);
				pstmt.setInt(2, sessionId);
				pstmt.setInt(3, offset);
				pstmt.setInt(4, limit);
			} else {
			    pstmt.setInt(1, sessionId);
				pstmt.setInt(2, offset);
				pstmt.setInt(3, limit);
			}
			ResultSet rs  = pstmt.executeQuery();
			Transaction t;
			Map<Integer, String> c = getCategories();
	        while (rs.next()) {
	        	t = new Transaction(rs.getInt("id"), rs.getString("date"), rs.getDouble("amount"), rs.getString("externalIBAN"), rs.getString("type"), rs.getString("description"));
	        	t.setCategory(new Category(rs.getInt("categoryID"), c.get(rs.getInt("categoryID"))));
	            transactions.add(t);
	        }
	        return transactions;
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
		
	}

    /**
     * Get all the category rules from the db with the given session ID.
     * @param sessionId
     *          ID of the current session
     * @return
     *          List of category rules
     */
	public static List<CategoryRule> getAllCategoryRules(int sessionId) {
	    List<CategoryRule> result = new ArrayList<>();
	    String sql = "SELECT * FROM categoryRules WHERE id IN " +
                "(SELECT categoryRule FROM categoryRuleSessions WHERE session = ?)";
        try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String description = resultSet.getString("description");
                String iban = resultSet.getString("IBAN");
                String type = resultSet.getString("type");
                int categoryId = resultSet.getInt("categoryId");
                boolean applyOnHistory = resultSet.getBoolean("applyOnHistory");
                CategoryRule cr = new CategoryRule(id, description, iban, type, categoryId);
                cr.setApplyOnHistory(applyOnHistory);
                result.add(cr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
	
	
	/**
	 * Adds a transaction object to the database.
     * Sets the category ID if there exists a category rule that fits it.
	 * @param t
	 * 			Transaction object
	 */
	public static void addTransaction(Transaction t) {
		String sql = "INSERT INTO transactions(id, date, amount, externalIBAN, type, description) VALUES(?,?,?,?,?,?)";
		try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, t.getId());
            pstmt.setString(2, t.getDate());
            pstmt.setDouble(3, t.getAmount());
            pstmt.setString(4, t.getExternalIBAN());
            pstmt.setString(5, t.getType().toString());
            pstmt.setString(6, t.getDescription());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        List<CategoryRule> cr = getCategoryRules();
		Map<Integer, String> c = getCategories();
		for (CategoryRule search : cr) {
		    if ((search.getDescription().equals(t.getDescription()) || search.getDescription().isEmpty()) &&
                    (search.getType() == t.getType() || search.getType() == null) &&
                    (search.getiBan().equals(t.getExternalIBAN()) || search.getiBan().isEmpty()) &&
                    c.containsKey(search.getCategoryId())) {
		        sql = "UPDATE transactions SET categoryID = ? WHERE id = ?";
                String sql2 = "UPDATE categoryRules SET applyOnHistory = ? WHERE id = ?";
		        try (Connection conn = connect();
                            PreparedStatement pstmt = conn.prepareStatement(sql);
                     PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
		            pstmt.setInt(1, search.getCategoryId());
		            pstmt.setInt(2, t.getId());
		            pstmt.executeUpdate();
                    stmt2.setBoolean(1, true);
                    stmt2.setInt(2, search.getId());
                    stmt2.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    /**
     * Get a categoryRule based on id and sessionID.
     * @param id of the category rule.
     * @param sessionID id of the session.
     * @return the category rule with given id and session id.
     */
    public static CategoryRule getCategoryRules(int id, int sessionID) {
	    CategoryRule cr = null;
	    String sql = "SELECT * FROM categoryRules WHERE id = ? AND id IN " +
                "(SELECT categoryRule FROM categoryRuleSessions WHERE session = ?)";
	    try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, id);
	        pstmt.setInt(2, sessionID);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
                cr = new CategoryRule(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getInt(5));
                cr.setApplyOnHistory(rs.getBoolean(6));
                return cr;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cr;
    }

    /**
     * Updates the categoryRule with given id and session id.
     * @param id of the categoryRule.
     * @param categoryRule the new categoryRule.
     * @param sessionID id of the session.
     */
    public static void updateCategoryRule(int id, CategoryRule categoryRule, int sessionID) {
	    String sql = "UPDATE categoryRules " +
                "SET description = ?, " +
                "IBAN = ?, " +
                "type = ?, " +
                "categoryID = ? " +
                "WHERE id = ? " +
                "AND id IN " +
                "(SELECT categoryRule FROM categoryRuleSessions WHERE session = ?)";
	    try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, categoryRule.getDescription());
	        pstmt.setString(2, categoryRule.getiBan());
	        pstmt.setString(3, categoryRule.getType().toString());
	        pstmt.setInt(4, categoryRule.getCategoryId());
	        pstmt.setInt(5, id);
	        pstmt.setInt(6, sessionID);
	        pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the categoryRule with given id and session id.
     * @param id of the categoryRule.
     * @param sessionID of the categoryRule.
     */
    public static void deleteCategoryRule(int id, int sessionID) {
	    String sql = "DELETE FROM categoryRules WHERE id = ? AND id IN " +
                "(SELECT categoryRule FROM categoryRuleSessions WHERE session = ?);";
	    try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, id);
	        pstmt.setInt(2, sessionID);
	        pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the first categoryRule id for which there is no id that equals the current plus 1.
     * @return the Id of the categoryRule or 0 if the table is empty.
     */
    public static int getLastCategoryRuleID() {
	    String sql = "SELECT cr.id FROM categoryRules AS cr WHERE NOT EXISTS " +
                "(SELECT id FROM categoryRules WHERE id = cr.id + 1) LIMIT 0,1";
	    try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Adds a categoryRule object to the database.
     * Also it searches for a transaction for which it can set the categoryID.
     * @param cr CategoryRule object.
     */
	public static void addCategoryRule(CategoryRule cr) {
	    String sql = "INSERT INTO categoryRules VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cr.getId());
            pstmt.setString(2, cr.getDescription());
            pstmt.setString(3, cr.getiBan());
            pstmt.setString(4, cr.getType().toString());
            pstmt.setInt(5, cr.getCategoryId());
            pstmt.setBoolean(6, cr.isApplyOnHistory());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<Transaction> t = getTransactions();
        Map<Integer, String> c = getCategories();
        for (Transaction search : t) {
            System.out.println(search.getCategory());
            if ((search.getExternalIBAN().equals(cr.getiBan()) || cr.getiBan().isEmpty()) &&
                    (search.getType() == cr.getType() || (cr.getType() == null)) &&
                    (search.getDescription().equals(cr.getDescription()) || cr.getDescription().isEmpty()) &&
                    (search.getCategory() == null) &&
                    c.containsKey(cr.getCategoryId())) {
                sql = "UPDATE transactions SET categoryID = ? WHERE id = ?";
                String sql2 = "UPDATE categoryRules SET applyOnHistory = ? WHERE id = ?";
                try (Connection conn = connect();
                            PreparedStatement stmt = conn.prepareStatement(sql);
                            PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                    stmt.setInt(1, cr.getCategoryId());
                    stmt.setInt(2, search.getId());
                    stmt.executeUpdate();
                    stmt2.setBoolean(1, true);
                    stmt2.setInt(2, cr.getId());
                    stmt2.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }
	
	/**
	 * Updates the transaction with the given id 
	 * @param t
	 * 			The updated transaction
	 * @param id
	 * 			Id of the transaction
	 */
	public static void updateTransaction(Transaction t, int id, int sessionId) {
		String sql = "UPDATE transactions SET date = ? , "
                + "amount = ? , "
                + "externalIBAN = ? , "
                + "type = ? ,"
				+ "description = ?"
                + "WHERE id = ? AND id IN "
                + "(SELECT transactions FROM transactionSessions WHERE session = ?)";
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set the corresponding param
            pstmt.setString(1, t.getDate());
            pstmt.setDouble(2, t.getAmount());
            pstmt.setString(3, t.getExternalIBAN());
            pstmt.setString(4, t.getType().toString());
            pstmt.setString(5, t.getDescription());
            pstmt.setInt(6, id);
            pstmt.setInt(7, sessionId);
            // update 
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	/**
	 * Deletes the transaction with the given id
	 * @param id
	 * 			The id of the transaction to delete
	 */
	public static void deleteTransaction(int id, int sessionId) {
        String sql = "DELETE FROM transactions WHERE id = ? and id IN " +
                "(SELECT transactions FROM transactionSessions WHERE session = ?)";
        
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set the corresponding param
            pstmt.setInt(1, id);
            pstmt.setInt(2, sessionId);
            // execute the delete statement
            pstmt.executeUpdate();
 
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	public static void assignCategory(int categoryID, int transactionID) {
		String sql = "UPDATE transactions SET categoryID = ?"
                + "WHERE id = ?";
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        		
        		
            // set the corresponding param
            pstmt.setInt(1, categoryID);
            pstmt.setInt(2, transactionID);
            // update 
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	/**
	 * Gets all categories from the database.
	 * @return
	 * 			List of categories
	 */
	public static List<Category> getAllCategories(int sessionId) {
		List<Category> categories = new ArrayList<>();
		
		String sql = "SELECT * FROM categories WHERE id IN " +
                "(SELECT categories FROM categorySessions WHERE session = ?)";
		try (Connection conn = connect();
	             PreparedStatement pstmt  = conn.prepareStatement(sql)) {
		    pstmt.setInt(1, sessionId);
			ResultSet rs  = pstmt.executeQuery();
	        while (rs.next()) {
	            categories.add(new Category(rs.getInt("id"), rs.getString("name")));
	        }
	        return categories;
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
	}
	/**
	 * Adds the given category object to the database.
	 * @param c
	 * 			Category object
	 */
	public static void addCategory(Category c) {
		String sql = "INSERT INTO categories VALUES(?,?)";
		
		try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, c.getId());
            pstmt.setString(2, c.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	/**
	 * Gets the category from the database with a specific id and in the given session.
	 * @param id
	 * 			Id of the category
	 * @return
	 * 			Category object from the database
	 */
	public static Category getCategory(int id, int sessionId) {
		String sql = "SELECT * FROM categories WHERE id == ? AND id IN " +
                "(SELECT categories FROM categorySessions WHERE session = ?)";
		try (Connection conn = connect(); PreparedStatement pstmt  = conn.prepareStatement(sql)) {
		    pstmt.setInt(1, id);
		    pstmt.setInt(2, sessionId);
	        ResultSet rs  = pstmt.executeQuery();
	        if (rs.next()) {
	            return new Category(rs.getInt("id"), rs.getString("name"));
	        }
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return null;
	}
	
	/**
	 * Deletes the category with the given id
	 * @param id
	 * 			The id of the category to delete
	 */
	public static void deleteCategory(int id, int sessionId) {
		String sql = "DELETE FROM categories WHERE id = ? AND id IN " +
                "(SELECT categories FROM categorySessions WHERE session = ?)";
		
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set the corresponding param
            pstmt.setInt(1, id);
            pstmt.setInt(2, sessionId);
            // execute the delete statement
            pstmt.executeUpdate();
 
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}
	
	/**
	 * Updates the category with the given id 
	 * @param c
	 * 			The updated category
	 * @param id
	 * 			Id of the category
	 */
	public static void updateCategory(Category c, int id, int sessionId) {
		String sql = "UPDATE categories SET name = ? "
                + "WHERE id = ? AND id IN "
                + "(SELECT categories FROM categorySessions WHERE session = ?)";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
        		// set the corresponding param
            pstmt.setString(1, c.getName());
            pstmt.setInt(2, id);
            pstmt.setInt(3, sessionId);
            // update 
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}

	public static void main(String[] args) {
//		DatabaseCommunication d = new DatabaseCommunication();
//	    d.generateTables();
//		DatabaseCommunication.addTransaction(new Transaction(4, "now", 12.5, "NL55RABO0258025899", "deposit", "food"));
//        DatabaseCommunication.addTransaction(new Transaction(5, "now + 1", 12, "NL55RABO0258025899", "deposit", "weapons"));
//        DatabaseCommunication.addTransaction(new Transaction(6, "now + 12", 1222, "NL99INGB0258025802", "deposit", "spotify premium"));
//        DatabaseCommunication.addCategory(new Category(1, "description"));
//		DatabaseCommunication.addCategoryRule(new CategoryRule(1, "", "NL99INGB0258025802", "deposit", 1));
//        DatabaseCommunication.addCategoryRule(new CategoryRule(3, "", "NL55RABO0258025899", "deposit", 2));
//	    DatabaseCommunication.addTransaction(new Transaction(1, "yesterday", 15, "NL99INGB0258025802", "deposit", ""));
//	    DatabaseCommunication.addCategory(new Category(2, "superman"));
//        int id = DatabaseCommunication.getLastTransactionIndex() + 1;
//        Transaction t = new Transaction(id, "2018-05-19T15:48.25", 40.1, "NL89INGB0258025802", "deposit", "");
//        DatabaseCommunication.addTransaction(t);
//        CategoryRule cr = new CategoryRule(6, "", "NL89INGB0258025802", "deposit", 2);
//        DatabaseCommunication.addCategoryRule(cr);
    }
}
