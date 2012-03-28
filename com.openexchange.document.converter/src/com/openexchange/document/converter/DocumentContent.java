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

package com.openexchange.document.converter;

import java.io.File;
import java.io.InputStream;
import com.openexchange.exception.OXException;

/**
 * {@link DocumentContent} - The content of a document for input for or result from a conversion operation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface DocumentContent {

    /**
     * This method returns an <code>InputStream</code> representing the data and throws the appropriate exception if it can not do so. Note
     * that a new <code>InputStream</code> object must be returned each time this method is called, and the stream must be positioned at the
     * beginning of the data.
     *
     * @return An input stream
     * @throws OXException If input stream cannot be returned
     */
    public InputStream getInputStream() throws OXException;

    /**
     * Gets the optional file carrying the content provided by {@link #getInputStream()}.
     *
     * @return The file or <code>null</code>
     * @throws OXException If file cannot be returned
     */
    public File optFile() throws OXException;

    /**
     * This method returns the MIME type of the data in the form of a string. It should always return a valid type. It is suggested that
     * getContentType return "application/octet-stream" if the InputContent implementation can not determine the data type.
     *
     * @return The MIME Type
     */
    public String getContentType();

    /**
     * Return the <i>name</i> of this object where the name of the object is dependent on the nature of the underlying objects. InputContent
     * encapsulating files may choose to return the filename of the object. (Typically this would be the last component of the filename, not
     * an entire pathname.)
     *
     * @return The name
     */
    public String getName();

}
