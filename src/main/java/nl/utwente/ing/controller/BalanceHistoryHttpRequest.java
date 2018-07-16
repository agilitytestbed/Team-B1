package nl.utwente.ing.controller;

import nl.utwente.ing.database.DatabaseCommunication;
import nl.utwente.ing.model.History;
import nl.utwente.ing.model.Interval;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1" , produces = "application/json", consumes = "application/json")
public class BalanceHistoryHttpRequest {

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
}
