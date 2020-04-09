# tracing-tests

Testing https://github.com/open-telemetry/opentelemetry-collector-contrib

Run with `mvn test`.

## MultipleReferencesTest

Tests the ability of backends to deal with OpenTracing spans with multiple references. Traces are sent to opentelemetry-collector over Jaeger thrift-http:

```
receivers:
  jaeger:
    protocols:
      thrift_http:
```
