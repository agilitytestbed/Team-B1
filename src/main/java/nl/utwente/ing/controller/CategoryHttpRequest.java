package nl.utwente.ing.controller;

import nl.utwente.ing.database.DatabaseCommunication;
import nl.utwente.ing.model.Category;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1" , produces = "application/json", consumes = "application/json")
public class CategoryHttpRequest {

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

        JSONObject jsonCategory;

        try {
            jsonCategory = new JSONObject(category);
        } catch (JSONException e) {
            throw new InvalidInputException();
        }

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
}
