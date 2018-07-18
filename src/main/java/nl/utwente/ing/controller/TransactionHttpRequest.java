package nl.utwente.ing.controller;

import nl.utwente.ing.database.DatabaseCommunication;
import nl.utwente.ing.model.Transaction;
import nl.utwente.ing.model.TransactionType;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1" , produces = "application/json", consumes = "application/json")
public class TransactionHttpRequest {

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

        String date = jsonTransaction.getString("date").replace("Z", "");

        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
            timeFormatter.parse(date);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException();
        }

        Transaction transaction = new Transaction(newId, jsonTransaction.getString("date"), jsonTransaction.getDouble("amount"),
                jsonTransaction.getString("externalIBAN"), jsonTransaction.getString("type"), jsonTransaction.getString("description"));

        if (!transaction.validTransaction()) {
            throw new InvalidInputException();
        }

        //Add the model id to the session
        DatabaseCommunication.addTransactionId(Integer.parseInt(sessionId), transaction.getId());
        DatabaseCommunication.addTransaction(transaction, Integer.parseInt(sessionId));
        DatabaseCommunication.updateBalance(Integer.parseInt(sessionId));
        DatabaseCommunication.updateSavingGoals(Integer.parseInt(sessionId));
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
        if (jsonTransaction.has("balance")) {
            transaction.setBalance(jsonTransaction.getDouble("balance"));
        }
        if (!transaction.validTransaction()) {
            throw new InvalidInputException();
        }

        if (DatabaseCommunication.getTransaction(id, Integer.parseInt(sessionId)) == null) {
            throw new ItemNotFound();
        }

        DatabaseCommunication.updateTransaction(transaction ,id, Integer.parseInt(sessionId));
        DatabaseCommunication.updateBalance(Integer.parseInt(sessionId));

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
}
