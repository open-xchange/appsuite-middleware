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

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.WebdavLock.Scope;
import com.openexchange.webdav.protocol.WebdavLock.Type;
import com.openexchange.webdav.xml.resources.PropertiesMarshaller;

public class WebdavLockAction extends AbstractAction {

	private static final Namespace DAV_NS = Namespace.getNamespace("DAV:");
	
	private static final Log LOG = LogFactory.getLog(WebdavLockAction.class);
	
	public void perform(final WebdavRequest req, final WebdavResponse res)
			throws WebdavException {
		final WebdavLock lock = new WebdavLock();
		
		
		lock.setTimeout(getTimeout(req.getHeader("Timeout")) * 1000);
		lock.setDepth(getDepth(req.getHeader("Depth")));
		
		try {
			final Element root = req.getBodyAsDocument().getRootElement();
			final Element lockscope = (Element) root.getChild("lockscope",DAV_NS).getChildren().get(0);
			
			if(lockscope.getNamespace().equals(DAV_NS)) {
				if(lockscope.getName().equalsIgnoreCase("shared")) {
					lock.setScope(Scope.SHARED_LITERAL);
				} else {
					lock.setScope(Scope.EXCLUSIVE_LITERAL);
				}
			}
			
			lock.setType(Type.WRITE_LITERAL);
			
			final Element owner = root.getChild("owner",DAV_NS);
			
			final XMLOutputter outputter = new XMLOutputter();

            if(owner != null) {
                lock.setOwner(outputter.outputString(owner.cloneContent()));
            }

			WebdavResource resource = req.getResource();
			int status = HttpServletResponse.SC_OK;
			 
			
			resource.lock(lock);
			
			// Reload, because it might have been switched to a lock null resource
			resource = req.getFactory().resolveResource(req.getUrl());
			
			res.setStatus(status);
			res.setHeader("Lock-Token",lock.getToken());
			res.setHeader("content-type", "application/xml");
			final WebdavProperty lockdiscovery = resource.getProperty("DAV:", "lockdiscovery");
			
			final Element lockDiscoveryElement = new PropertiesMarshaller(req.getCharset()).marshalProperty(lockdiscovery);
			
			final Document responseDoc = new Document();
			final Element rootElement = new Element("prop",DAV_NS);
			
			rootElement.addContent(lockDiscoveryElement);
			
			responseDoc.setContent(rootElement);
			
			outputter.output(responseDoc, res.getOutputStream());
			
		} catch (final JDOMException e) {
			LOG.error("JDOM Exception",e);
			throw new WebdavException(req.getUrl(),HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (final IOException e) {
			LOG.debug("Client gone?", e);
		}
	}

	private int getDepth(final String header) {
        if(null == header) {
			return 0;
		}
		if(header.equalsIgnoreCase("infinity")) {
			return WebdavCollection.INFINITY;
		}
		
		return Integer.parseInt(header);
	}

	private long getTimeout(String header) {
		if(null == header) {
			return 600;
		}
		if(header.indexOf(',') != -1) {
			header = header.substring(0,header.indexOf(',')).trim();
		}
		if(header.equalsIgnoreCase("infinite")) {
			return WebdavLock.NEVER;
		}
		
		return Long.parseLong(header.substring(7));
	}

}
