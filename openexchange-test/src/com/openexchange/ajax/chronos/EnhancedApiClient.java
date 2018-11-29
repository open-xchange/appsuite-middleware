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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;

/**
 * {@link EnhancedApiClient}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class EnhancedApiClient extends ApiClient {

    /**
     * Serialize the given Java object into string entity according the given
     * Content-Type (only JSON is supported for now).
     */
    @Override
    public Entity<?> serialize(final Object obj, final Map<String, Object> formParams, final String contentType) throws ApiException {
        Entity<?> entity = null;
        if (contentType.startsWith("multipart/form-data")) {
            // Extract attachments (if any)
            final Map<String, String> attachments = extractAttachments(formParams);
            final MultiPart multiPart = new MultiPart();
            for (final Entry<String, Object> param : formParams.entrySet()) {
                FormDataBodyPart bodyPart;
                if (param.getValue() instanceof File) {
                    final File file = (File) param.getValue();
                    final FormDataContentDisposition contentDisp = FormDataContentDisposition.name(param.getKey()).fileName(file.getName()).size(file.length()).build();
                    bodyPart = new FormDataBodyPart(contentDisp, file, MediaType.APPLICATION_OCTET_STREAM_TYPE);

                    // Set the contentId of the attachments (if exists)
                    final String contentId = attachments.get(file.getName());
                    if (contentId != null && !contentId.isEmpty()) {
                        bodyPart.getHeaders().add("Content-ID", contentId);
                    }
                } else {
                    final FormDataContentDisposition contentDisp = FormDataContentDisposition.name(param.getKey()).build();
                    bodyPart = new FormDataBodyPart(contentDisp, parameterToString(param.getValue()));
                }

                multiPart.bodyPart(bodyPart);
            }
            entity = Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE);
        } else if (contentType.startsWith("application/x-www-form-urlencoded")) {
            final Form form = new Form();
            for (final Entry<String, Object> param : formParams.entrySet()) {
                form.param(param.getKey(), parameterToString(param.getValue()));
            }
            entity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        } else {
            // We let jersey handle the serialization
            entity = Entity.entity(obj, contentType);
        }
        return entity;
    }

    /**
     * Create a {@link Map} with the attachments (if any)
     *
     * @param formParams The form parameters with the attachments
     * @return A {@link Map} with the attachment name and contentId
     * @throws ApiException if an API error is occurred
     */
    private Map<String, String> extractAttachments(final Map<String, Object> formParams) throws ApiException {
        final String json0 = (String) formParams.get("json_0");
        final Map<String, String> tmp = new HashMap<>();
        if (json0 != null) {
            JSONObject json;
            try {
                json = new JSONObject(json0);
            } catch (final JSONException e) {
                throw new ApiException(400, "The required parameter 'json0' when calling createEventWithAttachments is not of type JSONObject. " + json0);
            }
            JSONArray attachments = json.optJSONArray("attachments");
            if (attachments == null || attachments.isEmpty()) {
                JSONObject optJSONObject = json.optJSONObject("event");
                if (optJSONObject != null) {
                    attachments = optJSONObject.optJSONArray("attachments");
                    if (attachments == null || attachments.isEmpty()) {
                        throw new ApiException(400, "Missing the required field 'attachments' when calling createEventWithAttachments");
                    }
                } else {
                    throw new ApiException(400, "Missing the required field 'attachments' when calling createEventWithAttachments");
                }
            }

            try {
                for (int index = 0; index < attachments.length(); index++) {
                    final JSONObject attachment = attachments.getJSONObject(index);
                    tmp.put(attachment.getString("filename"), attachment.optString("cid"));
                }
            } catch (final JSONException e) {
                throw new ApiException(400, "A JSON error occurred: " + e.getMessage());
            }
        }
        return tmp;
    }
}
