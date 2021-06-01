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

package com.openexchange.caldav.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.query.Filter;
import com.openexchange.caldav.query.FilterParser;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.actions.PROPFINDAction;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.xml.resources.ResourceMarshaller;


/**
 * {@link CalendarQueryReport}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CalendarQueryReport extends PROPFINDAction {

    public static final String NAMESPACE = CaldavProtocol.CAL_NS.getURI();
    public static final String NAME = "calendar-query";

    /**
     * Initializes a new {@link CalendarQueryReport}.
     *
     * @param protocol The protocol
     */
    public CalendarQueryReport(DAVProtocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        Element multistatusElement = prepareMultistatusElement();
        Document requestBody = optRequestBody(request);
        ResourceMarshaller marshaller = getMarshaller(request, requestBody);
        List<Element> elements = new ArrayList<Element>();
        for (WebdavResource resource : getMatching(request, requestBody)) {
            elements.addAll(marshaller.marshal(resource));
        }
        multistatusElement.addContent(elements);
        sendMultistatusResponse(response, multistatusElement);
    }

    private List<WebdavResource> getMatching(final WebdavRequest req, final Document requestBody) throws WebdavProtocolException {
        if (requestBody == null) {
            return Collections.emptyList();
        }
        final Element filterDef = requestBody.getRootElement().getChild("filter", CaldavProtocol.CAL_NS);
        if (filterDef == null) {
            return Collections.emptyList();
        }

        final Filter filter = new FilterParser().parse(filterDef);
        if (false == FilteringResource.class.isInstance(req.getResource())) {
            throw new PreconditionException(DAVProtocol.CAL_NS.getURI(), "supported-filter", req.getUrl(), HttpServletResponse.SC_FORBIDDEN);
        }
        return ((FilteringResource) req.getResource()).filter(filter);
    }

}
