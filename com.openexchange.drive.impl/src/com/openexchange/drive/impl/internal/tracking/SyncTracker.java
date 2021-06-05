/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.drive.DefaultDirectoryVersion;
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
        ArrayList<HistoryEntry> history = extractHistory(session);
        synchronized (history) {
            return trackAndCheck(syncResult, history);
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
        ArrayList<HistoryEntry> history = extractHistory(session);
        synchronized (history) {
            return track(syncResult, path, history);
        }
    }

    private IntermediateSyncResult<DirectoryVersion> trackAndCheck(IntermediateSyncResult<DirectoryVersion> syncResult, ArrayList<HistoryEntry> history) {
        /*
         * track sync result & check for potential cycles
         */
        insert(syncResult, null, history);
        RepeatedSequence<HistoryEntry> cycle = findCycle(history);
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
                int resetAttempts = getFrequency(history,
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
                    session.trace("Clearing result history.");
                    history.clear();
                }
            } else {
                /*
                 * add 'reset' sync actions for whole directory structure
                 */
                optimizedActionsForClient.add(new SyncDirectoriesAction(true));
                /*
                 * check if history already contains a (therefore probably failed) reset-attempt of all directories
                 */
                int resetAttempts = getFrequency(history,
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
            insert(newResult, null, history);
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

    private IntermediateSyncResult<FileVersion> track(IntermediateSyncResult<FileVersion> syncResult, String path, ArrayList<HistoryEntry> history) {
        insert(syncResult, path, history);
        return syncResult;
    }

    /**
     * Adds a sync result to the history.
     *
     * @param syncResult The sync result to add
     * @param path The path where this sync result was resulting from, or <code>null</code> if not relevant
     * @param history The result history
     * @return The added history entry
     */
    private HistoryEntry insert(IntermediateSyncResult<? extends DriveVersion> syncResult, String path, ArrayList<HistoryEntry> history) {
        HistoryEntry entry = new HistoryEntry(syncResult, path);
        history.add(session.isTraceEnabled() && MAX_HISTORY_ENTRY_LENGTH >= syncResult.length() ? entry : entry.compact());
        if (MAX_HISTORY_SIZE < history.size()) {
            history.remove(0);
        }
        return entry;
    }

    /**
     * Checks the current sync history for cycles, i.e. repeated sequences of the same synchronization results.
     *
     * @param history The result history
     * @return A repeated sequence identifying the cycle, or <code>null</code> if no cycle was detected.
     */
    private RepeatedSequence<HistoryEntry> findCycle(ArrayList<HistoryEntry> history) {
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
     * @param history The result history
     * @param result The result to count
     * @return The number of occurrences
     */
    private int getFrequency(ArrayList<HistoryEntry> history, IntermediateSyncResult<DirectoryVersion> result) {
        return Collections.frequency(history, new HistoryEntry(result, null));
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
                    return new DefaultDirectoryVersion(path, checksums.get(0).getChecksum());
                }
            }
        } catch (OXException e) {
            LOG.warn("Error getting directory version", e);
        }
        /*
         * use empty checksum as fallback
         */
        return new DefaultDirectoryVersion(path, DriveConstants.EMPTY_MD5);
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
