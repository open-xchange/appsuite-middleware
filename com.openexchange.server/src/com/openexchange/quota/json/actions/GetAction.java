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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import com.openexchange.exception.OXException;
import com.openexchange.i18n.I18nService;
import com.openexchange.quota.usage.QuotaAndUsage;
import com.openexchange.quota.usage.QuotaAndUsageProvider;
import com.openexchange.quota.usage.QuotaAndUsageService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
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

        ServiceReference<QuotaAndUsageService> qausRef = context.getServiceReference(QuotaAndUsageService.class);
        if (qausRef == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(QuotaAndUsageService.class.getName());
        } else {
            QuotaAndUsageService quotaAndUsageService = context.getService(qausRef);
            try {
                if (quotaAndUsageService == null) {
                    throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(QuotaAndUsageService.class.getName());
                } else {
                    JSONValue result = performRequest(quotaAndUsageService, session, module, accountID);
                    return new AJAXRequestResult(result, "json");
                }
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e);
            } finally {
                if (qausRef != null & quotaAndUsageService != null) {
                    context.ungetService(qausRef);
                }
            }
        }
    }

    private JSONValue performRequest(QuotaAndUsageService quotaAndUsageService, ServerSession session, String module, String accountID) throws JSONException, OXException {
        if (module == null) {
            JSONObject allQuotas = new JSONObject();
            for (QuotaAndUsageProvider provider : quotaAndUsageService.getAllProviders()) {
                List<QuotaAndUsage> quotasAndUsages = provider.getFor(session);
                JSONArray jQuotas = buildQuotasJSON(quotasAndUsages);
                if (!jQuotas.isEmpty()) {
                    JSONObject jProvider = new JSONObject();
                    jProvider.put("display_name", localize(provider.getDisplayName(), session));
                    jProvider.put("accounts", jQuotas);
                    allQuotas.put(provider.getModuleID(), jProvider);
                }
            }

            return allQuotas;
        } else {
            QuotaAndUsageProvider provider = quotaAndUsageService.getProvider(module);
            if (provider == null) {
                throw AjaxExceptionCodes.BAD_REQUEST_CUSTOM.create("No provider exists for module '" + module + "'.");
            }

            if (accountID == null) {
                return buildQuotasJSON(provider.getFor(session));
            } else {
                QuotaAndUsage quotaAndUsage = provider.getFor(session, accountID);
                if (quotaAndUsage == null) {
                    throw AjaxExceptionCodes.BAD_REQUEST_CUSTOM.create("No account '" + accountID + "' exists for module '" + module + "'.");
                }

                return buildQuotaJSON(quotaAndUsage);
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

    private static JSONArray buildQuotasJSON(List<QuotaAndUsage> quotasAndUsages) throws JSONException {
        JSONArray jQuotas = new JSONArray();
        for (QuotaAndUsage quotaAndUsage : quotasAndUsages) {
            JSONObject jQuota = buildQuotaJSON(quotaAndUsage);
            if (jQuota != null) {
                jQuotas.put(jQuota);
            }
        }

        return jQuotas;
    }

    private static JSONObject buildQuotaJSON(QuotaAndUsage quotaAndUsage) throws JSONException {
        if (quotaAndUsage.hasStorageQuota() || quotaAndUsage.hasObjectQuota()) {
            JSONObject jQuota = new JSONObject();
            jQuota.put("account_id", quotaAndUsage.getAccountID());
            jQuota.put("account_name", quotaAndUsage.getAccountName());
            if (quotaAndUsage.hasStorageQuota()) {
                jQuota.put("quota", quotaAndUsage.getMaxStorage());
                jQuota.put("use", quotaAndUsage.getUsedStorage());
            }

            if (quotaAndUsage.hasObjectQuota()) {
                jQuota.put("countquota", quotaAndUsage.getMaxObjects());
                jQuota.put("countuse", quotaAndUsage.getUsedObjects());
            }

            return jQuota;
        }

        return null;
    }

}
