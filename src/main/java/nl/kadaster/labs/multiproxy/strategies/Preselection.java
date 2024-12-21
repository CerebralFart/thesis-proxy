package nl.kadaster.labs.multiproxy.strategies;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFWriter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

public class Preselection extends Strategy {
    private static String capture(Consumer<OutputStream> function) {
        var stream = new ByteArrayOutputStream();
        function.accept(stream);
        return stream.toString();
    }

    @Override
    public ResponseEntity<String> execute(String path, String query, String user) {
        var preselectionStart = System.currentTimeMillis();
        var queryObj = QueryFactory.create(query);
        if (queryObj.isUnknownType()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown query type");

        var constructString = """
                CONSTRUCT {
                    ?s ?p ?o
                } WHERE {
                    SERVICE <http://localhost:8000:path> {
                        ?s ?p ?o
                        FILTER(:sFilter)
                        FILTER(:pFilter)
                        FILTER(:oFilter)
                    }
                }
                """
                .replace(":path", path)
                .replace(":sFilter", TEMPLATE_FILTER.replace(":node", "s"))
                .replace(":pFilter", TEMPLATE_FILTER.replace(":node", "p"))
                .replace(":oFilter", TEMPLATE_FILTER.replace(":node", "o"))
                .replace(":user", user);
        try (QueryExecution constructQuery = QueryExecutionFactory.create(constructString, ModelFactory.createDefaultModel())) {
            var subgraph = constructQuery.execConstruct();
            var executionStart = System.currentTimeMillis();
            try (QueryExecution execution = QueryExecutionFactory.create(query, subgraph)) {
                var response = this.buildResponse(execution);
                var completion = System.currentTimeMillis();
                return ResponseEntity.ok()
                        .header(HEADER_SERVER_TIMING, "preselection;dur=%d, execution;dur=%d".formatted(
                                executionStart - preselectionStart,
                                completion - executionStart
                        ))
                        .body(response);
            }
        }
    }


    private String buildResponse(QueryExecution execution) {
        var query = execution.getQuery();
        if (query.isAskType()) {
            return """
                    <?xml version="1.0"?>
                    <sparql xmlns="http://www.w3.org/2005/sparql-results#">
                    <head></head>
                    <boolean>:result</boolean>
                    </sparql>
                    """.replace(":result", execution.execAsk() ? "true" : "false");
        } else if (query.isSelectType()) {
            return capture(output -> ResultSetFormatter.outputAsJSON(output, execution.execSelect()));
        }

        var writer = RDFWriter.create();
        writer.lang(Lang.TRIG); // TODO negotiate language

        if (query.isConstructType()) {
            writer.source(execution.execConstruct());
        } else if (query.isDescribeType()) {
            writer.source(execution.execDescribe());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Query method isn't implemented yet");
        }

        return capture(writer::output);
    }
}
