/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.example.mytest;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.util.concurrent.TimeUnit;

/**
 * All SDK management takes place here, away from the instrumentation code,
 * which should only access
 * the OpenTelemetry APIs.
 */
class ExampleConfiguration {

  /**
   * Initialize an OpenTelemetry SDK with a Jaeger exporter and a
   * SimpleSpanProcessor.
   *
   * @param jaegerEndpoint The endpoint of your Jaeger instance.
   * @return A ready-to-use {@link OpenTelemetry} instance.
   */
  static OpenTelemetry initOpenTelemetry() {
    // Export traces to Jaeger
    String jaegerEndpoint = "http://allinone:14250";
    JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder()
        .setEndpoint(jaegerEndpoint)
        .setTimeout(30, TimeUnit.SECONDS)
        .build();

    Resource serviceNameResource = Resource
        .create(Attributes.of(ResourceAttributes.SERVICE_NAME, "otel-jaeger-example"));

    // Set to process the spans by the Jaeger Exporter
    SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
        .addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter))
        .setResource(Resource.getDefault().merge(serviceNameResource))
        .build();
    OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider)
        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
        .build();

    // it's always a good idea to shut down the SDK cleanly at JVM exit.
    Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));

    return openTelemetry;
  }
}
