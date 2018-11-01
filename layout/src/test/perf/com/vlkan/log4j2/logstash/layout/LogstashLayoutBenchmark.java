package com.vlkan.log4j2.logstash.layout;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class LogstashLayoutBenchmark {

    public static void main(String[] args) throws Exception {
        fixJavaClassPath();
        ChainedOptionsBuilder optionsBuilder = new OptionsBuilder()
                .include(LogstashLayoutBenchmark.class.getSimpleName())
                .forks(2)
                .warmupIterations(3)
                .warmupTime(TimeValue.seconds(20))
                .measurementIterations(3)
                .measurementTime(TimeValue.seconds(30))
                .addProfiler(StackProfiler.class)
                .addProfiler(GCProfiler.class);
        enableJsonOutput(optionsBuilder);
        Options options = optionsBuilder
                .build();
        new Runner(options).run();
    }

    /**
     * Add project dependencies to <code>java.class.path</code> property used by JMH.
     * @see <a href="https://stackoverflow.com/q/35574688/1278899">How to Run a JMH Benchmark in Maven Using exec:java Instead of exec:exec</a>
     */
    private static void fixJavaClassPath() {
        URLClassLoader classLoader = (URLClassLoader) LogstashLayoutBenchmark.class.getClassLoader();
        StringBuilder classpathBuilder = new StringBuilder();
        for (URL url : classLoader.getURLs()) {
            String urlPath = url.getPath();
            classpathBuilder.append(urlPath).append(File.pathSeparator);
        }
        String classpath = classpathBuilder.toString();
        System.setProperty("java.class.path", classpath);
    }

    private static void enableJsonOutput(ChainedOptionsBuilder optionsBuilder) {
        String jsonOutputFile = System.getProperty("log4j2.logstashLayoutBenchmark.jsonOutputFile");
        if (jsonOutputFile != null) {
            optionsBuilder
                    .resultFormat(ResultFormatType.JSON)
                    .result(jsonOutputFile);
        }
    }

    @Benchmark
    public static void fullLogstashLayout(LogstashLayoutBenchmarkState state) {
        benchmark(state.getLogstashLayout(), state.getFullLogEvents(), state.getByteBufferDestination());
    }

    @Benchmark
    public static void liteLogstashLayout(LogstashLayoutBenchmarkState state) {
        benchmark(state.getLogstashLayout(), state.getLiteLogEvents(), state.getByteBufferDestination());
    }

    @Benchmark
    public static void fullDefaultJsonLayout(LogstashLayoutBenchmarkState state) {
        benchmark(state.getDefaultJsonLayout(), state.getFullLogEvents(), state.getByteBufferDestination());
    }

    @Benchmark
    public static void liteDefaultJsonLayout(LogstashLayoutBenchmarkState state) {
        benchmark(state.getDefaultJsonLayout(), state.getLiteLogEvents(), state.getByteBufferDestination());
    }

    @Benchmark
    public static void fullCustomJsonLayout(LogstashLayoutBenchmarkState state) {
        benchmark(state.getCustomJsonLayout(), state.getFullLogEvents(), state.getByteBufferDestination());
    }

    @Benchmark
    public static void liteCustomJsonLayout(LogstashLayoutBenchmarkState state) {
        benchmark(state.getCustomJsonLayout(), state.getLiteLogEvents(), state.getByteBufferDestination());
    }

    private static void benchmark(Layout<String> layout, List<LogEvent> logEvents, ByteBufferDestination destination) {
        // noinspection ForLoopReplaceableByForEach (for loop avoids iterator allocations)
        for (int logEventIndex = 0; logEventIndex < logEvents.size(); logEventIndex++) {
            LogEvent logEvent = logEvents.get(logEventIndex);
            layout.encode(logEvent, destination);
        }
        destination.getByteBuffer().clear();
    }

}
