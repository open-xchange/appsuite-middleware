/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.chronos.storage.rdb.migration;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.threadpool.AbstractTask;

/**
 * {@link MigrationTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class MigrationTask extends AbstractTask<Void> {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MigrationTask.class);

    protected final MigrationConfig config;
    protected final int contextId;
    protected final CalendarStorage sourceStorage;
    protected final CalendarStorage destinationStorage;
    private final MigrationProgress progress;

    private long lastLogTime;

    /**
     * Initializes a new {@link MigrationTask}.
     *
     * @param progress The migration progress callback, or <code>null</code> if not used
     * @param config The migration config to use
     * @param contextId The context identifier
     * @param sourceStorage The source calendar storage
     * @param destinationStorage The destination calendar storage
     */
    protected MigrationTask(MigrationProgress progress, MigrationConfig config, int contextId, CalendarStorage sourceStorage, CalendarStorage destinationStorage) {
        super();
        this.progress = progress;
        this.config = config;
        this.contextId = contextId;
        this.sourceStorage = sourceStorage;
        this.destinationStorage = destinationStorage;
    }

    /**
     * Collects the storage warnings that occurred during migration.
     *
     * @return The warnings, or an empty map if there are none
     */
    private Map<String, List<OXException>> collectWarnings() {
        SortedMap<String, List<OXException>> warnings = new TreeMap<String, List<OXException>>(CalendarUtils.ID_COMPARATOR);
        if (null != sourceStorage) {
            warnings.putAll(sourceStorage.getAndFlushWarnings());
        }
        if (null != destinationStorage) {
            warnings.putAll(destinationStorage.getAndFlushWarnings());
        }
        return warnings;
    }

    protected abstract void perform() throws OXException;

    protected String getName() {
        return getClass().getSimpleName();
    }

    protected void setProgress(long current, long total) {
        if (null != progress) {
            progress.setTaskProgress(current, total);
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastLogTime > TimeUnit.SECONDS.toMillis(10L)) {
            LOG.info("Migration task {} finished {}% for context {}.", getName(), I((int) (current * 100 / total)), I(contextId));
        }
        lastLogTime = currentTimeMillis;
    }

    @Override
    public Void call() throws Exception {
        long startTime = System.currentTimeMillis();
        LOG.info("Starting migration task {} in context {}.", getName(), I(contextId));
        if (null != progress) {
            progress.nextTask();
        }
        lastLogTime = startTime;
        try {
            perform();
        } catch (Exception e) {
            LOG.error("Error running migration task {} in context {}: {}", getName(), I(contextId), e.getMessage(), e);
            throw e;
        } finally {
            long millis = System.currentTimeMillis() - startTime;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
            if (0 < seconds) {
                millis -= TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS);
            }
            LOG.info("Finished migration task {} in context {}, {}.{} seconds elapsed.", getName(), I(contextId), L(seconds), L(millis));
            Map<String, List<OXException>> warnings = collectWarnings();
            if (null == warnings || 0 == warnings.size()) {
                LOG.info("No warnings occurred during execution of migration task {} in context {}.", getName(), I(contextId));
            } else {
                LOG.info("Encountered the following warnings during execution of migration task {} in context {}:{}{}",
                    getName(), I(contextId), System.lineSeparator(), dumpWarnings(warnings));
            }
        }
        return null;
    }

    private String dumpWarnings(Map<String, List<OXException>> warnings) {
        StringBuilder stringBuilder = new StringBuilder();
        if (null != warnings && 0 < warnings.size()) {
            for (Entry<String, List<OXException>> warningsPerEvent : warnings.entrySet()) {
                for (OXException warning : warningsPerEvent.getValue()) {
                    if (config.isSevere(warning)) {
                        try (StringWriter stringWriter = new StringWriter(); PrintWriter printWriter = new PrintWriter(stringWriter)) {
                            warning.printStackTrace(printWriter);
                            stringBuilder.append(stringWriter.toString()).append(System.lineSeparator());
                        } catch (IOException i) {
                            // ignore
                        }
                    } else {
                        stringBuilder.append(warning.getLogMessage()).append(System.lineSeparator());
                    }
                }
            }
        }
        return stringBuilder.toString().trim();
    }

    protected static <K, V> int countMultiMap(Map<K, List<V>> multiMap) {
        int count = 0;
        if (null != multiMap) {
            for (Entry<K, List<V>> entry : multiMap.entrySet()) {
                if (null != entry.getValue()) {
                    count += entry.getValue().size();
                }
            }
        }
        return count;
    }

    protected static <K1, K2, V> int countMultiMultiMap(Map<K1, Map<K2, List<V>>> multiMultiMap) {
        int count = 0;
        if (null != multiMultiMap) {
            for (Entry<K1, Map<K2, List<V>>> entry : multiMultiMap.entrySet()) {
                count += countMultiMap(entry.getValue());
            }
        }
        return count;
    }

    protected static String toString(Map<String, List<OXException>> warnings, boolean includeStackTrace) {
        StringBuilder stringBuilder = new StringBuilder();
        if (null != warnings && 0 < warnings.size()) {
            for (Entry<String, List<OXException>> entry : warnings.entrySet()) {
                for (OXException e : entry.getValue()) {
                    if (includeStackTrace) {
                        try (StringWriter stringWriter = new StringWriter(); PrintWriter printWriter = new PrintWriter(stringWriter)) {
                            e.printStackTrace(printWriter);
                            stringBuilder.append(stringWriter.toString()).append(System.lineSeparator());
                        } catch (IOException i) {
                            // ignore
                        }
                    } else {
                        stringBuilder.append(e.getLogMessage()).append(System.lineSeparator());
                    }
                }
            }
        }
        return stringBuilder.toString();
    }

}

