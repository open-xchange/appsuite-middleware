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

import com.openexchange.exception.OXException;


/**
 * {@link ContentDisposition} - The Content-Disposition header.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public interface ContentDisposition extends ParameterizedMessagingHeader {

    /**
     * The constant for "inline" disposition.
     */
    public static final String INLINE = "inline";

    /**
     * The constant for "attachment" disposition.
     */
    public static final String ATTACHMENT = "attachment";

    /**
     * Applies given content disposition to this content disposition
     *
     * @param contentDisposition The content disposition to apply
     */
    public void setContentDispositio(final ContentDisposition contentDisposition);

    /**
     * Gets the disposition.
     *
     * @return The disposition
     * @see #INLINE
     * @see #ATTACHMENT
     */
    public String getDisposition();

    /**
     * Sets the disposition.
     *
     * @param disposition The disposition
     * @see #INLINE
     * @see #ATTACHMENT
     */
    public void setDisposition(final String disposition);

    /**
     * Sets <code>"filename"</code> parameter.
     *
     * @param filename The file name; e.g. "sometext.txt"
     */
    public void setFilenameParameter(final String filename);

    /**
     * Gets <code>"filename"</code> parameter.
     *
     * @return The <code>"filename"</code> parameter value or <code>null</code> if not present
     */
    public String getFilenameParameter();

    /**
     * Checks if <code>"filename"</code> parameter is present.
     *
     * @return <code>true</code> if <code>"filename"</code> parameter is present, <code>false</code> otherwise if absent
     */
    public boolean containsFilenameParameter();

    /**
     * Sets the Content-Disposition.
     *
     * @param contentDisposition The Content-Disposition as a string
     * @throws OXException If applying Content-Disposition fails
     */
    public void setContentDisposition(final String contentDisposition) throws OXException;

    /**
     * Checks if disposition is inline
     *
     * @return <code>true</code> if disposition is inline; otherwise <code>false</code>
     * @see #INLINE
     */
    public boolean isInline();

    /**
     * Checks if disposition is attachment
     *
     * @return <code>true</code> if disposition is attachment; otherwise <code>false</code>
     * @see #ATTACHMENT
     */
    public boolean isAttachment();

}
