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
import com.openexchange.configuration.ServerConfig.Property;
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
    public static FileDataSource valueOf(final InputStream inputStream) throws IOException {
        if (null == inputStream) {
            return null;
        }
        FileOutputStream fos = null;
        final File tmpFile;
        try {
            tmpFile = File.createTempFile("openexchange-fds-", null, new File(ServerConfig.getProperty(Property.UploadDirectory)));
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

    private static void closeQuietly(final Closeable closeable) {
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
    public FileDataSource(final File file) {
        this(file, MimeType2ExtMap.getContentType(file.getName()));
    }

    /**
     * Creates a FileDataSource from a File object. <i>Note: The file will not actually be opened until a method is called that requires the
     * file to be opened.</i>
     *
     * @param file The file
     * @param contentType The content type
     */
    public FileDataSource(final File file, final String contentType) {
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
    public FileDataSource(final String name) {
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
    public FileDataSource(final String name, final String contentType) {
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
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    /**
     * Sets the name (and implicitly content type).
     *
     * @param name The name to set
     */
    public void setName(final String name) {
        this.name = name == null ? file.getName() : name;
        this.contentType = MimeType2ExtMap.getContentType(this.name);
    }

}
