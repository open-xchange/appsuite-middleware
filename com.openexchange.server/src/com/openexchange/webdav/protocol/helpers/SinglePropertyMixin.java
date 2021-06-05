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

import java.util.Collections;
import java.util.List;
import com.openexchange.webdav.protocol.WebdavProperty;


/**
 * {@link SinglePropertyMixin}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class SinglePropertyMixin implements PropertyMixin {

    private final String namespace;
    private final String name;

    public SinglePropertyMixin(String namespace, String name) {
        super();
        this.namespace = namespace;
        this.name = name;
    }

    @Override
    public List<WebdavProperty> getAllProperties() {
        return Collections.emptyList();
    }

    @Override
    public WebdavProperty getProperty(String namespace, String name) {
        if (this.namespace.equals(namespace) && this.name.equals(name)) {
            WebdavProperty property = new WebdavProperty(namespace, name);
            configureProperty(property);
            return property;
        }
        return null;
    }

    protected abstract void configureProperty(WebdavProperty property);

}
