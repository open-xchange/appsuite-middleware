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

import java.util.LinkedList;
import java.util.List;
import org.jdom2.Element;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

public class RecursiveMarshaller implements ResourceMarshaller {

	private final PropertiesMarshaller delegate;
	private final int depth;
	private final int limit;

    /**
     * Initializes a new {@link RecursiveMarshaller}.
     *
     * @param delegate The underlying properties marshaller
     * @param depth The depth as requested by the client
     * @param limit The maximum number of elements to marshall
     */
    public RecursiveMarshaller(PropertiesMarshaller delegate, int depth, int limit) {
        super();
        this.delegate = delegate;
        this.depth = depth;
        this.limit = limit;
    }

	@Override
    public List<Element> marshal(WebdavResource resource) throws WebdavProtocolException  {
		List<Element> elements = new LinkedList<Element>();
		elements.addAll(delegate.marshal(resource));
		if (resource.isCollection()) {
		    for (WebdavResource childResource : resource.toCollection().toIterable(depth)) {
		        elements.addAll(delegate.marshal(childResource));
		        if (elements.size() > limit) {
		            elements.add(getInsufficientStorageResponse(resource));
                    break;
		        }
            }
		}
		return elements;
	}

	/**
	 * Constructs a response element indicating a <code>HTTP/1.1 507 Insufficient Storage</code> error due to too many child resources
	 * of the parent resource.
	 *
	 * @param resource The parent resource whose children count exceeded the limit
	 * @return The respons element
	 */
	private Element getInsufficientStorageResponse(WebdavResource resource) {
        Element response = new Element("response", Protocol.DAV_NS);
        response.addContent(delegate.marshalHREF(resource.getUrl(), resource.isCollection()));
        Element status = delegate.marshalStatus(507);
        status.setText("HTTP/1.1 507 Insufficient Storage");
        response.addContent(status);
        Element error = new Element("error", Protocol.DAV_NS);
        error.addContent(new Element("number-of-matches-within-limits", Protocol.DAV_NS));
        response.addContent(error);
	    return response;
	}

}
