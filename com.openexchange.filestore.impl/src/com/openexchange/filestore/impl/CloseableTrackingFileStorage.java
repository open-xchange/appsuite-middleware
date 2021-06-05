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

package com.openexchange.filestore.impl;

import static com.openexchange.filestore.FileStorages.indicatesConnectionClosed;
import java.io.InputStream;
import java.net.URI;
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
    public URI getUri() {
        return delegate.getUri();
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
    public void setFileLength(long length, String name) throws OXException {
        delegate.setFileLength(length, name);
    }

    @Override
    public InputStream getFile(String name, long offset, long length) throws OXException {
        InputStream in = delegate.getFile(name, offset, length);
        OXThreadMarkers.rememberCloseableIfHttpRequestProcessing(in);
        return in;
    }

}
