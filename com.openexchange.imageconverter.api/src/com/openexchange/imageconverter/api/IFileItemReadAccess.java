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

package com.openexchange.imageconverter.api;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * {@link IFileItemReadAccess}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10.0
 */
public interface IFileItemReadAccess extends Closeable {

    /**
     * @return The {@link InputStream} to read from
     */
    public InputStream getInputStream();

    /**
     * Returns the input {@link File} to read from.</br>
     * A temporary file will be created, if
     * the chosen access option to acquire the {@link IFileItemReadAccess}
     * interface initially doesn't use a file to work on.</br>
     * For performance reasons, care should be taken to use the right access option
     * if planning to use the {@link getInputFile} method.</br>
     * If this method is called, a possibly previously used <code>InputStream</code>
     * object is closed and not usable anymore.</br>
     * In essence, it is best practice to initially decide for
     * {@link AccessOption.FILE_BASED} read access and use either
     * the file object or the stream obect to read content from throughout
     * the lifetime of this access object.
     *
     * @return The {@link File} containing
     *  the actual file content
     * @throws IOException
     */
    public File getInputFile() throws IOException;

    /**
     * @return The creation {@link Date} of the file as Gregorian calendar date
     */
    public Date getCreateDate();

    /**
     * @return The last access {@link Date} of the file in as Gregorian calendar date
     */
    public Date getModificationDate();

    /**
     * @return The length of the existing file item.
     */
    public long getLength();

    /**
     * Returns the value of the FileItem's property with the
     * given key. The key aliases need to be registered once
     * via the {
     *
     * @param key The key to retrieve the value for
     * @return
     */
    public String getKeyValue(final String key) throws FileItemException;
}
