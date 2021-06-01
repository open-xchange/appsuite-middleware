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

import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.actions.PROPFINDAction;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Multistatus;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.WebdavStatus;
import com.openexchange.webdav.xml.resources.PropertiesMarshaller;
import com.openexchange.webdav.xml.resources.ResourceMarshaller;

/**
 * A {@link CaldavMultigetReport} allows clients to retrieve properties of certain named resources. It is conceptually similar to a propfind.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CaldavMultigetReport extends PROPFINDAction {

    public static final String NAMESPACE = CaldavProtocol.CAL_NS.getURI();
    public static final String NAME = "calendar-multiget";

    /**
     * Initializes a new {@link CaldavMultigetReport}.
     *
     * @param protocol The protocol
     */
    public CaldavMultigetReport(DAVProtocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        /*
         * get paths of requested resources
         */
        Document requestBody = requireRequestBody(request);
        List<WebdavPath> paths = getHrefPaths(request, requestBody.getRootElement());
        /*
         * resolve & marshal requested resources
         */
        ResourceMarshaller marshaller = getMarshaller(request, requireRequestBody(request));
        PropertiesMarshaller helper = new PropertiesMarshaller(request.getURLPrefix(), request.getCharset());
        Element multistatusElement = prepareMultistatusElement();
        WebdavFactory factory = request.getFactory();
        if (GroupwareCaldavFactory.class.isInstance(factory)) {
            /*
             * batch-resolve requested resources
             */
            Multistatus<WebdavResource> multistatus = ((GroupwareCaldavFactory) factory).resolveResources(paths);
            for (int statusCode : multistatus.getStatusCodes()) {
                for (WebdavStatus<WebdavResource> status : multistatus.toIterable(statusCode)) {
                    if (null != status.getAdditional()) {
                        multistatusElement.addContent(marshaller.marshal(status.getAdditional()));
                    } else {
                        multistatusElement.addContent(new Element("response", Protocol.DAV_NS)
                            .addContent(helper.marshalHREF(status.getUrl(), false))
                            .addContent(helper.marshalStatus(status.getStatus()))
                        );
                    }
                }
            }
        } else {
            /*
             * resolve each requested resource individually
             */
            for (WebdavPath path : paths) {
                try {
                    WebdavResource resource = request.getFactory().resolveResource(path);
                    multistatusElement.addContent(marshaller.marshal(resource));
                } catch (WebdavProtocolException e) {
                    multistatusElement.addContent(new Element("response", Protocol.DAV_NS)
                        .addContent(helper.marshalHREF(path, false))
                        .addContent(helper.marshalStatus(e.getStatus()))
                    );
                }
            }
        }
        /*
         * send multistatus response
         */
        sendMultistatusResponse(response, multistatusElement);
    }

}
