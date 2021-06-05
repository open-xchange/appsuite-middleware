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

package com.openexchange.webdav.action;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
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
		} catch (JDOMException e1) {

			forceAllProp = true; //Assume All Prop, if all else fails

		} catch (IOException e1) {
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(new WebdavPath(), HttpServletResponse.SC_BAD_REQUEST);
		}

        final LoadingHints loadingHints = new LoadingHints();
        ResourceMarshaller marshaller;
        if (null != req.getHeader("Depth")) {
            int depth = 0;
            if (req.getHeader("depth").trim().equalsIgnoreCase("infinity")) {
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
            new XMLOutputter(Format.getPrettyFormat()).output(responseBody, res.getOutputStream());
		} catch (IOException e) {
			LOG.debug("Client gone?", e);
		}
	}

    protected PropertiesMarshaller getMarshaller(final WebdavRequest req, boolean forceAllProp, Document requestBody, LoadingHints loadingHints) throws WebdavProtocolException {
        if (loadingHints == null) {
            loadingHints = new LoadingHints();
        }
        PropertiesMarshaller marshaller = null;
		loadingHints.setUrl(req.getUrl());

        if (null != requestBody && null != requestBody.getRootElement().getChild("propname", DAV_NS)) {
            if (null != requestBody.getRootElement().getChild("allprop", DAV_NS)) {
                throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
            }
            marshaller = new PropfindPropNamesMarshaller(req.getURLPrefix(), req.getCharset());
            loadingHints.setProps(LoadingHints.Property.ALL);
		}

		if (null != requestBody && null != requestBody.getRootElement().getChild("allprop", DAV_NS) || forceAllProp) {
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
