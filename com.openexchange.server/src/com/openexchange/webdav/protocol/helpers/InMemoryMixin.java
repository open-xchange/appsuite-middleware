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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.WebdavProperty;


/**
 * {@link InMemoryMixin}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InMemoryMixin implements PropertyMixin {

    private final Map<String, WebdavProperty> properties = new HashMap<String, WebdavProperty>();

    private PropertyMixin mixin = null;

    @Override
    public List<WebdavProperty> getAllProperties() throws OXException {
        ArrayList<WebdavProperty> allProperties = new ArrayList<WebdavProperty>(properties.values());
        if (mixin != null) {
            allProperties.addAll(mixin.getAllProperties());
        }
        return allProperties;
    }

    @Override
    public WebdavProperty getProperty(String namespace, String name) throws OXException {
        WebdavProperty webdavProperty = properties.get(namespace+":"+name);
        if (webdavProperty == null && mixin != null) {
            return mixin.getProperty(namespace, name);
        }
        return webdavProperty;
    }

    public void setMixin(PropertyMixin mixin) {
        this.mixin = mixin;
    }

    public void setProperty(WebdavProperty property) {
        properties.put(property.getNamespace()+":"+property.getName(), property);
    }


}
