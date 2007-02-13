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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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



package com.openexchange.tools.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.tools.file.FileStorageException.Code;

import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.tools.file.FileStorageException.Code;


/**
 * This class defines the interface to the file storage for persistantly keeping
 * files like documents, attachments and so on. All methods preimplemented for
 * the file storage rely on a depth of subdirectories and a number of entries
 * each directory can store. If you do not want to use the preimplemented
 * methods you have to overwrite them.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class FileStorage {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(FileStorage.class);

    /**
     * Class implementing the file storage.
     */
    private static Class< ? extends FileStorage> IMPL;

    /**
     * Default number of files or directories per directory.
     */
    protected static final int DEFAULT_FILES = 256;

    /**
     * Default depth.
     */
    protected static final int DEFAULT_DEPTH = 3;

    /**
     * Lock timeout.
     */
    protected static final int LOCK_TIMEOUT = 10000;

    /**
     * Name of the file for keeping the state of the file storage.
     */
    protected static final String STATEFILENAME = "state";

    /**
     * Number of entries per directory.
     */
    private final transient int entries;

    /**
     * Depth of directories.
     */
    private final transient int depth;

    /**
     * Constructor with more detailed parameters. This file storage can store
     * entries ^ depth files.
     * @param depth depth of sub directories for storing files.
     * @param entries number of entries per sub directory.
     * @throws IOException if a problem occurs while creating the file storage.
     */
    protected FileStorage(final Object... args)
        throws IOException {
        super();
        if (!(args[0] instanceof Integer)) {
            throw new IOException("First parameter is not an integer.");
        }
        if (!(args[1] instanceof Integer)) {
            throw new IOException("Second parameter is not an integer.");
        }
        depth = (Integer) args[0];
        entries = (Integer) args[1];
        if (depth < 1) {
            throw new IOException("Depth must be equal or greater than 1.");
        }
        if (entries < 1) {
            throw new IOException("Entries must be equal or greater than 1.");
        }
    }
    
    /**
     * Factory method.
     * @param initData data for initializing the file storage. First argument
     * has to be a java.io.File object pointing to the folder for the
     * filestorage.
     * @return a file storage implementation.
     * @throws IOException if the file storage implementation can't be
     * instanciated.
     */
    public static final FileStorage getInstance(final Object... initData)
        throws IOException {
        return getInstance(DEFAULT_DEPTH, DEFAULT_FILES, initData);
    }

    /**
     * Factory method.
     * @param depth Directory depth of the file storage.
     * @param entries Number of entries per sub directory.
     * @param initData data for initializing the file storage. First argument
     * has to be a java.io.File object pointing to the folder for the
     * filestorage.
     * @return a file storage implementation.
     * @throws IOException if the file storage implementation can't be
     * instanciated.
     */
    public static final FileStorage getInstance(final int depth,
        final int entries, final Object... initData)
        throws IOException {
        try {
            // Varargs sometimes cause strange looking code.
            final Object[] args = new Object[2 + initData.length];
            args[0] = depth;
            args[1] = entries;
            System.arraycopy(initData, 0, args, 2, initData.length);
            final Constructor< ? extends FileStorage> constructor = 
                getImplementation().getConstructor(Object[].class);
            final FileStorage retval = constructor.newInstance(
                new Object[] { args });
            retval.checkStorage();
            return retval;
        } catch (InstantiationException e) {
            final IOException exp = new IOException(
                "Can't instanciate file storage implementation.");
            exp.initCause(e);
            throw exp;
        } catch (IllegalAccessException e) {
            final IOException exp = new IOException(
                "Can't access file storage implementation.");
            exp.initCause(e);
            throw exp;
        } catch (SecurityException e) {
            final IOException exp = new IOException("Security violation while "
                + "accessing file storage implementation.");
            exp.initCause(e);
            throw exp;
        } catch (NoSuchMethodException e) {
            final IOException exp = new IOException("Can't find necessary "
                + "constructor in file storage implementation.");
            exp.initCause(e);
            throw exp;
        } catch (IllegalArgumentException e) {
            final IOException exp = new IOException("Illegal arguments for "
                + "constructor of file storage implementation.");
            exp.initCause(e);
            throw exp;
        } catch (InvocationTargetException e) {
            final IOException exp = new IOException(
                "Exception while instanciating file storage implementation.");
            exp.initCause(e);
            throw exp;
        } catch (FileStorageException e) {
            final IOException exp = new IOException(e.getMessage());
            exp.initCause(e);
            throw exp;
        }
    }

    private static Class<? extends FileStorage> getImplementation()
        throws FileStorageException {
        if (null == IMPL) {
//            final String className = ServerConfig.getProperty(
//                Property.FileStorageImpl);
//            if (null == className) {
//                throw new FileStorageException(Code.PROPERTY_MISSING,
//                    Property.FileStorageImpl.name());
//            }
//            final Class clazz;
//            try {
//                clazz = Class.forName(className);
//            } catch (ClassNotFoundException e) {
//                throw new FileStorageException(Code.CLASS_NOT_FOUND, className);
//            }
//            IMPL = (Class<? extends FileStorage>) clazz;
            IMPL = LocalFileStorage.class;
        }
        return IMPL;
    }

    /**
     * Gets a file from the file storage.
     * @param identifier identifier of the file.
     * @return an inputstream from that the file can be read once.
     * @throws IOException if an error occurs.
     */
    public InputStream getFile(final String identifier) throws IOException {
        return load(identifier);
    }

    /**
     * @return a complete list of files in this filestorage
     */
    public SortedSet<String> getFileList() throws IOException {
    	SortedSet<String> retval = new TreeSet<String>();

    	String nextentry = computeFirstEntry();

    	while (nextentry != null) {
            if (exists(nextentry)) {
            	retval.add(nextentry);	
            }
            nextentry = computeNextEntry(nextentry);
        }
    	return retval;
    }
    
    /**
     * @param identifier identifier of the file.
     * @return the file size of the file.
     * @throws IOException if an error occurs.
     */
    public long getFileSize(final String identifier) throws IOException {
        return length(identifier);
    }

    /**
     * @param identifier identifier of the file.
     * @return the mime type of the file.
     * @throws IOException if an error occurs.
     */
    public String getMimeType(final String identifier) throws IOException {
    	return type(identifier);
    }
    
    /**
     * Stores a new file in the file storage.
     * @param input the files data will be written from this input stream.
     * @return the identifier of the newly created file.
     * @throws IOException if an error occurs while storing the file.
     */
    public String saveNewFile(final InputStream input) throws IOException {
        String nextentry = null;
        lock(LOCK_TIMEOUT);
        State state = new State(load(STATEFILENAME));
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
            // No empty slot and no next free slot then scan for an unused slot.
            if (nextentry == null) {
                final Set unused = scanForUnusedEntries();
                if (unused.isEmpty()) {
                    throw new IOException("FileStorage is full.");
                }
                final Iterator iter = unused.iterator();
                nextentry = (String) iter.next();
                while (iter.hasNext()) {
                    state.addUnused((String) iter.next());
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
        save(STATEFILENAME, state.saveState());
        unlock();
        try {
            save(nextentry, input);
        } catch (IOException ie) {
            lock(LOCK_TIMEOUT);
            state = new State(load(STATEFILENAME));
            state.addUnused(nextentry);
            save(STATEFILENAME, state.saveState());
            unlock();
            throw ie;
        }
        return nextentry;
    }

    /**
     * Deletes a file in the FileStorage.
     * @param identifier identifier of the file to delete.
     * @return true if the file has been deleted successfully.
     * @throws IOException if an error occurs.
     */
    public boolean deleteFile(final String identifier) throws IOException {
        final boolean retval = delete(identifier);
        if (retval) {
            lock(LOCK_TIMEOUT);
            final State state = new State(load(STATEFILENAME));
            state.addUnused(identifier);
            save(STATEFILENAME, state.saveState());
            unlock();
        }
        return retval;
    }

    /**
     * Releases resources of this file storage.
     */
    public void close() {
        closeImpl();
    }

    /**
     * {@inheritDoc}
     */
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    /**
     * Recreates the state file of a storage no matter if it exists or not
     * @throws IOException if an error occurs.
     */
    public void recreateStateFile() throws IOException {
        lock(LOCK_TIMEOUT);
        LOG.info("Repairing.");
        final State state = repairState();
        save(STATEFILENAME, state.saveState());
        unlock();
    }
    
    /**
     * Checks the storage.
     * @throws IOException if an error occurs.
     */
    private void checkStorage() throws IOException {
        lock(LOCK_TIMEOUT);
        if (!exists(STATEFILENAME)) {
            LOG.info("Repairing.");
            final State state = repairState();
            save(STATEFILENAME, state.saveState());
        }
        unlock();
    }

    /**
     * Searches in the file storage for unused file slots.
     * @return unused file slots.
     * @throws IOException if an error occurs.
     */
    private Set<String> scanForUnusedEntries() throws IOException {
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
     * Computes the first entry in a FileStorage.
     * @return the first entry
     */
    private String computeFirstEntry() {
        final StringBuffer retval = new StringBuffer();
        for (int i = 0; i < depth; i++) {
            retval.append(formatName(0));
            retval.append(File.separator);
        }
        retval.delete(retval.length() - 1, retval.length());
        return retval.toString();
    }

    /**
     * Maximal length of name.
     */
    private transient int nameLength = -1;

    /**
     * Formats the entry name as a string. Names will bein the range 0 till
     * (entries - 1).
     * @param entry to format
     * @return formated entry name
     */
    private String formatName(final int entry) {
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
     * Computes the next entry in the FileStorage. Increasing is done the
     * following way:
     * <pre>
     * 00/00/00/00/00
     *  5  4  3  2  1
     * </pre>
     * @param identifier lastly created file
     * @return the successor of oldentry or null if the FileStorage is full.
     * @throws IOException if there is something wrong.
     */
    private String computeNextEntry(final String identifier)
        throws IOException {
        int[] entry = new int[depth];
        final StringTokenizer tokenizer = new StringTokenizer(identifier,
            File.separator);
        if (tokenizer.countTokens() != depth) {
            throw new IOException("Depth mismatch while computing next entry.");
        }
        int actualDepth = 0;
        while (tokenizer.hasMoreTokens()) {
            entry[actualDepth++] = Integer.parseInt(tokenizer.nextToken(), 16);
        }
        boolean uebertrag = true;
        for (actualDepth = depth - 1; actualDepth >= 0 && uebertrag;
            actualDepth--) {
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
     * Tries to recreate the state file. This is only a fast restore because it
     * determines only the next free slot in the FileStorage. This method
     * doesn't care about empty (deleted) slots.
     * @return a fastly repaired state object.
     * @throws IOException if checking for existing files throws an IOException.
     */
    private State repairState() throws IOException {
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

    /**
     * Deletes a file in the file storage.
     * @param name name of the file to delete.
     * @return <code>true</code> if the file can be deleted successfully,
     * <code>false</code> otherwise.
     * @throws IOException if an error occurs.
     */
    protected abstract boolean delete(String name) throws IOException;

    /**
     * Save the data for the input stream into the file storage under the given
     * name.
     * @param name name the data should get in the file storage.
     * @param input the data that should be stored.
     * @throws IOException if an error occurs.
     */
    protected abstract void save(String name, InputStream input)
        throws IOException;

    /**
     * Loads a file from the file storage.
     * @param name name of the file.
     * @return an inputstream from that the file can be read once.
     * @throws IOException if an error occurs.
     */
    protected abstract InputStream load(String name) throws IOException;

    /**
     * @param name name of the file.
     * @return the file size of the file.
     * @throws IOException if an error occurs.
     */
    protected abstract long length(String name) throws IOException;

    /**
     * @param name name of the file.
     * @return the mime type of the file.
     * @throws IOException if an error occurs.
     */
    protected abstract String type(String name) throws IOException;
    
    /**
     * This method returns if a file exists in the storage.
     * @param name name of the file.
     * @return <code>true</code> if the entry exists, <code>false</code>
     * otherwise.
     * @throws IOException if an error occurs while checking if the file exists.
     */
    protected abstract boolean exists(String name) throws IOException;

    /**
     * This method locks the file storage, so no other can destroy a file in the
     * file storage. If the storage is already locked by another thread this
     * method must block for the given time. If the storage is still locked
     * after the to wait time an IOException must be thrown.
     * @param timeout time to block if the storage is already locked.
     * @throws IOException if the locking fails or the storage is still locked
     * after the to wait time.
     */
    protected abstract void lock(long timeout) throws IOException;

    /**
     * This method unlocks the file storage.
     * @throws IOException if an error occurs.
     */
    protected abstract void unlock() throws IOException;

    /**
     * Closes temporary resources of the implementing file storage.
     */
    protected abstract void closeImpl();
}
