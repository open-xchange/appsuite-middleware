package test;

import java.util.concurrent.TimeUnit;
import com.openexchange.metrics.descriptors.MeterDescriptor;
import com.openexchange.metrics.impl.MetricServiceImpl;
import com.openexchange.metrics.types.Meter;

public class MetricTest {

    public static void main(String[] args) {
        MetricServiceImpl metrics = new MetricServiceImpl();
        MeterDescriptor descriptor = MeterDescriptor.newBuilder("test", "ByteThroughput")
            .withRate(TimeUnit.SECONDS)
            .withUnit("bytes").build();
       Meter meter = metrics.meter(descriptor);
    }

}
