package nl.utwente.ing.database;

import nl.utwente.ing.model.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

	private static boolean executeSql(String sql, int sessionID, int id) {
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionID);
            pstmt.setInt(2, id);
            ResultSet result = pstmt.executeQuery();
            return !result.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void basicSql(String sql, int sessionID, int id) {
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionID);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a new model in the transactionSessions table.
     * Before doing this it checks if the model is already in the table with the given sessionID.
     * @param sessionID id of the session.
     * @param id id of the model.
     */
	public static void addTransactionId(int sessionID, int id) {
        String sql = "SELECT * FROM transactionSessions WHERE session = ? AND transactions = ?;";
        if(executeSql(sql, sessionID, id)) {
            sql = "INSERT INTO transactionSessions VALUES (?, ?)";
            basicSql(sql, sessionID, id);
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
        if (executeSql(sql, sessionID, id)) {
            sql = "INSERT INTO categorySessions VALUES (?, ?)";
            basicSql(sql, sessionID, id);
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
        if (executeSql(sql, sessionID, id)) {
            sql = "INSERT INTO categoryRuleSessions VALUES (?, ?)";
            basicSql(sql, sessionID, id);
        }

    }

    /**
     * Adds a new session id to every session table.
     * @param sessionID id of the session.
     */
	public static void basicSql(int sessionID) {
		// Add model session
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
     * @param id of the model
     * @param sessionID id of the session
     */
	public static void deleteTransactionId(int sessionID, int id) {
		String sql = "DELETE FROM transactionSessions WHERE session = ? AND transactions = ?;";
        basicSql(sql, sessionID, id);
	}

    /**
     * Deletes a category from the categorySessions table
     * @param id of the category
     * @param sessionID id of the session
     */
	public static void deleteCategoryId(int sessionID, int id) {
		String sql = "DELETE FROM categorySessions WHERE session = ? AND categories = ?";
        basicSql(sql, sessionID, id);
	}

    /**
     * Deletes a categoryRule from the categoryRuleSessions table
     * @param id of the categoryRule
     * @param sessionID id of the session
     */
	public static void deleteCategoryRulesId(int id, int sessionID) {
	    String sql = "DELETE FROM categoryRuleSessions WHERE session = ? AND categoryRule = ?;";
        basicSql(sql, sessionID, id);
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
    private static Map<Integer, String> getCategories() {
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

    private static List<CategoryRule> createCategoryRuleList(ResultSet result) throws SQLException {
        List<CategoryRule> cr = new ArrayList<>();
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
        return cr;
    }

    /**
     * Returns a list with all the categoryRules.
     * @return categoryRules list.
     */
    private static List<CategoryRule> getCategoryRules() {
	    String sql = "SELECT * FROM categoryRules;";
	    try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        ResultSet result = pstmt.executeQuery();
	        return createCategoryRuleList(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

	/*
	 * -------------------- Code for normal data --------------------
	 */

	private static int getLastIndex(String sql){
        try (Connection conn = connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    /**
     * Queries the database for the largest category index.
     * @return
     * 		int representing the largest category index or -1 if there are no entries
     */
    public static int getLastCategoryIndex() {
        String sql = "SELECT c.id FROM categories AS c WHERE NOT EXISTS " +
                "(SELECT id FROM categories WHERE id = c.id + 1) LIMIT 0,1";
        return getLastIndex(sql);
    }

    /**
     * Queries the database for the largest model index.
     * @return
     * 		int representing the largest model index or -1 if there are no entries
     */
    public static int getLastTransactionIndex() {
        String sql = "SELECT t.id FROM transactions AS t WHERE NOT EXISTS " +
                "(SELECT id FROM transactions WHERE id = t.id + 1) LIMIT 0,1";
        return getLastIndex(sql);
    }
	
	/**
	 * Gets the model from the database with a specific id.
	 * @param id
	 * 			Id of the model
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
	    String sql = "SELECT * FROM categoryRules WHERE id IN " +
                "(SELECT categoryRule FROM categoryRuleSessions WHERE session = ?)";
        try (Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            ResultSet resultSet = pstmt.executeQuery();
            return createCategoryRuleList(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	private static void updateCategoryRuleAndTransaction(String sql1, String sql2, int value1, int value2, int value3) {
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql1);
             PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
            pstmt.setInt(1, value1);
            pstmt.setInt(2, value2);
            pstmt.executeUpdate();
            stmt2.setBoolean(1, true);
            stmt2.setInt(2, value3);
            stmt2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	/**
	 * Adds a model object to the database.
     * Sets the category ID if there exists a category rule that fits it.
	 * @param t
	 * 			Transaction object
	 */
	public static void addTransaction(Transaction t) {
		String sql = "INSERT INTO transactions(id, date, amount, externalIBAN, type, description) VALUES(?,?,?,?,?,?)";
		try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, t.getId());
            pstmt.setString(2, t.getDate().toString());
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
                updateCategoryRuleAndTransaction(sql, sql2, search.getCategoryId(), t.getId(), search.getId());
                break;
            }
        }
    }

    /**
     * Adds a categoryRule object to the database.
     * Also it searches for a model for which it can set the categoryID.
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
            if ((search.getExternalIBAN().equals(cr.getiBan()) || cr.getiBan().isEmpty()) &&
                    (search.getType() == cr.getType() || (cr.getType() == null)) &&
                    (search.getDescription().equals(cr.getDescription()) || cr.getDescription().isEmpty()) &&
                    (search.getCategory() == null) &&
                    c.containsKey(cr.getCategoryId())) {
                sql = "UPDATE transactions SET categoryID = ? WHERE id = ?";
                String sql2 = "UPDATE categoryRules SET applyOnHistory = ? WHERE id = ?";
                updateCategoryRuleAndTransaction(sql, sql2, cr.getCategoryId(), search.getId(), cr.getId());
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
	    String sql = "DELETE FROM categoryRules WHERE id IN " +
                "(SELECT categoryRule FROM categoryRuleSessions WHERE session = ?) AND id = ?;";
        basicSql(sql, sessionID, id);
    }

    /**
     * Gets the first categoryRule id for which there is no id that equals the current plus 1.
     * @return the Id of the categoryRule or 0 if the table is empty.
     */
    public static int getLastCategoryRuleID() {
	    String sql = "SELECT cr.id FROM categoryRules AS cr WHERE NOT EXISTS " +
                "(SELECT id FROM categoryRules WHERE id = cr.id + 1) LIMIT 0,1";
        return getLastIndex(sql);
    }
	
	/**
	 * Updates the model with the given id
	 * @param t
	 * 			The updated model
	 * @param id
	 * 			Id of the model
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
            pstmt.setString(1, t.getDate().toString());
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
	 * Deletes the model with the given id
	 * @param id
	 * 			The id of the model to delete
	 */
	public static void deleteTransaction(int id, int sessionId) {
        String sql = "DELETE FROM transactions WHERE id IN " +
                "(SELECT transactions FROM transactionSessions WHERE session = ?) AND id = ?;";
        basicSql(sql, sessionId, id);
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
		String sql = "DELETE FROM categories WHERE id IN " +
                "(SELECT categories FROM categorySessions WHERE session = ?) AND id = ?";
        basicSql(sql, sessionId, id);
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

	public static History getBalanceHistory(int sessionID, String interval, int intervals) {
	    List<Transaction> transactions = new ArrayList<>();
        String limit = "";
        LocalDateTime date = LocalDateTime.now();
        long timestamp = 0;
        if (interval.equals(Interval.day.toString())) {
            limit = date.minusDays(intervals).toString();
            timestamp = date.minusDays(intervals).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()/1000;
        } else if (interval.equals(Interval.hour.toString())) {
            limit = date.minusHours(intervals).toString();
            timestamp = date.minusHours(intervals).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()/1000;
        } else if (interval.equals(Interval.week.toString())) {
            limit = date.minusWeeks(intervals).toString();
            timestamp = date.minusWeeks(intervals).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()/1000;
        } else if (interval.equals(Interval.month.toString())) {
            limit = date.minusMonths(intervals).toString();
            timestamp = date.minusMonths(intervals).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()/1000;
        } else if (interval.equals(Interval.year.toString())) {
            limit = date.minusYears(intervals).toString();
            timestamp = date.minusYears(intervals).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()/1000;
        }
	    String sql = "SELECT * FROM transactions t WHERE t.id IN " +
                "(SELECT transactions FROM transactionSessions WHERE session = ?) " +
                "ORDER BY t.date;";
	    try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, sessionID);
	        ResultSet resultSet = pstmt.executeQuery();
	        double balance = 0;
	        while(resultSet.next()) {
	        	int id = resultSet.getInt("id");
	        	String transactionDate = resultSet.getString("date");
	        	double amount = resultSet.getDouble("amount");
	        	String externalIBAN = resultSet.getString("externalIBAN");
	        	String type = resultSet.getString("type");
	        	String description = resultSet.getString("description");
	        	Transaction t = new Transaction(id, transactionDate, amount, externalIBAN, type, description);
	        	if (t.getType().equals(TransactionType.deposit)) {
	        	    balance += t.getAmount();
                } else {
	        	    balance -= t.getAmount();
                }
                t.setBalance(balance);
	        	transactions.add(t);
			}
			if (transactions.isEmpty()) {
	            return new History(0, 0, 0, 0, 0, timestamp);
            }
            double volume = 0;
	        double low = Double.MAX_VALUE;
	        double high = Double.MIN_VALUE;
            double open;
            double close;
            List<Transaction> transactionList = new ArrayList<>();
            for (Transaction search : transactions) {
	            if (search.getDate().toString().compareTo(limit) > 0) {
	                transactionList.add(search);
                }
            }
            if (transactionList.size() > 0) {
                for (Transaction search : transactionList) {
                    if (search.getBalance() > high) {
                        high = search.getBalance();
                    }
                    if (search.getBalance() < low) {
                        low = search.getBalance();
                    }
                    volume += search.getAmount();
                }
                open = transactionList.get(0).getBalance();
                close = transactionList.get(transactionList.size() - 1).getBalance();
                return new History(open, close, high, low, volume, timestamp);
            } else {
                return new History(0, 0, 0, 0, 0, timestamp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
//        LocalDateTime date = LocalDateTime.now();
//        System.out.println(date.minusHours(24));
//        System.out.println(DatabaseCommunication.getBalanceHistory(1, "day", 10));
        System.out.println(DatabaseCommunication.getBalanceHistory(1, "month", 1).getTimestamp());
    }
}
