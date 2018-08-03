package nl.utwente.ing.controller;


import nl.utwente.ing.database.DatabaseCommunication;
import nl.utwente.ing.model.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1" , produces = "application/json", consumes = "application/json")
public class MessageHttpRequest {

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
    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason="Resource not found")
    private class ItemNotFound extends RuntimeException {}

    @RequestMapping(value = "/messages", method = RequestMethod.GET, produces = "application/json", consumes = "*")
    public List<Message> getMessages(@RequestParam(value = "session_id", required = false) String sessionId,
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

        return DatabaseCommunication.getMessages(Integer.parseInt(sessionId));
    }

    @RequestMapping(value = "/messages/{messageId}", method = RequestMethod.PUT)
    public ResponseEntity<Message> updateMessage(@RequestParam(value = "session_id", required = false) String sessionId,
                                        @RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader,
                                        @PathVariable("messageId") String messageId) {
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

        if (DatabaseCommunication.getMessage(Integer.parseInt(messageId), Integer.parseInt(sessionId)) == null) {
            throw new ItemNotFound();
        }

        DatabaseCommunication.updateMessage(Integer.parseInt(messageId), Integer.parseInt(sessionId));
        return new ResponseEntity(DatabaseCommunication.getMessage(Integer.parseInt(messageId), Integer.parseInt(sessionId)), HttpStatus.OK);
    }
}
