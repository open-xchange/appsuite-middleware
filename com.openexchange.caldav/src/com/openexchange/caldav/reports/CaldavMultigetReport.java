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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.caldav.reports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.webdav.action.WebdavPropfindAction;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.xml.resources.ResourceMarshaller;

/**
 * A {@link CaldavMultigetReport} allows clients to retrieve properties of certain named resources. It is conceptually similar to a propfind.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CaldavMultigetReport extends WebdavPropfindAction {

    public static final String NAMESPACE = CaldavProtocol.CAL_NS.getURI();

    public static final String NAME = "calendar-multiget";

    public CaldavMultigetReport(Protocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest req, WebdavResponse res) throws WebdavProtocolException {
        final Element response = new Element("multistatus", DAV_NS);

        List<Namespace> namespaces = protocol.getAdditionalNamespaces();
        for (Namespace namespace : namespaces) {
            response.addNamespaceDeclaration(namespace);
        }

        final Document responseBody = new Document(response);

        boolean forceAllProp = false;
        Document requestBody = null;
        try {
            requestBody = req.getBodyAsDocument();
        } catch (JDOMException e) {
            forceAllProp = true;
        } catch (IOException e) {
            forceAllProp = true;
        }

        ResourceMarshaller marshaller = getMarshaller(req, forceAllProp, requestBody, null);

        List<WebdavPath> paths = getPaths(req, requestBody);

        List<Element> all = new ArrayList<Element>();

        for (WebdavPath webdavPath : paths) {
            List<Element> marshalled = marshaller.marshal(req.getFactory().resolveResource(webdavPath));
            all.addAll(marshalled);
        }

        response.addContent(all);

        try {
            res.setStatus(Protocol.SC_MULTISTATUS);
            res.setContentType("text/xml; charset=UTF-8");
            outputter.output(responseBody, res.getOutputStream());
        } catch (final IOException e) {
            // IGNORE
        }

    }

    private List<WebdavPath> getPaths(WebdavRequest req, Document requestBody) throws WebdavProtocolException {
        if (requestBody == null) {
            return Collections.emptyList();
        }

        List children = requestBody.getRootElement().getChildren("href", DAV_NS);
        if (children == null || children.isEmpty()) {
            return Collections.emptyList();
        }

        List<WebdavPath> paths = new ArrayList<WebdavPath>(children.size());
        int length = req.getURLPrefix().length();
        for (Object object : children) {
            Element href = (Element) object;
            String url = href.getText();
            url = url.substring(length);
            paths.add(((GroupwareCaldavFactory) req.getFactory()).decode(new WebdavPath(url)));
        }

        return paths;
    }

}
