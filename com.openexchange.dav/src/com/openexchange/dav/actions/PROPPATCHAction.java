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
