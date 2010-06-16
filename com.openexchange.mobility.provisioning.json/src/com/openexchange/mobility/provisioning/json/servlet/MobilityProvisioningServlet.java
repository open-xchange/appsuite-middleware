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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mobility.provisioning.json.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mobility.provisioning.json.action.ActionEmail;
import com.openexchange.mobility.provisioning.json.action.Actions;
import com.openexchange.server.ServiceException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.session.ServerSession;

/**
 * 
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 * 
 */
public final class MobilityProvisioningServlet extends PermissionServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1541427953784271108L;
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MobilityProvisioningServlet.class);

	/**
	 * Initializes
	 */
	public MobilityProvisioningServlet() {
		super();
	}
	
	@Override
	protected boolean hasModulePermission(final ServerSession session) {
		return true;
	}

	protected void doPut(final HttpServletRequest request,
			final HttpServletResponse resp) throws ServletException,
			IOException {
		
		try {
			actionGetData(request, resp);
		} catch (final JSONException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}

	}
	
	/**
	 * Performs the GET request!
	 * 
	 * @param request
	 *            The servlet request
	 * @param resp
	 *            The servlet response
	 * @throws JSONException
	 *             If JSON data cannot be composed
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	private void actionGetData(final HttpServletRequest request,
			final HttpServletResponse resp) throws JSONException, IOException {
		final Response response = new Response();
		
		boolean success = false;
		String message = "";
		
		JSONObject obj = new JSONObject();	
		

		try {
			final ServerSession session = getSessionObject(request);
			Context ctx = ContextStorage.getStorageContext(session);
			User user = UserStorage.getInstance().getUser(session.getUserId(), ctx);
			
			String target = JSONUtility.checkStringParameter(request, "target");
			
			if (JSONUtility.checkStringParameter(request, "action").equals(Actions.ACTION_EMAIL)) {
				new ActionEmail(new InternetAddress(target, true)).sendMail(session);
				message = "Provisioning mail has been send to " + target;
				success = true;
			} else if (JSONUtility.checkStringParameter(request, "action").equals(Actions.ACTION_SMS)) {
				message = "Action SMS not implemented yet.";
			} else {
				message = "Missing or wrong field action in JSON request.";
			}
		} catch (MailException e) {
			LOG.error("Couldn't send provisioning mail", e);
		} catch (ContextException e) {
			LOG.error("Cannot find context for user", e);
		} catch (LdapException e) {
			LOG.error("Cannot get user object", e);
		} catch (AddressException e) {
			LOG.error("Target Spam email address cannot be parsed", e);
		} catch (ServiceException e) {
			LOG.error("Cannot get configuration", e);
		} catch (UnsupportedEncodingException e) {
			LOG.error("Error on correcting provisioning url", e);
		} catch (AjaxException e) {
			LOG.error("Missing or wrong field action in JSON request", e);
		}
		
		obj.put("success", success);
		obj.put("message", message);
		response.setData(obj);

		/*
		 * Close response and flush print writer
		 */
		ResponseWriter.write(response, resp.getWriter());
	}
	
}
