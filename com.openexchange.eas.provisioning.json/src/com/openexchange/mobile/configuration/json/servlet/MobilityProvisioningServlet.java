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

package com.openexchange.mobile.configuration.json.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.ServiceException;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mobile.configuration.json.action.ActionException;
import com.openexchange.mobile.configuration.json.action.ActionService;
import com.openexchange.mobile.configuration.json.action.ActionTypes;
import com.openexchange.mobile.configuration.json.container.ProvisioningInformation;
import com.openexchange.mobile.configuration.json.container.ProvisioningResponse;
import com.openexchange.mobile.configuration.json.exception.MobileProvisioningJsonExceptionCodes;
import com.openexchange.mobile.configuration.json.osgi.MobilityProvisioningServiceRegistry;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 *
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 *
 */
public final class MobilityProvisioningServlet extends PermissionServlet {

	private static final long serialVersionUID = 8555223354984992000L;

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MobilityProvisioningServlet.class);

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

	@Override
    protected void doGet(final HttpServletRequest request,
			final HttpServletResponse resp) throws ServletException,
			IOException {
		final Response response = new Response();

		final JSONObject obj = new JSONObject();

		try {
			final String action = JSONUtility.checkStringParameter(request, "action");
			if (action.equals(ActionTypes.LISTSERVICES.code)) {
				final JSONArray services = new JSONArray();
				if (MobilityProvisioningServiceRegistry.getInstance().containsService(ActionTypes.EMAIL)) {
					services.put(ActionTypes.EMAIL.code);
				}
				if (MobilityProvisioningServiceRegistry.getInstance().containsService(ActionTypes.TELEPHONE)) {
					services.put(ActionTypes.TELEPHONE.code);
				}
				if (MobilityProvisioningServiceRegistry.getInstance().containsService(ActionTypes.OTHER)) {
					services.put(ActionTypes.OTHER.code);
				}

				obj.put("services", services);
			}
		} catch (final OXException e) {
			LOG.error("Missing or wrong field action in JSON request", e);
			response.setException(e);
		} catch (final JSONException e) {
			LOG.error(e.getLocalizedMessage(), e);
            response.setException(OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e));
		}

		response.setData(obj);

		/*
		 * Close response and flush print writer
		 */
		try {
			ResponseWriter.write(response, resp.getWriter(), localeFrom(getSessionObject(request)));
		} catch (final JSONException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
	}

	@Override
    protected void doPut(final HttpServletRequest request,
			final HttpServletResponse resp) throws ServletException,
			IOException {
		final Response response = new Response();

		final JSONObject obj = new JSONObject();

		final ServerSession session = getSessionObject(request);
		try {

			final String action = JSONUtility.checkStringParameter(request, "action");

			if (action.equals(ActionTypes.LISTSERVICES.code)) {
				final JSONArray services = new JSONArray();
				if (MobilityProvisioningServiceRegistry.getInstance().containsService(ActionTypes.EMAIL)) {
					services.put(ActionTypes.EMAIL.code);
				}
				if (MobilityProvisioningServiceRegistry.getInstance().containsService(ActionTypes.TELEPHONE)) {
					services.put(ActionTypes.TELEPHONE.code);
				}
				if (MobilityProvisioningServiceRegistry.getInstance().containsService(ActionTypes.OTHER)) {
					services.put(ActionTypes.OTHER.code);
				}

				obj.put("services", services);
			} else {
				final ActionService service;

				if (action.equals(ActionTypes.EMAIL.code)) {
				    service = MobilityProvisioningServiceRegistry.getInstance().getActionService(ActionTypes.EMAIL);
				} else if (action.equals(ActionTypes.TELEPHONE.code)) {
				    service = MobilityProvisioningServiceRegistry.getInstance().getActionService(ActionTypes.TELEPHONE);
				} else if (action.equals(ActionTypes.OTHER.code)) {
				    service = MobilityProvisioningServiceRegistry.getInstance().getActionService(ActionTypes.OTHER);
				} else {
					service = null;
				}

				ProvisioningResponse provisioningResponse = null;
			    if (service != null) {
			    	try {
						final Context ctx = ContextStorage.getStorageContext(session);
						final User user = UserStorage.getInstance().getUser(session.getUserId(), ctx);

						String url = MobilityProvisioningServletConfiguration.getProvisioningURL();
						url = url.replace("%h", URLEncoder.encode(request.getServerName(), MobilityProvisioningServletConfiguration.getProvisioningURLEncoding()));
						url = url.replace("%l", URLEncoder.encode(session.getLogin(), MobilityProvisioningServletConfiguration.getProvisioningURLEncoding()));
						url = url.replace("%c", URLEncoder.encode(String.valueOf(session.getContextId()), MobilityProvisioningServletConfiguration.getProvisioningURLEncoding()));
						url = url.replace("%u", URLEncoder.encode(session.getUserlogin(), MobilityProvisioningServletConfiguration.getProvisioningURLEncoding()));
						url = url.replace("%p", URLEncoder.encode(user.getMail(), MobilityProvisioningServletConfiguration.getProvisioningURLEncoding()));

						final ProvisioningInformation provisioningInformation = new com.openexchange.mobile.configuration.json.container.ProvisioningInformation(
								JSONUtility.checkStringParameter(request, "target"),
								url,
								MobilityProvisioningServletConfiguration.getProvisioningURLEncoding(),
								MobilityProvisioningServletConfiguration.getProvisioningMailFrom(),
								MobilityProvisioningServletConfiguration.getProvisioningEmailMessages(url),
								MobilityProvisioningServletConfiguration.getProvisioningSMSMessages(url),
								session,
								ctx,
								user);

			    		provisioningResponse = service.handleAction(provisioningInformation);
			    	} catch (final OXException e) {
						LOG.error(e.getLocalizedMessage(), e);
						response.setException(MobileProvisioningJsonExceptionCodes.USER_ERROR.create(e, session.getUserId()));
					} catch (final ServiceException e) {
						LOG.error(e.getLocalizedMessage(), e);
						response.setException(MobileProvisioningJsonExceptionCodes.CONFIGURATION_ERROR.create(e));
					} catch (final ActionException e) {
						LOG.error(e.getLocalizedMessage(), e);
			    		response.setException(MobileProvisioningJsonExceptionCodes.ACTION_ERROR.create(e));
					}
			    } else {
			    	provisioningResponse = new ProvisioningResponse(false, "Service " + action + " provisioning is not available.");
			    }

				if (provisioningResponse == null) {
					provisioningResponse = new ProvisioningResponse(false, "Unknown error");
				}

				obj.put("success", provisioningResponse.isSuccess());
				obj.put("message", provisioningResponse.getMessage());
			}
		} catch (final OXException e) {
			LOG.error("Missing or wrong field action in JSON request", e);
			response.setException(e);
		} catch (final JSONException e) {
			LOG.error(e.getLocalizedMessage(), e);
			response.setException(OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e));
		}

		response.setData(obj);

		/*
		 * Close response and flush print writer
		 */
		try {
			ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
		} catch (final JSONException e) {
			//cannot send this to user, so just log it:
			LOG.error(e.getLocalizedMessage(), e);
		}

	}

}
