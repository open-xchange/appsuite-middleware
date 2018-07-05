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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.ajax.chronos;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.GenericType;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.invoker.Pair;
import com.openexchange.testing.httpclient.modules.ChronosApi;

/**
 * {@link EnhancedChronosApi} - This enhanced API class overrides the {@link #createEventWithAttachments(String, String, String, File, Boolean, Boolean)}
 * to simply decorate the request with the 'plainJson' parameter. This is only used to get a proper JSON response instead of the regular HTML response
 * and to be able to use the already existing parsers.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class EnhancedChronosApi extends ChronosApi {

    private EnhancedApiClient enhancedApiClient;

    /**
     * Initialises a new {@link EnhancedChronosApi}.
     */
    public EnhancedChronosApi() {
        super();
    }

    /**
     * Initialises a new {@link EnhancedChronosApi}.
     *
     * @param apiClient
     */
    public EnhancedChronosApi(EnhancedApiClient apiClient) {
        super(apiClient);
        enhancedApiClient = apiClient;
    }

    /**
     * Copied from the original {@link #createEventWithAttachments(String, String, String, File, Boolean, Boolean)}. The request is enhanced
     * with the API parameter<code>plainJson</code> to signal the response renderer to return a plain json object instead of the regular
     * HTML response. It also takes multiple files as parameters
     */
    public String createEventWithAttachments(String session, String folder, String json0, List<File> files, Boolean ignoreConflicts, Boolean sendInternalNotifications) throws ApiException {
        Object localVarPostBody = null;

        // verify the required parameter 'session' is set
        if (session == null) {
            throw new ApiException(400, "Missing the required parameter 'session' when calling createEventWithAttachments");
        }

        // verify the required parameter 'folder' is set
        if (folder == null) {
            throw new ApiException(400, "Missing the required parameter 'folder' when calling createEventWithAttachments");
        }

        // verify the required parameter 'json0' is set
        if (json0 == null) {
            throw new ApiException(400, "Missing the required parameter 'json0' when calling createEventWithAttachments");
        }

        // verify the required parameter 'file0' is set
        if (files == null) {
            throw new ApiException(400, "Missing the required parameter 'file0' when calling createEventWithAttachments");
        }

        // create path and map variables
        String localVarPath = "/chronos?action=new".replaceAll("\\{format\\}", "json");

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "session", session));
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "folder", folder));
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "plainJson", true)); //Set this parameter explicitly to 'true' to return a regular json object instead of text/html
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "ignore_conflicts", ignoreConflicts));
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "sendInternalNotifications", sendInternalNotifications));

        if (json0 != null) {
            localVarFormParams.put("json_0", json0);
        }

        int index = 0;
        for (File file : files) {
            if (file != null) {
                localVarFormParams.put("file_" + index++, file);
            }
        }

        final String[] localVarAccepts = { "application/json" };
        final String localVarAccept = enhancedApiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = { "multipart/form-data" };
        final String localVarContentType = enhancedApiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {};

        GenericType<String> localVarReturnType = new GenericType<String>() {
            // empty
        };
        return enhancedApiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Copied from the original {@link #createEventWithAttachments(String, String, String, File, Boolean, Boolean)}. The request is enhanced
     * with the API parameter<code>plainJson</code> to signal the response renderer to return a plain json object instead of the regular
     * HTML response.
     */
    @Override
    public String createEventWithAttachments(String session, String folder, String json0, File file0, Boolean ignoreConflicts, Boolean sendInternalNotifications, Boolean extendedEntities) throws ApiException {
        return createEventWithAttachments(session, folder, json0, Collections.singletonList(file0), ignoreConflicts, sendInternalNotifications);
    }

    /**
     * Copied from the original {@link #updateEventWithAttachments(String, String, String, Long, String, File, String, Boolean, Boolean)}. The request is enhanced
     * with the API parameter<code>plainJson</code> to signal the response renderer to return a plain json object instead of the regular
     * HTML response.
     */
    @Override
    public String updateEventWithAttachments(String session, String folder, String id, Long timestamp, String json0, File file0, String recurrenceId, Boolean ignoreConflicts, Boolean sendInternalNotifications, Boolean extendedEntities) throws ApiException {
        Object localVarPostBody = null;

        // verify the required parameter 'session' is set
        if (session == null) {
            throw new ApiException(400, "Missing the required parameter 'session' when calling updateEventWithAttachments");
        }

        // verify the required parameter 'folder' is set
        if (folder == null) {
            throw new ApiException(400, "Missing the required parameter 'folder' when calling updateEventWithAttachments");
        }

        // verify the required parameter 'id' is set
        if (id == null) {
            throw new ApiException(400, "Missing the required parameter 'id' when calling updateEventWithAttachments");
        }

        // verify the required parameter 'timestamp' is set
        if (timestamp == null) {
            throw new ApiException(400, "Missing the required parameter 'timestamp' when calling updateEventWithAttachments");
        }

        // verify the required parameter 'json0' is set
        if (json0 == null) {
            throw new ApiException(400, "Missing the required parameter 'json0' when calling updateEventWithAttachments");
        }

        // verify the required parameter 'file0' is set
        if (file0 == null) {
            throw new ApiException(400, "Missing the required parameter 'file0' when calling updateEventWithAttachments");
        }

        // create path and map variables
        String localVarPath = "/chronos?action=update".replaceAll("\\{format\\}", "json");

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "session", session));
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "folder", folder));
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "id", id));
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "plainJson", true)); //Set this parameter explicitly to 'true' to return a regular json object instead of text/html
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "recurrenceId", recurrenceId));
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "ignore_conflicts", ignoreConflicts));
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "sendInternalNotifications", sendInternalNotifications));
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "timestamp", timestamp));

        if (json0 != null) {
            localVarFormParams.put("json_0", json0);
        }
        if (file0 != null) {
            localVarFormParams.put("file_0", file0);
        }

        final String[] localVarAccepts = { "application/json" };
        final String localVarAccept = enhancedApiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = { "multipart/form-data" };
        final String localVarContentType = enhancedApiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {};

        GenericType<String> localVarReturnType = new GenericType<String>() {
            //empty
        };
        return enhancedApiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
}
