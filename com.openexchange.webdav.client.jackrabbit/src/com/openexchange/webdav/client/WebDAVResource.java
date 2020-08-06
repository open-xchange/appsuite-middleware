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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.webdav.client;

import java.util.Date;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

/**
 * {@link WebDAVResource} - A generic WebDAV resource
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public interface WebDAVResource {

    /**
     * Gets the address of the resource
     *
     * @return The address
     */
    String getHref();

    /**
     * Returns whether or not the resource is a WebDAV collection.
     *
     * @return <code>True</code>, if the resource is a collection, <code>false</code> otherwise
     */
    boolean isCollection();

    /**
     * Returns a property
     *
     * @param name The name of the property
     * @return The property as {@link Element} , or null if the resource does not contain a property with the given name.
     */
    Element getProperty(QName name);

    /**
     * Returns a property value
     *
     * @param <T> The type of the value
     * @param name The name of the property
     * @param clazz The type of the value
     * @return The value of given property, or null if the resource does not contain a property with the given name.
     */
    <T> T getProperty(QName name, Class<T> clazz);

    /**
     *
     * Gets the display name
     *
     * @return The display name of the resource
     */
    String getDisplayName();

    /**
     *
     * Gets the creation date
     *
     * @return The creation date of the resource
     */
    Date getCreationDate();

    /**
     * Gets the modified date
     *
     * @return The modified date
     */
    Date getModifiedDate();

    /**
     *
     * Gets the ETag
     *
     * @return The ETag
     */
    String getEtag();

    /**
     * Gets the content-type
     *
     * @return The content-type
     */
    String getContentType();

    /**
     * Gets the content-length
     *
     * @return The content-length
     */
    Long getContentLength();

    /**
     * Gets the content-language
     *
     * @return The content-language
     */
    String getContentLanguage();
}
