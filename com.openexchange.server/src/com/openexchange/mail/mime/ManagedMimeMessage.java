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
import javax.mail.Flags;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.util.SharedByteArrayInputStream;
import javax.mail.util.SharedFileInputStream;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.config.MailReloadable;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
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

    private static volatile Boolean managedCloneEnabled;

    private static boolean managedCloneEnabled() {
        Boolean tmp = managedCloneEnabled;
        if (null == tmp) {
            synchronized (ManagedMimeMessage.class) {
                tmp = managedCloneEnabled;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    tmp = Boolean.valueOf(service != null && service.getBoolProperty("com.openexchange.mail.mime.managedCloneEnabled", false));
                    managedCloneEnabled = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    static {
        MailReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                managedCloneEnabled = null;
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("com.openexchange.mail.mime.managedCloneEnabled");
            }
        });
    }

    /**
     * Creates file-backed clones of passed <tt>MailMessage</tt> instances.
     *
     * @param originals The <tt>MailMessage</tt> instances to clone
     * @return The file-backed clones of passed <tt>MailMessage</tt> instances
     * @throws OXException If an error occurs
     */
    public static MailMessage[] clone(final MailMessage[] originals) throws OXException {
        if (null == originals) {
            return null;
        }
        if (!managedCloneEnabled()) {
            return originals;
        }
        final int length = originals.length;
        if (length <= 0) {
            return new MailMessage[0];
        }
        final MailMessage[] ret = new MailMessage[length];
        for (int i = 0; i < length; i++) {
            ret[i] = clone(originals[i]);
        }
        return ret;
    }

    /**
     * Creates a file-backed clone of passed <tt>MailMessage</tt> instance.
     *
     * @param original The <tt>MailMessage</tt> instance to clone
     * @return The file-backed clone of passed <tt>MailMessage</tt> instance
     * @throws OXException If an error occurs
     */
    public static MailMessage clone(final MailMessage original) throws OXException {
        if (null == original) {
            return null;
        }
        if (!managedCloneEnabled()) {
            return original;
        }
        try {
            final ManagedMimeMessage mimeMessage = new ManagedMimeMessage(original, original.getReceivedDateDirect());
            // Apply flags to MIME message
            {
                MimeMessageConverter.parseMimeFlags(original.getFlags(), mimeMessage);
                Flags flags = null;
                if (original.containsColorLabel()) {
                    flags = new Flags();
                    flags.add(MailMessage.getColorLabelStringValue(original.getColorLabel()));
                }
                if (original.containsUserFlags()) {
                    if (null == flags) {
                        flags = new Flags();
                    }
                    final String[] userFlags = original.getUserFlags();
                    for (final String userFlag : userFlags) {
                        flags.add(userFlag);
                    }
                }
                if (null != flags) {
                    mimeMessage.setFlags(flags, true);
                }
            }
            // Convert to MailMessage instance
            final MailMessage retval = MimeMessageConverter.convertMessage(mimeMessage, false);
            if (original.containsFolder()) {
                retval.setFolder(original.getFolder());
            }
            final String mailId = original.getMailId();
            if (mailId != null) {
                retval.setMailId(mailId);
            }
            if (original.containsMsgref()) {
                retval.setMsgref(original.getMsgref());
            }
            // Remember ManagedMimeMessage for clean-up
            if (null != mimeMessage.file) {
                MailAccess.rememberMimeCleanUp(mimeMessage);
            }
            return retval;
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private static final int DEFAULT_MAX_INMEMORY_SIZE = 1048576; // 1MB

    private static final int DEFAULT_BUFFER_SIZE = 131072; // 128KB

    private final Date receivedDate;

    private final Queue<Closeable> closeables;

    private volatile ManagedFile managedFile;

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
    private ManagedMimeMessage(final MailMessage original, final Date receivedDate) throws MessagingException, OXException, IOException {
        super(MimeDefaultSession.getDefaultSession());
        final File[] files = new File[1];
        final InputStream in = getInputStreamFor(original, files);
        parse(in);
        closeables = new ConcurrentLinkedQueue<Closeable>();
        closeables.add(in);
        this.managedFile = null;
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
    public ManagedMimeMessage(final Session session, final File file) throws MessagingException, IOException {
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
    public ManagedMimeMessage(final Session session, final File file, final Date receivedDate) throws MessagingException, IOException {
        this(session, file, new SharedFileInputStream(file, DEFAULT_BUFFER_SIZE), receivedDate);
    }

    private ManagedMimeMessage(final Session session, final File file, final InputStream in, final Date receivedDate) throws MessagingException {
        super(session, in);
        closeables = new ConcurrentLinkedQueue<Closeable>();
        closeables.add(in);
        this.managedFile = null;
        this.file = file;
        this.receivedDate = receivedDate;
    }

    /**
     * Gets the associated file
     *
     * @return The file
     */
    public File getFile() {
        final File file = this.file;
        return null == file ? managedFile.getFile() : file;
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
        final ManagedFile managedFile = this.managedFile;
        if (null != managedFile) {
            try {
                final ManagedFileManagement management = getFileManagement();
                if (null != management) {
                    try {
                        management.removeByID(managedFile.getID());
                    } catch (final OXException e) {
                        // Ignore
                    }
                }
            } finally {
                this.managedFile = null;
            }
        }
        final File file = this.file;
        if (null != file) {
            try {
                file.delete();
            } catch (final Exception e) {
                // Ignore
            } finally {
                this.file = null;
            }
        }
    }

    /*-
     * ######################################## Helpers ########################################
     */

    private static ManagedFileManagement getFileManagement() {
        return ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
    }

    private static InputStream getInputStreamFor(final MailMessage mail, final File[] files) throws OXException, IOException {
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
             * Double CRLF found
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
         * Neither double CRLF nor double LF found
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
