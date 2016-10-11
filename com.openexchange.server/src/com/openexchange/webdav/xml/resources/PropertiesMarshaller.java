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

package com.openexchange.webdav.xml.resources;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import com.openexchange.webdav.action.behaviour.BehaviourLookup;
import com.openexchange.webdav.protocol.Multistatus;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.WebdavStatus;
import com.openexchange.webdav.protocol.util.Utils;

public class PropertiesMarshaller implements ResourceMarshaller {

	protected static final Namespace DAV_NS = Protocol.DAV_NS;
	protected static final Namespace DATE_NS = Namespace.getNamespace("b",  "urn:uuid:c2f41010-65b3-11d1-a29f-00aa00c14882/");

	private String uriPrefix;

	private final String charset;

	protected Multistatus<Iterable<WebdavProperty>> getProps(final WebdavResource resource) {
		return new Multistatus<Iterable<WebdavProperty>>();
	}

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PropertiesMarshaller.class);

	public PropertiesMarshaller(final String charset){
		this.charset = charset;
	}

	public PropertiesMarshaller(final String uriPrefix, final String charset) {
		this.uriPrefix = uriPrefix;
		if(!this.uriPrefix.endsWith("/")) {
			this.uriPrefix += "/";
		}
		this.charset = charset;
	}

	@Override
	public List<Element> marshal(final WebdavResource resource) throws WebdavProtocolException {
		final Element response =  new Element("response",DAV_NS);
		response.addContent(marshalHREF(resource.getUrl(), resource.isCollection()));
		if (resource.exists()) {
			final Multistatus<Iterable<WebdavProperty>> multistatus = getProps(resource);
			for(final int statusCode : multistatus.getStatusCodes()) {
				for(final WebdavStatus<Iterable<WebdavProperty>> status : multistatus.toIterable(statusCode)) {
					final Element propstat = new Element("propstat",DAV_NS);
					final Element prop = new Element("prop", DAV_NS);

					for(final WebdavProperty p : status.getAdditional()) {
						if(p != null) {
							prop.addContent(marshalProperty(p, resource.getProtocol()));
						}
					}
					propstat.addContent(prop);
					propstat.addContent(marshalStatus(statusCode));
					response.addContent(propstat);
				}
			}
		} else {
			response.addContent(this.marshalStatus(HttpServletResponse.SC_NOT_FOUND));
		}
		return Arrays.asList(response);
	}

	public Element marshalHREF(final WebdavPath uri, boolean trailingSlash) {
		final Element href = new Element("href", DAV_NS);
        final StringBuilder builder = new StringBuilder(uriPrefix);
        if(builder.charAt(builder.length()-1) != '/') {
			builder.append('/');
		}
        for(final String component : uri) {
            builder.append(escape(component)).append('/');
        }
        if (!trailingSlash) {
            builder.setLength(builder.length()-1);
        }
        href.setText(builder.toString());
		return href;
	}

	private String escape(final String string) {
		final PropfindResponseUrlEncoder encoder = BehaviourLookup.getInstance().get(PropfindResponseUrlEncoder.class);
		if(null != encoder) {
			return encoder.encode(string);
		}
		try {
			return URLEncoder.encode(string,charset).replaceAll("\\+","%20");
		} catch (final UnsupportedEncodingException e) {
			LOG.error(e.toString());
			return string;
		}
	}

	public Element marshalStatus(final int s) {
		final Element status = new Element("status",DAV_NS);
		final StringBuilder content = new StringBuilder("HTTP/1.1 ");
		content.append(s);
		content.append(' ');
		content.append(Utils.getStatusString(s));
		status.setText(content.toString());
		return status;
	}

	public Element marshalProperty(WebdavProperty property, Protocol protocol) {
		Element propertyElement = new Element(property.getName(), getNamespace(property));
		if (null == property.getValue()) {
			return propertyElement;
		}
		if (property.isXML()) {
			try {
                StringBuilder xmlBuilder = new StringBuilder("<FKR:fakeroot xmlns:FKR=\"http://www.open-xchange.com/webdav/fakeroot\" xmlns:D=\"DAV:\"");
                if (false == "DAV:".equals(property.getNamespace())) {
                    xmlBuilder.append(" xmlns=\"").append(property.getNamespace()).append('"');
                }
                List<Namespace> namespaces = protocol.getAdditionalNamespaces();
                for (Namespace namespace : namespaces) {
                    xmlBuilder.append(" xmlns:").append(namespace.getPrefix()).append("=\"").append(namespace.getURI()).append('"');
                }
                xmlBuilder.append('>').append(property.getValue()).append("</FKR:fakeroot>");
				final Document doc = new SAXBuilder().build(new StringReader(xmlBuilder.toString()));
				propertyElement.setContent(doc.getRootElement().cloneContent());
                Map<String, String> attributes = property.getAttributes();
                if (null != attributes) {
                    for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                        propertyElement.setAttribute(attribute.getKey(), attribute.getValue());
                    }
                }
			} catch (JDOMException e) {
				// NO XML
				LOG.error(e.toString());
				propertyElement.setText(property.getValue());
			} catch (IOException e) {
				LOG.error("", e);
			}
		} else {
			if (property.isDate()) {
				propertyElement.setAttribute("dt", "dateTime.rfc1123", DATE_NS);
			}
			propertyElement.setText(property.getValue());
		}
		return propertyElement;
	}

	private Namespace getNamespace(final WebdavProperty property) {
		final String namespace = property.getNamespace();
		if(namespace.equals("DAV:")) {
			return DAV_NS;
		}
		return Namespace.getNamespace(namespace);
	}

}
