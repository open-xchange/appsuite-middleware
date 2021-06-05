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

package com.openexchange.mail.compose.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.RandomAccessAttachment;
import com.openexchange.mail.compose.ClientToken;
import com.openexchange.session.Session;

/**
 * {@link AttachmentFileHolder} - A file holder backed by an attachment.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class AttachmentFileHolder implements IFileHolder {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AttachmentFileHolder.class);
    }

    private final Session session;
    private final Attachment attachment;
    private final CompositionSpaceService compositionSpaceService;
    private String delivery;
    private final List<Runnable> tasks;

    /**
     * Initializes a new {@link AttachmentFileHolder}.
     *
     * @param attachment The attachment
     * @param compositionSpaceService The composition space service
     */
    public AttachmentFileHolder(Attachment attachment, CompositionSpaceService compositionSpaceService, Session session) {
        super();
        this.attachment = attachment;
        this.compositionSpaceService = compositionSpaceService;
        this.session = session;
        tasks = new LinkedList<Runnable>();
    }

    /**
     * Safely deletes attachment
     */
    void deleteSafe() {
        try {
            compositionSpaceService.deleteAttachment(attachment.getCompositionSpaceId(), attachment.getId(), ClientToken.NONE);
        } catch (Exception e) {
            LoggerHolder.LOG.warn("Failed to delete non-existent attachment {} from composition space {}", UUIDs.getUnformattedString(attachment.getId()), UUIDs.getUnformattedString(attachment.getCompositionSpaceId()), e);
        }
    }

    /**
     * Sets the delivery
     *
     * @param delivery The delivery to set
     */
    public void setDelivery(final String delivery) {
        this.delivery = delivery;
    }

    @Override
    public boolean repetitive() {
        return true;
    }

    @Override
    public void close() throws IOException {
        // Nothing to do
    }

    @Override
    public InputStream getStream() throws OXException {
        try {
            return attachment.getData();
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_RESOURCE.equals(e)) {
                deleteSafe();
            }
            throw e;
        }
    }

    @Override
    public RandomAccess getRandomAccess() throws OXException {
        if (!(attachment instanceof RandomAccessAttachment)) {
            return null;
        }

        RandomAccessAttachment randomAccessAttachment = (RandomAccessAttachment) attachment;
        if (!randomAccessAttachment.supportsRandomAccess()) {
            return null;
        }

        return new AttachmentFileHolderRandomAccess(randomAccessAttachment, this);
    }

    @Override
    public long getLength() {
        return attachment.getSize();
    }

    @Override
    public String getContentType() {
        return attachment.getMimeType();
    }

    @Override
    public String getName() {
        return attachment.getName();
    }

    @Override
    public String getDisposition() {
        return null == attachment.getContentDisposition() ? null : attachment.getContentDisposition().getId();
    }

    @Override
    public String getDelivery() {
        return delivery;
    }

    @Override
    public List<Runnable> getPostProcessingTasks() {
        return tasks;
    }

    @Override
    public void addPostProcessingTask(Runnable task) {
        if (null != task) {
            tasks.add(task);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class AttachmentFileHolderRandomAccess implements IFileHolder.RandomAccess, IFileHolder.InputStreamClosure {

        private final AttachmentFileHolder parent;
        private final RandomAccessAttachment attachment;
        private long pos = 0;

        /**
         * Initializes a new {@link AttachmentFileHolderRandomAccess}.
         */
        AttachmentFileHolderRandomAccess(RandomAccessAttachment attachment, AttachmentFileHolder parent) {
            super();
            this.attachment = attachment;
            this.parent = parent;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            long count = attachment.getSize();
            if (pos >= count) {
                return -1;
            }

            int length = len;
            long avail = count - pos;
            if (length > avail) {
                length = (int) avail;
            }
            if (length <= 0) {
                return 0;
            }

            InputStream partialIn = null;
            try {
                partialIn = attachment.getData(pos, length);
                int read = partialIn.read(b, off, length);
                pos += read;
                return read;
            } catch (OXException e) {
                if (CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_RESOURCE.equals(e)) {
                    parent.deleteSafe();
                }
                Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                throw new IOException(null == cause ? e : cause);
            } finally {
                Streams.close(partialIn);
            }
        }

        @Override
        public void close() throws IOException {
            // Nothing
        }

        @Override
        public void seek(long pos) throws IOException {
            this.pos = pos;
        }

        @Override
        public long length() throws IOException {
            return attachment.getSize();
        }

        @Override
        public InputStream newStream() throws OXException, IOException {
            try {
                return attachment.getData();
            } catch (OXException e) {
                if (CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_RESOURCE.equals(e)) {
                    parent.deleteSafe();
                }
                throw e;
            }
        }
    }

}