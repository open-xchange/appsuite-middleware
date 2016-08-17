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

package com.openexchange.filestore.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.java.Streams;

/**
 * {@link LocalFileStorage} - A storage backed by a local path/directory.
 */
public class LocalFileStorage extends DefaultFileStorage {

    /** The logger constant */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LocalFileStorage.class);

    /**
     * Checks specified URI for being used to create an instance of {@code java.io.File}.
     *
     * @param uri The URI to check
     * @return An URI with a scheme equal to <code>"file"</code>
     */
    private static URI checkUri(URI uri) {
        if (null == uri) {
            return uri;
        }

        URI fileUri = uri;
        if (null == fileUri.getScheme()) {
            try {
                fileUri = new URI("file", fileUri.getUserInfo(), fileUri.getHost(), fileUri.getPort(), fileUri.getPath(), fileUri.getQuery(), fileUri.getFragment());
            } catch (URISyntaxException e) {
                // Cannot occur...
                throw new IllegalArgumentException("URI syntax error.", e);
            }
        }

        return fileUri;
    }

    /**
     * Default number of files or directories per directory.
     */
    public static final int DEFAULT_FILES = 256;

    /**
     * Default depth.
     */
    public static final int DEFAULT_DEPTH = 3;

    /**
     * Lock timeout.
     */
    private static final int LOCK_TIMEOUT = 10000;

    /**
     * Name of the file for keeping the state of the file storage.
     */
    private static final String STATEFILENAME = "state";

    /**
     * Number of entries per directory.
     */
    private final transient int entries = DEFAULT_FILES;

    /**
     * Depth of directories.
     */
    private final transient int depth = DEFAULT_DEPTH;

    /**
     * Whether the path to this file storage has already been created
     */
	private volatile boolean alreadyInitialized;

    /**
     * Default buffer size (64K).
     */
    private static final int DEFAULT_BUFSIZE = 65536;

    /**
     * Name of the lock file.
     */
    private static final String LOCK_FILENAME = ".lock";

    /**
     * This time will be waited between iterations of getting the lock.
     */
    private static final int RELOCK_TIME = 10;

    /**
     * Set of Filenames that will not appear in the getFileList
     */
    protected static final Set<String> SPECIAL_FILENAMES;

    static {
        final Set<String> tmp = new HashSet<String>();
        tmp.add(LOCK_FILENAME);
        tmp.add(STATEFILENAME);
        SPECIAL_FILENAMES = Collections.unmodifiableSet(tmp);
    }

    /**
     * Initializes a new {@link LocalFileStorage}.
     *
     * @param uri An URI denoting the storage's root path, which is absolute, hierarchical with a scheme equal to <code>"file"</code>, a non-empty path component, and undefined authority, query, and fragment components
     * @throws IllegalArgumentException If the preconditions on the URI parameter do not hold
     */
    public LocalFileStorage(URI uri) {
        this(new File(checkUri(uri)));
    }

    /**
     * Initializes a new {@link LocalFileStorage}.
     *
     * @param storage The storage's root path
     */
    public LocalFileStorage(File storage) {
        super(storage);
        alreadyInitialized = storage.exists();
    }

    /**
     * Constructor for subclassing to run tests. It should be removed an the subclass should implement the whole FileStorage interface.
     */
    protected LocalFileStorage() {
        this(assignStorage());
    }

    //Ugly workaround because someone insisted to make storage final:
	private static File assignStorage() {
		try {
        	return File.createTempFile("test-storage", "tmp");
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

    /**
     * Checks, if Statefile is correct. Especially if nextEntry is right and all Files really exist.
     *
     * @author Steffen Templin
     * @return True if Statefile is correct
     * @throws OXException
     */
    @Override
    public boolean stateFileIsCorrect() throws OXException {
        lock(LOCK_TIMEOUT);
        try {
            final State state = loadState();
            final Set<String> unusedEntries = getUnusedEntries();

            // Check if next Entry is free
            if (exists(state.getNextEntry())) {
                return false;
            }

            // Check if all unused Entries are really unused
            if (state.hasUnused()) {
                for (final String unused : unusedEntries) {
                    if (exists(unused)) {
                        return false;
                    }
                }
            }

            // Convert next Entry to decimals as Array fields
            final String previousEntry = getPreviousEntry(state.getNextEntry());
            final String[] tokens = previousEntry.split("/");
            final int[] parts = new int[tokens.length];

            for (int i = 0; i < tokens.length; i++) {
                final int val = Integer.parseInt(tokens[i], 16);
                parts[i] = val;
            }

            // Calculate backwards number of Files from the last file on
            int entryCount = parts[0];
            entryCount *= Math.pow(entries, (tokens.length - 1));
            for (int i = 1; i < tokens.length; i++) {
                entryCount += parts[i] * Math.pow(entries, (tokens.length - 1 - i));
            }
            entryCount += 1;

            while (state.hasUnused()) {
                unusedEntries.add(state.getUnused());
            }

            for (final String entry : unusedEntries) {
                state.addUnused(entry);
            }

            final int realEntries = getFileList().size() + unusedEntries.size();

            // Check if Statefile matches to the calculation
            return realEntries == entryCount;
        } finally {
            unlock();
        }
    }

    /**
     * Calculates the Entrypath of the file that is previous to identifier
     *
     * @param identifier
     * @return String of the previous File
     * @throws OXException
     */
    protected String getPreviousEntry(final String identifier) throws OXException {
        // Split Path String into Array and make ints from the hex-Strings
        final String[] tokens = identifier.split("/");
        final int[] parts = new int[tokens.length];

        int sum = 0;
        for (int i = 0; i < tokens.length; i++) {
            try {
                final int val = Integer.parseInt(tokens[i], 16);
                parts[i] = val;
                sum += val;
            } catch (final NumberFormatException n) {
                throw FileStorageCodes.NO_NUMBER.create(n);
            }
        }

        // Path is ../00/00/00/...
        if (sum == 0) {
            return null;
        }

        // Calculate the decrement
        int i = tokens.length - 1;
        boolean overflow = false;
        do {
            if (parts[i] == 0) {
                parts[i] = entries - 1;
                overflow = true;
                i--;
            } else {
                parts[i]--;
                overflow = false;
            }
        } while (overflow && i > -1);

        // Make ex String again from the new ints-array
        final String maxChars = Integer.toHexString(entries - 1);
        final int charCount = maxChars.length();

        final String vals[] = new String[parts.length];
        for (int j = 0; j < parts.length; j++) {
            vals[j] = Integer.toHexString(parts[j]);
            while (vals[j].length() < charCount) {
                vals[j] = "0" + vals[j];
            }
        }

        // Forms the Output String
        final StringBuilder retval = new StringBuilder();
        for (int j = 0; j < vals.length; j++) {
            retval.append('/');
            retval.append(vals[j]);
        }
        return retval.substring(1);
    }

    protected Set<String> getUnusedEntries() throws OXException {
        final State state = loadState();
        final SortedSet<String> unusedEntries = new TreeSet<String>();

        while (state.hasUnused()) {
            unusedEntries.add(state.getUnused());
        }

        return unusedEntries;
    }

    /**
     * Stores a new file in the file storage.
     *
     * @param input the files data will be written from this input stream.
     * @return the identifier of the newly created file.
     * @throws OXException if an error occurs while storing the file.
     */
    @Override
    public String saveNewFile(final InputStream input) throws OXException {
    	try {
            initialize();
            String nextentry = null;
            State state = null;
            lock(LOCK_TIMEOUT);
            try {
                state = loadState();
                // Look for an empty slot
                while (nextentry == null && state.hasUnused()) {
                    nextentry = state.getUnused();
                    if (exists(nextentry)) {
                        nextentry = null;
                    }
                }
                // If no empty slot can be found use the next free one.
                if (nextentry == null) {
                    nextentry = state.getNextEntry();
                    // Does the next entry exist already? Then calculate the next.
                    while (nextentry != null && exists(nextentry)) {
                        nextentry = computeNextEntry(nextentry);
                    }
                    // No empty slot and no next free slot then scan for an unused
                    // slot.
                    if (nextentry == null) {
                        final Set<String> unused = scanForUnusedEntries();
                        if (unused.isEmpty()) {
                            throw FileStorageCodes.STORE_FULL.create();
                        }
                        final Iterator<String> iter = unused.iterator();
                        nextentry = iter.next();
                        while (iter.hasNext()) {
                            state.addUnused(iter.next());
                        }
                    }
                    // Calculate next slot and store it.
                    final String savenextentry = computeNextEntry(nextentry);
                    if (savenextentry == null) {
                        state.setNextEntry(nextentry);
                    } else {
                        state.setNextEntry(savenextentry);
                    }
                }
                saveState(state);
            } finally {
                unlock();
            }
            try {
                save(nextentry, input);
            } catch (final OXException ie) {
                delete(new String[] { nextentry });
                lock(LOCK_TIMEOUT);
                try {
                    state = loadState();
                    state.addUnused(nextentry);
                    saveState(state);
                } finally {
                    unlock();
                }
                throw ie;
            }
            return nextentry;
        } finally {
            Streams.close(input);
        }
    }

    /**
     * @return a complete list of files in this filestorage
     */
    @Override
    public SortedSet<String> getFileList() {
        final SortedSet<String> allIds = new TreeSet<String>();
        listRecursively(allIds, "", storage);
        return allIds;
    }

    protected void listRecursively(final SortedSet<String> allIds, String prefix, final File file) {
        if (prefix.length() > 0 && !prefix.endsWith("/")) {
            prefix += "/";
        }
        if (SPECIAL_FILENAMES.contains(file.getName())) {
            // Skip
            return;
        }

        if (file.isFile()) {
            allIds.add(prefix + file.getName());
        } else {
            File[] files = file.listFiles(); // <-- Returns null if this abstract pathname does not denote a directory, or if an I/O error occurs.
            if (files != null) {
                for (File subfile : files) {
                    if (file.equals(storage)) {
                        listRecursively(allIds, "", subfile);
                    } else {
                        listRecursively(allIds, prefix + file.getName(), subfile);
                    }
                }
            }
        }
    }

    /**
     * Deletes a file in the FileStorage.
     *
     * @param identifier identifier of the file to delete.
     * @return true if the file has been deleted successfully.
     * @throws OXException if an error occurs.
     */
    @Override
    public boolean deleteFile(final String identifier) throws OXException {
        final boolean retval = delete(new String[] { identifier }).isEmpty();
        if (retval) {
            lock(LOCK_TIMEOUT);
            try {
                final State state = loadState();
                state.addUnused(identifier);
                saveState(state);
            } finally {
                unlock();
            }
        }
        return retval;
    }

    /**
     * Deletes a set of files in the FileStorage.
     *
     * @param identifier identifier of the files to delete.
     * @return a set of identifiers that could not be deleted.
     * @throws OXException if an error occurs.
     */
    @Override
    public Set<String> deleteFiles(final String[] identifiers) throws OXException {
        final Set<String> notDeleted = delete(identifiers);
        if (notDeleted.size() < identifiers.length) {
            lock(LOCK_TIMEOUT);
            try {
                final State state = loadState();
                for (final String identifier : identifiers) {
                    if (!notDeleted.contains(identifier)) {
                        state.addUnused(identifier);
                    }
                }
                saveState(state);
            } finally {
                unlock();
            }
        }
        return notDeleted;
    }

    /**
     * This method removes the complete FileStorage and its elements.
     *
     * @throws OXException if removing fails.
     */
    @Override
    public void remove() throws OXException {
        // Already initialized?
        if (!alreadyInitialized || !storage.exists()) {
            return;
        }
        lock(LOCK_TIMEOUT);
        eliminate();
        // no unlock here because everything is removed.
    }

    /**
     * Recreates the state file of a storage no matter if it exists or not
     *
     * @throws OXException if an error occurs.
     */
    @Override
    public void recreateStateFile() throws OXException {
        lock(LOCK_TIMEOUT);
        try {
            LOG.info("Repairing.");
            final State state = repairState();
            saveState(state);
        } finally {
            unlock();
        }
    }

    /**
     * Calculates the next free Entry of the Storage
     *
     * @param identifier last used Entry in the Storage
     * @return Path of the next free Entry as String
     * @throws OXException if an error occurs while calculating the next free Entry
     */
    protected String computeNextEntry(final String identifier) throws OXException {
        final int[] entry = new int[depth];
        final StringTokenizer tokenizer = new StringTokenizer(identifier, File.separator);
        if (tokenizer.countTokens() != depth) {
            throw FileStorageCodes.DEPTH_MISMATCH.create();
        }
        int actualDepth = 0;
        while (tokenizer.hasMoreTokens()) {
            try {
                entry[actualDepth++] = Integer.parseInt(tokenizer.nextToken(), 16);
            } catch (final NumberFormatException n) {
                throw FileStorageCodes.NO_NUMBER.create(n);
            }
        }
        boolean uebertrag = true;
        for (actualDepth = depth - 1; actualDepth >= 0 && uebertrag; actualDepth--) {
            entry[actualDepth]++;
            if (entry[actualDepth] == entries) {
                if (actualDepth == 0) {
                    return null;
                }
                entry[actualDepth] = 0;
            } else {
                uebertrag = false;
            }
        }
        final StringBuffer retval = new StringBuffer();
        for (actualDepth = 0; actualDepth < depth; actualDepth++) {
            retval.append(formatName(entry[actualDepth]));
            retval.append(File.separator);
        }
        retval.delete(retval.length() - 1, retval.length());
        return retval.toString();
    }

    /**
     * Scans the whole Storage and creates a list of unused Entries
     *
     * @return Set of free Entries
     * @throws OXException if an error occurs while scanning
     */
    protected Set<String> scanForUnusedEntries() throws OXException {
        final Set<String> unused = new HashSet<String>();
        String entry = computeFirstEntry();
        while (entry != null) {
            if (!exists(entry)) {
                unused.add(entry);
            }
            entry = computeNextEntry(entry);
        }
        return unused;
    }

    /**
     * Calculates the very first Entry in the Store based on the depth
     *
     * @return Path to the first Entry as String
     */
    protected String computeFirstEntry() {
        final StringBuffer retval = new StringBuffer();
        for (int i = 0; i < depth; i++) {
            retval.append(formatName(0));
            retval.append(File.separator);
        }
        retval.delete(retval.length() - 1, retval.length());
        return retval.toString();
    }

    /**
     * Checks if File or Folder exists
     *
     * @param name Name of the file that has to be checked
     * @return true if File or Folder exists
     * @throws OXException
     */
    protected boolean exists(String name) {
        return new File(storage, name).exists();
    }

    /**
     * Saves the <code>InputStream</code> to the Storage
     *
     * @param name The name of the file in which InputStream content is supposed to be saved
     * @param input The <code>InputStream</code> providing the content to save
     * @throws OXException If saving content fails
     */
    protected void save(String name, InputStream input) throws OXException {
        File file = new File(storage, name);

        // Ensure existence of parent directories
        {
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                synchronized (this) {
                    if (!mkdirs(parentDir)) {
                        throw FileStorageCodes.CREATE_DIR_FAILED.create(parentDir.getAbsolutePath());
                    }
                }
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            int len = DEFAULT_BUFSIZE;
            byte[] buf = new byte[len];
            for (int read; (read = input.read(buf, 0, len)) > 0;) {
                fos.write(buf, 0, read);
            }
            fos.flush();
        } catch (final IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } finally {
            Streams.close(fos);
        }
    }

    /**
     * Locks this file storage; waiting if necessary for at most the given time-out to acquire the lock.
     *
     * @param timeoutMillis The maximum time to wait
     * @throws OXException If lock attempt fails
     */
    protected void lock(long timeoutMillis) throws OXException {
        ensureStorageExists();

        File lock = new File(storage, LOCK_FILENAME);
        long maxLifeTime = 100 * timeoutMillis;
        long lastModified = lock.lastModified();
        if (lastModified > 0 && lastModified + maxLifeTime < System.currentTimeMillis()) {
            unlock();
            LOG.error("Deleting a very old stale lock file here {}. Assuming it has not been removed by a crashed/restarted application.", lock.getAbsolutePath());
        }

        long failTime = System.currentTimeMillis() + timeoutMillis;
        boolean created = false;
        IOException ioe = null;
        do {
            try {
                created = lock.createNewFile();
            } catch (final IOException e) {
                // Try again to create the file.
                ioe = e;
                LOG.debug("", e);
            }
            if (!created) {
                try {
                    Thread.sleep(RELOCK_TIME);
                } catch (final InterruptedException e) {
                    // Should not be interrupted.
                    // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                    Thread.currentThread().interrupt();
                    LOG.error("", e);
                }
            }
        } while (!created && System.currentTimeMillis() < failTime);

        // Check if orderly created
        if (!created) {
            throw null == ioe ? FileStorageCodes.LOCK.create(lock.getAbsolutePath()) : FileStorageCodes.LOCK.create(ioe, lock.getAbsolutePath());
        }
    }

    /**
     * Deletes the lock
     *
     * @throws OXException
     */
    protected void unlock() throws OXException {
        final File lock = new File(storage, LOCK_FILENAME);
        if (!lock.delete()) {
            if (lock.exists()) {
                LOG.error("Couldn't delete lock file: {}. This will probably leave a stale lockfile behind rendering this filestorage unusable, delete in manually.", lock.getAbsolutePath());
                throw FileStorageCodes.UNLOCK.create();
            }
        }
    }

    /**
     * Loads the state file.
     *
     * @return a successfully loaded state file.
     * @throws OXException if the state file cannot be loaded.
     */
    protected State loadState() throws OXException {
        try {
            return new State(load(STATEFILENAME));
        } catch (final OXException e) {
            delete(new String[] { STATEFILENAME });
            throw e;
        }
    }

    /**
     * Saves the state file.
     *
     * @param state state file to save.
     * @throws OXException if the saving fails.
     */
    protected void saveState(final State state) throws OXException {
        InputStream in = null;
        try {
            in = state.saveState();
            save(STATEFILENAME, in);
        } catch (final OXException e) {
            delete(new String[] { STATEFILENAME });
            throw e;
        } finally {
            Streams.close(in);
        }
    }

    /**
     * Loads the File
     *
     * @param name Name of the File
     * @return File as FileInputStream
     * @throws OXException
     */
    protected InputStream load(final String name) throws OXException {
        final File dataFile = new File(storage, name);
        if (!dataFile.exists()) {
            throw FileStorageCodes.FILE_NOT_FOUND.create(dataFile.getAbsoluteFile().getAbsolutePath());
        }
        try {
            return new FileInputStream(new File(storage, name));
        } catch (final FileNotFoundException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        }
    }

    /**
     * Maximal length of name.
     */
    private transient int nameLength = -1;

    /**
     * Formats the entry name as a string. Names will begin the range 0 till (entries - 1).
     *
     * @param entry to format
     * @return formated entry name
     */
    protected String formatName(final int entry) {
        if (nameLength == -1) {
            nameLength = Integer.toHexString(entries - 1).length();
        }
        final StringBuffer stbf = new StringBuffer(Integer.toHexString(entry));
        while (stbf.length() < nameLength) {
            stbf.insert(0, "0");
        }
        return stbf.toString();
    }

    /**
     * This method eliminates the complete storage of files including state files and parent directory. Before eliminating the storage, it
     * will be locked to exclude other instances throwing ugly errors.
     *
     * @throws OXException if eliminating fails.
     */
    protected void eliminate() throws OXException {
        if (storage.exists() && !delete(storage)) {
            throw FileStorageCodes.NOT_ELIMINATED.create();
        }
    }

    protected static final boolean delete(final File file) {
        boolean retval = true;
        if (file.isDirectory()) {
            for (final File sub : file.listFiles()) {
                retval &= delete(sub);
            }
            retval &= file.delete();
        } else {
            retval = file.delete();
        }
        return retval;
    }

    /**
     * Deletes a set of Files
     *
     * @param names The Filenames
     * @return Set of Files that could not be deleted
     */
    protected Set<String> delete(final String[] names) {
        final Set<String> notDeleted = new HashSet<String>();
        for (final String name : names) {
            if (!new File(storage, name).delete()) {
                notDeleted.add(name);
            }
        }
        return notDeleted;
    }

    /**
     * Tries to recreate the state file. This is only a fast restore because it determines only the next free slot in the FileStorage. This
     * method doesn't care about empty (deleted) slots.
     *
     * @return a fastly repaired state object.
     * @throws OXException if checking for existing files throws an IOException.
     */
    protected State repairState() throws OXException {
        String nextentry = computeFirstEntry();
        final State state = new State(depth, entries, nextentry);
        String previousentry = null;
        while (nextentry != null && exists(nextentry)) {
            previousentry = nextentry;
            nextentry = computeNextEntry(nextentry);
        }
        if (nextentry == null) {
            state.setNextEntry(previousentry);
        } else {
            state.setNextEntry(nextentry);
        }
        return state;
    }

    private boolean mkdirs(final File directory) {
        if (directory.exists()) {
            return true;
        }

        File parent = directory.getParentFile();
        if (!mkdirs(parent)) {
            return false;
        }

        if (directory.mkdir()) {
            // Successfully created
            return true;
        }

        // Only return false is directory does no exist although attempted to be created
        return directory.exists();
    }

    private void initialize() throws OXException {
        if (alreadyInitialized) {
            return;
        }

        synchronized (this) {
            if (!mkdirs(storage)) {
                throw FileStorageCodes.CREATE_DIR_FAILED.create(storage.getAbsolutePath());
            }
        }

        lock(LOCK_TIMEOUT);
        try {
            if (!exists(STATEFILENAME)) {
                LOG.info("Repairing {}", new File(storage, STATEFILENAME).getPath());
                final State state = repairState();
                saveState(state);
            }
        } finally {
            unlock();
        }

        alreadyInitialized = true;
    }

}
