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

package com.openexchange.webdav.protocol.helpers;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link ResourcePropertyMixin}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public interface ResourcePropertyMixin extends PropertyMixin {

    /**
     * Gets all WebDAV properties.
     *
     * @param resource The WebDAV resource to get all properties for, or <code>null</code> if invoked without corresponding resource
     * @return The properties, or an empty list if there are none
     */
    List<WebdavProperty> getAllProperties(WebdavResource resource) throws OXException;

    /**
     * Gets a specific WebDAV property.
     *
     * @param resource The WebDAV resource to get the property for, or <code>null</code> if invoked without corresponding resource
     * @param namespace The namespace of the requested property
     * @param name The name of the requested property
     * @return The property, or <code>null</code> if not available
     */
    WebdavProperty getProperty(WebdavResource resource, String namespace, String name) throws OXException;

}
