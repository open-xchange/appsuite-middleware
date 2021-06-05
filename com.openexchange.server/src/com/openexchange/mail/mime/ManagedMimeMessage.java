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

package com.openexchange.mail.mime;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.util.SharedByteArrayInputStream;
import javax.mail.util.SharedFileInputStream;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Streams;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link ManagedMimeMessage} - A {@link MimeMessage} backed by an array or file dependent on provided byte array's size.
 * <p>
 * Invoke {@link #cleanUp()} to release used resources immediately; otherwise they will be released if a specific idle time has elapsed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ManagedMimeMessage extends MimeMessage implements MimeCleanUp {

    private static final int DEFAULT_MAX_INMEMORY_SIZE = 1048576; // 1MB

    private static final int DEFAULT_BUFFER_SIZE = 131072; // 128KB

    private final Date receivedDate;

    private final Queue<Closeable> closeables;

    private volatile File file;

    /**
     * Initializes a new {@link ManagedMimeMessage}.
     *
     * @param session The session
     * @param file The RFC822 source file
     * @param receivedDate The optional received date
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If a messaging error occurs
     * @throws IOException If an I/O error occurs
     */
    private ManagedMimeMessage(MailMessage original, Date receivedDate) throws MessagingException, OXException, IOException {
        super(MimeDefaultSession.getDefaultSession());
        final File[] files = new File[1];
        final InputStream in = getInputStreamFor(original, files);
        parse(in);
        closeables = new ConcurrentLinkedQueue<Closeable>();
        closeables.add(in);
        this.file = files[0];
        this.receivedDate = receivedDate;
    }

    /**
     * Initializes a new {@link ManagedMimeMessage}.
     *
     * @param session The session
     * @param file The RFC822 source file
     * @throws MessagingException If a messaging error occurs
     * @throws IOException If an I/O error occurs
     */
    public ManagedMimeMessage(Session session, File file) throws MessagingException, IOException {
        this(session, file, new SharedFileInputStream(file, DEFAULT_BUFFER_SIZE), null);
    }

    /**
     * Initializes a new {@link ManagedMimeMessage}.
     *
     * @param session The session
     * @param file The RFC822 source file
     * @param receivedDate The optional received date
     * @throws MessagingException If a messaging error occurs
     * @throws IOException If an I/O error occurs
     */
    public ManagedMimeMessage(Session session, File file, Date receivedDate) throws MessagingException, IOException {
        this(session, file, new SharedFileInputStream(file, DEFAULT_BUFFER_SIZE), receivedDate);
    }

    private ManagedMimeMessage(Session session, File file, InputStream in, Date receivedDate) throws MessagingException {
        super(session, in);
        closeables = new ConcurrentLinkedQueue<Closeable>();
        closeables.add(in);
        this.file = file;
        this.receivedDate = receivedDate;
    }

    /**
     * Gets the associated file
     *
     * @return The file
     */
    public File getFile() {
        return this.file;
    }

    @Override
    public Date getReceivedDate() throws MessagingException {
        if (receivedDate == null) {
            return super.getReceivedDate();
        }

        return receivedDate;
    }

    @Override
    protected void finalize() throws Throwable {
        cleanUp();
        super.finalize();
    }

    /**
     * Cleans up this managed MIME message.
     */
    @Override
    public void cleanUp() {
        {
            Closeable closeable;
            while ((closeable = closeables.poll()) != null) {
                Streams.close(closeable);
            }
        }
        final File file = this.file;
        if (null != file) {
            try {
                file.delete();
            } catch (Exception e) {
                // Ignore
            } finally {
                this.file = null;
            }
        }
    }

    /*-
     * ######################################## Helpers ########################################
     */

    private static InputStream getInputStreamFor(MailMessage mail, File[] files) throws OXException, IOException {
        final long size = mail.getSize();
        if (size > 0 && size <= DEFAULT_MAX_INMEMORY_SIZE) {
            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
            mail.writeTo(out);
            out.flush();
            files[0] = null;
            return new SharedByteArrayInputStream(out.toByteArray());
        }
        // Unknown size or exceeds max. in-memory limit
        final ManagedFileManagement service = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class, true);
        final File file = service.newTempFile();
        files[0] = file;
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            mail.writeTo(out);
            out.flush();
        } finally {
            Streams.close(out);
        }
        return new SharedFileInputStream(file, DEFAULT_BUFFER_SIZE);
    }
}
