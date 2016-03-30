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

package com.openexchange.tools.file;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import jonelo.jacksum.util.Service;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.tools.stream.CountingInputStream;
import com.openexchange.tx.AbstractUndoable;
import com.openexchange.tx.UndoableAction;

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
            store((com.openexchange.filestore.QuotaFileStorage)storage, stream, sizeHint);
        } else {
            store(storage, stream);
        }
    }

    @Override
    public void perform() throws OXException {
        CountingInputStream countingStream = null;
        DigestInputStream digestStream = null;
        try {
            countingStream = new CountingInputStream(data, -1);
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
