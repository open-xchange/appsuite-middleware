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

package com.openexchange.api.client.common.calls.quota;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.DefaultAccountQuota;
import com.openexchange.quota.QuotaType;

/**
 * {@link GetCall} - Gets quota information
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class GetCall extends AbstractGetCall<List<AccountQuota>> {

    private final String module;
    private final String account;
    private final String folder;

    /**
     * Initializes a new {@link GetCall}.
     *
     * @param module The module identifier (e.g. "share_links", "filestorage", ...) to get quota information for, required if account is set.
     * @param account The account identifier within the module to get quota information for.
     */
    public GetCall(String module, String account) {
        this(module, account, null);
    }

    /**
     * Initializes a new {@link GetCall}.
     *
     * @param module The module identifier (e.g. "share_links", "filestorage", ...) to get quota information for, required if account is set.
     * @param account The account identifier within the module to get quota information for.
     * @param folder The ID of the folder to query the quota for
     */
    public GetCall(String module, String account, String folder) {
        this.module = module;
        this.account = account;
        this.folder = folder;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/quota";
    }

    @Override
    public HttpResponseParser<List<AccountQuota>> getParser() {
        return new AbstractHttpResponseParser<List<AccountQuota>>() {

            private static final String JSON_ACCOUNT_ID = "account_id";
            private static final String JSON_ACCOUNT_NAME = "account_name";
            private static final String JSON_COUNTQUOTA = "countquota";
            private static final String JSON_COUNTUSE = "countuse";
            private static final String JSON_QUOTA = "quota";
            private static final String JSON_USE = "use";

            /**
             * Internal method to parse the given {@link JSONObject} into an {@link AccountQuota} object
             *
             * @param json The {@link JSONobject} to parse
             * @return The parse {@link AccountQuota} object
             * @throws OXException
             * @throws JSONException
             */
            private AccountQuota parseSingle(JSONObject json) throws OXException, JSONException {
                String accountID = null, accountName = null;
                if (json.has(JSON_ACCOUNT_ID)) {
                    accountID = json.getString(JSON_ACCOUNT_ID);
                } else {
                    throw ApiClientExceptions.UNEXPECTED_ERROR.create("The field account_id is missing in the response object.");
                }

                if (json.has(JSON_ACCOUNT_NAME)) {
                    accountName = json.getString(JSON_ACCOUNT_NAME);
                } else {
                    throw ApiClientExceptions.UNEXPECTED_ERROR.create("The field account_name is missing in the response object.");
                }

                DefaultAccountQuota quota = new DefaultAccountQuota(accountID, accountName);
                if (json.has(JSON_COUNTQUOTA) && json.has(JSON_COUNTUSE)) {
                    quota.addQuota(QuotaType.AMOUNT, json.getLong(JSON_COUNTQUOTA), json.getLong(JSON_COUNTUSE));
                }
                if (json.has(JSON_QUOTA) && json.has(JSON_USE)) {
                    quota.addQuota(QuotaType.SIZE, json.getLong(JSON_QUOTA), json.getLong(JSON_USE));
                }
                return quota;
            }

            /**
             * Internal method to parse the given {@link JSONArray} into a list of {@link AccountQuota}
             *
             * @param jsonArray The {@link JSONArray} to parse
             * @return The parsed list of {@link AccountQuota} objects
             * @throws OXException
             */
            private List<AccountQuota> parseMultiple(JSONArray jsonArray) throws OXException {
                try {
                    List<AccountQuota> ret = new ArrayList<AccountQuota>(jsonArray.length());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        ret.add(parseSingle(jsonArray.getJSONObject(i)));
                    }
                    return ret;
                } catch (JSONException e) {
                    throw ApiClientExceptions.JSON_ERROR.create(e, e.getMessage());
                }
            }

            @Override
            public List<AccountQuota> parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException, JSONException {
                //Response can either be a JSONObject or a JSONArray
                if (commonResponse.isJSONObject()) {
                    return Collections.singletonList(parseSingle(commonResponse.getJSONObject()));
                }
                return parseMultiple(commonResponse.getJSONArray());
            }
        };
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        putIfNotEmpty(parameters, "module", module);
        putIfNotEmpty(parameters, "account", account);
        putIfNotEmpty(parameters, "folder", folder);
    }

    @Override
    protected String getAction() {
        return "get";
    }
}
