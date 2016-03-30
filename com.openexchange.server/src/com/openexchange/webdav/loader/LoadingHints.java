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
