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

package com.openexchange.admin.tools.filestore;

import static com.openexchange.java.Autoboxing.I;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.osgi.FilestoreLocationUpdaterRegistry;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.ProgrammErrorException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.utils.Filestore2UserUtil;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.QuotaFileStorageExceptionCodes;
import com.openexchange.groupware.filestore.FileLocationHandler;
import com.openexchange.java.Streams;

/**
 * {@link FilestoreDataMover} - The base implementation to move files from one storage to another.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public abstract class FilestoreDataMover implements Callable<Void> {

    /** The logger constant */
    protected static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(FilestoreDataMover.class);

    // ------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance appropriate for moving the files for a single context.
     *
     * @param srcFilestore The source file storage
     * @param dstFilestore The destination file storage
     * @param ctx The associated context
     * @return The new instance
     */
    public static FilestoreDataMover newContextMover(Filestore srcFilestore, Filestore dstFilestore, Context ctx) {
        return new ContextFilestoreDataMover(srcFilestore, dstFilestore, ctx);
    }

    /**
     * Creates a new instance appropriate for moving the files to another storage location for a single user.
     *
     * @param srcFilestore The source file storage
     * @param dstFilestore The destination file storage
     * @param user The user
     * @param ctx The associated context
     * @return The new instance
     */
    public static FilestoreDataMover newUserMover(Filestore srcFilestore, Filestore dstFilestore, User user, Context ctx) {
        return new UserFilestoreDataMover(srcFilestore, dstFilestore, user, ctx);
    }

    /**
     * Creates a new instance appropriate for moving the files for a single user from an individual to a master storage.
     * <p>
     * <img src="./drive-business.png" alt="OX Drive Stand-Alone">
     *
     * @param srcFilestore The source file storage
     * @param dstFilestore The destination file storage
     * @param user The user
     * @param masterUser The master user
     * @param ctx The associated context
     * @return The new instance
     */
    public static FilestoreDataMover newUser2MasterMover(Filestore srcFilestore, Filestore dstFilestore, User user, User masterUser, Context ctx) {
        return new User2MasterUserFilestoreDataMover(srcFilestore, dstFilestore, user, masterUser, ctx);
    }

    /**
     * Creates a new instance appropriate for moving the files for a single user from a master to an individual storage.
     * <p>
     * <img src="./drive-standalone.png" alt="OX Drive Stand-Alone">
     *
     * @param srcFilestore The source file storage
     * @param dstFilestore The destination file storage
     * @param maxQuota The max. quota to apply
     * @param user The user
     * @param masterUser The master user
     * @param ctx The associated context
     * @return The new instance
     */
    public static FilestoreDataMover newUserFromMasterMover(Filestore srcFilestore, Filestore dstFilestore, long maxQuota, User user, User masterUser, Context ctx) {
        return new MasterUser2UserFilestoreDataMover(srcFilestore, dstFilestore, maxQuota, masterUser, user, ctx);
    }

    /**
     * Creates a new instance appropriate for moving files from a context to an individual user storage
     * <p>
     * <img src="./drive-userstorage.png" alt="OX Drive User Storage">
     *
     * @param srcFilestore The source file storage
     * @param dstFilestore The destination file storage
     * @param maxQuota The max. quota to apply
     * @param user The user
     * @param ctx The associated context
     * @return The new instance
     */
    public static FilestoreDataMover newContext2UserMover(Filestore srcFilestore, Filestore dstFilestore, long maxQuota, User user, Context ctx) {
        return new Context2UserFilestoreDataMover(srcFilestore, dstFilestore, maxQuota, user, ctx);
    }

    /**
     * Creates a new instance appropriate for moving files from an individual user to a context storage
     * <p>
     * <img src="./drive-userstorage.png" alt="OX Drive User Storage">
     *
     * @param srcFilestore The source file storage
     * @param dstFilestore The destination file storage
     * @param user The user
     * @param ctx The associated context
     * @return The new instance
     */
    public static FilestoreDataMover newUser2ContextMover(Filestore srcFilestore, Filestore dstFilestore, User user, Context ctx) {
        return new User2ContextFilestoreDataMover(srcFilestore, dstFilestore, user, ctx);
    }

    // ------------------------------------------------------------------------------------------------------------------

    /** The context */
    protected final Context ctx;

    /** The source file storage */
    protected final Filestore srcFilestore;

    /** The destination file storage */
    protected final Filestore dstFilestore;

    /** The queue holding post-process tasks */
    protected final Queue<PostProcessTask> postProcessTasks;

    /**
     * Initializes a new {@link FilestoreDataMover}.
     */
    protected FilestoreDataMover(Filestore srcFilestore, Filestore dstFilestore, Context ctx) {
        super();
        this.srcFilestore = srcFilestore;
        this.dstFilestore = dstFilestore;
        this.ctx = ctx;
        postProcessTasks = new ConcurrentLinkedQueue<PostProcessTask>();
    }

    @Override
    public Void call() throws StorageException, IOException, InterruptedException, ProgrammErrorException {
        try {
            copy();
        } catch (StorageException e) {
            LOGGER.error("", e);
            // Because the client side only knows of the exceptions defined in the core we have
            // to throw the trace as string
            throw new StorageException(e.toString());
        } catch (IOException e) {
            LOGGER.error("", e);
            throw e;
        } catch (InterruptedException e) {
            LOGGER.error("", e);
            throw e;
        } catch (ProgrammErrorException e) {
            LOGGER.error("", e);
            throw e;
        } catch (RuntimeException e) {
            LOGGER.error("", e);
            throw new StorageException(e.toString(), e);
        }
        return null;
    }

    /**
     * Adds given post-process task.
     *
     * @param task The task to execute when job is done successfully
     */
    public void addPostProcessTask(PostProcessTask task) {
        if (null != task) {
            postProcessTasks.add(task);
        }
    }

    /**
     * Gets the size in bytes for the denoted source directory
     *
     * @param sourcePathName The path name of the source directory
     * @return The size in bytes
     */
    public long getSize(String sourcePathName) {
        return FileUtils.sizeOfDirectory(new File(sourcePathName));
    }

    /**
     * Gets the list of files to copy from the source directory
     *
     * @param sourcePathName The path name of the source directory
     * @return The list of files to copy
     */
    public List<String> getFileList(String sourcePathName) {
        Collection<File> listFiles = FileUtils.listFiles(new File(sourcePathName), null, true);
        List<String> retval = new LinkedList<String>();
        for (final File file : listFiles) {
            retval.add(new StringBuilder(file.getPath()).append(File.pathSeparatorChar).append(file.getName()).toString());
        }
        return retval;
    }

    /**
     * Performs the copy (& delete).
     *
     * @throws StorageException If a storage error occurs
     * @throws InterruptedException If copy operation gets interrupted
     * @throws IOException If an I/O error occurs
     * @throws ProgrammErrorException If a program error occurs
     */
    private void copy() throws StorageException, IOException, InterruptedException, ProgrammErrorException {
        // Check "filestore2user" table
        if (Filestore2UserUtil.isNotTerminated(ClientAdminThreadExtended.cache)) {
            throw new StorageException("Table \"filestore2user\" not yet initialized");
        }

        // Pre-copy
        preDoCopy();

        Throwable thrown = null;
        boolean successful = false;
        try {
            // Do the copy
            doCopy(new URI(srcFilestore.getUrl()), new URI(dstFilestore.getUrl()));

            // Successfully performed
            successful = true;
        } catch (URISyntaxException e) {
            thrown = e; throw new StorageException(e.getMessage(), e);
        } catch (RuntimeException e) {
            thrown = e; throw new StorageException(e.getMessage(), e);
        } catch (Error x) {
            thrown = x; throw x;
        } catch (Throwable t) {
            thrown = t; throw t;
        } finally {
            // Post-copy
            postDoCopy(thrown);

            executePostProcessTasks(null == thrown ? null : new ExecutionException(thrown));
        }
    }

    /**
     * Copies specified files from source storage to destination storage.
     *
     * @param files The files to copy
     * @param srcStorage The source storage
     * @param dstStorage The destination storage
     * @param srcFullUri The fully qualifying URI from source storage
     * @param dstFullUri The fully qualifying URI from destination storage
     * @return The old file name to new file name mapping; [src-file] --&gt; [dst-file]
     * @throws StorageException If copy operation fails
     */
    protected CopyResult copyFiles(final Set<String> files, final FileStorage srcStorage, final FileStorage dstStorage, URI srcFullUri, URI dstFullUri) throws StorageException {
        if (files.isEmpty()) {
            return new CopyResult(Collections.<String, String> emptyMap(), null);
        }

        final String threadName = Thread.currentThread().getName();
        final Integer size = Integer.valueOf(files.size());

        if ("file".equalsIgnoreCase(srcFullUri.getScheme()) && "file".equalsIgnoreCase(dstFullUri.getScheme())) {
            // File-wise move possible
            try {
                final File srcParent = new File(srcFullUri);
                final File dstParent = new File(dstFullUri);
                long totalSize = 0L;

                int i = 1;
                for (String file : files) {
                    File toCopy = new File(srcParent, file);
                    long length = toCopy.length();
                    FileUtils.moveFile(toCopy, new File(dstParent, file));

                    if ((i % 10) == 0) {
                        LOGGER.info("{} copied {} files of {} in total", threadName, Integer.valueOf(i), size);
                    }
                    i++;

                    totalSize += length;
                }

                LOGGER.info("{} copied {} files.", threadName, size);

                Reverter reverter = new Reverter() {

                    @Override
                    public void revertCopy() throws StorageException {
                        int numReverted = 0;
                        int i = 1;
                        for (String file : files) {
                            File toRevert = new File(dstParent, file);
                            try {
                                FileUtils.moveFile(toRevert, new File(srcParent, file));

                                if ((i % 10) == 0) {
                                    LOGGER.info("{} reverted {} files of {} in total", threadName, Integer.valueOf(i), size);
                                }
                                i++;
                                numReverted++;
                            } catch (IOException e) {
                                LOGGER.error("{} failed to revert file {}", threadName, toRevert.getPath(), e);
                            }
                        }

                        LOGGER.info("{} reverted {} files.", threadName, Integer.valueOf(numReverted));
                    }
                };

                return new CopyResult(totalSize, reverter);
            } catch (IOException e) {
                throw new StorageException(e);
            }
        }

        // One-by-one...
        try {
            final Map<String, String> prevFileName2newFileName = new HashMap<String, String>(files.size());

            int i = 1;
            for (String file : files) {
                InputStream is = null;
                try {
                    is = srcStorage.getFile(file);
                    String newFile = dstStorage.saveNewFile(is);
                    if (null != newFile) {
                        prevFileName2newFileName.put(file, newFile);

                        if ((i % 10) == 0) {
                            LOGGER.info("{} copied {} files of {} in total", threadName, Integer.valueOf(i), size);
                        }
                        i++;
                    }
                } finally {
                    Streams.close(is);
                }
            }

            LOGGER.info("{} copied {} files.", threadName, size);

            Reverter reverter = new Reverter() {

                @Override
                public void revertCopy() throws StorageException {
                    String[] copiedFiles = prevFileName2newFileName.values().toArray(new String[size.intValue()]);
                    try {
                        dstStorage.deleteFiles(copiedFiles);
                    } catch (OXException x) {
                        // Failed bulk deletion - try one-by-one
                        for (String copiedFile : prevFileName2newFileName.values()) {
                            try {
                                dstStorage.deleteFile(copiedFile);
                            } catch (OXException e) {
                                LOGGER.error("{} failed to revert file {}", threadName, copiedFile, e.getCause() == null ? e : e.getCause());
                            }
                        }
                    }
                }
            };

            return new CopyResult(prevFileName2newFileName, reverter);
        } catch (OXException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Propagates new file locations throughout registered <code>FileLocationHandler</code> instances.
     *
     * @param prevFileName2newFileName The previous file name to new file name mapping
     * @throws OXException If an Open-Xchange error occurs
     * @throws SQLException If an SQL error occurs
     */
    protected void propagateNewLocations(Map<String, String> prevFileName2newFileName) throws OXException, SQLException {
        if (prevFileName2newFileName.isEmpty()) {
            return;
        }
        int contextId = ctx.getId().intValue();
        Connection con = Database.getNoTimeout(contextId, true);
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            for (FileLocationHandler updater : FilestoreLocationUpdaterRegistry.getInstance().getServices()) {
                updater.updateFileLocations(prevFileName2newFileName, contextId, con);
            }

            con.commit();
            rollback = false;
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            Database.backNoTimeout(contextId, true, con);
        }
    }

    /**
     * Executes the post-process tasks.
     *
     * @param executionError The optional execution error that occurred or <code>null</code>
     * @throws StorageException If processing the tasks fails
     */
    protected void executePostProcessTasks(ExecutionException executionError) throws StorageException {
        for (PostProcessTask task; (task = postProcessTasks.poll()) != null;) {
            task.perform(executionError);
        }
    }

    /**
     * Determines the file locations for given user/context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The associated file locations
     * @throws OXException If an Open-Xchange error occurs
     * @throws SQLException If an SQL error occurs
     */
    protected Set<String> determineFileLocationsFor(int userId, int contextId) throws OXException, SQLException {
        Set<String> srcFiles = new LinkedHashSet<String>();
        {
            Connection con = Database.getNoTimeout(contextId, true);
            try {
                for (FileLocationHandler updater : FilestoreLocationUpdaterRegistry.getInstance().getServices()) {
                    srcFiles.addAll(updater.determineFileLocationsFor(userId, contextId, con));
                }
            } finally {
                Database.backNoTimeout(contextId, false, con);
            }
        }
        return srcFiles;
    }

    /**
     * <ul>
     * <li>Copies the files from source storage to destination storage
     * <li>Propagates new file locations throughout registered FilestoreLocationUpdater instances (if necessary)
     * <li>Deletes source files (if necessary)
     * <li>Applies changes & clears caches
     * </ul>
     *
     * @param srcBaseUri The base URI from source storage
     * @param dstBaseUri The base URI from destination storage
     * @throws StorageException If a storage error occurs
     * @throws InterruptedException If copy operation gets interrupted
     * @throws IOException If an I/O error occurs
     * @throws ProgrammErrorException If a program error occurs
     */
    protected abstract void doCopy(URI srcBaseUri, URI dstBaseUri) throws StorageException, IOException, InterruptedException, ProgrammErrorException;

    /**
     * Implementation hook to perform pre-copy operations (prior to {@link #doCopy(URI, URI)} is called).
     *
     * @throws StorageException If pre-copy operations fails
     */
    protected void preDoCopy() throws StorageException {
        // Initially empty
    }

    /**
     * Implementation hook to perform post-copy operations (after {@link #doCopy(URI, URI)} was called).
     *
     * @param thrown The {@link Throwable} instance in case an error occurred; otherwise <code>null</code>
     */
    protected void postDoCopy(Throwable thrown) {
        // Initially empty
    }

    // ---------------------------------------------------------------------------------------------------------------------------

    /**
     * Signals what operation was performed to pass files to new file storage location.
     */
    static enum Operation {
        /**
         * Signals that files were moved to the new location; keeping file identifiers
         */
        MOVED,
        /**
         * Signals that files were copied to new location; new file identifiers
         */
        COPIED;
    }

    /**
     * The result for {@link FilestoreDataMover#copyFiles(Set, FileStorage, FileStorage, URI, URI)} invocation.
     */
    static class CopyResult {

        /** Signals what operation was performed to pass files to new file storage location */
        final Operation operation;

        /** The total size of all files passed to new file storage location; <code>0</code> in case {@link #operation} is {@link Operation#COPIED} */
        final long totalSize;

        /** A mapping for previous to new file name; <code>null</code> in case {@link #operation} is {@link Operation#MOVED} */
        final Map<String, String> prevFileName2newFileName;

        /** Reverts the previously copied files */
        final Reverter reverter;

        CopyResult(long totalSize, Reverter reverter) {
            super();
            operation = Operation.MOVED;
            this.totalSize = totalSize;
            prevFileName2newFileName = null;
            this.reverter = reverter;
        }

        CopyResult(Map<String, String> prevFileName2newFileName, Reverter reverter) {
            super();
            operation = Operation.COPIED;
            this.prevFileName2newFileName = prevFileName2newFileName;
            totalSize = 0L;
            this.reverter = reverter;
        }
    }

    /**
     * Used to revert previously copied files.
     */
    static interface Reverter {

        /**
         * Reverts the previously copied files.
         */
        void revertCopy() throws StorageException;
    }

    /**
     * Runs the specified task safely.
     *
     * @param task The task
     */
    protected static void runSafely(Runnable task) {
        if (null != task) {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.error("Unexpected exception while running task", e);
            }
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------

    protected static final class IncrementUsage {

        protected final long incrementUsage;
        protected final int ownerId;
        protected final int contextId;

        protected IncrementUsage(long usage, int ownerId, int contextId) {
            super();
            this.incrementUsage = usage;
            this.ownerId = ownerId;
            this.contextId = contextId;
        }
    }

    protected static IncrementUsage incUsage(long usage, int ownerId, int contextId) {
        return new IncrementUsage(usage, ownerId, contextId);
    }

    protected static final class DecrementUsage {

        protected final long decrementUsage;
        protected final int ownerId;
        protected final int contextId;

        protected DecrementUsage(long usage, int ownerId, int contextId) {
            super();
            this.decrementUsage = usage;
            this.ownerId = ownerId;
            this.contextId = contextId;
        }
    }

    protected static DecrementUsage decUsage(long usage, int ownerId, int contextId) {
        return new DecrementUsage(usage, ownerId, contextId);
    }

    /**
     * Increases/decreases the quota usages.
     *
     * @param incUsage The argument for increasing quota usage
     * @param decUsage The argument for decreasing quota usage
     * @throws StorageException If a database error occurs
     */
    protected static void changeUsage(DecrementUsage decUsage, IncrementUsage incUsage, int contextId) throws StorageException {
        Connection con = null;
        boolean rollback = false;
        try {
            con = ClientAdminThread.cache.getConnectionForContext(contextId);
            Databases.startTransaction(con);
            rollback = true;

            doDecUsage(decUsage.decrementUsage, decUsage.ownerId, contextId, con);
            doIncUsage(incUsage.incrementUsage, incUsage.ownerId, contextId, con);

            con.commit();
            rollback = false;
        } catch (PoolException e) {
            throw new StorageException(e);
        } catch (SQLException e) {
            throw new StorageException(e);
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);

            if (null != con) {
                try {
                    ClientAdminThread.cache.pushConnectionForContext(contextId, con);
                } catch (PoolException e) {
                    throw new StorageException(e);
                }
            }
        }
    }

    /**
     * Increases the quota usage.
     *
     * @param usage The value by which the quota is supposed to be increased
     * @throws StorageException If a database error occurs
     */
    private static void doIncUsage(long usage, int ownerId, int contextId, Connection con) throws StorageException {
        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        try {
            sstmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=? AND user=?");
            sstmt.setInt(1, contextId);
            sstmt.setInt(2, ownerId);
            rs = sstmt.executeQuery();
            final long oldUsage;
            if (rs.next()) {
                oldUsage = rs.getLong(1);
            } else {
                if (ownerId > 0) {
                    throw QuotaFileStorageExceptionCodes.NO_USAGE_USER.create(I(ownerId), I(contextId));
                }
                throw QuotaFileStorageExceptionCodes.NO_USAGE.create(I(contextId));
            }

            long newUsage = oldUsage + usage;
            ustmt = con.prepareStatement("UPDATE filestore_usage SET used=? WHERE cid=? AND user=?");
            ustmt.setLong(1, newUsage);
            ustmt.setInt(2, contextId);
            ustmt.setInt(3, ownerId);
            final int rows = ustmt.executeUpdate();
            if (rows == 0) {
                if (ownerId > 0) {
                    throw QuotaFileStorageExceptionCodes.UPDATE_FAILED_USER.create(I(ownerId), I(contextId));
                }
                throw QuotaFileStorageExceptionCodes.UPDATE_FAILED.create(I(contextId));
            }
        } catch (Exception s) {
            throw new StorageException(s);
        } finally {
            Databases.closeSQLStuff(rs);
            Databases.closeSQLStuff(sstmt);
            Databases.closeSQLStuff(ustmt);
        }
    }

    /**
     * Decreases the QuotaUsage.
     *
     * @param usage by that the Quota has to be decreased
     * @throws StorageException If a database error occurs
     */
    private static void doDecUsage(long usage, int ownerId, int contextId, Connection con) throws StorageException {
        PreparedStatement sstmt = null;
        PreparedStatement ustmt = null;
        ResultSet rs = null;
        try {
            sstmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=? AND user=?");
            sstmt.setInt(1, contextId);
            sstmt.setInt(2, ownerId);
            rs = sstmt.executeQuery();

            long oldUsage;
            if (rs.next()) {
                oldUsage = rs.getLong("used");
            } else {
                if (ownerId > 0) {
                    throw QuotaFileStorageExceptionCodes.NO_USAGE_USER.create(I(ownerId), I(contextId));
                }
                throw QuotaFileStorageExceptionCodes.NO_USAGE.create(I(contextId));
            }
            long newUsage = oldUsage - usage;

            if (newUsage < 0) {
                newUsage = 0;
                final OXException e = QuotaFileStorageExceptionCodes.QUOTA_UNDERRUN.create(I(ownerId), I(contextId));
                LOGGER.error("", e);
            }

            ustmt = con.prepareStatement("UPDATE filestore_usage SET used=? WHERE cid=? AND user=?");
            ustmt.setLong(1, newUsage);
            ustmt.setInt(2, contextId);
            ustmt.setInt(3, ownerId);

            int rows = ustmt.executeUpdate();
            if (1 != rows) {
                if (ownerId > 0) {
                    throw QuotaFileStorageExceptionCodes.UPDATE_FAILED_USER.create(I(ownerId), I(contextId));
                }
                throw QuotaFileStorageExceptionCodes.UPDATE_FAILED.create(I(contextId));
            }
        } catch (Exception s) {
            throw new StorageException(s);
        } finally {
            Databases.closeSQLStuff(rs);
            Databases.closeSQLStuff(sstmt);
            Databases.closeSQLStuff(ustmt);
        }
    }

}
