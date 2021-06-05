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
 * {@link ContentType} - The Content-Type header.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public interface ContentType extends ParameterizedMessagingHeader {

    public String getPrimaryType();

    /**
     * Sets primary type
     */
    public void setPrimaryType(String primaryType);

    /**
     * @return sub-type
     */
    public String getSubType();

    /**
     * Sets sub-type
     */
    public void setSubType(String subType);

    /**
     * Gets the base type.
     *
     * @return The base type (e.g. text/plain)
     */
    public String getBaseType();

    /**
     * Sets base type (e.g. text/plain)
     *
     * @param baseType The base type
     * @throws OXException If base type cannot be set
     */
    public void setBaseType(String baseType) throws OXException;

    /**
     * Sets <code>"charset"</code> parameter
     */
    public void setCharsetParameter(String charset);

    /**
     * @return the <code>"charset"</code> parameter value or <code>null</code> if not present
     */
    public String getCharsetParameter();

    /**
     * @return <code>true</code> if <code>"charset"</code> parameter is present, <code>false</code> otherwise
     */
    public boolean containsCharsetParameter();

    /**
     * Sets <code>"name"</code> parameter
     *
     * @param filename The <code>"name"</code> parameter
     */
    public void setNameParameter(String filename);

    /**
     * @return the <code>"name"</code> parameter value or <code>null</code> if not present
     */
    public String getNameParameter();

    /**
     * @return <code>true</code> if <code>"name"</code> parameter is present, <code>false</code> otherwise
     */
    public boolean containsNameParameter();

    /**
     * Sets this content type to given content type.
     *
     * @param contentType The content type to apply
     * @throws OXException If content type cannot be set
     */
    public void setContentType(String contentType) throws OXException;

    /**
     * Sets this content type to given content type.
     *
     * @param contentType The content type to apply
     * @throws OXException If content type cannot be set
     */
    public void setContentType(ContentType contentType);

    /**
     * Checks if Content-Type's base type matches given wildcard pattern (e.g text/plain, text/* or text/htm*)
     *
     * @return <code>true</code> if Content-Type's base type matches given pattern, <code>false</code> otherwise
     */
    public boolean isMimeType(String pattern);

    /**
     * Checks if Content-Type's base type ignore-case starts with specified prefix.
     *
     * @param prefix The prefix
     * @return <code>true</code> if Content-Type's base type ignore-case starts with specified prefix; otherwise <code>false</code>
     * @throws IllegalArgumentException If specified prefix is <code>null</code>
     */
    public boolean startsWith(String prefix);

}
