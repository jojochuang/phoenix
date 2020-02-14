/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.phoenix.trace;

import com.google.common.base.Preconditions;
import com.sun.istack.NotNull;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.mock.MockSpan;
import io.opentracing.util.GlobalTracer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import io.opentracing.Span;
//import org.apache.htrace.Sampler;
//import org.apache.htrace.wrappers.TraceRunnable;
import org.apache.phoenix.jdbc.PhoenixConnection;

import javax.annotation.Nullable;
import java.util.Map;

import static org.apache.phoenix.util.StringUtil.toBytes;

/**
 * Utilities for tracing
 */
public class TracingUtils {
    public static final String METRIC_SOURCE_KEY = "phoenix.";

    /** Set context to enable filtering */
    public static final String METRICS_CONTEXT = "tracing";

    /** Marker metric to ensure that we register the tracing mbeans */
    public static final String METRICS_MARKER_CONTEXT = "marker";

    public static Tracer getTracer() {
        return GlobalTracer.get();
    }

    /**
     * Start a span with the currently configured sampling frequency. Creates a new 'current' span
     * on this thread - the previous 'current' span will be replaced with this newly created span.
     * <p>
     * Hands back the direct span as you shouldn't be detaching the span - use {@link TraceRunnable}
     * instead to detach a span from this operation.
     * @param connection from which to read parameters
     * @param string description of the span to start
     * @return the underlying span.
     */
    public static Scope startNewSpan(PhoenixConnection connection, String string) {
        //Sampler<?> sampler = connection.getSampler();
        Scope scope = TracingUtils.createTrace(string/*, sampler*/);
        addCustomAnnotationsToSpan(scope.span(), connection);
        return scope;
    }

    private static void addCustomAnnotationsToSpan(@Nullable Span span, @NotNull PhoenixConnection conn) {
        Preconditions.checkNotNull(conn);

        if (span == null) {
            return;
        }
        Map<String, String> annotations = conn.getCustomTracingAnnotations();
        // copy over the annotations as bytes
        for (Map.Entry<String, String> annotation : annotations.entrySet()) {
            span.setTag(annotation.getKey(), annotation.getValue());
        }
    }

    /**
     * Wrapper method to create new child Scope with the given description
     * and parent scope's spanId
     * @param span parent span
     * @return Scope or null when not tracing
     */
    public static Scope createTrace(String description, Span span) {
        if(span == null) return createTrace(description);

        return (getTracer() == null) ? null : getTracer().buildSpan(description).
            asChildOf(span).startActive(true);
    }

    /**
     * Wrapper method to create new Scope with the given description
     * @return Scope or null when not tracing
     */
    public static Scope createTrace(String description) {
        return (getTracer() == null) ? null :
            getTracer().buildSpan(description).startActive(true);
    }

    /**
     * Wrapper method to add timeline annotiation to current span with given message
     */
    public static void addTimelineAnnotation(String msg) {
        Span span = getTracer().activeSpan();
        if (span != null) {
            span.log(msg);
        }
    }

    public static void addAnnotation(String message, int value) {
        Span span = getTracer().activeSpan();
        addAnnotation(span, message, value);
    }

    public static void addAnnotation(Span span, String message, int value) {
        span.setTag(message, value);
    }

    public static Pair<String, String> readAnnotation(byte[] key, byte[] value) {
        return new Pair<String, String>(new String(key), Bytes.toString(value));
    }

    /**
     * @see #getTraceMetricName(String)
     */
    public static final String getTraceMetricName(long traceId) {
        return getTraceMetricName(Long.toString(traceId));
    }

    /**
     * @param traceId unique id of the trace
     * @return the name of the metric record that should be generated for a given trace
     */
    public static final String getTraceMetricName(String traceId) {
        return METRIC_SOURCE_KEY + traceId;
    }

    public static long getTraceId(Span span) {
        if (span instanceof MockSpan) {
            MockSpan mockSpan = (MockSpan)span;
            return mockSpan.context().traceId();
        }
        return 0;
    }
}
