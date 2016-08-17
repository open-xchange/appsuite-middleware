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
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;
import com.openexchange.dav.DAVProperty;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.webdav.action.WebdavMkcolAction;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.util.Utils;

/**
 * {@link ExtendedMKCOLAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class ExtendedMKCOLAction extends WebdavMkcolAction {

    private final DAVProtocol protocol;

    /**
     * Initializes a new {@link ExtendedMKCOLAction}.
     */
    public ExtendedMKCOLAction(DAVProtocol protocol) {
        super();
        this.protocol = protocol;
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        if (false == request.hasBody()) {
            super.perform(request, response);
            return;
        }
        /*
         * get targeted resource & check mkcol body
         */
        WebdavResource resource = request.getResource();
        if (null == resource) {
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(request.getUrl(), HttpServletResponse.SC_NOT_FOUND);
        }
        if (resource.exists()) {
            throw new PreconditionException(DAV_NS.getURI(), "resource-must-be-null", request.getUrl(), HttpServletResponse.SC_CONFLICT);
        }
        Document requestBody;
        try {
            requestBody = request.getBodyAsDocument();
        } catch (JDOMException | IOException e) {
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST, e);
        }
        if (null == requestBody || null == requestBody.getRootElement()) {
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        Element rootElement = requestBody.getRootElement();
        if (null == rootElement || false == DAV_NS.equals(rootElement.getNamespace()) ||
            false == "mkcol".equals(rootElement.getName())) {
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        response.setHeader("Cache-Control", "no-cache");
        /*
         * process PROPPATCHes
         */
        List<Element> proppatchSets = getProppatchSets(rootElement);
        for (Element proppatchElement : proppatchSets) {
            Entry<Integer, Element> propstat = setProperty(resource, proppatchElement);
            int status = propstat.getKey().intValue();
            if (HttpServletResponse.SC_OK != status) {
                /*
                 * fail request if any proppatch operation fails
                 */
                sendResponse(response, getMkcolResponse(propstat.getValue()), status);
                return;
            }
        }
        /*
         * create resource
         */
        resource.create();
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    private void sendResponse(WebdavResponse response, Element responseElement, int status) {
        Document responseBody = new Document(responseElement);
        try {
            response.setStatus(status);
            response.setContentType("text/xml; charset=UTF-8");
            new XMLOutputter().output(responseBody, response.getOutputStream());
        } catch (IOException e) {
            org.slf4j.LoggerFactory.getLogger(PROPFINDAction.class).warn("Error sending WebDAV response", e);
        }
    }

    private Element getMkcolResponse(Element failedPropstatElement) {
        /*
         * prepare extended mkcol-response
         */
        Element mkcolResponseElement = new Element("mkcol-response", DAV_NS.getURI());
        for (Namespace namespace : protocol.getAdditionalNamespaces()) {
            mkcolResponseElement.addNamespaceDeclaration(namespace);
        }
        return mkcolResponseElement.addContent(failedPropstatElement);
    }

    private List<Element> getProppatchSets(Element rootElement) {
        List<Element> proppatchSets = new ArrayList<Element>();
        for (Element setElement : rootElement.getChildren("set", DAV_NS)) {
            for (Element propElement : setElement.getChildren("prop", DAV_NS)) {
                proppatchSets.addAll(propElement.getChildren());
            }
        }
        return proppatchSets;
    }

    private Map.Entry<Integer, Element> setProperty(WebdavResource resource, Element proppatchElement) {
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
        return new AbstractMap.SimpleEntry<Integer, Element>(Integer.valueOf(status), propstatElement);
    }

}
