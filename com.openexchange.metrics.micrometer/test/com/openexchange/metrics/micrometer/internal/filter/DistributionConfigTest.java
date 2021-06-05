/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.metrics.micrometer.internal.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Filter;
import com.openexchange.config.PropertyFilter;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.metrics.micrometer.internal.RegistryInitializer;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig.Builder;
import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * {@link DistributionConfigTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class DistributionConfigTest {

    private static final boolean DEBUG = true;

    private final RegistryInitializer initializer;

    public DistributionConfigTest(RegistryInitializer initializer) {
        this.initializer = initializer;
    }

    public void disableCertainMetricCompletely() throws Exception {
        String config = "" +
            "# Remove metric output completely\n" +
            "com.openexchange.metrics.micrometer.enable.appsuite.httpapi.requests = false\n";

        PrometheusMeterRegistry prometheusMeterRegistry = initializer.initialize(initConfigService(config));

        simulateHttpApiRequests();

        if (DEBUG) {
            System.out.println(prometheusMeterRegistry.scrape());
        }

        assertNoLineStartsWith("appsuite_httpapi_requests", prometheusMeterRegistry);
        assertAtLeastOneLineStartsWith("jvm_", prometheusMeterRegistry);
    }


    public void disableMetricButKeepForCertainTags() throws Exception {
        String config = "" +
            "# Remove metric output but keep for certain tags\n" +
            "com.openexchange.metrics.micrometer.enable.appsuite.httpapi.requests = false\n" +
            "com.openexchange.metrics.micrometer.filter.httpapilogins = appsuite.httpapi.requests{module=\"mail\"}\n" +
            "com.openexchange.metrics.micrometer.enable.httpapilogins = true\n";

        PrometheusMeterRegistry prometheusMeterRegistry = initializer.initialize(initConfigService(config));

        simulateHttpApiRequests();

        if (DEBUG) {
            System.out.println(prometheusMeterRegistry.scrape());
        }

        assertMetricOnlyOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests", "module", "mail");
    }

    public void overrideHistogramSLAsByDefaultBucketsWithLimits() throws Exception {
        String config = "" +
            "# Override SLO histogram with default latency buckets and limits\n" +
            "com.openexchange.metrics.micrometer.distribution.histogram.appsuite.httpapi.requests = true\n" +
            "com.openexchange.metrics.micrometer.distribution.minimum.appsuite.httpapi.requests = 100\n" +
            "com.openexchange.metrics.micrometer.distribution.maximum.appsuite.httpapi.requests = 60000\n" +
            "com.openexchange.metrics.micrometer.distribution.slo.appsuite.httpapi.requests =\n";

        PrometheusMeterRegistry prometheusMeterRegistry = initializer.initialize(initConfigService(config));

        simulateHttpApiRequests();

        if (DEBUG) {
            System.out.println(prometheusMeterRegistry.scrape());
        }

        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "le", "0.1");
        // this is one of the expected values. if min and max are changed or the library ever changes the way how it calculates the buckets,
        // this must be adjusted to another sample.
        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "le", "0.111848106");
        assertMetricNotOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "le", "0.2");
        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "le", "60.0");
    }

    public void overrideDefaultHistogramSLAs() throws Exception {
        String config = "" +
            "# Override SLO latency buckets of existing histogram\n" +
            "com.openexchange.metrics.micrometer.distribution.slo.appsuite.httpapi.requests = 11ms, 22ms, 60s";

        PrometheusMeterRegistry prometheusMeterRegistry = initializer.initialize(initConfigService(config));

        simulateHttpApiRequests();

        if (DEBUG) {
            System.out.println(prometheusMeterRegistry.scrape());
        }

        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "le", "0.011");
        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "le", "0.022");
        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "le", "60.0");
    }

    public void overrideDefaultSLAsWithFullyConfiguredHistogram() throws Exception {
        String config = "" +
            "com.openexchange.metrics.micrometer.distribution.histogram.appsuite.httpapi.requests = true\n" +
            "com.openexchange.metrics.micrometer.distribution.minimum.appsuite.httpapi.requests = 100\n" +
            "com.openexchange.metrics.micrometer.distribution.maximum.appsuite.httpapi.requests = 60000\n" +
            "com.openexchange.metrics.micrometer.distribution.slo.appsuite.httpapi.requests = 111ms, 555ms, 20s\n";

        PrometheusMeterRegistry prometheusMeterRegistry = initializer.initialize(initConfigService(config));

        simulateHttpApiRequests();

        if (DEBUG) {
            System.out.println(prometheusMeterRegistry.scrape());
        }

        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "le", "0.1");
        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "le", "0.111");
        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "le", "0.555");
        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "le", "20.0");
        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "le", "60.0");
    }

    public void disableHistogramForTimerWithDefaultSLAs() throws Exception {
        String config = "" +
            "com.openexchange.metrics.micrometer.distribution.slo.appsuite.httpapi.requests =\n";

        PrometheusMeterRegistry prometheusMeterRegistry = initializer.initialize(initConfigService(config));

        simulateHttpApiRequests();

        if (DEBUG) {
            System.out.println(prometheusMeterRegistry.scrape());
        }

        assertNoLineStartsWith("appsuite_httpapi_requests_seconds_bucket", prometheusMeterRegistry);
        assertAtLeastOneLineStartsWith("appsuite_httpapi_requests_seconds_count", prometheusMeterRegistry);
        assertAtLeastOneLineStartsWith("appsuite_httpapi_requests_seconds_sum", prometheusMeterRegistry);
        assertAtLeastOneLineStartsWith("appsuite_httpapi_requests_seconds_max", prometheusMeterRegistry);
    }

    public void turnHistogramForTimerWithDefaultSLAsIntoPercentileSummary() throws Exception {
        String config = "" +
            "com.openexchange.metrics.micrometer.distribution.histogram.appsuite.httpapi.requests = false\n" +
            "com.openexchange.metrics.micrometer.distribution.slo.appsuite.httpapi.requests =\n" +
            "com.openexchange.metrics.micrometer.distribution.percentiles.appsuite.httpapi.requests = 0.5, 0.75, 0.95, 0.99, 0.999\n";

        PrometheusMeterRegistry prometheusMeterRegistry = initializer.initialize(initConfigService(config));

        simulateHttpApiRequests();

        if (DEBUG) {
            System.out.println(prometheusMeterRegistry.scrape());
        }

        // appsuite_httpapi_requests_seconds{action="list",module="chronos",status="OK",quantile="0.5",} 6.442450944
        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds", "quantile", "0.5");
        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds", "quantile", "0.75");
        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds", "quantile", "0.95");
        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds", "quantile", "0.99");
        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds", "quantile", "0.999");
        assertAtLeastOneLineStartsWith("appsuite_httpapi_requests_seconds_count", prometheusMeterRegistry);
        assertAtLeastOneLineStartsWith("appsuite_httpapi_requests_seconds_sum", prometheusMeterRegistry);
        assertAtLeastOneLineStartsWith("appsuite_httpapi_requests_seconds_max", prometheusMeterRegistry);
        assertNoLineStartsWith("appsuite_httpapi_requests_seconds_bucket", prometheusMeterRegistry);
    }

    public void disableHistogramForAllButCertainTags() throws Exception {
        String config = "" +
            "com.openexchange.metrics.micrometer.distribution.histogram.appsuite.httpapi.requests = false\n" +
            "com.openexchange.metrics.micrometer.distribution.slo.appsuite.httpapi.requests =\n" +
            "com.openexchange.metrics.micrometer.filter.httpapilogins = appsuite.httpapi.requests{module=\"mail\",action=\"get\"}\n" +
            "com.openexchange.metrics.micrometer.distribution.histogram.httpapilogins = true\n";

        PrometheusMeterRegistry prometheusMeterRegistry = initializer.initialize(initConfigService(config));

        simulateHttpApiRequests();

        if (DEBUG) {
            System.out.println(prometheusMeterRegistry.scrape());
        }

        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "module", "mail", "action", "get", "le", "0.001");
        assertMetricNotOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "module", "mail", "action", "all", "le", "0.001");
        assertMetricNotOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "module", "mail", "action", "list", "le", "0.001");
        assertMetricNotOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "module", "chronos", "le", "0.001");
    }

    public void disableSLAsHistogramForAllButCertainTags() throws Exception {
        String config = "" +
            "com.openexchange.metrics.micrometer.distribution.histogram.appsuite.httpapi.requests = false\n" +
            "com.openexchange.metrics.micrometer.distribution.slo.appsuite.httpapi.requests =\n" +
            "com.openexchange.metrics.micrometer.filter.httpapilogins = appsuite.httpapi.requests{module=\"mail\",action=\"get\"}\n" +
            "com.openexchange.metrics.micrometer.distribution.slo.httpapilogins = 33ms, 66ms, 77s\n";

        PrometheusMeterRegistry prometheusMeterRegistry = initializer.initialize(initConfigService(config));

        simulateHttpApiRequests();

        if (DEBUG) {
            System.out.println(prometheusMeterRegistry.scrape());
        }

        assertMetricAtLeastOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "module", "mail", "action", "get", "le", "0.033");
        assertMetricNotOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "module", "mail", "action", "all", "le", "0.033");
        assertMetricNotOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "module", "mail", "action", "list", "le", "0.033");
        assertMetricNotOccursWithMatchingTags(prometheusMeterRegistry, "appsuite_httpapi_requests_seconds_bucket", "module", "chronos", "le", "0.033");
    }

    private static ConfigurationService initConfigService(String config) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(config));
        TestConfigService configService = new TestConfigService(properties);
        return configService;
    }

    private static void assertMetricAtLeastOccursWithMatchingTags(PrometheusMeterRegistry prometheusMeterRegistry, String prefix, String... tags) throws IOException {
        assert tags != null && tags.length > 0 && tags.length % 2 == 0;
        String output = prometheusMeterRegistry.scrape();
        BufferedReader reader = new BufferedReader(new StringReader(output));
        int found = 0;
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (line.startsWith(prefix)) {
                // jvm_gc_collection_seconds_sum{gc="PS MarkSweep",} 0.0
                int tagStart = line.indexOf('{');
                assert tagStart > 0;

                int tagEnd = line.lastIndexOf('}');
                assert tagEnd > 0;

                String tagStr = line.substring(tagStart + 1, tagEnd);
                for (int i = 0; i < tags.length; i++) {
                    String name = tags[i++];
                    String value = tags[i];
                    if (tagStr.contains(name + "=\"" + value + '"')) {
                        found++;
                    }
                }
            }
        }

        assert found > 0;
    }

    private static void assertMetricNotOccursWithMatchingTags(PrometheusMeterRegistry prometheusMeterRegistry, String prefix, String... tags) throws IOException {
        assert tags != null && tags.length > 0 && tags.length % 2 == 0;
        String output = prometheusMeterRegistry.scrape();
        BufferedReader reader = new BufferedReader(new StringReader(output));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (line.startsWith(prefix)) {
                // jvm_gc_collection_seconds_sum{gc="PS MarkSweep",} 0.0
                int tagStart = line.indexOf('{');
                assert tagStart > 0;

                int tagEnd = line.lastIndexOf('}');
                assert tagEnd > 0;

                int matchingTags = 0;
                String tagStr = line.substring(tagStart + 1, tagEnd);
                for (int i = 0; i < tags.length; i++) {
                    String name = tags[i++];
                    String value = tags[i];
                    if (tagStr.contains(name + "=\"" + value + '"')) {
                        matchingTags++;
                    }
                }
                assert matchingTags < tags.length;
            }
        }
    }

    private static void assertMetricOnlyOccursWithMatchingTags(PrometheusMeterRegistry prometheusMeterRegistry, String prefix, String... tags) throws IOException {
        assert tags != null && tags.length > 0 && tags.length % 2 == 0;
        String output = prometheusMeterRegistry.scrape();
        BufferedReader reader = new BufferedReader(new StringReader(output));
        boolean found = false;
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (line.startsWith(prefix)) {
                // jvm_gc_collection_seconds_sum{gc="PS MarkSweep",} 0.0
                int tagStart = line.indexOf('{');
                assert tagStart > 0;

                int tagEnd = line.lastIndexOf('}');
                assert tagEnd > 0;

                String tagStr = line.substring(tagStart + 1, tagEnd);
                for (int i = 0; i < tags.length; i++) {
                    String name = tags[i++];
                    String value = tags[i];
                    assert tagStr.contains(name + "=\"" + value + '"');
                    found = true;
                }
            }
        }

        assert found;
    }

    private static void assertAtLeastOneLineStartsWith(String prefix, PrometheusMeterRegistry prometheusMeterRegistry) throws IOException {
        String output = prometheusMeterRegistry.scrape();
        BufferedReader reader = new BufferedReader(new StringReader(output));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (line.startsWith(prefix)) {
                return;
            }
        }

        System.out.println("Found no line starting with: " + prefix);
        assert false;
    }

    private static void assertNoLineStartsWith(String prefix, PrometheusMeterRegistry prometheusMeterRegistry) throws IOException {
        String output = prometheusMeterRegistry.scrape();
        BufferedReader reader = new BufferedReader(new StringReader(output));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (line.startsWith(prefix)) {
                System.err.println("Unexpected line: " + line);
                assert false;
            }
        }
    }


    private static void simulateHttpApiRequests() {
        for (String module : new String[] {"mail", "chronos"}) {
            for (String action : new String[] {"all", "list", "get"}) {
                for (String status : new String[] {"OK"}) {
                    Timer timer = Timer.builder("appsuite.httpapi.requests")
                        .tags("module", module, "action", action, "status", status)
                        .description("HTTP API request times")
                        .serviceLevelObjectives(
                            Duration.ofMillis(50),
                            Duration.ofMillis(100),
                            Duration.ofMillis(150),
                            Duration.ofMillis(200),
                            Duration.ofMillis(250),
                            Duration.ofMillis(300),
                            Duration.ofMillis(400),
                            Duration.ofMillis(500),
                            Duration.ofMillis(750),
                            Duration.ofSeconds(1),
                            Duration.ofSeconds(2),
                            Duration.ofSeconds(60),
                            Duration.ofSeconds(100))
                        //.publishPercentileHistogram()
                        .register(Metrics.globalRegistry);
                    timer.record(ThreadLocalRandom.current().nextLong(50L, 60000L), TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        RegistryInitializer registryInitializer = new RegistryInitializer(Metrics.globalRegistry);
        DistributionConfigTest test = new DistributionConfigTest(registryInitializer);
        test.disableCertainMetricCompletely();
        test.disableMetricButKeepForCertainTags();
        test.overrideHistogramSLAsByDefaultBucketsWithLimits();
        test.overrideDefaultHistogramSLAs();
        test.overrideDefaultSLAsWithFullyConfiguredHistogram();
        test.disableHistogramForTimerWithDefaultSLAs();
        test.turnHistogramForTimerWithDefaultSLAsIntoPercentileSummary();
        test.disableHistogramForAllButCertainTags();
        test.disableSLAsHistogramForAllButCertainTags();
    }

    static class ConfigFilter implements MeterFilter {
        @Override
        public DistributionStatisticConfig configure(Id id, DistributionStatisticConfig config) {
            Builder builder = DistributionStatisticConfig.builder();
            if (id.getTag("module").equals("mail")) {
                builder.serviceLevelObjectives(Duration.ofMillis(50).toNanos(),
                            Duration.ofMillis(100).toNanos(),
                            Duration.ofMillis(150).toNanos());
                       //.percentilesHistogram(Boolean.FALSE)
                       //.percentiles(/*0.5, 0.75, 0.95, 0.99, 0.999*/ new double[0]);
            } else {
                builder.serviceLevelObjectives(new double[0])
                       .percentilesHistogram(Boolean.FALSE)
                       .percentiles(/*0.5, 0.75, 0.95, 0.99, 0.999*/ new double[0]);
            }

            return builder.build().merge(config);
        }
    }

    static class TestConfigService implements ConfigurationService {

        private final Properties properties;

        TestConfigService(Properties properties) {
            this.properties = properties;
        }

        @Override
        public Map<String, String> getProperties(PropertyFilter filter) throws OXException {
            Map<String, String> values = new HashMap<String, String>();
            Set<Entry<Object,Object>> entrySet = properties.entrySet();
            for (Entry<Object,Object> e : entrySet) {
                if (filter.accept(e.getKey().toString(), e.getValue().toString())) {
                    values.put(e.getKey().toString(), e.getValue().toString());
                }
            }
            return values;
        }

        @Override
        public String getProperty(String name) {
            return properties.getProperty(name);
        }

        @Override
        public String getProperty(String name, String defaultValue) {
            String value = getProperty(name);
            if (value == null) {
                value = defaultValue;
            }
            return value;
        }

        @Override
        public List<String> getProperty(String name, String defaultValue, String separator) {
            String value = getProperty(name, defaultValue);
            return Strings.splitAndTrim(value, separator);
        }

        @Override
        public boolean getBoolProperty(String name, boolean defaultValue) {
            return Boolean.parseBoolean(getProperty(name, Boolean.toString(defaultValue)));
        }

        @Override
        public int getIntProperty(String name, int defaultValue) {
            return Integer.parseInt(getProperty(name, Integer.toString(defaultValue)));
        }

        @Override
        public Iterator<String> propertyNames() {
            return properties.stringPropertyNames().iterator();
        }

        @Override
        public int size() {
            return properties.size();
        }

        @Override
        public Filter getFilterFromProperty(String name) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public Properties getFile(String fileName) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public File getDirectory(String directoryName) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public File getFileByName(String fileName) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public String getText(String fileName) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public Properties getPropertiesInFolder(String folderName) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public Object getYaml(String filename) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public Map<String, Object> getYamlInFolder(String dirName) {
            throw new RuntimeException("Not implemented");
        }

    }

}
