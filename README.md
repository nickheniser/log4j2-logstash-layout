[![Actions Status](https://github.com/vy/log4j2-logstash-layout/workflows/CI/badge.svg)](https://github.com/vy/log4j2-logstash-layout/actions)
[![Maven Central](https://img.shields.io/maven-central/v/com.vlkan.log4j2/log4j2-logstash-layout-parent.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.vlkan.log4j2%22)
[![License](https://img.shields.io/github/license/vy/log4j2-logstash-layout.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

`LogstashLayout` is **the fastest** [Log4j 2](https://logging.apache.org/log4j/2.x/)
JSON layout allowing schema customization and [Logstash](https://www.elastic.co/products/logstash)-friendly
output.

By default, `LogstashLayout` ships the official `JSONEventLayoutV1` stated by
[log4j-jsonevent-layout](https://github.com/logstash/log4j-jsonevent-layout)
Log4j 1.x plugin. Compared to
[JSONLayout](https://logging.apache.org/log4j/2.x/manual/layouts.html#JSONLayout)
included in Log4j 2 and `log4j-jsonevent-layout`, `LogstashLayout` provides
the following additional features:

- Superior [performance](#performance)
- Customizable JSON schema (see `eventTemplate[Uri]` and `stackTraceElementTemplate[Uri]` parameters)
- Customizable timestamp formatting (see `dateTimeFormatPattern` and `timeZoneId` parameters)

# Table of Contents

- [Usage](#usage)
- [Predefined Templates](#templates)
- [Features](#features)
- [Appender Support](#appender-support)
- [Performance](#performance)
- [F.A.Q.](#faq)
- [Contributors](#contributors)
- [License](#license)

<a name="usage"></a>

# Usage

Add the `log4j2-logstash-layout` dependency to your POM file

```xml
<dependency>
    <groupId>com.vlkan.log4j2</groupId>
    <artifactId>log4j2-logstash-layout</artifactId>
    <version>${log4j2-logstash-layout.version}</version>
</dependency>
```

in combination with a valid `log4j-core` dependency:

```xml
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>${log4j2.version}</version>
</dependency>
```

(Note that the Java 9 module name is `com.vlkan.log4j2.logstash.layout`.)

Below you can find a sample `log4j2.xml` snippet employing `LogstashLayout`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="warn">
    <Appenders>
        <Console name="CONSOLE" target="SYSTEM_OUT">
            <LogstashLayout dateTimeFormatPattern="yyyy-MM-dd'T'HH:mm:ss.SSSZZZ"
                            eventTemplateUri="classpath:LogstashJsonEventLayoutV1.json"
                            prettyPrintEnabled="true"
                            stackTraceEnabled="true"/>
        </Console>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="CONSOLE"/>
        </Root>
    </Loggers>
</Configuration>
```

Or using the `log4j2.properties` file instead:

```ini
status = warn

appender.console.name = CONSOLE
appender.console.type = CONSOLE
appender.console.target = SYSTEM_OUT

appender.console.logstash.type = LogstashLayout
appender.console.logstash.dateTimeFormatPattern = yyyy-MM-dd'T'HH:mm:ss.SSSZZZ
appender.console.logstash.eventTemplateUri = classpath:LogstashJsonEventLayoutV1.json
appender.console.logstash.prettyPrintEnabled = true
appender.console.logstash.stackTraceEnabled = true

rootLogger.level = info
rootLogger.appenderRef.stdout.ref = CONSOLE
```

This generates an output as follows:

```json
{
  "exception": {
    "exception_class": "java.lang.RuntimeException",
    "exception_message": "test",
    "stacktrace": "java.lang.RuntimeException: test\n\tat com.vlkan.log4j2.logstash.layout.demo.LogstashLayoutDemo.main(LogstashLayoutDemo.java:11)\n"
  },
  "line_number": 12,
  "class": "com.vlkan.log4j2.logstash.layout.demo.LogstashLayoutDemo",
  "@version": 1,
  "source_host": "varlik",
  "message": "Hello, error!",
  "thread_name": "main",
  "@timestamp": "2017-05-25T19:56:23.370+02:00",
  "level": "ERROR",
  "file": "LogstashLayoutDemo.java",
  "method": "main",
  "logger_name": "com.vlkan.log4j2.logstash.layout.demo.LogstashLayoutDemo"
}
```

`LogstashLayout` is configured with the following parameters:

| Parameter Name | Type | Description |
|----------------|------|-------------|
| `prettyPrintEnabled` | boolean | enables pretty-printer (defaults to `false`) |
| `locationInfoEnabled` | boolean | includes the filename and line number in the output (defaults to `false`) |
| `stackTraceEnabled` | boolean | includes stack traces (defaults to `false`) |
| `emptyPropertyExclusionEnabled` | boolean | exclude empty and null properties (defaults to `false`) |
| `dateTimeFormatPattern` | String | timestamp formatter pattern (defaults to `yyyy-MM-dd'T'HH:mm:ss.SSSZZZ`) |
| `timeZoneId` | String | time zone id (defaults to `TimeZone.getDefault().getID()`) |
| `locale` | String | locale in one of the following forms: `<language>`, `<language>_<country>`, or `<language>_<country>_<variant>` (defaults to `Locale.getDefault()`) |
| `mdcKeyPattern` | String | regex to filter MDC keys (does not apply to direct `mdc:key` access) |
| `ndcPattern` | String | regex to filter NDC items |
| `eventTemplate` | String | inline JSON template for rendering `LogEvent`s (has priority over `eventTemplateUri`) |
| `eventTemplateUri` | String | JSON template for rendering `LogEvent`s (defaults to [`classpath:LogstashJsonEventLayoutV1.json`](layout/src/main/resources/LogstashJsonEventLayoutV1.json)) |
| `eventTemplateAdditionalFields`<sup>1</sup> | KeyValuePair | additional key-value pairs appended to the root of the event template |
| `stackTraceElementTemplate` | String | inline JSON template for rendering `StackTraceElement`s (has priority over `stackTraceElementTemplateUri`) |
| `stackTraceElementTemplateUri` | String | JSON template for rendering `StackTraceElement`s (defaults to [`classpath:Log4j2StackTraceElementLayout.json`](layout/src/main/resources/Log4j2StackTraceElementLayout.json)) |
| `lineSeparator` | String | used to separate log outputs (defaults to `System.lineSeparator()`) |
| `maxByteCount` | int | used to cap the internal `byte[]` buffer used for serialization (defaults to 16 KiB) |
| `maxStringLength`<sup>2</sup> | int | truncate string values longer than the specified limit (defaults to 0) |
| `objectMapperFactoryMethod` | String | custom object mapper factory method (defaults to `com.fasterxml.jackson.databind.ObjectMapper.new`) |
| `mapMessageFormatterIgnored` | boolean | as a temporary work around for [LOG4J2-2703](https://issues.apache.org/jira/browse/LOG4J2-2703), serialize `MapMessage`s using Jackson rather than `MapMessage#getFormattedMessage()` (defaults to `true`) |

<sup>1</sup> One can configure additional event template fields as follows:

```xml
<LogstashLayout ...>
    <EventTemplateAdditionalFields>
        <KeyValuePair key="serviceName" value="auth-service"/>
        <KeyValuePair key="containerId" value="6ede3f0ca7d9"/>
    </EventTemplateAdditionalFields>
</LogstashLayout>
```

<sup>2</sup> Note that string value truncation via `maxStringLength` can take
place both in object keys and values, and this operation does not leave any
trace behind. `maxStringLength` is intended as a soft protection against bogus
input and one should always rely on `maxByteCount` for a hard limit.

`eventTemplateUri` denotes the URI pointing to the JSON template that will be used
while formatting the `LogEvent`s. By default, `LogstashLayout` ships
[`LogstashJsonEventLayoutV1.json`](layout/src/main/resources/LogstashJsonEventLayoutV1.json)
providing [the official Logstash `JSONEventLayoutV1`](https://github.com/logstash/log4j-jsonevent-layout).

```json
{
  "mdc": "${json:mdc}",
  "ndc": "${json:ndc}",
  "exception": {
    "exception_class": "${json:exception:className}",
    "exception_message": "${json:exception:message}",
    "stacktrace": "${json:exception:stackTrace:text}"
  },
  "line_number": "${json:source:lineNumber}",
  "class": "${json:source:className}",
  "@version": 1,
  "source_host": "${hostName}",
  "message": "${json:message}",
  "thread_name": "${json:thread:name}",
  "@timestamp": "${json:timestamp}",
  "level": "${json:level}",
  "file": "${json:source:fileName}",
  "method": "${json:source:methodName}",
  "logger_name": "${json:logger:name}"
}
```

Similarly, `stackTraceElementUri` denotes the URI pointing to the JSON template
that will be used while formatting the `StackTraceElement`s. By default,
`LogstashLayout` ships [`classpath:Log4j2StackTraceElementLayout.json`](layout/src/main/resources/Log4j2StackTraceElementLayout.json)
providing an identical stack trace structure produced by Log4j 2 `JSONLayout`.

```json
{
  "class": "${json:stackTraceElement:className}",
  "method": "${json:stackTraceElement:methodName}",
  "file": "${json:stackTraceElement:fileName}",
  "line": "${json:stackTraceElement:lineNumber}"
}
```

In case of need, you can create your own templates with a structure tailored
to your needs. That is, you can add new fields, remove or rename existing
ones, change the structure, etc. Please note that `eventTemplateUri` parameter
only supports `file` and `classpath` URI schemes. 

Below is the list of allowed `LogEvent` template variables that will be replaced
while rendering the JSON output.

| Variable Name | Description |
|---------------|-------------|
| `endOfBatch` | `logEvent.isEndOfBatch()` |
| `exception:className` | `logEvent.getThrown().getClass().getCanonicalName()` |
| `exception:message` | `logEvent.getThrown().getMessage()` |
| `exception:stackTrace` | `logEvent.getThrown().getStackTrace()` (inactive when `stackTraceEnabled=false`) |
| `exception:stackTrace:text` | `logEvent.getThrown().printStackTrace()` (inactive when `stackTraceEnabled=false`) |
| `exceptionRootCause:className` | the innermost `exception:className` in causal chain |
| `exceptionRootCause:message` | the innermost `exception:message` in causal chain |
| `exceptionRootCause:stackTrace[:text]` | the innermost `exception:stackTrace[:text]` in causal chain |
| `level` | `logEvent.getLevel()` |
| `level:severity` | [Syslog severity](https://en.wikipedia.org/wiki/Syslog#Severity_levels) keyword of `logEvent.getLevel()` |
| `level:severity:code` | [Syslog severity](https://en.wikipedia.org/wiki/Syslog#Severity_levels) code of `logEvent.getLevel()` |
| `logger:fqcn` | `logEvent.getLoggerFqcn()` |
| `logger:name` | `logEvent.getLoggerName()` |
| `main:<key>` | performs [Main Argument Lookup](https://logging.apache.org/log4j/2.0/manual/lookups.html#AppMainArgsLookup) for the given `key` |
| `map:<key>` | performs [Map Lookup](https://logging.apache.org/log4j/2.0/manual/lookups.html#MapLookup) for the given `key` |
| `marker:name` | `logEvent.getMarker.getName()` |
| `mdc` | Mapped Diagnostic Context `Map<String, String>` returned by `logEvent.getContextData()` |
| `mdc:<key>` | Mapped Diagnostic Context `String` associated with `key` (`mdcKeyPattern` is discarded) |
| `message` | `logEvent.getFormattedMessage()` |
| `message:json` | if `logEvent.getMessage()` is of type `MultiformatMessage` and supports JSON, its read value, otherwise, `{"message": <formattedMessage>}` object |
| `ndc` | Nested Diagnostic Context `String[]` returned by `logEvent.getContextStack()` |
| `source:className` | `logEvent.getSource().getClassName()` |
| `source:fileName` | `logEvent.getSource().getFileName()` (inactive when `locationInfoEnabled=false`) |
| `source:lineNumber` | `logEvent.getSource().getLineNumber()` (inactive when `locationInfoEnabled=false`) |
| `source:methodName` | `logEvent.getSource().getMethodName()` |
| `thread:id` | `logEvent.getThreadId()` |
| `thread:name` | `logEvent.getThreadName()` |
| `thread:priority` | `logEvent.getThreadPriority()` |
| `timestamp` | `logEvent.getTimeMillis()` formatted using `dateTimeFormatPattern` and `timeZoneId` |
| `timestamp:epoch` | epoch nanoseconds derived from `logEvent.getInstant()` |
| `timestamp:epoch:divisor=<divisor>` | epoch nanoseconds derived from `logEvent.getInstant()` divided by provided `divisor` (of type `double`) |
| `timestamp:epoch:divisor=<divisor>,integral` | epoch nanoseconds derived from `logEvent.getInstant()` divided by provided `divisor` (of type `double`) and casted to `long` |

JSON field lookups are performed using the `${json:<variable-name>}` scheme
where `<variable-name>` is defined as `<resolver-name>[:<resolver-key>]`.
Characters following colon (`:`) are treated as the `resolver-key`.

[Log4j 2 Lookups](https://logging.apache.org/log4j/2.0/manual/lookups.html)
(e.g., `${java:version}`, `${env:USER}`, `${date:MM-dd-yyyy}`) are supported
in templates too. Though note that while `${json:...}` template variables are
expected to occupy an entire field, that is, `"level": "${json:level}"`, a
lookup can be mixed within a regular text: `"myCustomField": "Hello, ${env:USER}!"`.

Similarly, below is the list of allowed `StackTraceElement` template variables:

| Variable Name | Description |
|---------------|-------------|
| `stackTraceElement:className` | `stackTraceElement.getClassName()` |
| `stackTraceElement:methodName` | `stackTraceElement.getMethodName()` |
| `stackTraceElement:fileName` | `stackTraceElement.getFileName()` |
| `stackTraceElement:lineNumber` | `stackTraceElement.getLineNumber()` |

As in `LogEvent` templates, `StackTraceElement` templates support Log4j 2
lookups too.

See [`layout-demo`](layout-demo) directory for a sample application
demonstrating the usage of `LogstashLayout`.

<a name="templates"></a>

# Predefined Templates

`log4j2-logstash-layout` artifact contains the following predefined templates:

- [`EcsLayout.json`](layout/src/main/resources/EcsLayout.json) described by
  [the Elastic Common Schema (ECS) specification](https://www.elastic.co/guide/en/ecs/current/ecs-reference.html)

- [`LogstashJsonEventLayoutV1.json`](layout/src/main/resources/LogstashJsonEventLayoutV1.json)
  described in [log4j-jsonevent-layout](https://github.com/logstash/log4j-jsonevent-layout)

- [`GelfLayout.json`](layout/src/main/resources/GelfLayout.json) described by
  [the Graylog Extended Log Format (GELF) payload specification](https://docs.graylog.org/en/3.1/pages/gelf.html#gelf-payload-specification)
  with additional `_thread` and `_logger`fields similar to
  [`GelfLayout` of Log4j 2](https://logging.apache.org/log4j/2.0/manual/layouts.html#GELFLayout)
  (Here it is advised to override the obligatory `host` field with a user
  provided constant via `eventTemplateAdditionalFields` to avoid `hostName`
  property lookup at runtime, which incurs an extra cost.)

<a name="features"></a>

# Features

Below is a feature comparison matrix between `LogstashLayout` and alternatives.

| Feature | `LogstashLayout` | `JsonLayout` | `EcsLayout` |
|---------|------------------|--------------|-------------|
| Java version | 8 | 7<sup>3</sup> | 6 |
| Dependencies | Jackson | Jackson | None |
| Full schema customization? | ✓ | ✕ | ✕ |
| Timestamp customization? | ✓ | ✕ | ✕ |
| (Almost) garbage-free? | ✓ | ✕ | ✓ |
| Custom typed `Message` serialization? | ✓ | ✕ | ✓<sup>4</sup> |
| Custom typed `MDC` value serialization? | ✓ | ✕ | ✕ |
| Rendering stack traces as array? | ✓ | ✓ | ✓ |
| Enabling/Disabling JSON pretty print? | ✓ | ✓ | ✕ |
| Additional fields? | ✓ | ✓ | ✓ |

<sup>3</sup> Log4j 2.4 and greater requires Java 7, versions 2.0-alpha1 to 2.3
required Java 6.

<sup>4</sup> Only for `ObjectMessage`s and if Jackson is in the classpath.

<a name="fat-jar"></a>

# Fat JAR

Project also contains a `log4j2-logstash-layout-fatjar` artifact which
includes all its transitive dependencies in a separate shaded package (to
avoid the JAR Hell) with the exception of `log4j-core`, that you need to
include separately.

This might come handy if you want to use this plugin along with already
compiled applications, e.g., Elasticsearch 5.x and 6.x versions, which
requires Log4j 2.

<a name="appender-support"></a>

# Appender Support

`log4j2-logstash-layout` is all about providing a highly customizable JSON
schema for your logs. Though this does not necessarily mean that all of its
features are expected to be supported by every appender in the market. For
instance, while `prettyPrintEnabled=true` works fine with
[log4j2-redis-appender](/vy/log4j2-redis-appender), it should be turned off
for Logstash's `log4j-json` file input type. (See
[Pretty printing in Logstash](/vy/log4j2-logstash-layout/issues/8) issue.)
Make sure you configure `log4j2-logstash-layout` properly in a way that
is aligned with your appender of preference.

<a name="performance"></a>

# Performance

The source code contains a benchmark comparing `LogstashLayout` performance with
[`JsonLayout`](https://logging.apache.org/log4j/2.0/manual/layouts.html#JSONLayout)
(shipped by default in Log4j 2) and
[`EcsLayout`](https://github.com/elastic/java-ecs-logging/tree/master/log4j2-ecs-layout)
(shipped by Elastic). There [JMH](https://openjdk.java.net/projects/code-tools/jmh/)
is used to assess the rendering performance of these layouts. In the tests,
different `LogEvent` profiles are employed:

- **full**: `LogEvent` contains MDC, NDC, and an exception.
- **lite:** `LogEvent` has no MDC, NDC, or exception attachment.

To give an idea, we ran the benchmark with the following settings:

- **CPU:** Intel i7 2.70GHz (x86-64, confined `java` process to a single core
  using [`taskset -c 0`](http://www.man7.org/linux/man-pages/man1/taskset.1.html))
- **JVM:** OpenJDK 64-Bit, AdoptOpenJDK, build 25.232-b09
  - `-XX:+TieredCompilation`
  - `-Dlog4j2.garbagefreeThreadContextMap=true`
  - `-Dlog4j2.enableDirectEncoders=true`
  - `-Dlog4j2.enable.threadlocals=true`
  - `-Dlog4j2.is.webapp=false`
- **OS:** Xubuntu 18.04.3 (4.15.0-70-generic, x86-64)
- **`LogstashLayout4{Ecs,Json,Gelf}Layout`** used default settings with the
  following exceptions:
  - **`stackTraceEnabled`:** `true`
  - **`maxByteCount`:** (4096) 4KiB
- **`JsonLayout`** used in two different flavors:
  - **`DefaultJsonLayout`:** default settings
  - **`CustomJsonLayout`:** default settings with an additional `"@version": 1`
    field (this forces instantiation of a wrapper class to obtain the necessary
    Jackson view)
- **`EcsLayout`** used with the following configurations:
  - **`serviceName`:** `benchmark`
  - **`additionalFields`:** `new KeyValuePair[0]`
- **`GelfLayout`** used with the following configurations:
  - **`compressionType`:** `off`

The figures for serializing 1,000 `LogEvent`s at each operation are as follows.
(See [`layout-benchmark`](layout-benchmark) directory for the full report.)

<div id="results">
    <table>
        <thead>
            <tr>
                <th>Benchmark</th>
                <th colspan="2">ops/sec<sup>5</sup></th>
                <th>B/op<sup>5</sup></th>
            </tr>
        </thead>
        <tbody>
            <tr data-benchmark="liteLogstashLayout4GelfLayout">
                <td class="benchmark">liteLogstashLayout4GelfLayout</td>
                <td class="op_rate">1,517,062</td>
                <td class="op_rate_bar">▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉ (100%)</td>
                <td class="gc_rate">0.0</td>
            </tr>
            <tr data-benchmark="liteLogstashLayout4EcsLayout">
                <td class="benchmark">liteLogstashLayout4EcsLayout</td>
                <td class="op_rate">1,196,255</td>
                <td class="op_rate_bar">▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉ (79%)</td>
                <td class="gc_rate">0.0</td>
            </tr>
            <tr data-benchmark="liteGelfLayout">
                <td class="benchmark">liteGelfLayout</td>
                <td class="op_rate">1,184,922</td>
                <td class="op_rate_bar">▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉ (78%)</td>
                <td class="gc_rate">0.0</td>
            </tr>
            <tr data-benchmark="liteLogstashLayout4JsonLayout">
                <td class="benchmark">liteLogstashLayout4JsonLayout</td>
                <td class="op_rate">870,012</td>
                <td class="op_rate_bar">▉▉▉▉▉▉▉▉▉▉▉ (57%)</td>
                <td class="gc_rate">0.0</td>
            </tr>
            <tr data-benchmark="liteEcsLayout">
                <td class="benchmark">liteEcsLayout</td>
                <td class="op_rate">836,648</td>
                <td class="op_rate_bar">▉▉▉▉▉▉▉▉▉▉▉ (55%)</td>
                <td class="gc_rate">0.0</td>
            </tr>
            <tr data-benchmark="liteDefaultJsonLayout">
                <td class="benchmark">liteDefaultJsonLayout</td>
                <td class="op_rate">506,985</td>
                <td class="op_rate_bar">▉▉▉▉▉▉▉ (33%)</td>
                <td class="gc_rate">5,331,680.0</td>
            </tr>
            <tr data-benchmark="liteCustomJsonLayout">
                <td class="benchmark">liteCustomJsonLayout</td>
                <td class="op_rate">446,243</td>
                <td class="op_rate_bar">▉▉▉▉▉▉ (29%)</td>
                <td class="gc_rate">5,740,400.0</td>
            </tr>
            <tr data-benchmark="fullLogstashLayout4JsonLayout">
                <td class="benchmark">fullLogstashLayout4JsonLayout</td>
                <td class="op_rate">118,294</td>
                <td class="op_rate_bar">▉▉ (8%)</td>
                <td class="gc_rate">104,000.1</td>
            </tr>
            <tr data-benchmark="fullLogstashLayout4GelfLayout">
                <td class="benchmark">fullLogstashLayout4GelfLayout</td>
                <td class="op_rate">73,102</td>
                <td class="op_rate_bar">▉ (5%)</td>
                <td class="gc_rate">35,663,200.3</td>
            </tr>
            <tr data-benchmark="fullLogstashLayout4EcsLayout">
                <td class="benchmark">fullLogstashLayout4EcsLayout</td>
                <td class="op_rate">60,569</td>
                <td class="op_rate_bar">▉ (4%)</td>
                <td class="gc_rate">35,631,200.4</td>
            </tr>
            <tr data-benchmark="fullEcsLayout">
                <td class="benchmark">fullEcsLayout</td>
                <td class="op_rate">27,887</td>
                <td class="op_rate_bar">▉ (2%)</td>
                <td class="gc_rate">46,479,200.5</td>
            </tr>
            <tr data-benchmark="fullGelfLayout">
                <td class="benchmark">fullGelfLayout</td>
                <td class="op_rate">21,458</td>
                <td class="op_rate_bar">▉ (1%)</td>
                <td class="gc_rate">58,911,200.7</td>
            </tr>
            <tr data-benchmark="fullDefaultJsonLayout">
                <td class="benchmark">fullDefaultJsonLayout</td>
                <td class="op_rate">13,513</td>
                <td class="op_rate_bar">▉ (1%)</td>
                <td class="gc_rate">234,102,401.5</td>
            </tr>
            <tr data-benchmark="fullCustomJsonLayout">
                <td class="benchmark">fullCustomJsonLayout</td>
                <td class="op_rate">13,511</td>
                <td class="op_rate_bar">▉ (1%)</td>
                <td class="gc_rate">234,238,401.5</td>
            </tr>
        </tbody>
    </table>
    <p id="footnotes">
        <sup>5</sup> 99<sup>th</sup> percentile
    </p>
</div>

Let us try to answer some common questions:

- **How come `log4j2-logstash-layout` can yield superior performance compared
  to Log4j 2 `JsonLayout`?** Log4j 2 `JsonLayout` employs a single Jackson view
  to generate JSON, XML, and YAML outputs. For this purpose, it uses Jackson
  `ObjectMapper`, which needs to walk over the class fields via reflection and
  perform heavy branching and intermediate object instantiation. On the
  contrary, `log4j2-logstash-layout` parses the given template once and
  compiles a (mostly) garbage- and (to a certain extent) branching-free
  JSON generator employing Jackson `JsonGenerator`.

- **Why is `log4j2-logstash-layout` is not totally garbage-free?**

  - Since `Throwable#getStackTrace()` clones the original
    `StackTraceElement[]`, accesses to (and hence rendering) stack traces can
    never be garbage-free.

  - Rendering of context data (that is, MDC) field values is garbage-free if
    the value is either `null`, or of type `String`, `Short`, `Integer`,
    `Long`, or `byte[]`.

- **How can one run the benchmark on his/her machine?** After a fresh
  `mvn clean verify` within the source directory, run
  `layout-benchmark/benchmark.py`.

- **What about thread-local allocations?** Even though Log4j 2 exposes a
  `log4j2.enable.threadlocals` flag to toggle TLAs, neither `EcsLayout`, nor
  Log4j 2 `JsonLayout` and `GelfLayout` honor it. Historically, `LogstashLayout`
  used to have TLAs taking the `log4j2.enable.threadlocals` flag into account.
  In version 0.18, we switched to simple memory-efficient object pools, though
  that incurred extra synchronization costs. Since version 0.22,
  `LogstashLayout` switched back to TLAs taking `log4j2.enable.threadlocals`
  into account.

<a name="faq"></a>

# F.A.Q.

- **How can one enable thread-local allocations?** For performance reasons,
  it is highly recommended to turn TLAs on. For this purpose, you need to make
  sure `log4j2.enable.threadlocals=true` and `log4j2.is.webapp=false`.

- **Is there a fat JAR of the plugin?** Project also contains a
  `log4j2-logstash-layout-fatjar` artifact which includes all its transitive
  dependencies in a separate shaded package (to avoid the JAR Hell) with the
  exception of `log4j-core`, that you need to include separately. Fat JAR might
  come handy if you want to use this plugin along with certain applications,
  e.g., Elasticsearch 5.x and 6.x versions, which requires Log4j 2.

- **Why do I get irrelevant stack traces from
  `exception[RootCause]:stackTrace[:text]` directives?** `LogstashLayout`
  uses `Throwable#printStackTrace(PrintWriter)` and `Throwable#getStackTrace()`
  methods to resolve these directives. Under certain circumstances, these
  methods may throw exception as well. Put another way, trying to access the
  stack trace of an exception might cause another exception. In such a case,
  `LogstashLayout` continues the directive resolution using the new exception
  and keeps on repeating until it manages to resolve a stack trace to at least
  provide some insight into the underlying cause.

<a name="contributors"></a>

# Contributors

- [bakomchik](https://github.com/bakomchik)
- [chrissydee](https://github.com/chrissydee)
- [Daniel Lundsgaard Skovenborg](https://github.com/waldeinburg)
- [Eric Schwartz](https://github.com/emschwar)
- [Felix Barnsteiner](https://github.com/felixbarny)
- [Johann Schmitz](https://github.com/ercpe)
- [John Ament](https://github.com/johnament)
- [Jonathan Guéhenneux](https://github.com/Achaaab)
- [justinsaliba](https://github.com/justinsaliba)
- [Maurice Zeijen](https://github.com/mzeijen)
- [Michael K. Edwards](https://github.com/mkedwards)
- [Mikael Strand](https://github.com/MikaelStrand)
- [Rafa Gómez](https://github.com/rgomezcasas)
- [Yaroslav Skopets](https://github.com/yskopets)

<a name="license"></a>

# License

Copyright &copy; 2017-2020 [Volkan Yazıcı](https://vlkan.com/)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
