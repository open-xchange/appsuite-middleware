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

package com.openexchange.api.client.common.calls.infostore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.ApiClientUtils;
import com.openexchange.api.client.common.calls.AbstractPutCall;
import com.openexchange.api.client.common.parser.AbstractHttpResponseParser;
import com.openexchange.api.client.common.parser.CommonApiResponse;
import com.openexchange.exception.OXException;

import static com.openexchange.java.Autoboxing.I;

/**
 * {@link DetachCall} - Deletes version of an infoitem
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class DetachCall extends AbstractPutCall<List<Integer>> {

    private final String id;
    private final String folder;
    private final long timestamp;
    private final String pushToken;

    private final int[] versionsToDelete;

    /**
     * Initializes a new {@link DetachCall}.
     *
     * @param id The ID of the file to delte the versions for
     * @param folder The folder ID of the file
     * @param timestamp The timestamp / sequencenumber
     * @param versionsToDelete A list of versions to delete
     */
    public DetachCall(String id, String folder, long timestamp, int[] versionsToDelete) {
        this(id, folder, timestamp, versionsToDelete, null);
    }

    /**
     * Initializes a new {@link DetachCall}.
     *
     * @param id The ID of the file to delte the versions for
     * @param folder The folder ID of the file
     * @param timestamp The timestamp / sequencenumber
     * @param versionsToDelete A list of versions to delete
     * @param pushToken The optional drive push-token
     */
    public DetachCall(String id, String folder, long timestamp, int[] versionsToDelete, @Nullable String pushToken) {
        this.id = id;
        this.folder = folder;
        this.timestamp = timestamp;
        this.versionsToDelete = versionsToDelete;
        this.pushToken = pushToken;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/infostore";
    }

    @Override
    @Nullable
    public HttpEntity getBody() throws OXException, JSONException {
        JSONArray jsonArray = new JSONArray(versionsToDelete.length);
        for(int i=0; i < versionsToDelete.length; i++) {
            jsonArray.put(versionsToDelete[i]);
        }
        return ApiClientUtils.createJsonBody(jsonArray);
    }

    @Override
    public HttpResponseParser<List<Integer>> getParser() {
        return new AbstractHttpResponseParser<List<Integer>>() {

            @Override
            public List<Integer> parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException, JSONException {
                JSONArray jsonArray = commonResponse.getJSONArray();
                List<Integer> ret = new ArrayList<>(jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++) {
                    if(!jsonArray.isNull(i)) {
                        ret.add(I(jsonArray.getInt(i)));
                    }
                }
                return ret;
            }
        };
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("id", id);
        parameters.put("folder", folder);
        parameters.put("timestamp", String.valueOf(timestamp));
        putIfNotEmpty(parameters, "pushToken", pushToken);
    }

    @Override
    protected String getAction() {
        return "detach";
    }
}
