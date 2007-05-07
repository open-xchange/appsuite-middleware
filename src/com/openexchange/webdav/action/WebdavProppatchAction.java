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

package com.openexchange.webdav.action;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.util.Utils;

public class WebdavProppatchAction extends AbstractAction {

	private static final Namespace DAV_NS = Namespace.getNamespace("DAV:");
	private static final Log LOG = LogFactory.getLog(WebdavProppatchAction.class);
	
	private PropertyAction SET = null;
	private static final PropertyAction REMOVE = new RemoveAction();
	
	private XMLOutputter outputter = new XMLOutputter();

	public WebdavProppatchAction(Protocol protocol) {
		SET = new SetAction(protocol);
	}
	
	
	public void perform(WebdavRequest req, WebdavResponse res) throws WebdavException {
		try {
			Document requestDoc = req.getBodyAsDocument();
			Document responseDoc = new Document();
			Element multistatus = new Element("multistatus",DAV_NS);
			responseDoc.addContent(multistatus);
			Element response = new Element("response", DAV_NS);
			Element href = new Element("href", DAV_NS);
			
			href.setText(req.getURLPrefix()+req.getUrl());
			response.addContent(href);
			
			multistatus.addContent(response);
			
			WebdavResource resource = req.getResource();
			for(Element element : (List<Element>) requestDoc.getRootElement().getChildren()) {
				PropertyAction action = null;
				if(element.getNamespace().equals(DAV_NS)) {
					if("set".equals(element.getName())) {
						action = SET;
					} else if ("remove".equals(element.getName())) {
						action = REMOVE;
					}
				}
				
				if(null == action) {
					continue;
				}
				
				for(Element prop : (List<Element>) element.getChildren("prop", DAV_NS)) {
					response.addContent(action.perform(prop, resource));
				}
			}
			resource.save();
			res.setStatus(Protocol.SC_MULTISTATUS);
			outputter.output(responseDoc, res.getOutputStream());
			
			
		} catch (JDOMException e) {
			LOG.error("JDOMException: ",e);
			throw new WebdavException(req.getUrl(),HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			LOG.debug("Client gone?" ,e);
		}
	}
	
	static interface PropertyAction {
		// Propstat element
		public Element perform(Element propElement, WebdavResource resource);
	}
	
	private static final class SetAction implements PropertyAction {
		private XMLOutputter outputter = new XMLOutputter();
		private Protocol protocol;

		public SetAction(Protocol protocol) {
			this.protocol = protocol;
		}
		
		public Element perform(Element propElement, WebdavResource resource) {
			int status = 200;
			
			if(propElement.getChildren().isEmpty()) {
				Element propstat = new Element("propstat", DAV_NS);
				
				Element statusElement = new Element("status", DAV_NS);
				statusElement.setText("HTTP/1.1 "+status+" "+Utils.getStatusString(status));
				propstat.addContent(statusElement);
				return propstat;
			}
			
			Element propertyElement = (Element) propElement.getChildren().get(0);
			WebdavProperty property = new WebdavProperty();
			property.setNamespace(propertyElement.getNamespaceURI());
			property.setName(propertyElement.getName());
			if(protocol.isProtected(propertyElement.getNamespaceURI(), propertyElement.getName())) {
				
				status = HttpServletResponse.SC_FORBIDDEN;
			} else {
			
				if(propertyElement.getChildren().size() > 0) {
					property.setXML(true);
					try {
						StringWriter w = new StringWriter();
						outputter.output(propertyElement.cloneContent(), w);
						property.setValue(w.toString());
					} catch (IOException e) {
						status = 500;
					}
					
					
				} else {
					property.setValue(propertyElement.getText());
				}
				
				try {
					resource.putProperty(property);
				} catch (WebdavException e) {
					status = e.getStatus();
				}
			}
			
			Element propstat = new Element("propstat", DAV_NS);
			
			Element prop = new Element("prop", DAV_NS);
			Element propContent = new Element(property.getName(), Namespace.getNamespace(property.getNamespace()));
			prop.addContent(propContent);
			
			propstat.addContent(prop);
			
			
			Element statusElement = new Element("status", DAV_NS);
			statusElement.setText("HTTP/1.1 "+status+" "+Utils.getStatusString(status));
			propstat.addContent(statusElement);
			
			return propstat;
		}
		
	}
	
	private static final class RemoveAction implements PropertyAction {

		public Element perform(Element propElement, WebdavResource resource) {
			int status = 200;
			if(propElement.getChildren().isEmpty()) {
				Element propstat = new Element("propstat", DAV_NS);
				
				Element statusElement = new Element("status", DAV_NS);
				statusElement.setText("HTTP/1.1 "+status+" "+Utils.getStatusString(status));
				propstat.addContent(statusElement);
				return propstat;
			}
			Element propertyElement = (Element) propElement.getChildren().get(0);
			
			try {
				resource.removeProperty(propertyElement.getNamespaceURI(), propertyElement.getName());
			} catch (WebdavException e) {
				status = e.getStatus();
			}
			
			Element propstat = new Element("propstat", DAV_NS);
			
			Element prop = new Element("prop", DAV_NS);
			Element propContent = new Element(propertyElement.getName(), Namespace.getNamespace(propertyElement.getNamespaceURI()));
			prop.addContent(propContent);
			
			propstat.addContent(prop);
			
			
			Element statusElement = new Element("status", DAV_NS);
			statusElement.setText("HTTP/1.1 "+status+" "+Utils.getStatusString(status));
			propstat.addContent(statusElement);
			
			return propstat;
		}
		
	}

}
