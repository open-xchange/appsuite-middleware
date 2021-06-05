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

package com.openexchange.filemanagement;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link ManagedFile} - Holds a file on disk with a time-out setting.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ManagedFile {

    /**
     * Constructs an URL to the resource held by this managed file.
     *
     * @param session The session
     * @param withRoute <code>true</code> to inject the load-balancing route information into generated URL; otherwise <code>false</code>
     * @return The URL to managed resource
     * @throws OXException If URL cannot be constructed
     */
    public String constructURL(Session session, boolean withRoute) throws OXException;

    /**
     * Gets the (optional) file name.
     *
     * @return The file name
     */
    public String getFileName();

    /**
     * Sets the (optional) file name.
     *
     * @param fileName The file name
     */
    public void setFileName(String fileName);

    /**
     * Gets the (optional) content type.
     *
     * @return The content type
     */
    public String getContentType();

    /**
     * Sets the (optional) content type.
     *
     * @param contentType The content type
     */
    public void setContentType(String contentType);

    /**
     * Gets the (optional) content disposition.
     *
     * @return The content disposition
     */
    public String getContentDisposition();

    /**
     * Sets the (optional) content disposition.
     *
     * @param contentType The content disposition
     */
    public void setContentDisposition(String contentDisposition);

    /**
     * Gets the (optional) size.
     *
     * @return The size
     */
    public long getSize();

    /**
     * Sets the (optional) size. <br>
     * <b><i>Note that size should already be set if created through managed file management.</i></b>
     *
     * @param size The size
     */
    public void setSize(long size);

    /**
     * Gets the (optional) affiliation identifier.
     *
     * @return The affiliation identifier or <code>null</code>
     */
    public String getAffiliation();

    /**
     * Sets the (optional) affiliation identifier.
     *
     * @param affiliation The affiliation identifier
     */
    public void setAffiliation(String affiliation);

    /**
     * Gets the backed file.
     *
     * @return The backed file or <code>null</code> if already deleted.
     */
    public File getFile();

    /**
     * Gets this managed file's unique ID.
     *
     * @return This managed file's unique ID
     */
    public String getID();

    /**
     * Gets the backed file's content as an input stream.
     *
     * @return The backed file's content as an input stream or <code>null</code> if already deleted.
     * @throws OXException If file content cannot be returned as an input stream.
     */
    public InputStream getInputStream() throws OXException;

    /**
     * Writes specified byte range into given output stream.
     *
     * @param out The output stream to write to
     * @param off The offset position to start reading from
     * @param len The number of bytes to write into output stream
     * @return The number of bytes written to output stream
     * @throws OXException If writing to output stream fails
     */
    public int writeTo(OutputStream out, int off, int len) throws OXException;

    /**
     * Gets last-access timestamp.
     *
     * @return The last-access timestamp
     */
    public long getLastAccess();

    /**
     * Touches this file's last access timestamp.
     */
    public void touch();

    /**
     * Manually removes file (from disk).
     */
    public void delete();

    /**
     * Checks if backed file has been deleted (in the meantime).
     *
     * @return <code>true</code> if backed file has been deleted (in the meantime); otherwise <code>false</code>
     */
    public boolean isDeleted();
}
