package nl.utwente.ing.controller;

import nl.utwente.ing.database.DatabaseCommunication;
import nl.utwente.ing.model.SavingGoal;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1" , produces = "application/json", consumes = "application/json")
public class SavingGoalHttpRequest {

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

    //----------------- Saving Goals -------------

    public boolean correctJsonSavingGoal(JSONObject jsonObject) {
        return jsonObject.has("name") && jsonObject.has("goal") &&
                jsonObject.has("savePerMonth");
    }

    //GET
    @RequestMapping(value = "/savingGoals", method = RequestMethod.GET, produces = "application/json", consumes = "*")
    public List<SavingGoal> getSavingGoals(@RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader,
                                           @RequestParam(value = "session_id", required = false) String sessionId) {
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
    public ResponseEntity<SavingGoal> addSavingGoal(@RequestParam(value = "session_id", required = false) String sessionId,
                                                    @RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader,
                                                    @RequestBody(required = false) String saving) {
        if (saving == null) {
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

        JSONObject jsonObject = new JSONObject(saving);
        if (!correctJsonSavingGoal(jsonObject)) {
            throw new InvalidInputException();
        }
        if (!jsonObject.has("minBalanceRequired")) {
            jsonObject.put("minBalanceRequired", 0);
        }
        int id = DatabaseCommunication.getLastSavingsID() + 1;
        String name = jsonObject.getString("name");
        double goal = jsonObject.getDouble("goal");
        double savePerMonth = jsonObject.getDouble("savePerMonth");
        double minBalanceRequired = jsonObject.getDouble("minBalanceRequired");
        SavingGoal savingGoal = new SavingGoal(id, name, goal, savePerMonth, minBalanceRequired);
        if (!savingGoal.validSavingGoal()) {
            throw new InvalidInputException();
        }
        DatabaseCommunication.addGoalId(Integer.parseInt(sessionId), id);
        DatabaseCommunication.addSavingGoal(savingGoal);
        return new ResponseEntity<>(savingGoal, HttpStatus.CREATED);
    }

    //DELETE
    @RequestMapping(value = "/savingGoals/{savingGoalId}", method = RequestMethod.DELETE, produces = "application/json", consumes = "*")
    public ResponseEntity deleteSavingGoal(@RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader,
                                           @RequestParam(value = "session_id", required = false) String sessionId,
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
        DatabaseCommunication.deleteSavingGoalId(savingGoal, Integer.parseInt(sessionId));
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
