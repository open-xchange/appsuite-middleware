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

import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.dav.DAVProperty;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.actions.DAVAction;
import com.openexchange.dav.resources.DAVCollection;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link MKCALENDARAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class MKCALENDARAction extends DAVAction {

    /**
     * Initializes a new {@link MKCALENDARAction}.
     *
     * @param protocol The underlying protocol
     */
    public MKCALENDARAction(Protocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse res) throws WebdavProtocolException {
        DAVCollection resource = requireResource(request, DAVCollection.class);
        if (resource.exists()) {
            // https://www.ietf.org/mail-archive/web/caldav/current/msg00123.html
            throw new PreconditionException(Protocol.DAV_NS.getURI(), "resource-must-be-null", request.getUrl(), HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        Document requestBody = optRequestBody(request);
        if (null != requestBody) {
            /*
             * process inline PROPPATCHes
             */
            Element rootElement = requestBody.getRootElement();
            if (null == rootElement || false == CaldavProtocol.CAL_NS.equals(rootElement.getNamespace()) || false == "mkcalendar".equals(rootElement.getName())) {
                throw WebdavProtocolException.generalError(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
            }
            for (Element element : rootElement.getChildren("set", Protocol.DAV_NS)) {
                for (Element prop : element.getChildren("prop", Protocol.DAV_NS)) {
                    for (Element propertyElement : prop.getChildren()) {
                        //                        if (request.getFactory().getProtocol().isProtected(propertyElement.getNamespaceURI(), propertyElement.getName())) {
                        //                            throw WebdavProtocolException.generalError(request.getUrl(), HttpServletResponse.SC_FORBIDDEN);
                        //                        } else {
                        //                            resource.putProperty(new DAVProperty(propertyElement));
                        //                        }
                        resource.putProperty(new DAVProperty(propertyElement));
                    }
                }
            }
        }
        /*
         * create resource & return appropriate response
         */
        resource.create();
        res.setStatus(HttpServletResponse.SC_CREATED);
    }

}
