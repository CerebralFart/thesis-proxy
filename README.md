# Thesis Proxy

This repository is part of the replication package of my thesis. Other parts can be found at:

- [Benchmarking Script](https://github.com/CerebralFart/thesis-benchmark)
- [Benchmarking Results](https://github.com/CerebralFart/thesis-results)

This repository contains the code for the authorization proxy described in my thesis. Below are instructions on how to build and execute it.

## Running the Proxy

The project is a standard gradle project, so it can be built using

```bash
	./gradlew build
```

This will create a stand-alone JAR-file in `build/libs/`, which can be run using Java.

```bash
	java -jar build/libs/multi-proxy-0.1-DEV.jar
```

This will attempt to start the proxy server on port 8080. If this port is already in use, it will exit with an appropriate error message.
A SPARQL query can be executed against any endpoint in this server, the user is expected to supply a user and authorization mode via URL parameters. The mode must be one of `plain`, `preselection`, or `rewriting`.
The query will be altered acordingly and forwarded to the underlying triple store, which is expected to listen at port 8000.
The timings of the process are returned in the [Server-Timing header](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Server-Timing).

