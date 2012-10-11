
package com.openexchange.memory.analyzer;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * Keeps track of code interaction with a specific collection. The flag stop is used to turn statistics off.
 */
@SuppressWarnings("rawtypes")
public class CollectionStatisticsMaps extends CollectionStatistics {

    private final WeakReference<Map> mapRef;

    public CollectionStatisticsMaps(String className, Map mapRef, int delayPeriod, int minElements, int maxSizeSamples, float increasingPercentage, int timeoutPeriod) {
        super(className, System.identityHashCode(mapRef), delayPeriod, minElements, maxSizeSamples, increasingPercentage, timeoutPeriod);
        this.mapRef = new WeakReference<Map>(mapRef);
    }

    @Override
    public int getSize() {
        Map tmpMap = mapRef.get();
        if (tmpMap == null) {
            return 0;
        }
        try {
            return tmpMap.size();
        } catch (java.lang.UnsupportedOperationException e) {
            return 0;
        }
    }
}
