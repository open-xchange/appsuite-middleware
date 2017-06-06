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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaService;
import com.openexchange.quota.QuotaType;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class GetAction implements AJAXActionService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link GetAction}.
     */
    public GetAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData req, ServerSession session) throws OXException {
        String module = req.getParameter("module");
        String accountID = req.getParameter("account");

        QuotaService quotaService = services.getOptionalService(QuotaService.class);
        if (quotaService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(QuotaService.class.getName());
        }

        try {
            JSONValue result = performRequest(quotaService, session, module, accountID);
            return new AJAXRequestResult(result, "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
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
                    jProvider.put("display_name", StringHelper.valueOf(session.getUser().getLocale()).getString(provider.getDisplayName()));
                    jProvider.put("accounts", jQuotas);
                    allQuotas.put(provider.getModuleID(), jProvider);
                }
            }

            return allQuotas;
        }

        // Module available...
        QuotaProvider provider = quotaService.getProvider(module);
        if (provider == null) {
            throw AjaxExceptionCodes.BAD_REQUEST_CUSTOM.create("No provider exists for module '" + module + "'.");
        }

        if (accountID == null) {
            return buildQuotasJSON(provider.getFor(session));
        }

        // Account available...
        AccountQuota quota = provider.getFor(session, accountID);
        if (quota == null) {
            throw AjaxExceptionCodes.BAD_REQUEST_CUSTOM.create("No account '" + accountID + "' exists for module '" + module + "'.");
        }

        return buildQuotaJSON(quota);
    }

    private static JSONArray buildQuotasJSON(List<AccountQuota> accountQuotas) throws JSONException {
        JSONArray jQuotas = new JSONArray(accountQuotas.size());
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

        JSONObject jQuota = new JSONObject(8);
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
