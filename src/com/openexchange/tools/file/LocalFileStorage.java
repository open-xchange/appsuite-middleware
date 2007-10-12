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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.tools.file.FileStorageException.Code;

/**
 * File storage implementation storing the files on a local directory.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LocalFileStorage extends FileStorage {

	
	private static final Log LOG = LogFactory.getLog(LocalFileStorage.class);
    /**
     * This time will be waited between iterations of getting the lock.
     */
    private static final int RELOCK_TIME = 10;

    /**
     * Default buffer size.
     */
    private static final int DEFAULT_BUFSIZE = 1024;

    /**
     * Name of the lock file.
     */
    private static final String LOCK_FILENAME = ".lock";

    /**
     * Location of the storage.
     */
    private final File storage;

    /**
     * Default constructor.
     * @param initData data for initializing this file storage.
     * @throws FileStorageException if a problem occurs while creating the file
     * storage.
     */
    public LocalFileStorage(final Object... initData) throws FileStorageException {
        super(initData);
        if (!(initData[2] instanceof URI)) {
            throw new FileStorageException(FileStorageException.Code
                .INVALID_PARAMETER, Integer.valueOf(2), initData[2].getClass().getName());
        }
        final URI uri = (URI) initData[2];
        try {
            storage = new File(uri);
        } catch (final IllegalArgumentException e) {
            throw new FileStorageException(FileStorageException.Code
                .INSTANTIATIONERROR, e, uri);
        } catch (final NullPointerException e) {
            throw new FileStorageException(FileStorageException.Code
                .INSTANTIATIONERROR, e, uri);
        }
        if (!this.storage.exists() && !storage.mkdir()) {
            throw new FileStorageException(FileStorageException.Code
                .CREATE_DIR_FAILED, storage.getAbsolutePath());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
	protected InputStream load(final String name) throws FileStorageException {
        try {
            return new FileInputStream(new File(storage, name));
        } catch (final FileNotFoundException e) {
            throw new FileStorageException(FileStorageException.Code.IOERROR,
                e, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
	protected long length(final String name) {
        return new File(storage, name).length();
    }

    @Override
	protected String type(final String name) {
    	final MimetypesFileTypeMap map = new MimetypesFileTypeMap();
		return map.getContentType(new File(storage, name));
	}

	/**
     * {@inheritDoc}
     */
    @Override
	protected boolean exists(final String name) {
        return new File(storage, name).exists();
    }

    /**
     * {@inheritDoc}
     */
    @Override
	protected void closeImpl() {
        // Nothing to do.
    }

    /**
     * {@inheritDoc}
     */
    @Override
	protected boolean delete(final String name) {
        return new File(storage, name).delete();
    }

    /**
     * {@inheritDoc}
     */
    @Override
	protected void save(final String name, final InputStream input)
        throws FileStorageException {
        final File file = new File(storage, name);
        file.getParentFile().mkdirs();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            final byte[] buf = new byte[DEFAULT_BUFSIZE];
            int len = input.read(buf);
            while (len != -1) {
                fos.write(buf, 0, len);
                len = input.read(buf);
            }
        } catch (final FileNotFoundException e) {
            throw new FileStorageException(FileStorageException.Code.IOERROR,
                e, e.getMessage());
        } catch (final IOException e) {
            throw new FileStorageException(FileStorageException.Code.IOERROR,
                e, e.getMessage());
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (final IOException e) {
                    throw new FileStorageException(FileStorageException.Code
                        .IOERROR, e, e.getMessage());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void eliminate() throws FileStorageException {
        if (storage.exists() && !storage.delete()) {
            throw new FileStorageException(Code.NOT_ELIMINATED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
	protected void unlock() throws FileStorageException {
        final File lock = new File(storage, LOCK_FILENAME);
        if (!lock.delete()) {
        	if(lock.exists()) {
				LOG.error("Couldn't delete lock file : "+lock.getAbsolutePath()+". This will probably leave a stale lockfile behind rendering this filestorage unusable, delete in manually.");
			}
            throw new FileStorageException(FileStorageException.Code.UNLOCK);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
	protected void lock(final long timeout) throws FileStorageException {
        final File lock = new File(storage, LOCK_FILENAME);
        final long failTime = System.currentTimeMillis() + timeout;
        boolean created = false;
        do {
            try {
                created = lock.createNewFile();
            } catch (final IOException e) {
                // Try again to create the file.
            	if (LOG.isDebugEnabled()) {
            		LOG.debug(e.getMessage(), e);
            	}
            }
            if (!created) {
                try {
                    Thread.sleep(RELOCK_TIME);
                } catch (final InterruptedException e) {
                    // Won't be interrupted.
                	if (LOG.isErrorEnabled()) {
                		LOG.error(e.getMessage(), e);
                	}
                }
            }
        } while (!created && System.currentTimeMillis() < failTime);
        if (!created) {
        	LOG.error("Cannot create Lock file. Either there is a stale .lock file here "+lock.getAbsolutePath()+" or the filestore was used too long.");
            throw new FileStorageException(FileStorageException.Code.LOCK);
        }
    }

}
