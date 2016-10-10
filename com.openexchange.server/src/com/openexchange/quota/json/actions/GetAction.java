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

package com.openexchange.quota.json.actions;

import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.I18nService;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaService;
import com.openexchange.quota.QuotaType;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
@Action(method = RequestMethod.GET, name = "get", description = "Get quota information", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "module", optional = true, description = "The module identifier to get quota information for."),
    @Parameter(name = "account", optional = true, description = "The account identifier within the module to get quota information for, required if account is set.")
}, responseDescription = "A JSON object containing the requested quota information. If no \"module\" was specified, all defined " +
    "modules quotas are set in the JSON object, each one mapped to it's module identifier. If the quota from a \"module\" was " +
    "requested, a JSON array containing all account quotas of this module are returned. If both a \"module\" and \"account\" were " +
    "requested, a JSON object representing the account quota is returned.")
public class GetAction implements AJAXActionService {

    private final BundleContext context;

    public GetAction(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData req, ServerSession session) throws OXException {
        String module = req.getParameter("module");
        String accountID = req.getParameter("account");

        ServiceReference<QuotaService> quotaServiceRef = context.getServiceReference(QuotaService.class);
        if (quotaServiceRef == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(QuotaService.class.getName());
        } else {
            QuotaService quotaService = context.getService(quotaServiceRef);
            try {
                if (quotaService == null) {
                    throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(QuotaService.class.getName());
                } else {
                    JSONValue result = performRequest(quotaService, session, module, accountID);
                    return new AJAXRequestResult(result, "json");
                }
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e);
            } finally {
                if (quotaServiceRef != null & quotaService != null) {
                    context.ungetService(quotaServiceRef);
                }
            }
        }
    }

    private JSONValue performRequest(QuotaService quotaService, ServerSession session, String module, String accountID) throws JSONException, OXException {
        if (module == null) {
            JSONObject allQuotas = new JSONObject();
            for (QuotaProvider provider : quotaService.getAllProviders()) {
                List<AccountQuota> accountQuotas = provider.getFor(session);
                JSONArray jQuotas = buildQuotasJSON(accountQuotas);
                if (!jQuotas.isEmpty()) {
                    JSONObject jProvider = new JSONObject();
                    jProvider.put("display_name", localize(provider.getDisplayName(), session));
                    jProvider.put("accounts", jQuotas);
                    allQuotas.put(provider.getModuleID(), jProvider);
                }
            }

            return allQuotas;
        } else {
            QuotaProvider provider = quotaService.getProvider(module);
            if (provider == null) {
                throw AjaxExceptionCodes.BAD_REQUEST_CUSTOM.create("No provider exists for module '" + module + "'.");
            }

            if (accountID == null) {
                return buildQuotasJSON(provider.getFor(session));
            } else {
                AccountQuota quota = provider.getFor(session, accountID);
                if (quota == null) {
                    throw AjaxExceptionCodes.BAD_REQUEST_CUSTOM.create("No account '" + accountID + "' exists for module '" + module + "'.");
                }

                return buildQuotaJSON(quota);
            }
        }
    }

    private String localize(String str, ServerSession session) throws OXException {
        String localized = null;
        try {
            for (ServiceReference<I18nService> ref : context.getServiceReferences(I18nService.class, null)) {
                Locale locale = (Locale) ref.getProperty(I18nService.LANGUAGE);
                if (locale != null && locale.equals(session.getUser().getLocale())) {
                    I18nService i18nService = context.getService(ref);
                    if (i18nService != null) {
                        localized = i18nService.getLocalized(str);
                        context.ungetService(ref);
                        break;
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            throw new OXException(e);
        }

        if (localized == null) {
            return str;
        }

        return localized;
    }

    private static JSONArray buildQuotasJSON(List<AccountQuota> accountQuotas) throws JSONException {
        JSONArray jQuotas = new JSONArray();
        for (AccountQuota accountQuota : accountQuotas) {
            JSONObject jQuota = buildQuotaJSON(accountQuota);
            if (jQuota != null) {
                jQuotas.put(jQuota);
            }
        }

        return jQuotas;
    }

    private static JSONObject buildQuotaJSON(AccountQuota accountQuota) throws JSONException {
        Quota sizeQuota = accountQuota.getQuota(QuotaType.SIZE);
        Quota amountQuota = accountQuota.getQuota(QuotaType.AMOUNT);
        if ((null == sizeQuota || Quota.UNLIMITED == sizeQuota.getLimit()) &&
            (null == amountQuota || Quota.UNLIMITED == amountQuota.getLimit())) {
            return null;
        }
        JSONObject jQuota = new JSONObject();
        jQuota.put("account_id", accountQuota.getAccountID());
        jQuota.put("account_name", accountQuota.getAccountName());
        if (null != amountQuota && amountQuota.getLimit() > 0) {
            jQuota.put("countquota", amountQuota.getLimit());
            jQuota.put("countuse", amountQuota.getUsage());
        }
        if (null != sizeQuota && sizeQuota.getLimit() > 0) {
            jQuota.put("quota", sizeQuota.getLimit());
            jQuota.put("use", sizeQuota.getUsage());
        }
        return jQuota;
    }

}
