package nl.utwente.ing.controller;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import nl.utwente.ing.database.DatabaseCommunication;


@RestController
@RequestMapping(value = "/api/v1" , produces = "application/json", consumes = "application/json")
public class SessionHttpRequest {

	// ---------------- Sessions -----------------
	// POST
	@RequestMapping(value = "/sessions", method = RequestMethod.POST, produces = "application/json", consumes = "*")
	public ResponseEntity getSessionId() {
		// One more than the maximum session Id present.
		int newSessionId = DatabaseCommunication.getMaxSessionId() + 1;
		JSONObject jsonObject = new JSONObject().put("id", newSessionId);
		DatabaseCommunication.basicSql(newSessionId);
		return new ResponseEntity<>(jsonObject.toString(), HttpStatus.CREATED);
	}
}
