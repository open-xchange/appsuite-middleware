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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.mobility.provisioning.json.action.ActionException;
import com.openexchange.mobility.provisioning.json.action.ActionService;
import com.openexchange.mobility.provisioning.json.action.ActionTypes;
import com.openexchange.mobility.provisioning.json.action.Actions;
import com.openexchange.mobility.provisioning.json.osgi.MobilityProvisioningServiceRegistry;
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
		
		JSONObject obj = new JSONObject();

		try {
			final ServerSession session = getSessionObject(request);

			String action = JSONUtility.checkStringParameter(request, "action");

			if (action.equals(Actions.ACTION_LISTSERVICES)) {
				JSONObject services = new JSONObject();
				if (MobilityProvisioningServiceRegistry.getInstance().containsService(ActionTypes.EMAIL)) {
					services.put(Actions.ACTION_EMAIL, true);
				}
				if (MobilityProvisioningServiceRegistry.getInstance().containsService(ActionTypes.TELEPHONE)) {
					services.put(Actions.ACTION_TELEPHONE, true);
				}
				if (MobilityProvisioningServiceRegistry.getInstance().containsService(ActionTypes.OTHER)) {
					services.put(Actions.ACTION_OTHER, true);
				}
				
				obj.put("services", services);
			} else {
				boolean success = false;
				String message = "Couldn't send provisioning message.";
				
				final ActionService service;
				
				if (action.equals(Actions.ACTION_EMAIL)) {
				    service = MobilityProvisioningServiceRegistry.getInstance().getActionService(ActionTypes.EMAIL);
				} else if (action.equals(Actions.ACTION_TELEPHONE)) {
				    service = MobilityProvisioningServiceRegistry.getInstance().getActionService(ActionTypes.TELEPHONE);
				} else {
				    service = MobilityProvisioningServiceRegistry.getInstance().getActionService(ActionTypes.OTHER);
				}
				
			    if (service != null) {
			    	try {
			    		message = service.handleAction(JSONUtility.checkStringParameter(request, "target"), session);
			    		success = true;
			    	} catch (ActionException e) {
			    		e.printStackTrace();
			    	}
			    } else {
			    	message = "Service " + action + " provisioning is not available.";
			    }
			    
				if (message.trim().length() <= 0) {
					message = "Couldn't send provisioning message.";
				}

				obj.put("success", success);
				obj.put("message", message);
			}
		} catch (AjaxException e) {
			LOG.error("Missing or wrong field action in JSON request", e);
		}
		
		response.setData(obj);

		/*
		 * Close response and flush print writer
		 */
		ResponseWriter.write(response, resp.getWriter());
	}
	
}
