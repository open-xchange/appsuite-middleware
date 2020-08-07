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
 *    trademarks of the OX Software GmbH. group of companies.
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

    /**
     * Initializes a new {@link GetCall}.
     *
     * @param module The module identifier (e.g. "share_links", "filestorage", ...) to get quota information for, required if account is set.
     * @param account The account identifier within the module to get quota information for.
     */
    public GetCall(String module, String account) {
        this.module = module;
        this.account = account;
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
    }

    @Override
    protected String getAction() {
        return "get";
    }
}
