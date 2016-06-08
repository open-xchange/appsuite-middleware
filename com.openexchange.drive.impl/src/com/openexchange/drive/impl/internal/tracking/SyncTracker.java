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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.drive.impl.internal.tracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.actions.ErrorDirectoryAction;
import com.openexchange.drive.impl.actions.SyncDirectoriesAction;
import com.openexchange.drive.impl.actions.SyncDirectoryAction;
import com.openexchange.drive.impl.checksum.ChecksumProvider;
import com.openexchange.drive.impl.checksum.DirectoryChecksum;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.storage.execute.DirectoryActionExecutor;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;
import com.openexchange.drive.impl.sync.SimpleDirectoryVersion;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFolder;

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
     * The maximum number of tries to send reset-actions before blacklisting affected folders.
     */
    private static final int MAX_RESET_ATTEMPTS = 3;

    /**
     * The sequence when the synchronization is idle, i.e. client and server are in-sync.
     */
    private static final List<HistoryEntry> IDLE_SEQUENCE = Arrays.asList(new HistoryEntry(new IntermediateSyncResult<DirectoryVersion>(
        new ArrayList<AbstractAction<DirectoryVersion>>(0), new ArrayList<AbstractAction<DirectoryVersion>>(0)), null).compact());

    /**
     * The maximum number of tracked sync results in the history.
     */
    private static final int MAX_HISTORY_SIZE = 200;

    /**
     * The maximum number of actions to be stored per history entry; others will be compacted implicitly.
     */
    private static final int MAX_HISTORY_ENTRY_LENGTH = 10;

    private static final String PARAM_RESULT_HISTORY_PREFIX = "com.openexchange.drive.resultHistory";
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SyncTracker.class);

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
            Collection<DirectoryVersion> affectedDirectoryVersions = getAffectedDirectoryVersions(session, cycle);
            if (0 < affectedDirectoryVersions.size()) {
                /*
                 * add 'reset' sync actions for affected directories
                 */
                for (DirectoryVersion directoryVersion : affectedDirectoryVersions) {
                    SyncDirectoryAction action = new SyncDirectoryAction(directoryVersion, null, true);
                    optimizedActionsForClient.add(action);
                }
                /*
                 * check if history already contains a (therefore probably failed) reset-attempt of individual directories
                 */
                int resetAttempts = getFrequency(
                    new IntermediateSyncResult<DirectoryVersion>(optimizedActionsForServer, optimizedActionsForClient));
                if (1 == resetAttempts) {
                    /*
                     * reset server checksums as well for affected directories to be sure
                     */
                    session.trace("Cycle still detected after first attempt to reset directory checksums " +
                        "- resetting affected server checksums as well...");
                    resetServerChecksums(session, affectedDirectoryVersions);
                } else if (MAX_RESET_ATTEMPTS <= resetAttempts) {
                    /*
                     * gave client enough chances to reset his directory checksums, request client to stop for self protection
                     */
                    session.trace("Already tried to reset checksums " + resetAttempts +
                        " times - adding 'quarantine' action for affected directories to interrupt further processing.");
                    optimizedActionsForClient.clear();
                    for (DirectoryVersion directoryVersion : affectedDirectoryVersions) {
                        OXException e = DriveExceptionCodes.REPEATED_SYNC_PROBLEMS.create(
                            directoryVersion.getPath(), directoryVersion.getChecksum());
                        LOG.warn("Requesting client to stop synchronization: {}", session, e);
                        optimizedActionsForClient.add(new ErrorDirectoryAction(null, directoryVersion, null, e, false, true));
                    }
                    /*
                     * clear result history after aborting sync
                     */
                    session.trace("Clrearing result history.");
                    getResultHistory().clear();
                }
            } else {
                /*
                 * add 'reset' sync actions for whole directory structure
                 */
                optimizedActionsForClient.add(new SyncDirectoriesAction(true));
                /*
                 * check if history already contains a (therefore probably failed) reset-attempt of all directories
                 */
                int resetAttempts = getFrequency(
                    new IntermediateSyncResult<DirectoryVersion>(optimizedActionsForServer, optimizedActionsForClient));
                if (1 == resetAttempts) {
                    session.trace("Cycle still detected after first attempt to reset directory checksums " +
                        "- resetting affected server checksums as well...");
                    resetServerChecksums(session, affectedDirectoryVersions);
                }
            }
            /*
             * prepare new sync result containing 'reset' actions
             */
            IntermediateSyncResult<DirectoryVersion> newResult =
                new IntermediateSyncResult<DirectoryVersion>(optimizedActionsForServer, optimizedActionsForClient);
            /*
             * track & return new sync result
             */
            insert(newResult, null);
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

    /**
     * Gets the number of occurrences of the supplied result in the result history.
     *
     * @param result The result to count
     * @return The number of occurrences
     */
    private int getFrequency(IntermediateSyncResult<DirectoryVersion> result) {
        return Collections.frequency(getResultHistory(), new HistoryEntry(result, null));
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

    /**
     * Creates a directory version representing the supplied path. If possible, the directory version uses the server's checksum,
     * if not, it falls back to a placeholder checksum.
     *
     * @param session The sync session
     * @param path The path
     * @return A directory checksum to be used in error actions
     */
    private static DirectoryVersion getDirectoryVersion(SyncSession session, String path) {
        /*
         * try to use current server checksum if possible
         */
        try {
            FileStorageFolder folder = session.getStorage().optFolder(path);
            if (null != folder) {
                List<DirectoryChecksum> checksums = ChecksumProvider.getChecksums(session, Arrays.asList(new String[] { folder.getId() }));
                if (null != checksums && 1 == checksums.size()) {
                    return new SimpleDirectoryVersion(path, checksums.get(0).getChecksum());
                }
            }
        } catch (OXException e) {
            LOG.warn("Error getting directory version", e);
        }
        /*
         * use empty checksum as fallback
         */
        return new SimpleDirectoryVersion(path, DriveConstants.EMPTY_MD5);
    }

    private static void trace(SyncSession session, RepeatedSequence<HistoryEntry> cycle) {
        if (null != cycle && session.isTraceEnabled()) {
            List<HistoryEntry> sequence = cycle.getSequence();
            StringBuilder StringBuilder = new StringBuilder();
            StringBuilder.append("A synchronization cycle was detected - the following ").append(sequence.size())
                .append(" sync results were repeated ").append(cycle.getRepetitions()).append(" times:\n\n");
            for (int i = 0; i < sequence.size(); i++) {
                HistoryEntry entry = sequence.get(i);
                StringBuilder.append(" # ").append(i + 1).append(' ').append(null != entry.getPath() ? entry.getPath() : "")
                    .append(" : [").append(entry.hashCode()).append("]\n");
                IntermediateSyncResult<? extends DriveVersion> result = entry.getSyncResult();
                if (null != result) {
                    StringBuilder.append(result);
                }
            }
            session.trace(StringBuilder.toString());
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
        String parameterName = PARAM_RESULT_HISTORY_PREFIX + ':' + session.getRootFolderID();
        ArrayList<HistoryEntry> history;
        Object value = session.getServerSession().getParameter(parameterName);
        if (null != value) {
            history = (ArrayList<HistoryEntry>) value;
        } else {
            history = new ArrayList<HistoryEntry>();
            session.getServerSession().setParameter(parameterName, history);
        }
        return history;
    }

    private static void resetServerChecksums(SyncSession session, Collection<DirectoryVersion> directoryVersions) {
        /*
         * generate adequate reset actions...
         */
        List<AbstractAction<DirectoryVersion>> resetActions = new ArrayList<AbstractAction<DirectoryVersion>>();
        if (null == directoryVersions || 0 == directoryVersions.size()) {
            resetActions.add(new SyncDirectoriesAction(true));
        } else {
            for (DirectoryVersion directoryVersion : directoryVersions) {
                resetActions.add(new SyncDirectoryAction(directoryVersion, null, true));
            }
        }
        /*
         * ... and execute them
         */
        try {
            new DirectoryActionExecutor(session, true, true).execute(
                new IntermediateSyncResult<DirectoryVersion>(resetActions, Collections.<AbstractAction<DirectoryVersion>>emptyList()));
        } catch (OXException e) {
            LOG.warn("Error resetting server checksums", e);
        }
    }

}
