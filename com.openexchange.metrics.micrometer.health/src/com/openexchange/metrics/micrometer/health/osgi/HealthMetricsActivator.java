package com.openexchange.metrics.micrometer.health.osgi;

import java.util.Stack;
import com.openexchange.health.MWHealthCheckService;
import com.openexchange.health.MWHealthState;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.version.VersionService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Metrics;

/**
 * {@link HealthMetricsActivator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class HealthMetricsActivator extends HousekeepingActivator {

    private final Stack<Meter> meters = new Stack<>();

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { VersionService.class, MWHealthCheckService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        for (MWHealthState state : MWHealthState.values()) {
            meters.push(Gauge.builder("appsuite.health.status", () -> getService(MWHealthCheckService.class).check().getStatus() == state ? 1.0d : 0.0d)
                .tag("status", state.name())
                .description("Application health status")
                .register(Metrics.globalRegistry));
        }

        VersionService versionService = getService(VersionService.class);
        meters.push(Gauge.builder("appsuite.version.info", () -> 1.0d)
            .tags("server_version", versionService.getVersionString(), "build_date", versionService.getBuildDate())
            .description("App Suite version")
            .register(Metrics.globalRegistry));
    }

    @Override
    protected void stopBundle() throws Exception {
        while (!meters.empty()) {
            Metrics.globalRegistry.remove(meters.pop());
        }
        super.stopBundle();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }


}
