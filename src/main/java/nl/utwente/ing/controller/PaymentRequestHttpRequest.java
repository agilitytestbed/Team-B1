package nl.utwente.ing.controller;

import nl.utwente.ing.database.DatabaseCommunication;
import nl.utwente.ing.model.PaymentRequest;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1" , produces = "application/json", consumes = "application/json")
public class PaymentRequestHttpRequest {

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

    //Payment Requests
    //GET
    @RequestMapping(value = "/paymentRequests", method = RequestMethod.GET, produces = "application/json", consumes = "*")
    public List<PaymentRequest> getPaymentRequests(@RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader,
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

        return DatabaseCommunication.getPaymentRequests(Integer.parseInt(sessionId));
    }

    private boolean validJsonPaymentRequest(JSONObject jsonObject) {
        return jsonObject.has("description") && jsonObject.has("due_date") &&
                jsonObject.has("amount") && jsonObject.has("number_of_requests");
    }

    //POST
    @RequestMapping(value = "/paymentRequests", method = RequestMethod.POST, produces = "application/json", consumes = "*")
    public ResponseEntity<PaymentRequest> addPaymentRequest(@RequestHeader(value = "X-session-ID", required = false) String sessionIDHeader,
                                      @RequestParam(value = "session_id", required = false) String sessionId,
                                      @RequestBody String paymentRequestString) {
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
                !DatabaseCommunication.validSessionId(Integer.parseInt(sessionIDHeader))) {
            throw new SessionIDException();
        }

        if (paymentRequestString.isEmpty()) {
            throw new InvalidInputException();
        }

        JSONObject paymentRequestJson = new JSONObject(paymentRequestString);

        if (!validJsonPaymentRequest(paymentRequestJson)) {
            throw new InvalidInputException();
        }

        int id = DatabaseCommunication.getLastPaymentRequestsId() + 1;
        String description = paymentRequestJson.getString("description");
        String dueDate = paymentRequestJson.getString("due_date");
        double amount = paymentRequestJson.getDouble("amount");
        int nbOfRequests = paymentRequestJson.getInt("number_of_requests");
        PaymentRequest paymentRequest = new PaymentRequest(id, description, dueDate, amount, nbOfRequests);
        if (!paymentRequest.checkValidPaymentRequest()) {
            throw new InvalidInputException();
        }
        DatabaseCommunication.addPaymentRequest(paymentRequest);
        DatabaseCommunication.addPaymentId(Integer.parseInt(sessionId), id);
        return new ResponseEntity(paymentRequest, HttpStatus.CREATED);
    }
}
