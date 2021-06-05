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

package com.openexchange.webdav.xml.resources;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.Multistatus;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.WebdavStatusImpl;

public class PropfindResponseMarshaller extends PropertiesMarshaller implements ResourceMarshaller{

    private final Set<WebdavProperty> requestedProps = new HashSet<WebdavProperty>();
    private final boolean brief;

    /**
     * Initializes a new {@link PropfindResponseMarshaller}.
     *
     * @param uriPrefix The uri prefix
     * @param charset The charset
     * @param brief <code>true</code> to omit not found properties, <code>false</code>, otherwise
     */
	public PropfindResponseMarshaller(final String uriPrefix, final String charset, boolean brief) {
		super(uriPrefix, charset);
		this.brief = brief;
	}

    public PropfindResponseMarshaller addProperty(final String namespace, final String name) {
        return addProperty(new WebdavProperty(namespace, name));
    }

    /**
     * Adds a requested property for marshaling.
     *
     * @param property The property to add
     * @return A self reference
     */
    public PropfindResponseMarshaller addProperty(WebdavProperty property) {
        requestedProps.add(property);
        return this;
    }

	@Override
	protected Multistatus<Iterable<WebdavProperty>> getProps(final WebdavResource resource) {
		final Multistatus<Iterable<WebdavProperty>> multistatus =  new Multistatus<Iterable<WebdavProperty>>();
		final List<WebdavProperty> props = new LinkedList<WebdavProperty>();
		final List<WebdavProperty> notFound = new LinkedList<WebdavProperty>();
		for(final WebdavProperty prop : requestedProps) {
			try {
				final WebdavProperty p = resource.getProperty(prop);
				if (p == null) {
				    if (false == brief) {
				        notFound.add(prop);
				    }
				} else {
					props.add(p);
				}
			} catch (WebdavProtocolException e) {
				multistatus.addStatus(new WebdavStatusImpl<Iterable<WebdavProperty>>(e.getStatus(), e.getUrl(),Arrays.asList(prop)));
			} catch (OXException e) {
                multistatus.addStatus(new WebdavStatusImpl<Iterable<WebdavProperty>>(500, new WebdavPath(), Arrays.asList(prop)));
            }
		}
		if (!props.isEmpty()) {
			multistatus.addStatus(new WebdavStatusImpl<Iterable<WebdavProperty>>(HttpServletResponse.SC_OK, resource.getUrl(), props));
		}
		if (!notFound.isEmpty()) {
			multistatus.addStatus(new WebdavStatusImpl<Iterable<WebdavProperty>>(HttpServletResponse.SC_NOT_FOUND, resource.getUrl(), notFound));
		}

		return multistatus;
	}

}
