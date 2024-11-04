package nl.kadaster.labs.multiproxy;

import jakarta.servlet.http.HttpServletRequest;
import nl.kadaster.labs.multiproxy.strategies.Strategy;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class Endpoint {
    private final Map<String, Strategy> strategies = new HashMap<>();

    @GetMapping("*")
    public String queryGet(HttpServletRequest request, @RequestParam("mode") String mode, @RequestParam("user") String user, @RequestParam("query") String query) {
    @GetMapping("**")
    public String queryGet(HttpServletRequest request, @RequestParam("mode") String mode, @RequestParam("user") String user, @RequestParam("query") String query) throws IOException {
        return this.query(request, mode, user, query);
    }

    @PostMapping(value = "**", consumes = "application/x-www-form-urlencoded")
    public String queryInBody(HttpServletRequest request, @RequestParam("mode") String mode, @RequestParam("user") String user, @RequestParam("query") String query) throws IOException {
        return this.query(request, mode, user, query);
    }

    @PostMapping(value = "**", consumes = "application/sparql-query")
    public String queryPlain(HttpServletRequest request, @RequestParam("mode") String mode, @RequestParam("user") String user, @RequestBody String query) throws IOException {
        return this.query(request, mode, user, query);
    }


    public String query(HttpServletRequest request, String mode, String user, String query) throws IOException {
        if (!this.strategies.containsKey(mode)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mode not found");
        }

        return null;
    }
}
