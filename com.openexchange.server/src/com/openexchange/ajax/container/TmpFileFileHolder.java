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

package com.openexchange.ajax.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link TmpFileFileHolder} - The {@link IFileHolder file holder} backed by a temporary {@link File file}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TmpFileFileHolder implements IFileHolder {

    private final File tmpFile;
    private long length;
    private String contentType;
    private String name;
    private String disposition;
    private String delivery;

    /**
     * Initializes a new {@link TmpFileFileHolder}.
     *
     * @throws OXException If initialization fails
     */
    public TmpFileFileHolder() throws OXException {
        super();
        tmpFile = newTempFile();
        length = -1L;
    }

    @Override
    public boolean repetitive() {
        return true;
    }

    @Override
    public void close() throws IOException {
        tmpFile.delete();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            close();
        } catch (final Exception ignore) {
            // Ignore
        }
    }

    /**
     * Gets the newly created file.
     *
     * @return The file
     */
    public File getTmpFile() {
        return tmpFile;
    }

    @Override
    public InputStream getStream() throws OXException {
        try {
            return new FileInputStream(tmpFile);
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisposition() {
        return disposition;
    }

    /**
     * Sets the length
     *
     * @param length The length to set
     */
    public void setLength(final long length) {
        this.length = length;
    }

    /**
     * Sets the content type
     *
     * @param contentType The content type to set
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    /**
     * Sets the name
     *
     * @param name The name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the disposition
     *
     * @param disposition The disposition to set
     */
    public void setDisposition(final String disposition) {
        this.disposition = disposition;
    }

    private static volatile File uploadDirectory;
    private static File uploadDirectory() {
        File tmp = uploadDirectory;
        if (null == tmp) {
            synchronized (TmpFileFileHolder.class) {
                tmp = uploadDirectory;
                if (null == tmp) {
                    tmp = new File(ServerConfig.getProperty(ServerConfig.Property.UploadDirectory));
                    uploadDirectory = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Creates a new empty file. If this method returns successfully then it is guaranteed that:
     * <ol>
     * <li>The file denoted by the returned abstract pathname did not exist before this method was invoked, and
     * <li>Neither this method nor any of its variants will return the same abstract pathname again in the current invocation of the virtual
     * machine.
     * </ol>
     *
     * @return An abstract pathname denoting a newly-created empty file
     * @throws OXException If a file could not be created
     */
    public static File newTempFile() throws OXException {
        try {
            final File tmpFile = File.createTempFile("open-xchange-", ".tmp", uploadDirectory());
            tmpFile.deleteOnExit();
            return tmpFile;
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Sets the delivery
     *
     * @param delivery The delivery to set
     */
    public void setDelivery(String delivery) {
        this.delivery = delivery;
    }

    @Override
    public String getDelivery() {
        return delivery;
    }

}
