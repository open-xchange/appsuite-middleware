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

package com.openexchange.webdav.action;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;
import com.openexchange.webdav.loader.LoadingHints;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.xml.resources.PropertiesMarshaller;
import com.openexchange.webdav.xml.resources.PropfindAllPropsMarshaller;
import com.openexchange.webdav.xml.resources.PropfindPropNamesMarshaller;
import com.openexchange.webdav.xml.resources.PropfindResponseMarshaller;
import com.openexchange.webdav.xml.resources.RecursiveMarshaller;
import com.openexchange.webdav.xml.resources.ResourceMarshaller;

public class WebdavPropfindAction extends AbstractAction {

	protected static final Namespace DAV_NS = Protocol.DAV_NS;

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebdavPropfindAction.class);

	protected final XMLOutputter outputter = new XMLOutputter();

    protected Protocol protocol;

	public WebdavPropfindAction(Protocol protocol) {
	    this.protocol = protocol;
	}

	@Override
	public void perform(final WebdavRequest req, final WebdavResponse res)
			throws WebdavProtocolException {

		final Element response = new Element("multistatus",DAV_NS);

        List<Namespace> namespaces = protocol.getAdditionalNamespaces();
        for (Namespace namespace : namespaces) {
            response.addNamespaceDeclaration(namespace);
        }


		final Document responseBody = new Document(response);

		boolean forceAllProp = false;
		Document requestBody = null;
		try {
			requestBody = req.getBodyAsDocument();
		} catch (final JDOMException e1) {

			forceAllProp = true; //Assume All Prop, if all else fails

		} catch (final IOException e1) {
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(new WebdavPath(), HttpServletResponse.SC_BAD_REQUEST);
		}

        final LoadingHints loadingHints = new LoadingHints();
        ResourceMarshaller marshaller;
        if (null != req.getHeader("Depth")) {
            int depth = 0;
            if(req.getHeader("depth").trim().equalsIgnoreCase("infinity")) {
                depth = WebdavCollection.INFINITY;
            } else {
                depth = Integer.parseInt(req.getHeader("Depth"));
            }

            PropertiesMarshaller delegate = getMarshaller(req, forceAllProp, requestBody, loadingHints);
            marshaller = new RecursiveMarshaller(delegate, depth, protocol.getRecursiveMarshallingLimit());
            loadingHints.setDepth(depth);
        } else {
            marshaller = getMarshaller(req, forceAllProp, requestBody, loadingHints);
        }

		preLoad(loadingHints);
		if (marshaller != null) {
			response.addContent(marshaller.marshal(req.getResource()));
		}

		try {
			res.setStatus(Protocol.SC_MULTISTATUS);
			res.setContentType("text/xml; charset=UTF-8");
			outputter.output(responseBody, res.getOutputStream());
		} catch (final IOException e) {
			LOG.debug("Client gone?", e);
		}
	}

    protected PropertiesMarshaller getMarshaller(final WebdavRequest req, boolean forceAllProp, Document requestBody, LoadingHints loadingHints) {
        if (loadingHints == null) {
            loadingHints = new LoadingHints();
        }
        PropertiesMarshaller marshaller = null;
		loadingHints.setUrl(req.getUrl());

		if(null != requestBody && null != requestBody.getRootElement().getChild("propname", DAV_NS)) {
			marshaller = new PropfindPropNamesMarshaller(req.getURLPrefix(),req.getCharset());
			loadingHints.setProps(LoadingHints.Property.ALL);
		}

		if(null != requestBody && null != requestBody.getRootElement().getChild("allprop", DAV_NS) || forceAllProp) {
			marshaller = new PropfindAllPropsMarshaller(req.getURLPrefix(), req.getCharset());
			loadingHints.setProps(LoadingHints.Property.ALL);
		}
		if (null != requestBody && null != requestBody.getRootElement().getChild("prop",DAV_NS)) {
			marshaller = new PropfindResponseMarshaller(req.getURLPrefix(), req.getCharset(), req.isBrief());
			loadingHints.setProps(LoadingHints.Property.SOME);

			for(final Element props : requestBody.getRootElement().getChildren("prop", DAV_NS)){
				for(final Element requested : props.getChildren()) {
					((PropfindResponseMarshaller) marshaller).addProperty(requested.getNamespaceURI(), requested.getName());
					loadingHints.addProperty(requested.getNamespaceURI(), requested.getName());
				}
			}
		}
        return marshaller;
    }

}
