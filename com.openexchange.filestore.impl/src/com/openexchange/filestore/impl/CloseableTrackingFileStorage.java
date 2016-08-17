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

import java.io.InputStream;
import java.util.Set;
import java.util.SortedSet;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.java.Streams;
import com.openexchange.marker.OXThreadMarkers;


/**
 * {@link CloseableTrackingFileStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class CloseableTrackingFileStorage implements FileStorage {

    private final FileStorage delegate;

    /**
     * Initializes a new {@link CloseableTrackingFileStorage}.
     */
    public CloseableTrackingFileStorage(FileStorage delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public String saveNewFile(InputStream file) throws OXException {
        try {
            return delegate.saveNewFile(file);
        } catch (OXException e) {
            if (indicatesConnectionClosed(e.getCause())) {
                // End of stream has been reached unexpectedly during reading input
                throw FileStorageCodes.CONNECTION_CLOSED.create(e.getCause(), new Object[0]);
            }
            throw e;
        } finally {
            Streams.close(file);
        }
    }

    @Override
    public InputStream getFile(String name) throws OXException {
        InputStream in = delegate.getFile(name);
        OXThreadMarkers.rememberCloseableIfHttpRequestProcessing(in);
        return in;
    }

    @Override
    public SortedSet<String> getFileList() throws OXException {
        return delegate.getFileList();
    }

    @Override
    public long getFileSize(String name) throws OXException {
        return delegate.getFileSize(name);
    }

    @Override
    public String getMimeType(String name) throws OXException {
        return delegate.getMimeType(name);
    }

    @Override
    public boolean deleteFile(String identifier) throws OXException {
        return delegate.deleteFile(identifier);
    }

    @Override
    public Set<String> deleteFiles(String[] identifiers) throws OXException {
        return delegate.deleteFiles(identifiers);
    }

    @Override
    public void remove() throws OXException {
        delegate.remove();
    }

    @Override
    public void recreateStateFile() throws OXException {
        delegate.recreateStateFile();
    }

    @Override
    public boolean stateFileIsCorrect() throws OXException {
        return delegate.stateFileIsCorrect();
    }

    @Override
    public long appendToFile(InputStream file, String name, long offset) throws OXException {
        try {
            return delegate.appendToFile(file, name, offset);
        } finally {
            Streams.close(file);
        }
    }

    @Override
    public void setFileLength(long length, String name) throws OXException {
        delegate.setFileLength(length, name);
    }

    @Override
    public InputStream getFile(String name, long offset, long length) throws OXException {
        InputStream in = delegate.getFile(name, offset, length);
        OXThreadMarkers.rememberCloseableIfHttpRequestProcessing(in);
        return in;
    }

    /**
     * Gets a value indicating whether the supplied exception cause indicates that the end of stream has been reached unexpectedly while
     * reading from the input or not.
     *
     * @param cause The cause to check
     * @return <code>true</code>, if an unexpected connection close is indicated by the cause, <code>false</code>, otherwise
     */
    private static boolean indicatesConnectionClosed(Throwable cause) {
        if (null != cause) {
            if (cause instanceof java.io.EOFException || cause instanceof java.util.concurrent.TimeoutException) {
                return true;
            }
            return indicatesConnectionClosed(cause.getCause());
        }
        return false;
    }

}
