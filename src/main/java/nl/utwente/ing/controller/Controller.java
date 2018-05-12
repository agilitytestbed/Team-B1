package nl.utwente.ing.controller;

import java.util.List;

import nl.utwente.ing.transaction.CategoryRule;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import nl.utwente.ing.transaction.Category;
import nl.utwente.ing.transaction.DatabaseCommunication;
import nl.utwente.ing.transaction.Transaction;


@RestController
@RequestMapping(value = "/api/v2" , produces = "application/json", consumes = "application/json")
public class Controller {


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

	@RequestMapping(value = "/transactions")
	public List<Transaction> getAllTransactions(
			@RequestParam(value="offset", defaultValue="0") int offset,
			@RequestParam(value="limit", defaultValue="20") int limit,
			@RequestParam(value="category", defaultValue="-1") int categoryID,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		// Throw an exception if the two session variables are both null or both are not null but are different from each other
		if ((X_session_ID == null && session_id == null) ||
				(X_session_ID != null && session_id != null && !X_session_ID.equals(session_id))
				) {
			throw new SessionIDException();
		}

		// Only the case in which one of them is null remains
		// We use X_session_ID to represent the actual session id and proceed from there
		if (X_session_ID == null) {
			X_session_ID = session_id;
		}

		// If the session id is not in the session, throw an exception
		if (!DatabaseCommunication.validSessionId(Integer.parseInt(X_session_ID))) {
			throw new SessionIDException();
		}

		// Enforce the limits for offset and limit
		offset = Math.max(offset, 0);
		limit = Math.max(limit, 1);
		limit = Math.min(limit, 100);


		return DatabaseCommunication.getAllTransactions(offset, limit, categoryID, Integer.parseInt(X_session_ID));
	}

	// POST
	@SuppressWarnings("rawtypes") // Because we don't care about RensponseEntity's generic type
	@RequestMapping(method = RequestMethod.POST, value = "/transactions")
	public ResponseEntity addTransaction(
			@RequestBody Transaction t,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {

		// Throw an exception if the two session variables are both null or both are not null but are different from each other
		if ((X_session_ID == null && session_id == null) ||
				(X_session_ID != null && session_id != null && !X_session_ID.equals(session_id))
				) {
			throw new SessionIDException();
		}

		// Only the case in which one of them is null remains
		// We use X_session_ID to represent the actual session id and proceed from there
		if (X_session_ID == null) {
			X_session_ID = session_id;
		}

		// If the session id is not in the session, throw an exception
		if (!DatabaseCommunication.validSessionId(Integer.parseInt(X_session_ID))) {
			throw new SessionIDException();
		}

		// If it's not a valid transaction
		if(t == null || !t.validTransaction()) {
			throw new InvalidInputException();
		}

		// Generate new id
		int newId = DatabaseCommunication.getLastTransactionIndex() + 1;
		t.setId(newId);



		//Add the transaction id to the session
		DatabaseCommunication.addTransactionId(Integer.parseInt(X_session_ID), t.getId());

		DatabaseCommunication.addTransaction(t);


		// Create a response add the created object to it

		return new ResponseEntity<>(t, HttpStatus.CREATED);
	}

	// GET
	@RequestMapping("/transactions/{id}")
	public Transaction getTransaction(
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		// Throw an exception if the two session variables are both null or both are not null but are different from each other
		if ((X_session_ID == null && session_id == null) ||
				(X_session_ID != null && session_id != null && !X_session_ID.equals(session_id))
				) {
			throw new SessionIDException();
		}

		// Only the case in which one of them is null remains
		// We use X_session_ID to represent the actual session id and proceed from there
		if (X_session_ID == null) {
			X_session_ID = session_id;
		}

		// If the session id is not in the session, throw an exception
		if (!DatabaseCommunication.validSessionId(Integer.parseInt(X_session_ID))) {
			throw new SessionIDException();
		}

		Transaction transaction = DatabaseCommunication.getTransaction(id, Integer.parseInt(X_session_ID));
		if (transaction == null) {
			throw new ItemNotFound();
		}

		return transaction;
	}

	// PUT
	@RequestMapping(method = RequestMethod.PUT, value = "/transactions/{id}")
	public ResponseEntity<Transaction> updateTransaction(
			@RequestBody Transaction t ,
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		// Throw an exception if the two session variables are both null or both are not null but are different from each other
		if ((X_session_ID == null && session_id == null) ||
				(X_session_ID != null && session_id != null && !X_session_ID.equals(session_id))
				) {
			throw new SessionIDException();
		}

		// Only the case in which one of them is null remains
		// We use X_session_ID to represent the actual session id and proceed from there
		if (X_session_ID == null) {
			X_session_ID = session_id;
		}

		// If the session id is not in the session, throw an exception
		if (!DatabaseCommunication.validSessionId(Integer.parseInt(X_session_ID))) {
			throw new SessionIDException();
		}

		if(t == null || !t.validTransaction()) {
			throw new InvalidInputException();
		}

		if (DatabaseCommunication.getTransaction(id, Integer.parseInt(X_session_ID)) == null) {
			throw new ItemNotFound();
		}
		DatabaseCommunication.updateTransaction(t ,id, Integer.parseInt(X_session_ID));

		return new ResponseEntity<>(DatabaseCommunication.getTransaction(id, Integer.parseInt(X_session_ID)), HttpStatus.OK);
	}

	// DELETE
	@SuppressWarnings("rawtypes") // Because we don't care about RensponseEntity's generic type
	@RequestMapping(method = RequestMethod.DELETE, value = "/transactions/{id}")
	public ResponseEntity deleteTransaction(
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		// Throw an exception if the two session variables are both null or both are not null but are different from each other
		if ((X_session_ID == null && session_id == null) ||
				(X_session_ID != null && session_id != null && !X_session_ID.equals(session_id))
				) {
			throw new SessionIDException();
		}

		// Only the case in which one of them is null remains
		// We use X_session_ID to represent the actual session id and proceed from there
		if (X_session_ID == null) {
			X_session_ID = session_id;
		}

		// If the session id is not in the session, throw an exception
		if (!DatabaseCommunication.validSessionId(Integer.parseInt(X_session_ID))) {
			throw new SessionIDException();
		}

		if (DatabaseCommunication.getTransaction(id, Integer.parseInt(X_session_ID)) == null) {
			throw new ItemNotFound();
		}
		DatabaseCommunication.deleteTransaction(id, Integer.parseInt(X_session_ID));

		// Remove it from the sessions
		DatabaseCommunication.deleteTransactionId(Integer.parseInt(X_session_ID), id);

		return new ResponseEntity(HttpStatus.NO_CONTENT);
	}

	// PATCH
	@RequestMapping(method = RequestMethod.PATCH, value = "/transactions/{transactionID}/category")
	public ResponseEntity<Transaction> assignCategory(
			@RequestBody String category_id,
			@PathVariable int transactionID,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		// Throw an exception if the two session variables are both null or both are not null but are different from each other
		if ((X_session_ID == null && session_id == null) ||
				(X_session_ID != null && session_id != null && !X_session_ID.equals(session_id))
				) {
			throw new SessionIDException();
		}

		// Only the case in which one of them is null remains
		// We use X_session_ID to represent the actual session id and proceed from there
		if (X_session_ID == null) {
			X_session_ID = session_id;
		}

		// If the session id is not in the session, throw an exception
		if (!DatabaseCommunication.validSessionId(Integer.parseInt(X_session_ID))) {
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

		if (DatabaseCommunication.getTransaction(transactionID, Integer.parseInt(X_session_ID)) == null ||
				DatabaseCommunication.getCategory(categoryID, Integer.parseInt(X_session_ID)) == null) {
			throw new ItemNotFound();
		}

		DatabaseCommunication.assignCategory(categoryID, transactionID);

		return new ResponseEntity<>(DatabaseCommunication.getTransaction(transactionID, Integer.parseInt(X_session_ID)), HttpStatus.OK);
	}

	// ---------------- Categories -----------------

	// GET
	@RequestMapping("/categories")
	public List<Category> getCategories(
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		// Throw an exception if the two session variables are both null or both are not null but are different from each other
		if ((X_session_ID == null && session_id == null) ||
				(X_session_ID != null && session_id != null && !X_session_ID.equals(session_id))
				) {
			throw new SessionIDException();
		}

		// Only the case in which one of them is null remains
		// We use X_session_ID to represent the actual session id and proceed from there
		if (X_session_ID == null) {
			X_session_ID = session_id;
		}

		// If the session id is not in the session, throw an exception
		if (!DatabaseCommunication.validSessionId(Integer.parseInt(X_session_ID))) {
			throw new SessionIDException();
		}

		return DatabaseCommunication.getAllCategories(Integer.parseInt(X_session_ID));
	}

	// POST
	@RequestMapping(method = RequestMethod.POST, value = "/categories")
	public ResponseEntity<Category> addCategory(
			@RequestBody Category category,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {

		// Throw an exception if the two session variables are both null or both are not null but are different from each other
		if ((X_session_ID == null && session_id == null) ||
				(X_session_ID != null && session_id != null && !X_session_ID.equals(session_id))
				) {
			throw new SessionIDException();
		}

		// Only the case in which one of them is null remains
		// We use X_session_ID to represent the actual session id and proceed from there
		if (X_session_ID == null) {
			X_session_ID = session_id;
		}

		// If the session id is not in the session, throw an exception
		if (!DatabaseCommunication.validSessionId(Integer.parseInt(X_session_ID))) {
			throw new SessionIDException();
		}

		if (category == null || !category.validCategory()) {
			throw new InvalidInputException();
		}

		// Generate new id
		int newId = DatabaseCommunication.getLastCategoryIndex() + 1;
		category.setId(newId);



		//Add the category id to the session
		DatabaseCommunication.addCategoryId(Integer.parseInt(X_session_ID), category.getId());

		DatabaseCommunication.addCategory(category);

		return new ResponseEntity<>(category, HttpStatus.CREATED);
	}

	// GET
	@RequestMapping("/categories/{id}")
	public Category getCategory(
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		// Throw an exception if the two session variables are both null or both are not null but are different from each other
		if ((X_session_ID == null && session_id == null) ||
				(X_session_ID != null && session_id != null && !X_session_ID.equals(session_id))
				) {
			throw new SessionIDException();
		}

		// Only the case in which one of them is null remains
		// We use X_session_ID to represent the actual session id and proceed from there
		if (X_session_ID == null) {
			X_session_ID = session_id;
		}

		// If the session id is not in the session, throw an exception
		if (!DatabaseCommunication.validSessionId(Integer.parseInt(X_session_ID))) {
			throw new SessionIDException();
		}

		Category category = DatabaseCommunication.getCategory(id, Integer.parseInt(X_session_ID));
		if (category == null) {
			throw new ItemNotFound();
		}

		return category;
	}

	// PUT
	@RequestMapping(method = RequestMethod.PUT, value = "/categories/{id}")
	public ResponseEntity<Category> putCategory(
			@RequestBody Category category ,
			@RequestParam(value="session_id", required =false) String session_id,
			@PathVariable int id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		// Throw an exception if the two session variables are both null or both are not null but are different from each other
		if ((X_session_ID == null && session_id == null) ||
				(X_session_ID != null && session_id != null && !X_session_ID.equals(session_id))
				) {
			throw new SessionIDException();
		}

		// Only the case in which one of them is null remains
		// We use X_session_ID to represent the actual session id and proceed from there
		if (X_session_ID == null) {
			X_session_ID = session_id;
		}

		// If the session id is not in the session, throw an exception
		if (!DatabaseCommunication.validSessionId(Integer.parseInt(X_session_ID))) {
			throw new SessionIDException();
		}

		if (category == null || !category.validCategory()) {
			throw new InvalidInputException();
		}

		if (DatabaseCommunication.getCategory(id, Integer.parseInt(X_session_ID)) == null) {
			throw new ItemNotFound();
		}

		DatabaseCommunication.updateCategory(category, id, Integer.parseInt(X_session_ID));

		return new ResponseEntity<>(DatabaseCommunication.getCategory(id, Integer.parseInt(X_session_ID)), HttpStatus.OK);
	}

	// DELETE
	@SuppressWarnings("rawtypes") // Because we don't care about RensponseEntity's generic type
	@RequestMapping(method = RequestMethod.DELETE, value = "/categories/{id}")
	public ResponseEntity deleteCategory(
			@PathVariable int id,
			@RequestParam(value="session_id", required =false) String session_id,
			@RequestHeader(value = "X-session-ID", required=false) String X_session_ID) {
		// Throw an exception if the two session variables are both null or both are not null but are different from each other
		if ((X_session_ID == null && session_id == null) ||
				(X_session_ID != null && session_id != null && !X_session_ID.equals(session_id))
				) {
			throw new SessionIDException();
		}

		// Only the case in which one of them is null remains
		// We use X_session_ID to represent the actual session id and proceed from there
		if (X_session_ID == null) {
			X_session_ID = session_id;
		}

		// If the session id is not in the session, throw an exception
		if (!DatabaseCommunication.validSessionId(Integer.parseInt(X_session_ID))) {
			throw new SessionIDException();
		}

		if (DatabaseCommunication.getCategory(id, Integer.parseInt(X_session_ID)) == null) {
			throw new ItemNotFound();
		}

		DatabaseCommunication.deleteCategory(id, Integer.parseInt(X_session_ID));

		// Remove it from the sessions
		DatabaseCommunication.deleteCategoryId(Integer.parseInt(X_session_ID), id);

		return new ResponseEntity(HttpStatus.NO_CONTENT);
	}

	// ---------------- Category Rules -----------------
    //GET
    @RequestMapping (value = "/categoryRules", method = RequestMethod.GET, produces = "application/json", consumes = "*")
    public List<CategoryRule> getCategoryRules(
            @RequestParam (value = "session_id", required = false) String sessionId) {

	    if (sessionId == null || !DatabaseCommunication.validSessionId(Integer.parseInt(sessionId))) {
	        throw new SessionIDException();
        }
	    return DatabaseCommunication.getAllCategoryRules(Integer.parseInt(sessionId));
    }

    //GET
    @RequestMapping (value = "/categoryRules/{categoryRuleId}", method = RequestMethod.GET, produces = "application/json", consumes = "*")
    public CategoryRule getCategoryRule(@RequestParam(value = "session_id") String sessionID,
                                        @PathVariable("categoryRuleId") String categoryRuleId) {
        int id = Integer.parseInt(categoryRuleId);

	    if (sessionID == null || !DatabaseCommunication.validSessionId(Integer.parseInt(sessionID))) {
	        throw new SessionIDException();
        }

	    return DatabaseCommunication.getCategoryRules(id, Integer.parseInt(sessionID));
    }

    //POST
    @RequestMapping (value = "/categoryRules", method = RequestMethod.POST, produces = "application/json", consumes = "*")
    public ResponseEntity<CategoryRule> addCategoryRule(@RequestBody CategoryRule categoryRule,
                                @RequestParam (value = "session_id") String sessionID) {
	    if (sessionID == null || !DatabaseCommunication.validSessionId(Integer.parseInt(sessionID))) {
	        throw new SessionIDException();
        }

        if (categoryRule == null) {
	        throw new InvalidInputException();
        }

        int newID = DatabaseCommunication.getLastCategoryRuleID();
	    categoryRule.setId(newID);
	    DatabaseCommunication.addCategoryRulesId(Integer.parseInt(sessionID), categoryRule.getId());
	    DatabaseCommunication.addCategoryRule(categoryRule);

        return new ResponseEntity<>(categoryRule, HttpStatus.CREATED);
    }

    //PUT
    @RequestMapping (value = "/categoryRules/{categoryRuleId}", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<CategoryRule> putCategoryRule(@RequestBody CategoryRule categoryRule,
														@RequestParam(value = "session_id") String sessionID,
														@PathVariable("categoryRuleId") String categoryRuleId) {
		int id = Integer.parseInt(categoryRuleId);
	    if (sessionID == null || !DatabaseCommunication.validSessionId(Integer.parseInt(sessionID))) {
	        throw new SessionIDException();
        }
        if (categoryRule == null) {
	        throw new InvalidInputException();
        }
        if (DatabaseCommunication.getCategoryRules(id, Integer.parseInt(sessionID)) == null) {
            throw new ItemNotFound();
        }
        DatabaseCommunication.updateCategoryRule(id, categoryRule, Integer.parseInt(sessionID));
	    return new ResponseEntity<>(DatabaseCommunication.getCategoryRules(id, Integer.parseInt(sessionID)), HttpStatus.OK);
    }

    //DELETE
    @SuppressWarnings("rawtyper")
    @RequestMapping(value = "/categoryRules/{categoryRuleId}", method = RequestMethod.DELETE, produces = "application/json", consumes = "*")
    public ResponseEntity deleteCategoyRule(@RequestParam(value = "session_id") String sessionID,
                                            @PathVariable("categoryRuleId") String categoryRuleId) {
        int id = Integer.parseInt(categoryRuleId);
	    if (sessionID == null || !DatabaseCommunication.validSessionId(Integer.parseInt(sessionID))) {
	        throw new SessionIDException();
        }
        if (DatabaseCommunication.getCategoryRules(id, Integer.parseInt(sessionID)) == null) {
            throw new ItemNotFound();
        }
        DatabaseCommunication.deleteCategoryRulesId(id);
        DatabaseCommunication.deleteCategoryRule(id, Integer.parseInt(sessionID));
	    return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

	// ---------------- Sessions -----------------
	// POST
	@RequestMapping(value = "/sessions", method = RequestMethod.POST, produces = "application/json", consumes = "*")
	public String getSessionId() {
		// One more than the maximum session Id present.
		int newSessionId = DatabaseCommunication.getMaxSessionId() + 1;
		DatabaseCommunication.addSession(newSessionId);
		return "{\n" +
				"  \"id\": \"" + newSessionId + "\"\n" +
				"}";
	}
}
