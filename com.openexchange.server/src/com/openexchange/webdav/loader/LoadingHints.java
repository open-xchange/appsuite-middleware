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

package com.openexchange.webdav.loader;

import java.util.HashSet;
import java.util.Set;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;

public class LoadingHints {
	public static enum Property {ALL, SOME, NONE}

	private Property property;
	private final Set<WebdavProperty> properties = new HashSet<WebdavProperty>();
	private int depth;
	private WebdavPath url;
	private boolean loadLocks;

	public void setProps(final Property which) {
		this.property = which;
	}

	public Property getProps(){
		return property;
	}

	public void addProperty(final String namespaceURI, final String name) {
		properties.add(new WebdavProperty(namespaceURI, name));
	}

	public Set<WebdavProperty> getPropterties(){
		return properties;
	}

	public void setDepth(final int depth) {
		this.depth = depth;
	}

	public int getDepth(){
		return depth;
	}

	public void setUrl(final WebdavPath url) {
		this.url = url;
	}

	public WebdavPath getUrl(){
		return url;
	}

	public void loadLocks(final boolean b) {
		this.loadLocks = b;
	}

	public boolean getLoadLocks(){
		return loadLocks;
	}

    public boolean mustLoad(String namespace, String name) {
        return properties.contains(new WebdavProperty(namespace, name));
    }

    public boolean mustLoad(com.openexchange.webdav.protocol.Protocol.Property p) {
        return mustLoad(p.getNamespace(), p.getName());
    }

    public boolean loadOnly(Object...propertyDefinitions) {
        Set<WebdavProperty> propDefs = new HashSet<WebdavProperty>(propertyDefinitions.length);
        Object cache = null;
        for (Object o : propertyDefinitions) {
            if (String.class.isInstance(o)) {
                if (cache == null) {
                    cache = o;
                } else {
                    propDefs.add(new WebdavProperty((String) cache, (String) o));
                    cache = null;
                }
            }

            if (com.openexchange.webdav.protocol.Protocol.Property.class.isInstance(o)) {
                cache = null;

                com.openexchange.webdav.protocol.Protocol.Property p = (com.openexchange.webdav.protocol.Protocol.Property) o;
                propDefs.add(new WebdavProperty(p.getNamespace(), p.getName()));
            }
        }

        return properties.containsAll(propDefs);
    }

}
