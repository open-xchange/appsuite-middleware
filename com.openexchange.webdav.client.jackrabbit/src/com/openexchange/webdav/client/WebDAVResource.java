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
