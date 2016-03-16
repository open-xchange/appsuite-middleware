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

package com.openexchange.mail.mime.converters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.SharedInputStream;
import javax.mail.util.SharedFileInputStream;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.MimeCleanUp;

/**
 * {@link FileBackedMimeMessage} - The MIME message backed by a file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileBackedMimeMessage extends MimeMessage implements MimeCleanUp {

    /**
     * Flushes passed input stream to denoted file
     *
     * @param in The input stream to write
     * @param tempFile The target file
     * @throws IOException If an I/O error occurs
     */
    public static void writeToFile(InputStream in, File tempFile) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tempFile);
            int len = 8192;
            byte[] buf = new byte[len];
            for (int read; (read = in.read(buf, 0, len)) > 0;) {
                out.write(buf, 0, read);
            }
            out.flush();
        } finally {
            Streams.close(out);
        }

    }

    // ------------------------------------------------------------------------------------------------------------------------

    /** The backing file */
    private File tempFile;

    /** The received date */
    private final Date receivedDate;

    /**
     * Initializes a new {@link FileBackedMimeMessage}.
     *
     * @throws IOException If an I/O error occurs
     * @throws MessagingException If message cannot be parsed
     */
    public FileBackedMimeMessage(Session session, File tempFile) throws MessagingException, IOException {
        this(session, tempFile, null);
    }

    /**
     * Initializes a new {@link FileBackedMimeMessage}.
     *
     * @throws IOException If an I/O error occurs
     * @throws MessagingException If message cannot be parsed
     */
    public FileBackedMimeMessage(Session session, File tempFile, Date receivedDate) throws MessagingException, IOException {
        super(session, new SharedFileInputStream(tempFile));
        this.tempFile = tempFile;
        this.receivedDate = receivedDate;
    }

    /**
     * Initializes a new {@link FileBackedMimeMessage}.
     *
     * @throws IOException If an I/O error occurs
     * @throws MessagingException If message cannot be parsed
     */
    public FileBackedMimeMessage(Session session, SharedInputStream sharedIn) throws MessagingException {
        this(session, sharedIn, null);
    }

    /**
     * Initializes a new {@link FileBackedMimeMessage}.
     *
     * @throws IOException If an I/O error occurs
     * @throws MessagingException If message cannot be parsed
     */
    public FileBackedMimeMessage(Session session, SharedInputStream sharedIn, Date receivedDate) throws MessagingException {
        super(session, (InputStream) sharedIn);
        this.tempFile = null;
        this.receivedDate = receivedDate;
    }

    @Override
    public Date getReceivedDate() throws MessagingException {
        return null == receivedDate ? super.getReceivedDate() : receivedDate;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        cleanUp();
    }

    /**
     * Gets the temp. file
     *
     * @return The temp. file
     */
    public File getTempFile() {
        return tempFile;
    }

    @Override
    public final synchronized void cleanUp() {
        File tempFile = this.tempFile;
        if (null != tempFile) {
            tempFile.delete();
            this.tempFile = null;
        }
    }

}
