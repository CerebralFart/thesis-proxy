package nl.kadaster.labs.multiproxy.strategies;

import java.io.IOException;

public class Plain extends Strategy {
    @Override
    public String execute(String path, String query, String user) throws IOException {
        return this.execute(path, query);
    }
}
