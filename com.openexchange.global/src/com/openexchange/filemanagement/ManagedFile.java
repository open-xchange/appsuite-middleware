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
     * @return The URL to managed resource
     * @throws OXException If URL cannot be constructed
     */
    public String constructURL(Session session) throws OXException;

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
