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

package com.openexchange.mobile.configuration.json.servlet;

import static com.openexchange.java.Autoboxing.I;
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
import com.openexchange.user.User;

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
		} catch (OXException e) {
			LOG.error("Missing or wrong field action in JSON request", e);
			response.setException(e);
		} catch (JSONException e) {
			LOG.error(e.getLocalizedMessage(), e);
            response.setException(OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e));
		}

		response.setData(obj);

		/*
		 * Close response and flush print writer
		 */
		try {
			ResponseWriter.write(response, resp.getWriter(), localeFrom(getSessionObject(request)));
		} catch (JSONException e) {
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
			    	} catch (OXException e) {
						LOG.error(e.getLocalizedMessage(), e);
						response.setException(MobileProvisioningJsonExceptionCodes.USER_ERROR.create(e, I(session.getUserId())));
					} catch (ServiceException e) {
						LOG.error(e.getLocalizedMessage(), e);
						response.setException(MobileProvisioningJsonExceptionCodes.CONFIGURATION_ERROR.create(e));
					} catch (ActionException e) {
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
		} catch (OXException e) {
			LOG.error("Missing or wrong field action in JSON request", e);
			response.setException(e);
		} catch (JSONException e) {
			LOG.error(e.getLocalizedMessage(), e);
			response.setException(OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e));
		}

		response.setData(obj);

		/*
		 * Close response and flush print writer
		 */
		try {
			ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
		} catch (JSONException e) {
			//cannot send this to user, so just log it:
			LOG.error(e.getLocalizedMessage(), e);
		}

	}

}
