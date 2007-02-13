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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.openexchange.webdav.protocol.Multistatus;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.WebdavStatus;
import com.openexchange.webdav.protocol.util.Utils;

public class PropertiesMarshaller implements ResourceMarshaller {

	protected static final Namespace DAV_NS = Protocol.DAV_NS;
	protected static final Namespace DATE_NS = Namespace.getNamespace("b",  "urn:uuid:c2f41010-65b3-11d1-a29f-00aa00c14882/");
	
	private String uriPrefix;

	private String charset;

	protected Multistatus<Iterable<WebdavProperty>> getProps(WebdavResource resource) {
		return new Multistatus<Iterable<WebdavProperty>>();
	}

	private static final Log LOG = LogFactory.getLog(PropertiesMarshaller.class);
	
	public PropertiesMarshaller(String charset){
		this.charset = charset;
	}
	
	public PropertiesMarshaller(String uriPrefix, String charset) {
		this.uriPrefix = uriPrefix;
		if(!this.uriPrefix.endsWith("/"))
			this.uriPrefix += "/";
		this.charset = charset;
	}

	public List<Element> marshal(WebdavResource resource) {
		Element response =  new Element("response",DAV_NS);
		response.addContent(marshalHREF(resource.getUrl()));
		Multistatus<Iterable<WebdavProperty>> multistatus = getProps(resource);
		for(int statusCode : multistatus.getStatusCodes()) {
			for(WebdavStatus<Iterable<WebdavProperty>> status : multistatus.toIterable(statusCode)) {
				Element propstat = new Element("propstat",DAV_NS);
				Element prop = new Element("prop", DAV_NS);
				
				for(WebdavProperty p : status.getAdditional()) {
					if(p != null)
						prop.addContent(marshalProperty(p));
				}
				propstat.addContent(prop);
				propstat.addContent(marshalStatus(statusCode));
				response.addContent(propstat);
			}
		}
		return Arrays.asList(response);
	}
	
	
	public Element marshalHREF(String uri) {
		Element href = new Element("href", DAV_NS);
		if(uri.startsWith("/"))
			uri = uri.substring(1);
		
		href.setText(escape(uriPrefix+uri));
		return href;
	}
	
	private String escape(String string) {
		/*try {
			String[] components = string.split("/+");
			StringBuilder builder = new StringBuilder();
			for(String comp : components) {
				if(comp != null && !"".equals(comp)) {
					builder.append("/");
					builder.append(URLEncoder.encode(comp,charset).replaceAll("\\+","%20")); //FIXME
				}
			}
			return builder.toString();
		} catch (UnsupportedEncodingException e) {
			LOG.fatal(e);
			return string;
		} */
		return string;
	}

	public Element marshalStatus(int s) {
		Element status = new Element("status",DAV_NS);
		StringBuilder content = new StringBuilder("HTTP/1.1 ");
		content.append(s);
		content.append(" ");
		content.append(Utils.getStatusString(s));
		status.setText(content.toString());
		return status;
	}

	public Element marshalProperty(WebdavProperty property) {
		Element propertyElement = new Element(property.getName(), getNamespace(property));
		if(property.getValue() == null)
			return propertyElement;
		if(property.isXML()) {
			try {
				String asXML = null;
				if("DAV:".equals(property.getNamespace())) {
					asXML = "<FKR:fakeroot xmlns:FKR=\"http://www.open-xchange.com/webdav/fakeroot\" xmlns:D=\""+property.getNamespace()+"\">"+property.getValue()+"</FKR:fakeroot>";
				} else {
					asXML = "<FKR:fakeroot xmlns:FKR=\"http://www.open-xchange.com/webdav/fakeroot\" xmlns=\""+property.getNamespace()+"\">"+property.getValue()+"</FKR:fakeroot>";
				}
				Document doc = new SAXBuilder().build(new StringReader(asXML));
				propertyElement.setContent(doc.getRootElement().cloneContent());
			} catch (JDOMException e) {
				// NO XML
				LOG.error(e);
				propertyElement.setText(property.getValue());
			} catch (IOException e) {
				LOG.error(e);
			}
		} else {
			if(property.isDate()) {
				propertyElement.setAttribute("dt", "dateTime.tz", DATE_NS);
			}
			propertyElement.setText(property.getValue());
		}
		return propertyElement;
	}

	private Namespace getNamespace(WebdavProperty property) {
		String namespace = property.getNamespace();
		if(namespace.equals("DAV:"))
			return DAV_NS;
		return Namespace.getNamespace(namespace);
	}

}
