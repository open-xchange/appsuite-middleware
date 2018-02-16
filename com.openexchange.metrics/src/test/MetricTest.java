package test;

import java.util.concurrent.TimeUnit;
import com.openexchange.metrics.Meter;
import com.openexchange.metrics.MeterDescriptor;
import com.openexchange.metrics.impl.MetricServiceImpl;

public class MetricTest {

    public static void main(String[] args) {
        MetricServiceImpl metrics = new MetricServiceImpl();
        MeterDescriptor descriptor = MeterDescriptor.newBuilder("test", "ByteThroughput")
            .withRate(TimeUnit.SECONDS)
            .withUnit("bytes").build();
       Meter meter = metrics.meter(descriptor);
    }

}
