/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.contrib;

import io.jaegertracing.Configuration.SenderConfiguration;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.reporters.RemoteReporter;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.spi.Reporter;
import io.jaegertracing.spi.Sender;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultipleReferencesTest {

    private static Tracer tracer;

    @BeforeClass
    public static void setupTracer() {
        Sender sender = new SenderConfiguration()
                .withEndpoint("http://localhost:14268/api/traces")
                .getSender();
        Reporter reporter = new RemoteReporter.Builder()
                .withSender(sender)
                .build();
        tracer = new JaegerTracer.Builder("MultipleReferencesTest")
                .withSampler(new ConstSampler(true))
                .withReporter(reporter)
                .build();
    }

    @AfterClass
    public static void closeTracer() {
        tracer.close();
    }

    @Test
    public void testSingleParent() {
        Span root = tracer.buildSpan("test-single-parent-root").start();
        root.finish();
        Span span1 = tracer.buildSpan("test-single-parent-span1").asChildOf(root).start();
        span1.finish();
        Span span2 = tracer.buildSpan("test-single-parent-span2").asChildOf(root).start();
        span2.finish();
        Span span3 = tracer.buildSpan("test-single-parent-span3").asChildOf(span1).start();
        span3.finish();
    }

    @Test
    public void testMultipleParentSameTrace() {
        Span root = tracer.buildSpan("test-multi-parent-root").start();
        root.finish();
        Span span1 = tracer.buildSpan("test-multi-parent-span1").asChildOf(root).start();
        span1.finish();
        Span span2 = tracer.buildSpan("test-multi-parent-span2").asChildOf(root).start();
        span2.finish();
        Span span3 = tracer.buildSpan("test-multi-parent-span3").asChildOf(span1).asChildOf(span2).start();
        span3.finish();
    }

    @Test
    public void testMultipleParentMultipleTraces() {
        Span root1 = tracer.buildSpan("test-multi-trace-root1").start();
        root1.finish();
        Span root2 = tracer.buildSpan("test-multi-trace-root2").start();
        root2.finish();
        Span root3 = tracer.buildSpan("test-multi-trace-root3").start();
        root3.finish();
        tracer.buildSpan("test-multi-trace-join")
            .asChildOf(root3)
            .asChildOf(root1)
            .asChildOf(root2)
            .start().finish();
    }
}
