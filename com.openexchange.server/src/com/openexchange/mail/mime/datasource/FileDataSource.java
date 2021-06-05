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

package com.openexchange.mail.mime.datasource;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.MimeType2ExtMap;

/**
 * {@link FileDataSource} - A simple {@link DataSource data source} that encapsulates a file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileDataSource implements DataSource {

    /**
     * Gets a file data source for specified input stream's data.
     *
     * @param inputStream The input stream providing binary data
     * @return The generated file data source (Content-Type and name still need to be set appropriately)
     * @throws IOException If an I/O error occurs
     */
    public static FileDataSource valueOf(InputStream inputStream) throws IOException {
        if (null == inputStream) {
            return null;
        }
        FileOutputStream fos = null;
        final File tmpFile;
        try {
            tmpFile = File.createTempFile("openexchange-fds-", null, ServerConfig.getTmpDir());
            tmpFile.deleteOnExit();
            final int bufLen = 8192;
            final byte[] buf = new byte[bufLen];
            fos = new FileOutputStream(tmpFile);
            for (int read = inputStream.read(buf, 0, bufLen); read > 0; read = inputStream.read(buf, 0, bufLen)) {
                fos.write(buf, 0, read);
            }
            fos.flush();
        } finally {
            if (null != fos) {
                closeQuietly(fos);
            }
            closeQuietly(inputStream);
        }
        return new FileDataSource(tmpFile);
    }

    private static void closeQuietly(Closeable closeable) {
        Streams.close(closeable);
    }

    private String contentType;

    private String name;

    private final File file;

    /**
     * Creates a FileDataSource from a File object. <i>Note: The file will not actually be opened until a method is called that requires the
     * file to be opened.</i>
     * <p>
     * Content type is initially set to "application/octet-stream".
     *
     * @param file The file
     */
    public FileDataSource(File file) {
        this(file, MimeType2ExtMap.getContentType(file.getName()));
    }

    /**
     * Creates a FileDataSource from a File object. <i>Note: The file will not actually be opened until a method is called that requires the
     * file to be opened.</i>
     *
     * @param file The file
     * @param contentType The content type
     */
    public FileDataSource(File file, String contentType) {
        super();
        this.file = file; // save the file Object...
        this.contentType = contentType == null ? "application/octet-stream" : contentType;
        name = file.getName();
    }

    /**
     * Creates a FileDataSource from the specified path name. <i>Note: The file will not actually be opened until a method is called that
     * requires the file to be opened.</i>
     * <p>
     * Content type is initially set to "application/octet-stream".
     *
     * @param name The system-dependent file name.
     */
    public FileDataSource(String name) {
        this(new File(name)); // use the file constructor
    }

    /**
     * Creates a FileDataSource from the specified path name. <i>Note: The file will not actually be opened until a method is called that
     * requires the file to be opened.</i>
     * <p>
     * Content type is initially set to "application/octet-stream".
     *
     * @param name The system-dependent file name.
     * @param contentType The content type
     */
    public FileDataSource(String name, String contentType) {
        this(new File(name), contentType); // use the file constructor
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(file);
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Return the file that corresponds to this FileDataSource.
     *
     * @return The file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Sets the content type.
     *
     * @param contentType The content type.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Sets the name (and implicitly content type).
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name == null ? file.getName() : name;
        this.contentType = MimeType2ExtMap.getContentType(this.name);
    }

}
