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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.internal.tracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.actions.AbstractAction;
import com.openexchange.drive.actions.SyncDirectoriesAction;
import com.openexchange.drive.actions.SyncDirectoryAction;
import com.openexchange.drive.checksum.ChecksumProvider;
import com.openexchange.drive.checksum.DirectoryChecksum;
import com.openexchange.drive.comparison.ServerDirectoryVersion;
import com.openexchange.drive.internal.SyncSession;
import com.openexchange.drive.storage.DriveConstants;
import com.openexchange.drive.sync.IntermediateSyncResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.java.StringAllocator;

/**
 * {@link SyncTracker}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SyncTracker {

    /**
     * The minimum number of repeated sequences to be considered as cycle.
     */
    private static final int MIN_REPETITION_COUNT = 3;

    /**
     * The minimum length of a sequence to be considered as cycle.
     */
    private static final int MIN_SEQUENCE_LENGTH = 1;

    /**
     * The sequence when the synchronization is idle, i.e. client and server are in-sync.
     */
    private static final List<HistoryEntry> IDLE_SEQUENCE = Arrays.asList(new HistoryEntry(new IntermediateSyncResult<DirectoryVersion>(
        new ArrayList<AbstractAction<DirectoryVersion>>(0), new ArrayList<AbstractAction<DirectoryVersion>>(0)), null).compact());

    /**
     * The maximum number of tracked sync results in the history.
     */
    private static final int MAX_HISTORY_SIZE = 50;

    /**
     * The maximum number of actions to be stored per history entry; others will be compacted implicitly.
     */
    private static final int MAX_HISTORY_ENTRY_LENGTH = 10;

    /**
     * Enable this after the first server bug :)
     */
    private static final boolean RESET_SERVER_DIRECTORIES = false;

    private static final String PARAM_RESULT_HISTORY = "com.openexchange.drive.resultHistory";
    private static final Log LOG = com.openexchange.log.Log.loggerFor(SyncTracker.class);

    private final SyncSession session;
    private ArrayList<HistoryEntry> resultHistory;

    /**
     * Initializes a new {@link SyncTracker}.
     *
     * @param session The sync session
     */
    public SyncTracker(SyncSession session) {
        super();
        this.session = session;
    }

    /**
     * Tracks the supplied sync result and performs checks to detect potential cycles in the synchronization history. If problems are
     * detected during the checks, the sync results are adjusted to reset stored checksums automatically.
     *
     * @param syncResult The sync results
     * @return The checked and potentially adjusted sync results
     */
    public IntermediateSyncResult<DirectoryVersion> trackAndCheck(IntermediateSyncResult<DirectoryVersion> syncResult) {
        /*
         * track sync result & check for potential cycles
         */
        insert(syncResult, null);
        RepeatedSequence<HistoryEntry> cycle = findCycle();
        if (null != cycle) {
            trace(session, cycle);
            List<AbstractAction<DirectoryVersion>> optimizedActionsForClient = new ArrayList<AbstractAction<DirectoryVersion>>();
            List<AbstractAction<DirectoryVersion>> optimizedActionsForServer = new ArrayList<AbstractAction<DirectoryVersion>>();
            Collection<DirectoryVersion> directoryVersions = getAffectedDirectoryVersions(session, cycle);
            if (0 < directoryVersions.size()) {
                /*
                 * add 'reset' sync actions for affected directories
                 */
                for (DirectoryVersion directoryVersion : directoryVersions) {
                    SyncDirectoryAction action = new SyncDirectoryAction(directoryVersion, null, true);
                    optimizedActionsForClient.add(action);
                    if (RESET_SERVER_DIRECTORIES) {
                        optimizedActionsForServer.add(action);
                    }
                }
            } else {
                /*
                 * add 'reset' sync actions for whole directory structure
                 */
                SyncDirectoriesAction action = new SyncDirectoriesAction(true);
                optimizedActionsForClient.add(action);
                if (RESET_SERVER_DIRECTORIES) {
                    optimizedActionsForServer.add(action);
                }
            }
            /*
             * pass 'reset' actions as new sync result
             */
            IntermediateSyncResult<DirectoryVersion> newResult =
                new IntermediateSyncResult<DirectoryVersion>(optimizedActionsForServer, optimizedActionsForClient);
            if (session.isTraceEnabled()) {
                session.trace(newResult);
            }
            return newResult;
        } else {
            /*
             * all fine, pass through result
             */
            return syncResult;
        }
    }

    /**
     * Tracks the supplied sync results.
     *
     * @param syncResult The sync results
     * @param path The path where this sync result was resulting from, or <code>null</code> if not relevant
     * @return The passed sync result
     */
    public IntermediateSyncResult<FileVersion> track(IntermediateSyncResult<FileVersion> syncResult, String path) {
        insert(syncResult, path);
        return syncResult;
    }

    /**
     * Adds a sync result to the history.
     *
     * @param syncResult The sync result to add
     * @param path The path where this sync result was resulting from, or <code>null</code> if not relevant
     * @return The added history entry
     */
    private HistoryEntry insert(IntermediateSyncResult<? extends DriveVersion> syncResult, String path) {
        HistoryEntry entry = new HistoryEntry(syncResult, path);
        ArrayList<HistoryEntry> history = getResultHistory();
        history.add(session.isTraceEnabled() && MAX_HISTORY_ENTRY_LENGTH >= syncResult.length() ? entry : entry.compact());
        if (MAX_HISTORY_SIZE < history.size()) {
            history.remove(0);
        }
        return entry;
    }

    /**
     * Gets the result history.
     *
     * @return The result history
     */
    private ArrayList<HistoryEntry> getResultHistory() {
        if (null == this.resultHistory) {
            resultHistory = extractHistory(session);
        }
        return resultHistory;
    }

    /**
     * Checks the current sync history for cycles, i.e. repeated sequences of the same synchronization results.
     *
     * @return A repeated sequence identifying the cycle, or <code>null</code> if no cycle was detected.
     */
    private RepeatedSequence<HistoryEntry> findCycle() {
        ArrayList<HistoryEntry> history = getResultHistory();
        for (int i = 0; i < history.size(); i++) {
            List<HistoryEntry> subList = history.subList(i, history.size());
            RepeatedSequence<HistoryEntry> repetitions = findRepetitions(subList);
            if (null != repetitions && MIN_REPETITION_COUNT <= repetitions.getRepetitions() &&
                null != repetitions.getSequence() && MIN_SEQUENCE_LENGTH <= repetitions.getSequence().size() &&
                false == IDLE_SEQUENCE.equals(repetitions.getSequence())) {
                return repetitions;
            }
        }
        return null;
    }

    private static Collection<DirectoryVersion> getAffectedDirectoryVersions(SyncSession session, RepeatedSequence<HistoryEntry> cycle) {
        Map<String, DirectoryVersion> directoryVersions = new HashMap<String, DirectoryVersion>();
        List<HistoryEntry> sequence = cycle.getSequence();
        for (HistoryEntry entry : sequence) {
            String path = entry.getPath();
            if (null != path && false == directoryVersions.containsKey(path)) {
                directoryVersions.put(path, getDirectoryVersion(session, path));
            }
        }
        return directoryVersions.values();
    }

    private static DirectoryVersion getDirectoryVersion(SyncSession session, final String path) {
        try {
            FileStorageFolder folder = session.getStorage().optFolder(path, false);
            if (null != folder) {
                List<DirectoryChecksum> checksums = ChecksumProvider.getChecksums(session, Arrays.asList(new String[] { folder.getId() }));
                if (null != checksums && 1 == checksums.size()) {
                    return new ServerDirectoryVersion(path, checksums.get(0));
                }
            }
        } catch (OXException e) {
            LOG.warn("Error getting directory version", e);
        }
        /*
         * fallback to simple directory version
         */
        return new DirectoryVersion() {

            @Override
            public String getChecksum() {
                return DriveConstants.EMPTY_MD5;
            }

            @Override
            public String getPath() {
                return path;
            }
        };
    }

    private static void trace(SyncSession session, RepeatedSequence<HistoryEntry> cycle) {
        if (null != cycle && session.isTraceEnabled()) {
            List<HistoryEntry> sequence = cycle.getSequence();
            StringAllocator stringAllocator = new StringAllocator();
            stringAllocator.append("A synchronization cycle was detected - The following ").append(sequence.size())
                .append(" sync results were repeated ").append(cycle.getRepetitions()).append(" times:\n\n");
            for (int i = 0; i < sequence.size(); i++) {
                HistoryEntry entry = sequence.get(i);
                stringAllocator.append(" # ").append(i + 1).append(' ').append(null != entry.getPath() ? entry.getPath() : "")
                    .append(" : [").append(entry.hashCode()).append(']');
                IntermediateSyncResult<? extends DriveVersion> result = entry.getSyncResult();
                if (null != result) {
                    stringAllocator.append('\n').append(result);
                }
            }
            session.trace(stringAllocator.toString());
        }
    }

    /**
     * Finds repetitions of the same sequence towards the end of the supplied list. An element sequence is considered to repeat if the
     * same elements in the same order are repeating until the end of the list, i.e.:
     * <ul>
     * <li><code>ABCABCABCABC => 4x ABC</code></li>
     * <li><code>XYABCABC => 2x ABC</code></li>
     * </ul>
     * <p/>
     * Partial sequences at the end of the list are not taken into account, hence, the following would be detected as:
     * <ul>
     * <li><code>ABCABCA => 1x ABCABCA</code></li>
     * <li><code>XYABC => 1x XYABC</code></li>
     * </ul>
     *
     * @param list The list
     * @return The repeated sequence
     */
    private static <T> RepeatedSequence<T> findRepetitions(List<T> list) {
        if (null == list || 2 > list.size()) {
            return new RepeatedSequence<T>(list, 1);
        }
        /*
         * start with first element
         */
        ArrayList<T> currentSequence = new ArrayList<T>();
        currentSequence.add(list.get(0));
        int sequenceIndex = 0;
        int currentRepetitions = 1;
        /*
         * match further sequences
         */
        for (int i = 1; i < list.size(); i++) {
            T element = list.get(i);
            if (currentSequence.get(sequenceIndex).equals(element)) {
                /*
                 * element match, proceed through sequence
                 */
                sequenceIndex++;
                if (sequenceIndex >= currentSequence.size()) {
                    /*
                     * sequence matched entirely, add repetition
                     */
                    sequenceIndex = 0;
                    currentRepetitions++;
                }
            } else {
                /*
                 * sequence mismatch
                 * => build new sequence consisting of current sequence repetitions and current partial sequence matches
                 */
                ArrayList<T> newSequence = new ArrayList<T>();
                for (int r = 0; r < currentRepetitions; r++) {
                    newSequence.addAll(currentSequence);
                }
                newSequence.addAll(currentSequence.subList(0, sequenceIndex));
                newSequence.add(element);
                currentSequence = newSequence;
                sequenceIndex = 0;
                currentRepetitions = 1;
            }
        }
        /*
         * Wrap result
         */
        if (0 != sequenceIndex) {
            /*
             * Not repeated entirely, treat as single sequence
             */
            return new RepeatedSequence<T>(list, 1);
        } else {
            return new RepeatedSequence<T>(currentSequence, currentRepetitions);
        }
    }

    private static ArrayList<HistoryEntry> extractHistory(SyncSession session) {
        ArrayList<HistoryEntry> history;
        Object value = session.getServerSession().getParameter(PARAM_RESULT_HISTORY);
        if (null != value) {
            history = (ArrayList<HistoryEntry>)value;
        } else {
            history = new ArrayList<HistoryEntry>();
            session.getServerSession().setParameter(PARAM_RESULT_HISTORY, history);
        }
        return history;
    }

}
