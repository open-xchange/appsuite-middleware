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
