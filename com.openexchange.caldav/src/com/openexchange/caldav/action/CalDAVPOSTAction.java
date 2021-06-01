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

package com.openexchange.caldav.action;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Element;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.resources.EventResource;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.actions.POSTAction;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.xml.resources.PropfindResponseMarshaller;

/**
 * {@link CalDAVPOSTAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CalDAVPOSTAction extends POSTAction {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalDAVPOSTAction.class);

    protected final GroupwareCaldavFactory factory;

    /**
     * Initializes a new {@link CalDAVPOSTAction}.
     *
     * @param factory The factory
     */
    public CalDAVPOSTAction(GroupwareCaldavFactory factory) {
        super(factory.getProtocol());
	    this.factory = factory;
	}

	@Override
	public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
	    if (handle(request, response)) {
	        return;
	    }
        WebdavResource resource = request.getResource();
		if (null != request.getHeader("content-length")) {
			resource.setLength(new Long(request.getHeader("content-length")));
		}
		/*
		 * put request body
		 */
		try {
			resource.putBodyAndGuessLength(request.getBody());
		} catch (IOException e) {
			LOG.debug("Client Gone?", e);
		}
		/*
		 * write back response
		 */
		writeResource(resource, response);
	}

    @Override
    protected boolean handle(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        if ("split".equals(request.getParameter("action"))) {
            /*
             * handle special "split" action
             */
            EventResource eventResource = requireResource(request, EventResource.class);
            if (false == eventResource.exists()) {
                throw DAVProtocol.protocolException(request.getUrl(), HttpServletResponse.SC_NOT_FOUND);
            }
            WebdavPath splitComponentUrl = eventResource.split(request.getParameter("rid"), request.getParameter("uid"));
            if ("return=representation".equals(request.getHeader("Prefer"))) {
                /*
                 * render multistatus response upon success, yielding the representation of both resulting components
                 */
                WebdavResource updatedResource = factory.resolveResource(request.getUrl());
                WebdavResource newResource = factory.resolveResource(splitComponentUrl);
                setHeaderOpt("ETag", updatedResource.getETag(), true, response);
                PropfindResponseMarshaller marshaller = new PropfindResponseMarshaller(request.getURLPrefix(), request.getCharset(), request.isBrief())
                    .addProperty(DAVProtocol.DAV_NS.getURI(), "getetag")
                    .addProperty(DAVProtocol.CAL_NS.getURI(), "calendar-data")
                ;
                Element multistatusElement = prepareMultistatusElement()
                    .addContent(marshaller.marshal(updatedResource))
                    .addContent(marshaller.marshal(newResource))
                ;
                sendMultistatusResponse(response, multistatusElement);
            } else {
                /*
                 * return simple response with "Split-Component-URL" header, otherwise
                 */
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.setHeader("Split-Component-URL", splitComponentUrl.toString());
            }
            return true;
        }
        return super.handle(request, response);
    }

}
