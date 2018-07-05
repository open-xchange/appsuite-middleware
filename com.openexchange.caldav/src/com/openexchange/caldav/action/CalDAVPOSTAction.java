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
                response.setHeader("ETag", updatedResource.getETag());
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
