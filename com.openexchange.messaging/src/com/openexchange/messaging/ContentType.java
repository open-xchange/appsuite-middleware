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
