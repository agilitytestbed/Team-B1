package nl.utwente.ing.controller;

import nl.utwente.ing.database.DatabaseCommunication;
import nl.utwente.ing.model.CategoryRule;
import nl.utwente.ing.model.TransactionType;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1" , produces = "application/json", consumes = "application/json")
public class CategoryRuleHttpRequest {

    private boolean correctJsonCategoryRule(JSONObject jsonCategoryRule) {
        return jsonCategoryRule.has("description") && jsonCategoryRule.has("IBAN") &&
                jsonCategoryRule.has("type") && jsonCategoryRule.has("categoryId");
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

    // ---------------- Category Rules -----------------
    //GET
    @RequestMapping(value = "/categoryRules", method = RequestMethod.GET, produces = "application/json", consumes = "*")
    public List<CategoryRule> getCategoryRules(
            @RequestParam(value = "session_id", required = false) String sessionId,
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
        DatabaseCommunication.addCategoryRule(cr, Integer.parseInt(sessionId));

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
}
