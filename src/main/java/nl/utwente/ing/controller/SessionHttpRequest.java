package nl.utwente.ing.controller;

import org.springframework.web.bind.annotation.*;

import nl.utwente.ing.database.DatabaseCommunication;


@RestController
@RequestMapping(value = "/api/v1" , produces = "application/json", consumes = "application/json")
public class SessionHttpRequest {

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
