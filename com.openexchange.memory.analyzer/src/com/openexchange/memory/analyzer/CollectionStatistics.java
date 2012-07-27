
package com.openexchange.memory.analyzer;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;

/**
 * Keeps track of code interaction with a specific collection. The flag stop is used to turn statistics off.
 */
public abstract class CollectionStatistics {

    static final transient Log LOG = LogFactory.getLog(CollectionStatistics.class);
    
    protected int minElements;

    protected final String className;

    protected final int id;

    /**
     * These Sets track unique calling lines of code. Those can get very long and a memory leak on their own!
     */
    protected final Set<String> interactingCodeWrite = new HashSet<String>();
    protected final Set<String> interactingCodeRead = new HashSet<String>();
    protected final Set<String> interactingCodeDelete = new HashSet<String>();

//    protected long reads = 0;
//
//    protected long deletes = 0;

    protected boolean stop = true;

    protected boolean warned = false;

    protected boolean isALeak = false;

    protected long size = 0;

    protected long lastSize = 0;

    protected long increasing = 0;

    protected long decreasing = 0;

    protected long maxSizeSamples;

    protected long lastAccess = System.currentTimeMillis();

    protected long createTime = System.currentTimeMillis();

    protected int delayPeriod;
    
    protected int timeoutPeriod;

    protected float increasingPercentage;

    protected String creationStackTrace;

    public CollectionStatistics(String className, int id, int delayPeriod, int minElements, int maxSizeSamples, float increasingPercentage,int timeoutPeriod) {
        this.className = className;
        this.id = id;
        this.delayPeriod = delayPeriod;
        this.minElements = minElements;
        this.maxSizeSamples = maxSizeSamples;
        this.increasingPercentage = increasingPercentage;
        this.timeoutPeriod = timeoutPeriod;
    }

    public void recordWrite(final String invokingCode) {
        if (warned)
            return;
        if (!interactingCodeWrite.contains(invokingCode))
            interactingCodeWrite.add(invokingCode);

    }

    public void recordRead(final String invokingCode) {
        if (warned)
            return;
        if (!interactingCodeRead.contains(invokingCode))
            interactingCodeRead.add(invokingCode);
//        reads++;
    }

    public void recordDelete(final String invokingCode) {
        if (warned)
            return;
        if (!interactingCodeDelete.contains(invokingCode))
            interactingCodeDelete.add(invokingCode);
//        deletes++;
    }

    public void recordAcess() {
        lastAccess = System.currentTimeMillis();
    }

    public void recordCreation(final String fullStackTrace) {
        creationStackTrace = fullStackTrace;
    }

    public void evaluate() {

        if (warned) {
            return;
        }

        // When the time of delayPeriod is over, this collection will be observed
        if (stop) {
            if (System.currentTimeMillis() - createTime > 1000 * delayPeriod) {
                stop = false;
            }
        }

        // First check if size is great enough to be tested
        size = getSize();
        if (size >= minElements) {

            if (size > lastSize) {
                increasing++;
            } else if (size < lastSize) {
                decreasing--;
            }

            if ((increasing + decreasing) > maxSizeSamples) {

                if (decreasing < 0) {
                    isALeak = true;
                }

                if (increasing > (maxSizeSamples * increasingPercentage)) {
                    isALeak = true;
                }

                increasing--;
                decreasing--;
            }

            lastSize = size;

        } else {
            increasing = 0;
            decreasing = 0;
        }

        if (isALeak) {
            
            StringBuilder sb = new StringBuilder();
            sb.append("Information for Collection ").append(className).append(" (id: ").append(id).append(")\n");
            sb.append(" * Collection is very long (").append(size).append("!\n");

            printStatistics(sb);
            printInteractionCode(sb);

            sb.append("Warned about Collection ").append(className).append(" (id: ").append(id).append("). For performance reasons not warning about it anymore.\n");

            LOG.error(sb.toString());

            // at least somehow reduce our impact on CPU and memory
            stop = true;
            warned = true;
            clearInteractionCode();
        }
    }

    public abstract int getSize();

    public void evaluateTime() {
        if (warned) {
            return;
        }
        if ((System.currentTimeMillis() - lastAccess) > timeoutPeriod * 3600000 /* hour -> millisec */) {
            StringBuilder sb = new StringBuilder();
            sb.append("Information for Collection ").append(className).append(" (id: ").append(id).append(")\n");
            sb.append(" * Collection size (").append(size).append("!\n");
            sb.append(" * Collection has been idle since (").append(timeoutPeriod).append(" hours!\n");

            printStatistics(sb);
            printInteractionCode(sb);

            sb.append("Warned about Collection ").append(className).append(" (id: ").append(id).append("). For performance reasons not warning about it anymore.\n");

            LOG.error(sb.toString());

            // at least somehow reduce our impact on CPU and memory
            stop = true;
            warned = true;
            clearInteractionCode();
        }
    }

    private void printStatistics(StringBuilder sb) {
        if (interactingCodeRead.size() == 0) {
            sb.append(" * Collection was never read!\n");
        }

        if (interactingCodeDelete.size() == 0) {
            sb.append(" * Collection was never reduced!\n");
        }
    }

    private void printInteractionCode(StringBuilder sb) {
        if (creationStackTrace != null) {
            sb.append("Recorded creation stacktrace:\n");
            sb.append(creationStackTrace);
        }
        sb.append("Recorded usage for this Collection:\n");
        for (String code : interactingCodeWrite) {
            sb.append(" * write:  ").append(code).append(")\n");
        }
        for (String code : interactingCodeRead) {
            sb.append(" * read:   ").append(code).append(")\n");
        }
        for (String code : interactingCodeDelete) {
            sb.append(" * delete: ").append(code).append(")\n");
        }
    }

    private void clearInteractionCode() {
        interactingCodeWrite.clear();
        interactingCodeRead.clear();
        interactingCodeDelete.clear();
    }

}
