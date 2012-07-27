
package com.openexchange.memory.analyzer;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.aspectj.lang.JoinPoint;
import com.openexchange.log.LogFactory;
import com.openexchange.memory.analyzer.WeakIdentityHashMap;

public privileged aspect CollectionAnalyzerAspect {

    static final transient Log LOG = LogFactory.getLog(CollectionAnalyzerAspect.class);
    
    // After this time in minutes, the collection will be checked
    private int STARTUP_DELAY = Integer.parseInt(System.getProperty("ox.ma.startup.delay", "2"));
    {
        if (STARTUP_DELAY < 0){
            LOG.info("ox.ma.startup.delay.delay has to be at least 0 or more");
//            System.out.println("ma.period.startup.delay has to be at least 0 or more");
            STARTUP_DELAY = 0;
        }
    }
    // Minimum number of elememts, under which a collection will never be checked
    private final int MIN_COLLECTION_ELEMENTS = Integer.parseInt(System.getProperty("ox.ma.collectionelements.minimum", "5000"));
    // Time in minutes, a collection will be watched at minimum
    private int SUSPECT_PERIOD = Integer.parseInt(System.getProperty("ox.ma.suspect.period", "10"));
    {
        if (SUSPECT_PERIOD < 1){
            LOG.info("ox.ma.period.suspect has to be at least 1 or more");
            SUSPECT_PERIOD = 1;
        }
    }
    // Interval in seconds, the overwatch thread will iterate through the changed collections.
    int CHECKCHANGED_INTERVAL = Integer.parseInt(System.getProperty("ox.ma.checkchanged.interval", "5"));
    {
        if (CHECKCHANGED_INTERVAL < 1){
            LOG.info("ox.ma.check.interval has to be at least 1 or more");
            CHECKCHANGED_INTERVAL = 1;
        }
    }
    int CHECKTIME_INTERVAL = Integer.parseInt(System.getProperty("ox.ma.checktime.interval", "1"));
    {
        if (CHECKCHANGED_INTERVAL < 1){
            LOG.info("ox.ma.check.interval has to be at least 1 or more");
            CHECKCHANGED_INTERVAL = 1;
        }
    }
    // Time in hours, a collection will be marked as long-living.
    private int TIMEOUT_PERIOD = Integer.parseInt(System.getProperty("ox.ma.timeout.period", "24"));
    {
        if (TIMEOUT_PERIOD < 1){
            LOG.info("ox.ma.timeout.period has to be at least 1 or more");
            TIMEOUT_PERIOD = 1;
        }
    }
    // The Increasing Percentage has to be higher than 50% but lower than 100% to be in range
    private float INCREASING_PERCENTAGE = Float.parseFloat(System.getProperty("ox.ma.increasing.percentage", "0.75"));
    {
        if (INCREASING_PERCENTAGE > 1) {
            LOG.info("ox.ma.increasing.percentage has to be between 0.5 and 1. To get less false-positives, a value of 0.75 is prefered");
            INCREASING_PERCENTAGE = 1;
        } else if (INCREASING_PERCENTAGE < 0.5) {
            LOG.info("ox.ma.increasing.percentage has to be between 0.5 and 1. To get less false-positives, a value of 0.75 is prefered");
            INCREASING_PERCENTAGE = 0.5F;
        }
    }

    private final int SIZE_FOR_COLLECTIONS = SUSPECT_PERIOD * 60 / CHECKCHANGED_INTERVAL;

    protected WeakIdentityHashMap<Object, CollectionStatistics> statistics = new WeakIdentityHashMap<Object, CollectionStatistics>();

    protected WeakIdentityHashMap<Object, CollectionStatistics> hasChangedStatistics = new WeakIdentityHashMap<Object, CollectionStatistics>();

    protected WeakIdentityHashMap<Iterator, WeakReference<Object>> iteratorStats = new WeakIdentityHashMap<Iterator, WeakReference<Object>>();

    private boolean init = false;

    CollectionAnalyzerAspect() {
        init();
    }

    private synchronized void init() {

        if (init) {
            return;
        }
        CollectionAnalyzerThread myThread = new CollectionAnalyzerThread(this);
        myThread.start();
        init = true;
    }

    // Collection access
    before(Collection col) : call(* java.util.Collection+.*(..)) && target(col) && !within(com.openexchange.memory.analyzer..*){
        final CollectionStatistics stats = getStatistics(col);
        stats.recordAcess();
    }

    //Collection creation
    after() returning (Collection col): call(java.util.Collection+.new(..)) && !within(com.openexchange.memory.analyzer..*){
        final CollectionStatistics stats = getStatistics(col);
        stats.recordCreation(getFullStackTrace());
    }
    
    // Collection writes
    pointcut writeCol(): (call(* java.util.Collection+.add*(..)) || call(* java.util.Collection+.addAll*(..)));
    before(Collection col) : writeCol() && target(col) && !within(com.openexchange.memory.analyzer..*){
        final CollectionStatistics stats = getStatistics(col);
        stats.recordWrite(getLocation(thisJoinPoint));
    }

    // Collection reads
    before(Collection col) : call(* java.util.Collection+.get*(..)) && target(col) && !within(com.openexchange.memory.analyzer..*){
        final CollectionStatistics stats = getStatistics(col);
        stats.recordRead(getLocation(thisJoinPoint));
    }

    after(Collection col) returning (Iterator it): call(* java.util.Collection+.iterator*(..)) && target(col) && !within(com.openexchange.memory.analyzer..*){
        iteratorStats.put(it, new WeakReference<Object>(col));
    }

    // Collection deletes
    pointcut deleteCol(): (call(* java.util.Collection+.remove*(..)) || call(* java.util.Collection+.clear*(..)));
    before(Collection col) : deleteCol()  && target(col) && !within(com.openexchange.memory.analyzer..*){
        final CollectionStatistics stats = getStatistics(col);
        stats.recordDelete(getLocation(thisJoinPoint));
    }

    // Map access
    before(Map map) : call(* java.util.Map+.*(..)) && target(map) && !within(com.openexchange.memory.analyzer..*){
        final CollectionStatistics stats = getStatistics(map);
        stats.recordAcess();
    }
    
    //Map creation
    after() returning (Map map): call(java.util.Map+.new(..)) && !within(com.openexchange.memory.analyzer..*){
        final CollectionStatistics stats = getStatistics(map);
        stats.recordCreation(getFullStackTrace());
    }

    // Map writes
    pointcut writeMap(): (call(* java.util.Map+.put*(..)) || call(* java.util.Map+.putAll*(..)));
    before(Map map) : writeMap() && target(map) && !within(com.openexchange.memory.analyzer..*){
        final CollectionStatistics stats = getStatistics(map);
        stats.recordWrite(getLocation(thisJoinPoint));
    }

    // Map reads
    before(Map map) : call(* java.util.Map+.get*(..)) && target(map) && !within(com.openexchange.memory.analyzer..*){
        final CollectionStatistics stats = getStatistics(map);
        stats.recordRead(getLocation(thisJoinPoint));
    }

    after(Map map) returning (Iterator it) : call(* java.util.Map+.iterator*(..)) && target(map) &&  !within(com.openexchange.memory.analyzer..*){
        iteratorStats.put(it, new WeakReference<Object>(map));
    }

    // Map deletes
    pointcut deleteMap(): (call(* java.util.Map+.remove*(..)) || call(* java.util.Map+.clear*(..)));
    before(Map map) : deleteMap() && target(map) && !within(com.openexchange.memory.analyzer..*){
        final CollectionStatistics stats = getStatistics(map);
        stats.recordDelete(getLocation(thisJoinPoint));
    }

    // Iterator Operations
    before(Iterator it) : call(* java.util.Iterator+.next*(..)) && target(it) &&  !within(com.openexchange.memory.analyzer..*){
        final WeakReference<Object> wR = iteratorStats.get(it);
        if (wR != null) {
            final Object ob = wR.get();
            if (ob != null) {
                CollectionStatistics stats = getStatistics(ob);
                stats.recordRead(getLocation(thisJoinPoint));
            }
        }
    }

    before(Iterator it) : call(* java.util.Iterator+.remove*(..)) && target(it) &&  !within(com.openexchange.memory.analyzer..*){
        final WeakReference<Object> wR = iteratorStats.get(it);
        if (wR != null) {
            final Object ob = wR.get();
            if (ob != null) {
                CollectionStatistics stats = getStatistics(ob);
                stats.recordDelete(getLocation(thisJoinPoint));
            }
        }
    }

    private String getFullStackTrace() {
        final StackTraceElement[] traces = new Throwable().getStackTrace();

        final StringBuffer sb = new StringBuffer();
        for (int i = traces.length - 1; i >= 0; i--) {
            if (traces[i].toString().indexOf("com.openexchange.memory.analyzer") < 0 && traces[i].toString().indexOf("aspectj") < 0) {
                sb.append(traces[i].toString() + "\n");
            }
        }
        return sb.toString();
    }

    public void evaluateChanged() {
        for (final Iterator iter = hasChangedStatistics.values().iterator(); iter.hasNext();) {
            ((CollectionStatistics) iter.next()).evaluate();
        }
        hasChangedStatistics.clear();
    }

    public void evaluateTime() {
        for (final Iterator iter = statistics.values().iterator(); iter.hasNext();) {
            ((CollectionStatistics) iter.next()).evaluateTime();
        }
    }

    /**
     * Returns a CollectionStatistics instance for the given collection.
     */
    private CollectionStatistics getStatistics(final Object targetCollection) {
        CollectionStatistics stats = statistics.get(targetCollection);
        if (stats == null) {
            if (targetCollection instanceof Map) {
                stats = new CollectionStatisticsMaps(
                    targetCollection.getClass().getName(),
                    (Map) targetCollection,
                    STARTUP_DELAY,
                    MIN_COLLECTION_ELEMENTS,
                    SIZE_FOR_COLLECTIONS,
                    INCREASING_PERCENTAGE,
                    TIMEOUT_PERIOD);
            } else {
                stats = new CollectionStatisticsCollection(
                    targetCollection.getClass().getName(),
                    (Collection) targetCollection,
                    STARTUP_DELAY,
                    MIN_COLLECTION_ELEMENTS,
                    SIZE_FOR_COLLECTIONS,
                    INCREASING_PERCENTAGE,
                    TIMEOUT_PERIOD);
            }
            statistics.put(targetCollection, stats);
            hasChangedStatistics.put(targetCollection, stats);
        } else {
            hasChangedStatistics.put(targetCollection, stats);
        }
        return stats;
    }

    /**
     * Returns a developer usable String for the line of code the joinpoint is acting on.
     */
    private String getLocation(final JoinPoint thisJoinPoint) {
        return thisJoinPoint.getStaticPart().getSourceLocation().toString();
    }

    private static class CollectionAnalyzerThread extends Thread {

        private CollectionAnalyzerAspect asp;

        public CollectionAnalyzerThread(CollectionAnalyzerAspect asp) {
            this.asp = asp;
            this.setName("OX-MemoryAnalyzer-Thread");
            this.setDaemon(true);
        }

        @Override
        public void run() {
            try {
                long lastCheckChanged = System.currentTimeMillis();
                long lastCheckTime = System.currentTimeMillis();
                
                LOG.info("******************************\n*      OX-MemoryAnalyzer     *\n*           started          *\n******************************");
                while (true) {
                    if (lastCheckChanged + asp.CHECKCHANGED_INTERVAL * 1000 < System.currentTimeMillis()) {
                        asp.evaluateChanged();
                        lastCheckChanged = System.currentTimeMillis();
                    }
                    if (lastCheckTime + asp.CHECKTIME_INTERVAL * 60000 < System.currentTimeMillis()) {
                        asp.evaluateTime();
                        lastCheckTime = System.currentTimeMillis();
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                LOG.info("******************************\n*      OX-MemoryAnalyzer     *\n*           finished         *\n******************************");
                asp = null;
            }

        }
    }

}
