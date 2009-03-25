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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

/**
 * {@link ManagedFile} - Holds a file on disk with a time-out setting.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ManagedFile {

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
     * Gets the (optional) size.
     * 
     * @return The size
     */
    public long getSize();

    /**
     * Sets the (optional) size.
     * <p>
     * Note that size should already be set if created through managed file management.
     * 
     * @param size The size
     */
    public void setSize(long size);

    /**
     * Gets the backed file.
     * 
     * @return The backed file or <code>null</code> if already deleted.
     */
    public File getFile();

    /**
     * Gets this manages file's unique ID.
     * 
     * @return This manages file's unique ID
     */
    public String getID();

    /**
     * Gets the backed file's content as an input stream.
     * 
     * @return The backed file's content as an input stream or <code>null</code> if already deleted.
     * @throws ManagedFileException If file content cannot be returned as an input stream.
     */
    public InputStream getInputStream() throws ManagedFileException;

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
