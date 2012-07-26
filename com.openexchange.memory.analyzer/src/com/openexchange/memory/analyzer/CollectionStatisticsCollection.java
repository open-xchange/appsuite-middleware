
package com.openexchange.memory.analyzer;

import java.lang.ref.WeakReference;
import java.util.Collection;

/**
 * Keeps track of code interaction with a specific collection. The flag stop is used to turn statistics off.
 */
@SuppressWarnings("rawtypes")
public class CollectionStatisticsCollection extends CollectionStatistics {


    private final WeakReference<Collection> listRef;
    


    public CollectionStatisticsCollection(String className, Collection listRef,int delayPeriod, int minElements, int maxSizeSamples, float increasingPercentage,int timeoutPeriod) {
        super(className,System.identityHashCode(listRef),delayPeriod,minElements,maxSizeSamples,increasingPercentage,timeoutPeriod);
        this.listRef = new WeakReference<Collection>(listRef);
    }

    @Override
    public int getSize() {
        Collection tmpCollection = listRef.get();
        if (tmpCollection == null){
            return 0;
        }
        return tmpCollection.size();
    }
}
