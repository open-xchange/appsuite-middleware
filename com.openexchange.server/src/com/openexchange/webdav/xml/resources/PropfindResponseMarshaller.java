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
				if(p == null) {
				    if (false == brief) {
				        notFound.add(prop);
				    }
				} else {
					props.add(p);
				}
			} catch (final WebdavProtocolException e) {
				multistatus.addStatus(new WebdavStatusImpl<Iterable<WebdavProperty>>(e.getStatus(), e.getUrl(),Arrays.asList(prop)));
			} catch (final OXException e) {
                multistatus.addStatus(new WebdavStatusImpl<Iterable<WebdavProperty>>(500, new WebdavPath(), Arrays.asList(prop)));
            }
		}
		if(!props.isEmpty()) {
			multistatus.addStatus(new WebdavStatusImpl<Iterable<WebdavProperty>>(HttpServletResponse.SC_OK, resource.getUrl(), props));
		}
		if(!notFound.isEmpty()) {
			multistatus.addStatus(new WebdavStatusImpl<Iterable<WebdavProperty>>(HttpServletResponse.SC_NOT_FOUND, resource.getUrl(), notFound));
		}

		return multistatus;
	}

}
