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

package com.openexchange.carddav.reports;

import java.util.ArrayList;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import com.openexchange.carddav.CarddavProtocol;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.actions.PROPFINDAction;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.xml.resources.ResourceMarshaller;

/**
 * A {@link CaldavMultigetReport} allows clients to retrieve properties of certain named resources. It is conceptually similar to a propfind.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AddressbookMultigetReport extends PROPFINDAction {

    public static final String NAMESPACE = CarddavProtocol.CARD_NS.getURI();
    public static final String NAME = "addressbook-multiget";

    /**
     * Initializes a new {@link AddressbookMultigetReport}.
     *
     * @param protocol The protocol
     */
    public AddressbookMultigetReport(DAVProtocol protocol) {
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
        List<Element> elements = new ArrayList<Element>();
        ResourceMarshaller marshaller = getMarshaller(request, requestBody);
        for (WebdavPath webdavPath : paths) {
            elements.addAll(marshaller.marshal(request.getFactory().resolveResource(webdavPath)));
        }
        Element multistatusElement = prepareMultistatusElement();
        multistatusElement.addContent(elements);
        sendMultistatusResponse(response, multistatusElement);
    }

}
