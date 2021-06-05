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
 * {@link SingleResourcePropertyMixin}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public abstract class SingleResourcePropertyMixin extends SinglePropertyMixin implements ResourcePropertyMixin {

    protected final String namespace;
    protected final String name;

    /**
     * Initializes a new {@link SingleResourcePropertyMixin}.
     *
     * @param namespace The namespace of the property
     * @param name The name of the property
     */
    public SingleResourcePropertyMixin(String namespace, String name) {
        super(namespace, name);
        this.namespace = namespace;
        this.name = name;
    }

    @Override
    public List<WebdavProperty> getAllProperties(WebdavResource resource) throws OXException {
        return super.getAllProperties();
    }

    @Override
    public WebdavProperty getProperty(WebdavResource resource, String namespace, String name) throws OXException {
        if (this.namespace.equals(namespace) && this.name.equals(name)) {
            return getProperty(resource);
        }
        return null;
    }

    @Override
    protected void configureProperty(WebdavProperty property) {
        throw new UnsupportedOperationException("unable to configure '" + namespace + '.' + name + "' without underlying WebDAV resource");
    }

    /**
     * Prepares a new, empty property with the target namespace and name.
     *
     * @param xml <code>true</code> to mark the property as xml content, <code>false</code>, otherwise
     * @return The prepared property
     */
    protected WebdavProperty prepareProperty(boolean xml) {
        WebdavProperty property = new WebdavProperty(namespace, name);
        property.setXML(xml);
        return property;
    }

    /**
     * Gets the WebdAV property.
     *
     * @param resource The WebDAV resource to get the property for
     * @return The property, or <code>null</code> if not available
     */
    protected abstract WebdavProperty getProperty(WebdavResource resource) throws OXException;

}
