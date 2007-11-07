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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavResource;

public class WebdavLogAction extends AbstractAction {

	private static final Log LOG = LogFactory.getLog(WebdavLogAction.class);
	private boolean logBody;
	private boolean logResponse;
	
	public void perform(WebdavRequest req, WebdavResponse res)
			throws WebdavException {
		StringBuilder b = new StringBuilder();
		try {
			b.append("URL: "); b.append(req.getUrl()); b.append('\n');
			for(String header : req.getHeaderNames()) {
				b.append(header); b.append(": "); b.append(req.getHeader(header)); b.append('\n');
			}
			final WebdavResource resource = req.getResource();
			b.append("Resource: "); b.append(resource); b.append('\n');
			b.append("exists: "); b.append(resource.exists()); b.append('\n');
			b.append("isCollection: "); b.append(resource.isCollection()); b.append('\n');
		
			if (LOG.isDebugEnabled()) {
				LOG.debug(b.toString());
			}
		
			if(LOG.isTraceEnabled()) {
				if(logBody) {
					req = new ReplayWebdavRequest(req);
					printRequestBody(req);
				}
				if(logResponse) {
					res = new CapturingWebdavResponse(res);
				}
			}
		
			yield(req,res);
			b = new StringBuilder();
			b.append("DONE URL: "); b.append(req.getUrl()); b.append(' '); b.append(res.getStatus()); b.append('\n');
				
			if (LOG.isDebugEnabled()) {
				LOG.debug(b.toString());
			}
			
			if(LOG.isTraceEnabled() && logResponse) {
				LOG.trace(((CapturingWebdavResponse)res).getBodyAsString());
			}
			
		} catch (WebdavException x) {
			b = new StringBuilder();
			b.append("Status: "); b.append(x.getMessage()); b.append(' '); b.append(x.getStatus()); b.append('\n');
			b.append("WebdavException: ");
			if (LOG.isDebugEnabled()) {
				LOG.debug(b.toString(),x);
			} else if(LOG.isErrorEnabled() && x.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
				LOG.error("The request: "+b.toString()+" caused an internal server error: "+x.getMessage(),x);
			}
			throw x;
		} catch (RuntimeException x) {
			if (LOG.isErrorEnabled()) {
				LOG.error("RuntimeException In WebDAV for request: "+b.toString(),x);
			}
			throw x;
		}
	}

	private void printRequestBody(final WebdavRequest req) {
		if(logBody) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(req.getBody(), "UTF-8"));
				String line = null;
				final StringBuilder b = new StringBuilder();
				while((line = reader.readLine()) != null) {
					b.append(line);
					b.append('\n');
				}
				if (LOG.isTraceEnabled()) {
					LOG.trace(b);
				}
			} catch (IOException x) {
				LOG.debug("",x);
			}finally {
				if(reader != null) {
					try {
						reader.close();
					} catch (IOException x2) {
						LOG.debug("",x2);
					}
				}
			}
		}
	}
	
	public void setLogRequestBody(final boolean b) {
		logBody = b;
	}
	
	public void setLogResponseBody(final boolean b) {
		logResponse = b;
	}

	public boolean isEnabled() {
		return LOG.isErrorEnabled();
	}

}
