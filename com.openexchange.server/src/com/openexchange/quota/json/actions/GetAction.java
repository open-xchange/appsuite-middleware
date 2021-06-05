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

package com.openexchange.quota.json.actions;

import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.unified.UnifiedQuotaService;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.AccountQuotas;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaService;
import com.openexchange.quota.QuotaType;
import com.openexchange.quota.json.QuotaAJAXRequest;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class GetAction extends AbstractUnifiedQuotaAction {

    /**
     * Initializes a new {@link GetAction}.
     */
    public GetAction(ServiceListing<UnifiedQuotaService> unifiedQuotaServices, ServiceLookup services) {
        super(unifiedQuotaServices, services);
    }

    @Override
    protected AJAXRequestResult perform(QuotaAJAXRequest req) throws OXException, JSONException {
        String module = req.getParameter("module");
        String accountID = req.getParameter("account");
        String folder = req.getParameter("folder");

        QuotaService quotaService = services.getOptionalService(QuotaService.class);
        if (quotaService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(QuotaService.class.getName());
        }

        try {
            List<OXException> warnings = new LinkedList<>();
            JSONValue result = performRequest(quotaService, req.getSession(), module, accountID, folder, warnings);
            return new AJAXRequestResult(result, "json").addWarnings(warnings);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        }
    }

    private JSONValue performRequest(QuotaService quotaService, ServerSession session, String module, String accountID, String folder, List<OXException> warnings) throws JSONException, OXException {
        if (module == null) {
            JSONObject allQuotas = new JSONObject();
            for (QuotaProvider provider : quotaService.getAllProviders()) {
                AccountQuotas accountQuotas = provider.getFor(session);
                warnings.addAll(accountQuotas.getWarnings());
                JSONArray jQuotas = buildQuotasJSON(accountQuotas);
                if (!jQuotas.isEmpty()) {
                    JSONObject jProvider = new JSONObject();
                    jProvider.put("display_name", StringHelper.valueOf(session.getUser().getLocale()).getString(provider.getDisplayName()));
                    jProvider.put("accounts", jQuotas);
                    allQuotas.put(provider.getModuleID(), jProvider);
                }
            }

            UnifiedQuotaService unifiedQuotaService = getHighestRankedBackendService(session.getUserId(), session.getContextId());
            if (unifiedQuotaService != null) {
                JSONObject jQuota = buildUnifiedQuotaJSON(unifiedQuotaService, session);

                JSONArray jQuotas = new JSONArray(1).put(jQuota);

                JSONObject jProvider = new JSONObject();
                jProvider.put("display_name", StringHelper.valueOf(session.getUser().getLocale()).getString("Unified Quota"));
                jProvider.put("accounts", jQuotas);
                allQuotas.put(UnifiedQuotaService.MODE, jProvider);
            }

            return allQuotas;
        }

        // Module available...
        if (UnifiedQuotaService.MODE.equals(module)) {
            UnifiedQuotaService unifiedQuotaService = getHighestRankedBackendService(session.getUserId(), session.getContextId());
            if (unifiedQuotaService == null) {
                throw ServiceExceptionCode.absentService(UnifiedQuotaService.class);
            }

            if (accountID == null) {
                JSONObject jQuota = buildUnifiedQuotaJSON(unifiedQuotaService, session);
                return new JSONArray(1).put(jQuota);
            }

            if (!UnifiedQuotaService.MODE.equals(accountID)) {
                throw AjaxExceptionCodes.BAD_REQUEST_CUSTOM.create("No account '" + accountID + "' exists for module '" + module + "'.");
            }

            return buildUnifiedQuotaJSON(unifiedQuotaService, session);
        }

        QuotaProvider provider = quotaService.getProvider(module);
        if (provider == null) {
            throw AjaxExceptionCodes.BAD_REQUEST_CUSTOM.create("No provider exists for module '" + module + "'.");
        }

        if (accountID == null) {
            AccountQuotas accountQuotas = provider.getFor(session);
            warnings.addAll(accountQuotas.getWarnings());
            return buildQuotasJSON(accountQuotas);
        }

        // Account available...
        AccountQuota quota = provider.getFor(session, accountID, folder);
        if (quota == null) {
            throw AjaxExceptionCodes.BAD_REQUEST_CUSTOM.create("No account '" + accountID + "' exists for module '" + module + "'.");
        }

        return buildQuotaJSON(quota);
    }

    private JSONObject buildUnifiedQuotaJSON(UnifiedQuotaService unifiedQuotaService, ServerSession session) throws JSONException, OXException {
        JSONObject jQuota = new JSONObject(8);
        jQuota.put("account_id", UnifiedQuotaService.MODE);
        jQuota.put("account_name", "Unified Quota");
        jQuota.put("quota", unifiedQuotaService.getLimit(session.getUserId(), session.getContextId()));
        jQuota.put("use", unifiedQuotaService.getUsage(session.getUserId(), session.getContextId()).getTotal());
        return jQuota;
    }

    private static JSONArray buildQuotasJSON(AccountQuotas accountQuotas) throws JSONException {
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
