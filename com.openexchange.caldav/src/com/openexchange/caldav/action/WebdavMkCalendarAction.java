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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.io.Writer;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.XMLOutputter;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.java.Streams;
import com.openexchange.webdav.action.AbstractAction;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link WebdavMkCalendarAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class WebdavMkCalendarAction extends AbstractAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebdavMkCalendarAction.class);

    /**
     * Initializes a new {@link WebdavMkCalendarAction}.
     */
    public WebdavMkCalendarAction() {
        super();
    }

    @Override
    public void perform(WebdavRequest req, WebdavResponse res) throws WebdavProtocolException {
        try {
            WebdavResource resource = req.getResource();
            if (null == resource) {
                throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(),HttpServletResponse.SC_NOT_FOUND);
            }
            if (req.hasBody()) {
                /*
                 * check body
                 */
                Document requestDoc = req.getBodyAsDocument();
                if (null == requestDoc || null == requestDoc.getRootElement()) {
                    throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(),HttpServletResponse.SC_BAD_REQUEST);
                }
                Element rootElement = requestDoc.getRootElement();
                if (null == rootElement || false == CaldavProtocol.CAL_NS.equals(rootElement.getNamespace()) ||
                    false == "mkcalendar".equals(rootElement.getName())) {
                    throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(),HttpServletResponse.SC_BAD_REQUEST);
                }
                /*
                 * process mkcalendar proppatches
                 */
                for (Element element : rootElement.getChildren("set", Protocol.DAV_NS)) {
                    for (Element prop : element.getChildren("prop", Protocol.DAV_NS)) {
                        for (Element propertyElement : prop.getChildren()) {
                            if (req.getFactory().getProtocol().isProtected(propertyElement.getNamespaceURI(), propertyElement.getName())) {
                                throw WebdavProtocolException.generalError(req.getUrl(), HttpServletResponse.SC_FORBIDDEN);
                            } else {
                                resource.putProperty(getProperty(propertyElement));
                            }
                        }
                    }
                }
            }
            /*
             * create resource
             */
            resource.create();
            res.setStatus(HttpServletResponse.SC_CREATED);
        } catch (JDOMException e) {
            LOG.error("JDOMException: ", e);
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(),HttpServletResponse.SC_BAD_REQUEST, e);
        } catch (IOException e) {
            LOG.debug("Client gone?", e);
        }
    }

    /**
     * Extracts a WebDAV property from the supplied property XML element.
     *
     * @param propertyElement The XML element representing the property
     * @return The WebDAV property
     * @throws IOException
     */
    private static WebdavProperty getProperty(Element propertyElement) throws IOException {
        WebdavProperty property = new WebdavProperty(propertyElement.getNamespaceURI(), propertyElement.getName());
        if (null != propertyElement.getChildren() && 0 < propertyElement.getChildren().size()) {
            property.setXML(true);
            Writer writer = null;
            try {
                writer = new AllocatingStringWriter();
                new XMLOutputter().output(propertyElement.cloneContent(), writer);
                property.setValue(writer.toString());
            } finally {
                Streams.close(writer);
            }
        } else {
            property.setValue(propertyElement.getText());
        }
        return property;
    }
}
