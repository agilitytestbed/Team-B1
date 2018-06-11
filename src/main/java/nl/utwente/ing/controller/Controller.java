package nl.utwente.ing.controller;

import java.util.List;

import nl.utwente.ing.model.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import nl.utwente.ing.database.DatabaseCommunication;


@RestController
@RequestMapping(value = "/api/v1" , produces = "application/json", consumes = "application/json")
public class Controller {

    private boolean correctJsonTransaction(JSONObject jsonTransaction) {
        return jsonTransaction.has("date") && jsonTransaction.has("amount") &&
                jsonTransaction.has("externalIBAN") && jsonTransaction.has("type") &&
                jsonTransaction.has("description");
    }

    private boolean checkValidType(String type) {
        for (TransactionType search : TransactionType.values()) {
            if (search.name().equals(type)) {
                return true;
            }
        }
        return false;
    }

    private boolean correctJsonCategoryRule(JSONObject jsonCategoryRule) {
        return jsonCategoryRule.has("description") && jsonCategoryRule.has("IBAN") &&
                jsonCategoryRule.has("type") && jsonCategoryRule.has("categoryId");
    }

	// ---------------- Exception handling --------------------
	@ResponseStatus(value=HttpStatus.METHOD_NOT_ALLOWED,
            reason="Invalid input given")  // 405
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public void invalidInput() {}

	// ---------------- Responses --------------------
	// No/Wrong sessionID
	@SuppressWarnings("serial")
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason="Session ID is missing or invalid")
	private class SessionIDException extends RuntimeException {}

	// Invalid input
	@SuppressWarnings("serial")
	@ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED, reason="Invalid input given")
	private class InvalidInputException extends RuntimeException {}

	// Invalid input
	@SuppressWarnings("serial")
	@ResponseStatus(value = HttpStatus.NOT_FOUND, reason="Item(s) not found")
	private class ItemNotFound extends RuntimeException {}


	// ---------------- Transactions -----------------
	// GET - Offset, limit and category parameter

	@RequestMapping(value = "/transactions", method = RequestMethod.GET, produces = "application/json", consumes = "*")
	public List<Transaction> getAllTransactions(
			@RequestParam(value="offset", defaultValue="0") int offset,
			@RequestParam(value="limit", defaultValue="20") int limit,
			@RequestParam(value="category", defaultValue="-1") int categoryID,
			@RequestParam(value="session_id", required =false) String sessionId,
            @RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader) {

        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

		// Enforce the limits for offset and limit
		offset = Math.max(offset, 0);
		limit = Math.max(limit, 1);
		limit = Math.min(limit, 100);


		return DatabaseCommunication.getAllTransactions(offset, limit, categoryID, Integer.parseInt(sessionId));
	}

	// POST
	@RequestMapping(method = RequestMethod.POST, value = "/transactions", produces = "application/json", consumes = "*")
	public ResponseEntity addTransaction(
			@RequestBody String t,
			@RequestParam(value="session_id", required =false) String sessionId,
            @RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader) {

        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

        if(t.isEmpty()) {
			throw new InvalidInputException();
		}

		int newId = DatabaseCommunication.getLastTransactionIndex() + 1;
        JSONObject jsonTransaction = new JSONObject(t);

        if (!correctJsonTransaction(jsonTransaction)) {
            throw new InvalidInputException();
        }

        if (!checkValidType(jsonTransaction.getString("type"))) {
            throw new InvalidInputException();
        }

        Transaction transaction = new Transaction(newId, jsonTransaction.getString("date"), jsonTransaction.getDouble("amount"),
                jsonTransaction.getString("externalIBAN"), jsonTransaction.getString("type"), jsonTransaction.getString("description"));

        if (!transaction.validTransaction()) {
            throw new InvalidInputException();
        }

        //Add the model id to the session
		DatabaseCommunication.addTransactionId(Integer.parseInt(sessionId), transaction.getId());
		DatabaseCommunication.addTransaction(transaction);

		// Create a response add the created object to it
		return new ResponseEntity<>(t, HttpStatus.CREATED);
	}

	// GET
	@RequestMapping(value = "/transactions/{id}", method = RequestMethod.GET, produces = "application/json", consumes = "*")
	public Transaction getTransaction(
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String sessionId,
            @RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader) {

        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

		Transaction transaction = DatabaseCommunication.getTransaction(id, Integer.parseInt(sessionId));
		if (transaction == null) {
			throw new ItemNotFound();
		}

		return transaction;
	}

	// PUT
	@RequestMapping(method = RequestMethod.PUT, value = "/transactions/{id}", produces = "application/json", consumes = "*")
	public ResponseEntity<Transaction> updateTransaction(
			@RequestBody String t ,
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String sessionId,
            @RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader) {

	    if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

        if(t.isEmpty()) {
            throw new InvalidInputException();
        }

        JSONObject jsonTransaction = new JSONObject(t);

        if (!correctJsonTransaction(jsonTransaction)) {
            throw new InvalidInputException();
        }

        if (!checkValidType(jsonTransaction.getString("type"))) {
            throw new InvalidInputException();
        }

        Transaction transaction = new Transaction(id, jsonTransaction.getString("date"), jsonTransaction.getDouble("amount"),
                jsonTransaction.getString("externalIBAN"), jsonTransaction.getString("type"), jsonTransaction.getString("description"));

        if (!transaction.validTransaction()) {
            throw new InvalidInputException();
        }

        if (DatabaseCommunication.getTransaction(id, Integer.parseInt(sessionId)) == null) {
            throw new ItemNotFound();
        }

		DatabaseCommunication.updateTransaction(transaction ,id, Integer.parseInt(sessionId));

		return new ResponseEntity<>(DatabaseCommunication.getTransaction(id, Integer.parseInt(sessionId)), HttpStatus.OK);
	}

	// DELETE
	@SuppressWarnings("rawtypes") // Because we don't care about RensponseEntity's generic type
	@RequestMapping(method = RequestMethod.DELETE, value = "/transactions/{id}", produces = "application/json", consumes = "*")
	public ResponseEntity deleteTransaction(
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String sessionId,
            @RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader) {

        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

		if (DatabaseCommunication.getTransaction(id, Integer.parseInt(sessionId)) == null) {
			throw new ItemNotFound();
		}
		DatabaseCommunication.deleteTransaction(id, Integer.parseInt(sessionId));

		// Remove it from the sessions
		DatabaseCommunication.deleteTransactionId(Integer.parseInt(sessionId), id);

		return new ResponseEntity(HttpStatus.NO_CONTENT);
	}

	// PATCH
	@RequestMapping(method = RequestMethod.PATCH, value = "/transactions/{transactionID}/category", produces = "application/json", consumes = "*")
	public ResponseEntity<Transaction> assignCategory(
			@RequestBody String category_id,
			@PathVariable int transactionID,
			@RequestParam(value="session_id", required =false) String sessionId,
            @RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader) {

        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

		// Create a JSON object
		JSONObject category;
		int categoryID;
		try {
			category = new JSONObject(category_id);
			categoryID = category.getInt("category_id");
		} catch (JSONException e) {
			throw new ItemNotFound();
		}

		if (DatabaseCommunication.getTransaction(transactionID, Integer.parseInt(sessionId)) == null ||
				DatabaseCommunication.getCategory(categoryID, Integer.parseInt(sessionId)) == null) {
			throw new ItemNotFound();
		}

		DatabaseCommunication.assignCategory(categoryID, transactionID);

		return new ResponseEntity<>(DatabaseCommunication.getTransaction(transactionID, Integer.parseInt(sessionId)), HttpStatus.OK);
	}

	// ---------------- Categories -----------------

	// GET
	@RequestMapping(value = "/categories", method = RequestMethod.GET, produces = "application/json", consumes = "*")
	public List<Category> getCategories(
			@RequestParam(value="session_id", required =false) String sessionId,
            @RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader) {

        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

		return DatabaseCommunication.getAllCategories(Integer.parseInt(sessionId));
	}

	// POST
	@RequestMapping(method = RequestMethod.POST, value = "/categories", produces = "application/json", consumes = "*")
	public ResponseEntity<Category> addCategory(
			@RequestBody String category,
			@RequestParam(value="session_id", required = false) String sessionId,
            @RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader) {

        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

        if (category.isEmpty()) {
            throw new InvalidInputException();
        }

        int newId = DatabaseCommunication.getLastCategoryIndex() + 1;
        JSONObject jsonCategory = new JSONObject(category);

        if (!jsonCategory.has("name")) {
            throw new InvalidInputException();
        }

        Category c = new Category(newId, jsonCategory.getString("name"));

		if (!c.validCategory()) {
			throw new InvalidInputException();
		}

		//Add the category id to the session
		DatabaseCommunication.addCategoryId(Integer.parseInt(sessionId), c.getId());
		DatabaseCommunication.addCategory(c);

		return new ResponseEntity<>(c, HttpStatus.CREATED);
	}

	// GET
	@RequestMapping(value = "/categories/{id}", method = RequestMethod.GET, produces = "application/json", consumes = "*")
	public Category getCategory(
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String sessionId,
            @RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader) {

	    if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

		Category category = DatabaseCommunication.getCategory(id, Integer.parseInt(sessionId));
		if (category == null) {
			throw new ItemNotFound();
		}

		return category;
	}

	// PUT
	@RequestMapping(method = RequestMethod.PUT, value = "/categories/{id}", produces = "application/json", consumes = "*")
	public ResponseEntity<Category> putCategory(
			@RequestBody String category ,
			@RequestParam(value="session_id", required =false) String sessionId,
			@PathVariable int id,
            @RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader) {

        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

        JSONObject jsonCategory = new JSONObject(category);

        if (!jsonCategory.has("name")) {
            throw new InvalidInputException();
        }

        Category c = new Category(id, jsonCategory.getString("name"));

        if (!c.validCategory()) {
            throw new InvalidInputException();
        }

		if (DatabaseCommunication.getCategory(id, Integer.parseInt(sessionId)) == null) {
			throw new ItemNotFound();
		}

		DatabaseCommunication.updateCategory(c, id, Integer.parseInt(sessionId));

		return new ResponseEntity<>(DatabaseCommunication.getCategory(id, Integer.parseInt(sessionId)), HttpStatus.OK);
	}

	// DELETE
	@SuppressWarnings("rawtypes") // Because we don't care about RensponseEntity's generic type
	@RequestMapping(method = RequestMethod.DELETE, value = "/categories/{id}", produces = "application/json", consumes = "*")
	public ResponseEntity deleteCategory(
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String sessionId,
            @RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader) {
        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

		if (DatabaseCommunication.getCategory(id, Integer.parseInt(sessionId)) == null) {
			throw new ItemNotFound();
		}

		DatabaseCommunication.deleteCategory(id, Integer.parseInt(sessionId));
		// Remove it from the sessions
		DatabaseCommunication.deleteCategoryId(Integer.parseInt(sessionId), id);

		return new ResponseEntity(HttpStatus.NO_CONTENT);
	}

	// ---------------- Category Rules -----------------
    //GET
    @RequestMapping (value = "/categoryRules", method = RequestMethod.GET, produces = "application/json", consumes = "*")
    public List<CategoryRule> getCategoryRules(
            @RequestParam (value = "session_id", required = false) String sessionId,
			@RequestHeader (value = "X-session-ID", required = false) String sessionIDHeader) {

        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }


	    return DatabaseCommunication.getAllCategoryRules(Integer.parseInt(sessionId));
    }

    //GET
    @RequestMapping (value = "/categoryRules/{categoryRuleId}", method = RequestMethod.GET, produces = "application/json", consumes = "*")
    public CategoryRule getCategoryRule(@RequestParam(value = "session_id", required = false) String sessionId,
                                        @RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader,
                                        @PathVariable("categoryRuleId") String categoryRuleId) {
        int id = Integer.parseInt(categoryRuleId);

        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

        if (DatabaseCommunication.getCategoryRules(id, Integer.parseInt(sessionId)) == null) {
            throw new ItemNotFound();
        }

	    return DatabaseCommunication.getCategoryRules(id, Integer.parseInt(sessionId));
    }

    //POST
    @RequestMapping (value = "/categoryRules", method = RequestMethod.POST, produces = "application/json", consumes = "*")
    public ResponseEntity<CategoryRule> addCategoryRule(@RequestBody String categoryRule,
                                                        @RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader,
                                                        @RequestParam(value = "session_id", required = false) String sessionId) {

	    if (categoryRule.isEmpty()) {
	        throw new InvalidInputException();
        }

        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
	        throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

        JSONObject jsonCategoryRule = new JSONObject(categoryRule);

	    if (!correctJsonCategoryRule(jsonCategoryRule)) {
	        throw new InvalidInputException();
        }

        if (!checkValidType(jsonCategoryRule.getString("type"))) {
	        throw new InvalidInputException();
        }

        int newID = DatabaseCommunication.getLastCategoryRuleID() + 1;
	    String description = jsonCategoryRule.getString("description");
	    String IBAN = jsonCategoryRule.getString("IBAN");
	    String type = jsonCategoryRule.getString("type");
	    int categoryId = jsonCategoryRule.getInt("categoryId");
        CategoryRule cr = new CategoryRule(newID, description, IBAN, type, categoryId);

	    DatabaseCommunication.addCategoryRulesId(Integer.parseInt(sessionId), newID);
	    DatabaseCommunication.addCategoryRule(cr);

        return new ResponseEntity<>(cr, HttpStatus.CREATED);
    }

    //PUT
    @RequestMapping (value = "/categoryRules/{categoryRuleId}", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<CategoryRule> putCategoryRule(@RequestBody String categoryRule,
														@RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader,
														@RequestParam(value = "session_id", required = false) String sessionId,
														@PathVariable("categoryRuleId") String categoryRuleId) {
		int id = Integer.parseInt(categoryRuleId);
        if (categoryRule.isEmpty()) {

            throw new InvalidInputException();
        }

        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

        JSONObject jsonCategoryRule = new JSONObject(categoryRule);

        if (!correctJsonCategoryRule(jsonCategoryRule)) {
            throw new InvalidInputException();
        }

        if (!checkValidType(jsonCategoryRule.getString("type"))) {
            throw new InvalidInputException();
        }

        String description = jsonCategoryRule.getString("description");
        String IBAN = jsonCategoryRule.getString("IBAN");
        String type = jsonCategoryRule.getString("type");
        int categoryId = jsonCategoryRule.getInt("categoryId");
        CategoryRule cr = new CategoryRule(id, description, IBAN, type, categoryId);

        DatabaseCommunication.updateCategoryRule(id, cr, Integer.parseInt(sessionId));
	    return new ResponseEntity<>(DatabaseCommunication.getCategoryRules(id, Integer.parseInt(sessionIDHeader)), HttpStatus.OK);
    }

    //DELETE
    @SuppressWarnings("rawtyper")
    @RequestMapping(value = "/categoryRules/{categoryRuleId}", method = RequestMethod.DELETE, produces = "application/json", consumes = "*")
    public ResponseEntity deleteCategoyRule(@RequestParam(value = "session_id", required = false) String sessionId,
                                            @RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader,
                                            @PathVariable("categoryRuleId") String categoryRuleId) {
        int id = Integer.parseInt(categoryRuleId);
        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

        if (DatabaseCommunication.getCategoryRules(id, Integer.parseInt(sessionId)) == null) {
            throw new ItemNotFound();
        }

        DatabaseCommunication.deleteCategoryRule(id, Integer.parseInt(sessionId));
        DatabaseCommunication.deleteCategoryRulesId(id, Integer.parseInt(sessionId));
	    return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    //----------------- Balance History ----------------

    private boolean checkInterval(String interval) {
        for (Interval search : Interval.values()) {
            if (search.name().equals(interval)) {
                return true;
            }
        }
        return false;
    }

    //GET
    @RequestMapping(value = "/balance/history", method = RequestMethod.GET, produces = "application/json", consumes = "*")
    public History getBalanceHistory(@RequestParam(value = "interval", required = false, defaultValue = "month") String interval,
                                     @RequestParam(value = "intervals", required = false, defaultValue = "24") String intervals,
                                     @RequestParam(value = "session_id", required = false) String sessionId,
                                     @RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader) {

        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

        if (!checkInterval(interval)) {
            throw new InvalidInputException();
        }

        if (Integer.parseInt(intervals) <= 0 || Integer.parseInt(intervals) > 200) {
            throw new InvalidInputException();
        }

        return DatabaseCommunication.getBalanceHistory(Integer.parseInt(sessionId),
                interval, Integer.parseInt(intervals));
    }

    //----------------- Saving Goals -------------

    public boolean correctJsonSavingGoal(JSONObject jsonObject) {
        return jsonObject.has("id") && jsonObject.has("name") && jsonObject.has("goal") &&
                jsonObject.has("savePerMonth") && jsonObject.has("minBalanceRequired") && jsonObject.has("balance");
    }

    //GET
    @RequestMapping(value = "/savingGoals", method = RequestMethod.GET, produces = "application/json", consumes = "*")
    public List<SavingGoal> getSavingGoals(@RequestHeader("X-session-ID") String sessionIDHeader,
                                           @RequestParam("session_id") String sessionId) {
        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

        return DatabaseCommunication.getSavingGoals(Integer.parseInt(sessionId));
    }

    //POST
    @RequestMapping(value = "/savingGoals", method = RequestMethod.POST, produces = "application/json", consumes = "*")
    public ResponseEntity<SavingGoal> addSavingGoal(@RequestParam("session_id") String sessionId,
                                                    @RequestHeader("X-session-ID") String sessionIDHeader,
                                                    @RequestBody String saving) {
        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

        JSONObject jsonObject = new JSONObject(saving);
        if (!correctJsonSavingGoal(jsonObject)) {
            throw new InvalidInputException();
        }
        int id = DatabaseCommunication.getLastSavingsID() + 1;
        String name = jsonObject.getString("name");
        double balance = jsonObject.getDouble("balance");
        double goal = jsonObject.getDouble("goal");
        double savePerMonth = jsonObject.getDouble("savePerMonth");
        double minBalanceRequired = jsonObject.getDouble("minBalanceRequired");
        SavingGoal savingGoal = new SavingGoal(id, name, goal, savePerMonth, minBalanceRequired, balance);
        if (!savingGoal.validSavingGoal()) {
            throw new InvalidInputException();
        }
        DatabaseCommunication.addSavingGoal(savingGoal);
        return new ResponseEntity<>(savingGoal, HttpStatus.CREATED);
    }

    //DELETE
    @RequestMapping(value = "/savingGoals/{savingGoalId}", method = RequestMethod.DELETE, produces = "application/json", consumes = "*")
    public ResponseEntity deleteSavingGoal(@RequestHeader("X-session-ID") String sessionIDHeader,
                                           @RequestParam("session_id") String sessionId,
                                           @PathVariable("savingGoalId") int savingGoal) {
        if (sessionId == null && sessionIDHeader != null) {
            sessionId = sessionIDHeader;
        }

        if (sessionId != sessionIDHeader && sessionId != null && sessionIDHeader != null) {
            throw new SessionIDException();
        }

        if (sessionId == null && sessionIDHeader == null) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.validSessionId(Integer.parseInt(sessionId)) ||
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
            throw new SessionIDException();
        }

        if (!DatabaseCommunication.checkValidSavingGoal(savingGoal)) {
            throw new ItemNotFound();
        }
        DatabaseCommunication.deleteSavingGoal(savingGoal, Integer.parseInt(sessionId));
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

	// ---------------- Sessions -----------------
	// POST
	@RequestMapping(value = "/sessions", method = RequestMethod.POST, produces = "application/json", consumes = "*")
	public String getSessionId() {
		// One more than the maximum session Id present.
		int newSessionId = DatabaseCommunication.getMaxSessionId() + 1;
		DatabaseCommunication.basicSql(newSessionId);
		return "{\n" +
				"  \"id\": \"" + newSessionId + "\"\n" +
				"}";
	}
}
