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

package com.openexchange.mail.mime.converters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.SharedInputStream;
import javax.mail.util.SharedFileInputStream;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.MimeCleanUp;

/**
 * {@link FileBackedMimeBodyPart} - The MIME message backed by a file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileBackedMimeBodyPart extends MimeBodyPart implements MimeCleanUp {

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
    private final File tempFile;

    /**
     * Initializes a new {@link FileBackedMimeBodyPart}.
     *
     * @throws IOException If an I/O error occurs
     * @throws MessagingException If message cannot be parsed
     */
    public FileBackedMimeBodyPart(File tempFile) throws MessagingException, IOException {
        super(new SharedFileInputStream(tempFile));
        this.tempFile = tempFile;
    }

    /**
     * Initializes a new {@link FileBackedMimeBodyPart}.
     *
     * @throws IOException If an I/O error occurs
     * @throws MessagingException If message cannot be parsed
     */
    public FileBackedMimeBodyPart(SharedInputStream sharedIn) throws MessagingException {
        super((InputStream) sharedIn);
        this.tempFile = null;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        cleanUp();
    }

    /**
     * Gets the temporary file
     *
     * @return The temporary file
     */
    public File getTempFile() {
        return tempFile;
    }

    @Override
    public final void cleanUp() {
        File tempFile = this.tempFile;
        if (null != tempFile) {
            tempFile.delete();
        }
    }

}
