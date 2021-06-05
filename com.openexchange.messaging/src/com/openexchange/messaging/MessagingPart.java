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

package com.openexchange.messaging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link MessagingPart} - A message part.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public interface MessagingPart {

    /**
     * The part should be presented as an attachment.
     */
    public static final String ATTACHMENT = "attachment";

    /**
     * The part should be presented inline.
     */
    public static final String INLINE = "inline";

    /**
     * Gets the headers as an unmodifiable {@link Map}.
     *
     * @return The headers as an unmodifiable {@link Map}.
     * @throws OXException If headers cannot be returned
     */
    public Map<String, Collection<MessagingHeader>> getHeaders() throws OXException;

    /**
     * Gets the header associated with specified name or <code>null</code> if not present
     *
     * @param name The header name
     * @return The header associated with specified name or <code>null</code> if not present
     * @throws OXException If header cannot be returned
     */
    public Collection<MessagingHeader> getHeader(String name) throws OXException;

    /**
     * Gets the first header value associated with specified name or <code>null</code> if not present
     *
     * @param name The header name
     * @return The first header value associated with specified name or <code>null</code> if not present
     * @throws OXException If header cannot be returned
     */
    public MessagingHeader getFirstHeader(String name) throws OXException;

    /**
     * Gets the disposition.
     * <p>
     * The disposition describes how the part should be presented (see RFC 2183). The return value should be compared case-insensitive. For
     * example:
     * <p>
     *
     * <pre>
     * String disposition = part.getDisposition();
     * if (disposition == null || MessagingPart.ATTACHMENT.equalsIgnoreCase(disposition))
     *  // treat as attachment if not first part
     * </pre>
     *
     * @return The disposition of this part, or null if unknown
     * @throws OXException If disposition cannot be returned
     * @see #ATTACHMENT
     * @see #INLINE
     */
    public String getDisposition() throws OXException;

    /**
     * Gets the <code>Content-Type</code> header of this part's content. <code>null</code> is returned if the <code>Content-Type</code>
     * header could not be determined.
     *
     * @return The <code>Content-Type</code> header of this part
     * @throws OXException If content type cannot be returned
     */
    public ContentType getContentType() throws OXException;

    /**
     * Get the size of this part in bytes. Return <code>-1</code> if the size cannot be determined.
     *
     * @return The size of this part or <code>-1</code>
     * @throws OXException If size cannot be returned
     */
    public long getSize() throws OXException;

    /**
     * Get the filename associated with this part, if possible.
     * <p>
     * Useful if this part represents an "attachment" that was loaded from a file. The filename will usually be a simple name, not including
     * directory components.
     *
     * @return The filename to associate with this part
     * @throws OXException If filename cannot be returned
     */
    public String getFileName() throws OXException;

    /**
     * Gets the section identifier.
     *
     * @return The section identifier or <code>null</code> if top level
     */
    public String getSectionId();

    /**
     * Gets the content.
     *
     * @return The content
     * @throws OXException If content cannot be returned
     */
    public MessagingContent getContent() throws OXException;

    /**
     * Writes this part's bytes to given output stream. The bytes are typically an aggregation of the headers and appropriately encoded
     * content bytes.
     * <p>
     * The bytes are typically used for transport.
     *
     * @exception IOException If an I/O error occurs
     * @exception OXException If an error occurs fetching the data to be written
     */
    public void writeTo(OutputStream os) throws IOException, OXException;

}
