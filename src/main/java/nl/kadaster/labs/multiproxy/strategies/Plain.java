package nl.kadaster.labs.multiproxy.strategies;

import org.springframework.http.ResponseEntity;

import java.io.IOException;

public class Plain extends Strategy {
    @Override
    public ResponseEntity<String> execute(String path, String query, String user) throws IOException {
        var start = System.currentTimeMillis();
        var response = this.execute(path, query);
        var completion = System.currentTimeMillis();
        
        return ResponseEntity.ok()
                .header(HEADER_SERVER_TIMING, "execution;dur=%d".formatted(
                        completion - start
                ))
                .body(response);
    }
}
