package nl.kadaster.labs.multiproxy.strategies;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public abstract class Strategy {
    protected static final String HEADER_SERVER_TIMING = "Server-Timing";
    protected static final String TEMPLATE_FILTER = """
           (NOT EXISTS{?:node <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/Product>} || EXISTS {?:node ^<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/product>/<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/vendor> <:user>})
        && (NOT EXISTS{?:node <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/Offer>} || EXISTS {?:node <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/vendor> <:user>})
        && (NOT EXISTS{?:node <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/Vendor>} || ?:node = <:user>)
        && (NOT EXISTS{?:node <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>} || EXISTS {?:node ^<http://purl.org/stuff/rev#reviewer>/<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/reviewFor>/^<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/product>/<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/vendor> <:user>})
        && (NOT EXISTS{?:node <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/stuff/rev#Review>} || EXISTS {?:node <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/reviewFor>/^<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/product>/<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/vendor> <:user>})
    """;


    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.MINUTES)
            .build();

    public abstract ResponseEntity<String> execute(String path, String query, String user) throws IOException;

    public String execute(String path, String query) throws IOException {
        var body = new FormBody.Builder()
                .add("query", query)
                .build();
        var request = new Request.Builder()
                .url("http://localhost:8000" + path)
                .header("Accept", "application/sparql-results+json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(body)
                .build();
        try (var response = CLIENT.newCall(request).execute()) {
            var text = response.body();
            return text == null ? null : text.string();
        }
    }
}
