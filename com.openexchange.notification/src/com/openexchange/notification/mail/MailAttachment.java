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

package com.openexchange.notification.mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * {@link MailAttachment}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface MailAttachment extends AutoCloseable {

    /**
     * Closes this mail attachment and releases any system resources associated with it. If the attachment is already closed, then invoking
     * this method has no effect.
     *
     * @throws Exception If close attempt fails
     */
    @Override
    void close() throws Exception;

    /**
     * Gets the content's input stream.
     * <p>
     * <b>Note</b>: The {@link #close()} method is supposed being invoked in a wrapping <code>try-finally</code> block.
     *
     * @return The input stream
     * @throws IOException If input stream cannot be returned
     */
    InputStream getStream() throws IOException;

    /**
     * Gets the content's length.
     *
     * @return The content length or <code>-1</code> if unknown
     */
    long getLength();

    /**
     * Gets the content type.
     *
     * @return The content type or <code>null</code> if unknown
     */
    String getContentType();

    /**
     * Sets the content type.
     *
     * @param contentType The content type to set
     */
    void setContentType(String contentType);

    /**
     * Gets the name.
     *
     * @return The name or <code>null</code> if unknown
     */
    String getName();

    /**
     * Sets the name.
     *
     * @param name The name to set
     */
    void setName(String name);

    /**
     * Gets the (optional) disposition.
     *
     * @return The disposition or <code>null</code>
     */
    String getDisposition();

    /**
     * Sets the disposition; <code>"inline"</code> or <code>"attachment"</code>.
     *
     * @param disposition The disposition to set
     */
    void setDisposition(String disposition);

    /**
     * Gets additional headers associated with this mail attachment
     *
     * @return The headers or <code>null</code>
     */
    Map<String, String> getHeaders();

    /**
     * Sets specified header.
     * <p>
     * A <code>null</code> value removes the header.
     *
     * @param name The name
     * @param value The value
     */
    void setHeader(String name, String value);

    /**
     * Adds specified headers
     *
     * @param headers The headers to add
     */
    void addHeaders(Map<? extends String, ? extends String> headers);

}
