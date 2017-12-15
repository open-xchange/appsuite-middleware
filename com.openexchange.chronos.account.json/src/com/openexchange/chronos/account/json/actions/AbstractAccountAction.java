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

package com.openexchange.chronos.account.json.actions;

import static com.openexchange.chronos.account.json.CalendarAccountFields.CONFIGURATION;
import static com.openexchange.chronos.account.json.CalendarAccountFields.ID;
import static com.openexchange.chronos.account.json.CalendarAccountFields.PROVIDER;
import static com.openexchange.chronos.account.json.CalendarAccountFields.TIMESTAMP;
import static com.openexchange.java.Autoboxing.L;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link AbstractAccountAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
abstract class AbstractAccountAction implements AJAXActionService {

    protected final String PARAMETER_PROVIDER_ID = "providerId";
    protected final String PARAMETER_ACCOUNT_ID = "accountId";
    private ServiceLookup services;

    /**
     * Initializes a new {@link AbstractAccountAction}.
     *
     * @param services The {@link ServiceLookup} instance
     */
    public AbstractAccountAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the service of specified type
     *
     * @param clazz The service's class
     * @return The service
     * @throws IllegalStateException If an error occurs while returning the demanded service
     */
    <S extends Object> S getService(final Class<? extends S> clazz) {
        if (null == services) {
            throw new IllegalStateException("Missing ServiceLookup instance. Bundle \"com.openexchange.chronos.account.json\" not started?");
        }
        return services.getService(clazz);
    }

    protected CalendarAccountService getAccountService() throws OXException {
        CalendarAccountService accountService = getService(CalendarAccountService.class);
        if (null == accountService) {
            throw ServiceExceptionCode.absentService(CalendarAccountService.class);
        }
        return accountService;
    }

    protected JSONObject serializeAccount(CalendarAccount account) throws OXException {
        try {
            return new JSONObject()
                .put(ID, account.getAccountId())
                .put(PROVIDER, account.getProviderId())
                .putOpt(TIMESTAMP, null != account.getLastModified() ? L(account.getLastModified().getTime()) : null)
                .putOpt(CONFIGURATION, account.getUserConfiguration())
            ;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    protected JSONObject serializeProvider(CalendarProvider provider, Locale locale) throws OXException {
        try {
            return new JSONObject()
                .put(ID, provider.getId())
                .put("name", provider.getDisplayName(locale))
            ;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Get the a parameter
     *
     * @param requestData The {@link AJAXRequestData}
     * @param parameter The parameter name
     * @param coerceTo The type the parameter should be interpreted as
     * @return The parameter
     * @throws OXException If the parameter can't be found
     */
    protected <T> T getParameterSafe(AJAXRequestData requestData, String parameter, Class<T> coerceTo) throws OXException {
        T param = requestData.getParameter(parameter, coerceTo);
        if (null == param) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(parameter);
        }
        return param;
    }

}
