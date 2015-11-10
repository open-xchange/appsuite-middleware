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

package com.openexchange.dav.actions;

import java.io.IOException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.webdav.action.WebdavPropfindAction;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.loader.LoadingHints;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.xml.resources.PropertiesMarshaller;
import com.openexchange.webdav.xml.resources.PropfindAllPropsMarshaller;
import com.openexchange.webdav.xml.resources.PropfindPropNamesMarshaller;
import com.openexchange.webdav.xml.resources.PropfindResponseMarshaller;
import com.openexchange.webdav.xml.resources.RecursiveMarshaller;
import com.openexchange.webdav.xml.resources.ResourceMarshaller;

/**
 * {@link DAVPropfindAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public abstract class DAVPropfindAction extends WebdavPropfindAction {

    protected static final XMLOutputter OUTPUTTER = new XMLOutputter();

    /**
     * Initializes a new {@link DAVPropfindAction}.
     *
     * @param protocol The underlying WebDAV protocol
     */
    public DAVPropfindAction(DAVProtocol protocol) {
        super(protocol);
    }

    /**
     * Prepares an XML element ready to be used as root element for a multistatus response, containing any additionally defined
     * namespace declarations of the underlying protocol.
     *
     * @return A new multistatus element
     */
    protected Element prepareMultistatusElement() {
        Element multistatusElement = new Element("multistatus", DAV_NS);
        for (Namespace namespace : protocol.getAdditionalNamespaces()) {
            multistatusElement.addNamespaceDeclaration(namespace);
        }
        return multistatusElement;
    }

    /**
     * Gets a resource marshaller appropriate for the supplied request.
     *
     * @param request The WebDAV request
     * @param requestBody The request body document, or <code>null</code> if not available
     * @return The resource marshaller
     */
    protected ResourceMarshaller getMarshaller(WebdavRequest request, Document requestBody) throws WebdavProtocolException {
        return getMarshaller(request, requestBody, request.getURLPrefix());
    }

    /**
     * Gets a resource marshaller appropriate for the supplied request.
     *
     * @param request The WebDAV request
     * @param requestBody The request body document, or <code>null</code> if not available
     * @param urlPrefix The URL prefix to use for marshalling
     * @return The resource marshaller
     */
    protected ResourceMarshaller getMarshaller(WebdavRequest request, Document requestBody, String urlPrefix) throws WebdavProtocolException {
        /*
         * prepare loading hints
         */
        int depth = request.getDepth(0);
        LoadingHints loadingHints = new LoadingHints();
        loadingHints.setDepth(depth);
        loadingHints.setUrl(request.getUrl());
        /*
         * create appropriate response marshaller
         */
        PropertiesMarshaller marshaller;
        if (null == requestBody || null != requestBody.getRootElement().getChild("allprop", DAV_NS)) {
            /*
             * marshal all properties
             */
            loadingHints.setProps(LoadingHints.Property.ALL);
            marshaller = new PropfindAllPropsMarshaller(urlPrefix, request.getCharset());
        } else if (null != requestBody.getRootElement().getChild("propname", DAV_NS)) {
            /*
             * marshal all property names
             */
            loadingHints.setProps(LoadingHints.Property.ALL);
            marshaller = new PropfindPropNamesMarshaller(urlPrefix, request.getCharset());
        } else {
            /*
             * marshal specific properties
             */
            loadingHints.setProps(LoadingHints.Property.SOME);
            PropfindResponseMarshaller responseMarshaller = new PropfindResponseMarshaller(urlPrefix, request.getCharset(), request.isBrief());
            for (Element requestedProps : requestBody.getRootElement().getChildren("prop", DAV_NS)){
                for (Element requestedProperty : requestedProps.getChildren()) {
                    loadingHints.addProperty(requestedProperty.getNamespaceURI(), requestedProperty.getName());
                    responseMarshaller.addProperty(requestedProperty.getNamespaceURI(), requestedProperty.getName());
                }
            }
            marshaller = responseMarshaller;
        }
        /*
         * pre-load as needed
         */
        preLoad(loadingHints);
        /*
         * wrap into recursive marshaller if needed
         */
        return 0 == depth ? marshaller : new RecursiveMarshaller(marshaller, depth, protocol.getRecursiveMarshallingLimit());
    }

    /**
     * Optionally extracts the request body document from a WebDAV request.
     *
     * @param request The WebDAV request
     * @return The response body, or <code>null</code> if there is none or no document could be parsed
     */
    protected Document optRequestBody(WebdavRequest request) {
        try {
            return request.getBodyAsDocument();
        } catch (JDOMException | IOException e) {
            org.slf4j.LoggerFactory.getLogger(DAVPropfindAction.class).warn("Error getting WebDAV request body", e);
            return null;
        }
    }

    /**
     * Sends a multistatus response.
     *
     * @param response The WebDAV response to write to
     * @param multistatusElement The root element for the multistatus response
     */
    protected void sendMultistatusResponse(WebdavResponse response, Element multistatusElement) {
        sendXMLResponse(response, new Document(multistatusElement), Protocol.SC_MULTISTATUS);
    }

    /**
     * Sends a XML response document.
     *
     * @param response The WebDAV response to write to
     * @param responseBody The response body document
     * @param status The HTTP status code to use
     */
    protected void sendXMLResponse(WebdavResponse response, Document responseBody, int status) {
        try {
            response.setStatus(status);
            response.setContentType("text/xml; charset=UTF-8");
            OUTPUTTER.output(responseBody, response.getOutputStream());
        } catch (IOException e) {
            org.slf4j.LoggerFactory.getLogger(DAVPropfindAction.class).warn("Error sending WebDAV response", e);
        }
    }

}
