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

package com.openexchange.xing.json.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.mail.internet.AddressException;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.xing.UserField;
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.access.XingExceptionCodes;
import com.openexchange.xing.access.XingOAuthAccess;
import com.openexchange.xing.access.XingOAuthAccessProvider;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.exception.XingPermissionDeniedException;
import com.openexchange.xing.exception.XingUnlinkedException;
import com.openexchange.xing.json.XingRequest;
import com.openexchange.xing.session.WebAuthSession;

/**
 * {@link AbstractXingAction} - The abstract XING action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractXingAction implements AJAXActionService {

    private static final List<UserField> USER_FIELDS = Arrays.asList(UserField.values());

    private final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractXingAction}.
     */
    protected AbstractXingAction(final ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the service of specified type
     *
     * @param clazz The service's class
     * @return The service or <code>null</code> if absent
     */
    protected <S> S getService(final Class<? extends S> clazz) {
        return services.getOptionalService(clazz);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        if (checkCapability() && !hasXingCapability(session)) {
            throw OXException.noPermissionForModule("xing");
        }
        try {
            return perform(new XingRequest(requestData, session));
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (XingUnlinkedException e) {
            throw XingExceptionCodes.UNLINKED_ERROR.create(e, new Object[0]);
        } catch (XingPermissionDeniedException e) {
            throw XingExceptionCodes.INSUFFICIENT_PRIVILEGES.create(e, new Object[0]);
        } catch (XingException e) {
            throw XingExceptionCodes.XING_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw XingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private boolean hasXingCapability(final ServerSession session) throws OXException {
        final CapabilityService capabilityService = getService(CapabilityService.class);
        return (null != capabilityService && capabilityService.getCapabilities(session).contains("xing"));
    }

    /**
     * Checks if capability should be checked prior to serving action.
     *
     * @return <code>true</code> to check; otherwise <code>false</code>
     */
    protected boolean checkCapability() {
        return true;
    }

    /**
     * Gets the XING OAuth access.
     *
     * @param req The associated XING request
     * @return The XING OAuth access
     * @throws OXException If XING OAuth access cannot be returned
     */
    protected XingOAuthAccess getXingOAuthAccess(final XingRequest req) throws OXException {
        final XingOAuthAccessProvider provider = services.getService(XingOAuthAccessProvider.class);
        if (null == provider) {
            throw ServiceExceptionCode.absentService(XingOAuthAccessProvider.class);
        }

        final ServerSession session = req.getSession();
        final int xingOAuthAccount = provider.getXingOAuthAccount(session);
        return provider.accessFor(xingOAuthAccount, session);
    }

    /**
     * Gets the XING OAuth access.
     *
     * @param token The token identifier
     * @param secret The secret identifier
     * @param session The session
     * @return The XING OAuth access
     * @throws OXException If XING OAuth access cannot be returned
     */
    protected XingOAuthAccess getXingOAuthAccess(final String token, final String secret, final Session session) throws OXException {
        final XingOAuthAccessProvider provider = services.getService(XingOAuthAccessProvider.class);
        if (null == provider) {
            throw ServiceExceptionCode.absentService(XingOAuthAccessProvider.class);
        }

        return provider.accessFor(token, secret, session);
    }

    /**
     * Get an instance to the {@link XingAPI}.
     *
     * @param req The XING request
     * @return A {@link XingAPI} instance
     * @throws OXException If instance cannot be returned
     */
    protected XingAPI<WebAuthSession> getXingAPI(XingRequest req) throws OXException {
        String token = req.getParameter("testToken");
        String secret = req.getParameter("testSecret");
        final XingOAuthAccess xingOAuthAccess;

        if (Strings.isNotEmpty(token) && Strings.isNotEmpty(secret)) {
            xingOAuthAccess = getXingOAuthAccess(token, secret, req.getSession());
        } else {
            xingOAuthAccess = getXingOAuthAccess(req);
        }
        return xingOAuthAccess.getXingAPI();
    }

    /**
     * Get the UserFields from the specified parameter object.
     *
     * @param user_fields as a comma separated String
     * @return a {@link Collection} with {@link UserField}s, or null
     * @throws OXException
     */
    protected Collection<UserField> getUserFields(Object user_fields) throws OXException {
        Collection<UserField> optUserFields = null;
        if (user_fields != null) {
            if (user_fields instanceof String) {
                String[] split = Strings.splitByComma((String) user_fields);
                optUserFields = new ArrayList<UserField>();
                for (String s : split) {
                    try {
                        optUserFields.add(USER_FIELDS.get(Integer.parseInt(s)));
                    } catch (NumberFormatException e) {
                        throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(s, "user_field");
                    }
                }
            }
        }
        return optUserFields;
    }

    /**
     * Get the value of the specified mandatory parameter from the specified {@link XingRequest}.
     *
     * @param request
     * @param param the parameter's name
     * @return the value of the parameter
     * @throws OXException if the parameter is missing from the request.
     */
    protected String getMandatoryStringParameter(XingRequest request, String param) throws OXException {
        String pv = request.getParameter(param);
        if (pv == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(param);
        }
        return pv;
    }

    /**
     * Get the specified integer parameter from the specified request.
     *
     * @param request
     * @param param the parameter's name
     * @return the parameter as integer, or -1 if not present.
     * @throws OXException if the parameter value cannot be parsed to an integer
     */
    protected int getOptIntParameter(XingRequest request, String param) throws OXException {
        String p = request.getParameter(param);
        if (p == null) {
            return -1;
        }
        try {
            return Integer.parseInt(p);
        } catch (NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(param, p);
        }
    }

    /**
     * Validates the value of an email address
     *
     * @param email The E-Mail address to validate
     * @return The Unicode representation of the mail address
     * @throws OXException if the parameter is missing from the request.
     */
    protected String validateMailAddress(String email) throws OXException {
        try {
            return QuotedInternetAddress.toIDN(new QuotedInternetAddress(email, false).getAddress());
        } catch (AddressException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Performs specified XING request.
     *
     * @param req The XING request
     * @return The result
     * @throws OXException If an error occurs
     * @throws JSONException If a JSON error occurs
     * @throws XingException If XING API error occurs
     */
    protected abstract AJAXRequestResult perform(XingRequest req) throws OXException, JSONException, XingException;

}
