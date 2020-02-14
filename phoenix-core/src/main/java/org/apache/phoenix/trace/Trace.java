package org.apache.phoenix.trace;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

public class Trace {
  public static Scope startSpan(String description) {
    Tracer tracer = GlobalTracer.get();
    return (tracer == null) ? null :
        tracer.buildSpan(description).startActive(true);
  }
}
