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
