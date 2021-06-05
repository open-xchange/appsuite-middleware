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

package com.openexchange.dav.actions;

import static com.openexchange.webdav.protocol.Protocol.DAV_NS;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Element;
import com.openexchange.dav.DAVProperty;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.util.Utils;
import com.openexchange.webdav.xml.resources.PropertiesMarshaller;

/**
 * {@link PROPPATCHAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class PROPPATCHAction extends DAVAction {

    /**
     * Initializes a new {@link PROPPATCHAction}.
     *
     * @param protocol The underlying WebDAV protocol
     */
    public PROPPATCHAction(DAVProtocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        /*
         * get resource & prepare multistatus response
         */
        WebdavResource resource = requireResource(request, WebdavResource.class);
        Element responseElement = new Element("response", DAV_NS);
        responseElement.addContent(new PropertiesMarshaller(request.getURLPrefix(), request.getCharset()).marshalHREF(request.getUrl(), resource.isCollection()));
        Element multistatusElement = prepareMultistatusElement();
        multistatusElement.addContent(responseElement);
        /*
         * process 'sets' & 'removes'
         */
        Element rootElement = requireRootElement(request, DAV_NS, "propertyupdate");
        for (Element proppatchElement : getProppatchSets(rootElement)) {
            responseElement.addContent(setProperty(resource, proppatchElement));
        }
        for (Element proppatchElement : getProppatchRemoves(rootElement)) {
            responseElement.addContent(removeProperty(resource, proppatchElement));
        }
        /*
         * save resource & send multistatus response
         */
        resource.save();
        sendMultistatusResponse(response, multistatusElement);
    }

    private static List<Element> getProppatchSets(Element rootElement) {
        List<Element> proppatchSets = new ArrayList<Element>();
        for (Element setElement : rootElement.getChildren("set", DAV_NS)) {
            for (Element propElement : setElement.getChildren("prop", DAV_NS)) {
                proppatchSets.addAll(propElement.getChildren());
            }
        }
        return proppatchSets;
    }

    private static List<Element> getProppatchRemoves(Element rootElement) {
        List<Element> proppatchSets = new ArrayList<Element>();
        for (Element setElement : rootElement.getChildren("remove", DAV_NS)) {
            for (Element propElement : setElement.getChildren("prop", DAV_NS)) {
                proppatchSets.addAll(propElement.getChildren());
            }
        }
        return proppatchSets;
    }

    private static Element setProperty(WebdavResource resource, Element proppatchElement) {
        Element propstatElement = new Element("propstat", DAV_NS);
        propstatElement.addContent(new Element("prop", DAV_NS).addContent(new Element(proppatchElement.getName(), proppatchElement.getNamespace())));
        int status = HttpServletResponse.SC_OK;
        try {
            resource.putProperty(new DAVProperty(proppatchElement));
        } catch (PreconditionException e) {
            status = e.getStatus();
            propstatElement.addContent(new Element("error", DAVProtocol.DAV_NS).addContent(e.getPreconditionElement()));
        } catch (WebdavProtocolException e) {
            status = e.getStatus();
        }
        propstatElement.addContent(new Element("status", DAV_NS).setText("HTTP/1.1 " + status + " " + Utils.getStatusString(status)));
        return propstatElement;
    }

    private static Element removeProperty(WebdavResource resource, Element proppatchElement) {
        Element propstatElement = new Element("propstat", DAV_NS);
        propstatElement.addContent(new Element("prop", DAV_NS).addContent(new Element(proppatchElement.getName(), proppatchElement.getNamespace())));
        int status = HttpServletResponse.SC_OK;
        try {
            resource.removeProperty(proppatchElement.getNamespaceURI(), proppatchElement.getName());
        } catch (PreconditionException e) {
            status = e.getStatus();
            propstatElement.addContent(new Element("error", DAVProtocol.DAV_NS).addContent(e.getPreconditionElement()));
        } catch (WebdavProtocolException e) {
            status = e.getStatus();
        }
        propstatElement.addContent(new Element("status", DAV_NS).setText("HTTP/1.1 " + status + " " + Utils.getStatusString(status)));
        return propstatElement;
    }

}
