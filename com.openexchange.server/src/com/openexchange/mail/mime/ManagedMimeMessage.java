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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mail.mime;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.mail.util.SharedFileInputStream;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link ManagedMimeMessage} - A {@link MimeMessage} backed by an array or file dependent on provided byte array's size.
 * <p>
 * Invoke {@link #cleanUp()} to release used resources immediately; otherwise they will be released if a specific idle time has elapsed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ManagedMimeMessage extends MimeMessage {

    private static final int DEFAULT_MAX_INMEMORY_SIZE = 131072; // 128KB

    private final List<Closeable> closeables;

    private volatile ManagedFile managedFile;

    private volatile File file;

    /**
     * Initializes a new {@link ManagedMimeMessage} with default in-memory size of 128KB.
     *
     * @param session The session
     * @param sourceBytes The RFC822 source bytes
     * @throws MessagingException If a messaging error occurs
     * @throws IOException If an I/O error occurs
     */
    public ManagedMimeMessage(final Session session, final byte[] sourceBytes) throws MessagingException, IOException {
        this(session, sourceBytes, DEFAULT_MAX_INMEMORY_SIZE);
    }

    /**
     * Initializes a new {@link ManagedMimeMessage}.
     *
     * @param session The session
     * @param sourceBytes The RFC822 source bytes
     * @param maxInMemorySize The max. in-memory size in bytes
     * @throws MessagingException If a messaging error occurs
     * @throws IOException If an I/O error occurs
     */
    public ManagedMimeMessage(final Session session, final byte[] sourceBytes, final int maxInMemorySize) throws MessagingException, IOException {
        super(session);
        if (0 > maxInMemorySize) {
            throw new IllegalArgumentException("maxInMemorySize is less than zero.");
        }
        closeables = new ArrayList<Closeable>(2);
        flags = new Flags(); // empty Flags object
        final byte[][] splitted = split(sourceBytes);
        headers =
            splitted[0].length == 0 ? new InternetHeaders() : new InternetHeaders(new UnsynchronizedByteArrayInputStream(splitted[0]));
        final byte[] contentBytes = splitted[1];
        if (contentBytes.length > maxInMemorySize) {
            final ManagedFileManagement management = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
            if (null == management) {
                content = contentBytes;
            } else {
                try {
                    managedFile = management.createManagedFile(contentBytes);
                    contentStream = new SharedFileInputStream(managedFile.getFile(), maxInMemorySize);
                } catch (final OXException e) {
                    throw new MessagingException(e.getMessage(), e);
                }
            }
        } else {
            content = contentBytes;
        }
        modified = false;
        saved = true;
    }

    /**
     * Initializes a new {@link ManagedMimeMessage}.
     *
     * @param session The session
     * @param managedFile The RFC822 managed file
     * @throws MessagingException If a messaging error occurs
     * @throws IOException If an I/O error occurs
     */
    public ManagedMimeMessage(final Session session, final ManagedFile managedFile) throws MessagingException, IOException {
        super(session);
        closeables = new ArrayList<Closeable>(2);
        this.file = null;
        this.managedFile = managedFile;
        final SharedFileInputStream sis = new SharedFileInputStream(managedFile.getFile(), DEFAULT_MAX_INMEMORY_SIZE);
        flags = new Flags(); // empty Flags object
        headers = createInternetHeaders(sis);
        contentStream = sis.newStream(sis.getPosition(), -1);
        modified = false;
        saved = true;
    }

    /**
     * Initializes a new {@link ManagedMimeMessage}.
     *
     * @param session The session
     * @param file The RFC822 source file
     * @throws MessagingException If a messaging error occurs
     * @throws IOException If an I/O error occurs
     */
    public ManagedMimeMessage(final Session session, final File file) throws MessagingException, IOException {
        this(session, file, new SharedFileInputStream(file, DEFAULT_MAX_INMEMORY_SIZE));
    }

    private ManagedMimeMessage(final Session session, final File file, final InputStream in) throws MessagingException {
        super(session, in);
        closeables = new ArrayList<Closeable>(2);
        closeables.add(in);
        this.managedFile = null;
        this.file = file;
    }

    /**
     * Gets the associated file
     * 
     * @return The file
     */
    public File getFile() {
        return null == file ? managedFile.getFile() : file;
    }

    @Override
    protected void finalize() throws Throwable {
        cleanUp();
        super.finalize();
    }

    /**
     * Cleans up this managed MIME message.
     */
    public void cleanUp() {
        while (!closeables.isEmpty()) {
            try {
                closeables.remove(0).close();
            } catch (final Exception e) {
                // Ignore
            }
        }
        if (null != managedFile) {
            try {
                final ManagedFileManagement management = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
                if (null != management) {
                    try {
                        management.removeByID(managedFile.getID());
                    } catch (final OXException e) {
                        // Ignore
                    }
                }
            } finally {
                managedFile = null;
            }
        }
        if (null != file) {
            try {
                file.delete();
            } catch (final Exception e) {
                // Ignore
            } finally {
                file = null;
            }
        }
    }

    /*-
     * ######################################## Helpers ########################################
     */

    private static final byte[] DOUBLE_CRLF = { '\r', '\n', '\r', '\n' };

    private static final byte[] DOUBLE_LF = { '\n', '\n' };

    private static final int[] COMPUTED_FAILURE_DOUBLE_CRLF;

    private static final int[] COMPUTED_FAILURE_DOUBL_LF;

    static {
        class TMP {

            int[] computeFailure(final byte[] pattern) {
                if (pattern == null) {
                    return null;
                }
                final int[] failure = new int[pattern.length];

                int j = 0;
                for (int i = 1; i < pattern.length; i++) {
                    while (j > 0 && pattern[j] != pattern[i]) {
                        j = failure[j - 1];
                    }
                    if (pattern[j] == pattern[i]) {
                        j++;
                    }
                    failure[i] = j;
                }
                return failure;
            }
        }
        final TMP tmp = new TMP();
        COMPUTED_FAILURE_DOUBLE_CRLF = tmp.computeFailure(DOUBLE_CRLF);
        COMPUTED_FAILURE_DOUBL_LF = tmp.computeFailure(DOUBLE_LF);
    }

    private static final byte[][] split(final byte[] sourceBytes) {
        if (null == sourceBytes) {
            return new byte[][] { new byte[] {}, new byte[] {} };
        }
        byte[] pattern = DOUBLE_CRLF;
        int pos = indexOf(sourceBytes, pattern, 0, COMPUTED_FAILURE_DOUBLE_CRLF);
        if (pos >= 0) {
            /*
             *  Double CRLF found
             */
            final byte[] a = new byte[pos];
            final int endPos = pos + DOUBLE_CRLF.length;
            final byte[] b = new byte[sourceBytes.length - endPos];
            System.arraycopy(sourceBytes, 0, a, 0, a.length);
            System.arraycopy(sourceBytes, endPos, b, 0, b.length);
            return new byte[][] { a, b };
        }
        pattern = DOUBLE_LF;
        pos = indexOf(sourceBytes, pattern, 0, COMPUTED_FAILURE_DOUBL_LF);
        if (pos >= 0) {
            /*
             * Double LF found
             */
            final byte[] a = new byte[pos];
            final int endPos = pos + DOUBLE_LF.length;
            final byte[] b = new byte[sourceBytes.length - endPos];
            System.arraycopy(sourceBytes, 0, a, 0, a.length);
            System.arraycopy(sourceBytes, endPos, b, 0, b.length);
            return new byte[][] { a, b };
        }
        /*
         *  Neither double CRLF nor double LF found
         */
        return new byte[][] { new byte[] {}, sourceBytes };
    }

    private static int indexOf(final byte[] data, final byte[] pattern, final int beginIndex, final int[] failure) {
        final int length = data.length;
        if ((beginIndex < 0) || (beginIndex > length)) {
            throw new IndexOutOfBoundsException(String.valueOf(beginIndex));
        }

        int j = 0;
        if (length == 0) {
            return -1;
        }

        for (int i = beginIndex; i < length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

}
