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

package com.openexchange.tools.file;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.openexchange.exception.OXException;
import com.openexchange.java.SizeKnowingInputStream;
import com.openexchange.java.Streams;
import com.openexchange.tools.stream.CountingOnlyInputStream;
import com.openexchange.tx.AbstractUndoable;
import com.openexchange.tx.UndoableAction;
import jonelo.jacksum.util.Service;

/**
 * {@link FileStreamAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class FileStreamAction extends AbstractUndoable implements UndoableAction {

    private static final String CHECKSUM_ALGORITHM = "MD5";

    private final com.openexchange.filestore.FileStorage storage;
    private final InputStream data;
    private String checksum;
    private long bytesRead;
    private final long sizeHint;
    private boolean calculateChecksum;

    /**
     * Initializes a new {@link FileStreamAction}.
     *
     * @param storage The storage to save the stream to
     * @param data The input stream
     * @param sizeHint A size hint about the expected stream length in bytes, or <code>-1</code> if unknown
     * @param calculateChecksum <code>true</code> to calculate a checksum for the saved data, <code>false</code>, otherwise
     */
    protected FileStreamAction(com.openexchange.filestore.FileStorage storage, InputStream data, long sizeHint, boolean calculateChecksum) {
        super();
        this.storage = storage;
        this.data = data;
        this.sizeHint = sizeHint;
        this.calculateChecksum = calculateChecksum;
    }

    protected abstract void store(com.openexchange.filestore.FileStorage storage, InputStream stream) throws OXException;

    protected abstract void store(com.openexchange.filestore.QuotaFileStorage storage, InputStream stream, long sizeHint) throws OXException;

    protected abstract void undo(com.openexchange.filestore.FileStorage storage) throws OXException;

    private void store(InputStream stream) throws OXException {
        if (0 < sizeHint && com.openexchange.filestore.QuotaFileStorage.class.isInstance(storage)) {
            store((com.openexchange.filestore.QuotaFileStorage) storage, stream, sizeHint);
        } else if (SizeKnowingInputStream.class.isInstance(stream) && com.openexchange.filestore.QuotaFileStorage.class.isInstance(storage)) {
            store((com.openexchange.filestore.QuotaFileStorage) storage, stream, ((SizeKnowingInputStream) stream).getSize());
        } else {
            store(storage, stream);
        }
    }

    @Override
    public void perform() throws OXException {
        CountingOnlyInputStream countingStream = null;
        DigestInputStream digestStream = null;
        try {
            countingStream = new CountingOnlyInputStream(data);
            if (calculateChecksum) {
                try {
                    digestStream = new DigestInputStream(countingStream, MessageDigest.getInstance(CHECKSUM_ALGORITHM));
                } catch (NoSuchAlgorithmException e) {
                    // okay, save without checksum instead
                }
            }
            if (null != digestStream) {
                store(digestStream);
                checksum = Service.format(digestStream.getMessageDigest().digest());
            } else {
                store(countingStream);
            }
            bytesRead = countingStream.getCount();
        } finally {
            Streams.close(countingStream, digestStream);
        }
    }

    @Override
    protected void undoAction() throws OXException {
        undo(storage);
    }

    /**
     * Gets the associated ID that was assigned by the file storage after the stream was processed.
     *
     * @return The file storage ID, or <code>null</code> if not available
     */
    public abstract String getFileStorageID();

    /**
     * Gets the number of read bytes after the stream was processed.
     *
     * @return The number of read bytes
     */
    public long getByteCount() {
        return bytesRead;
    }

    /**
     * Gets the calculated checksum after the stream was processed.
     *
     * @return The checksum, or <code>null</code> if not available
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Gets the calculateChecksum
     *
     * @return The calculateChecksum
     */
    public boolean isCalculateChecksum() {
        return calculateChecksum;
    }

    /**
     * Sets the calculateChecksum
     *
     * @param calculateChecksum The calculateChecksum to set
     */
    public void setCalculateChecksum(boolean calculateChecksum) {
        this.calculateChecksum = calculateChecksum;
    }

}
