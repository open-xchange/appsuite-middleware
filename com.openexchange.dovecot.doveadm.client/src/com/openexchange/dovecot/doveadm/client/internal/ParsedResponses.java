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

package com.openexchange.dovecot.doveadm.client.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.dovecot.doveadm.client.DoveAdmClientExceptionCodes;
import com.openexchange.dovecot.doveadm.client.DoveAdmResponse;
import com.openexchange.dovecot.doveadm.client.Result;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link ParsedResponses}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ParsedResponses implements Iterable<DoveAdmResponse> {

    /**
     * Parses the <code>ParsedResponses</code> instance from specified JSON array.
     *
     * @param jResponses The JSON array
     * @return The <code>ParsedResponses</code> instance
     * @throws OXException If parsing fails
     */
    public static ParsedResponses valueFor(JSONArray jResponses) throws OXException {
        if (null == jResponses) {
            return null;
        }

        try {
            int numResponses = jResponses.length();
            List<DoveAdmResponse> responses = new ArrayList<>(numResponses);
            Map<String, DoveAdmResponse> taggedResponses = new LinkedHashMap<>(numResponses);
            List<DoveAdmResponse> untaggedResponses = null;

            for (int k = 0; k < numResponses; k++) {
                JSONArray jResponse = jResponses.getJSONArray(k);

                String optionalIdentifier = jResponse.length() > 2 ? jResponse.optString(2, null) : null;
                DoveAdmResponse doveAdmResponse;
                {
                    String responseType = jResponse.getString(0);
                    if (DoveAdmResponse.TYPE_DATA_RESPONSE.equals(responseType)) {
                        JSONArray jResults = jResponse.getJSONArray(1);

                        int numResults = jResults.length();
                        if (numResults <= 0) {
                            doveAdmResponse = new DoveAdmDataResponseImpl(Collections.<Result> emptyList(), optionalIdentifier);
                        } else {
                            List<Result> results = new ArrayList<>(numResults);
                            for (int j = numResults, i = 0; j-- > 0; i++) {
                                JSONObject jResult = jResults.getJSONObject(i);
                                ResultImpl.Builder builder = ResultImpl.builder();
                                for (Entry<String, Object> resultEntry : jResult.entrySet()) {
                                    builder.setParameter(resultEntry.getKey(), (String) resultEntry.getValue());
                                }
                                results.add(builder.build());
                            }
                            doveAdmResponse = new DoveAdmDataResponseImpl(results, optionalIdentifier);
                        }
                    } else if (DoveAdmResponse.TYPE_ERROR_RESPONSE.equals(responseType)) {
                        JSONObject jError = jResponse.getJSONObject(1);
                        String type = jError.getString("type");
                        int exitCode = jError.getInt("exitCode");
                        doveAdmResponse = new DoveAdmErrorResponseImpl(type, exitCode, optionalIdentifier);
                    } else {
                        // Unknown response identifier
                        throw DoveAdmClientExceptionCodes.UNKNOWN_RESPONSE_TYPE.create(responseType);
                    }
                }

                responses.add(doveAdmResponse);
                if (Strings.isEmpty(optionalIdentifier)) {
                    if (null == untaggedResponses) {
                        untaggedResponses = new ArrayList<>(4);
                    }
                    untaggedResponses.add(doveAdmResponse);
                } else {
                    taggedResponses.put(optionalIdentifier, doveAdmResponse);
                }
            }

            return new ParsedResponses(responses, taggedResponses, untaggedResponses);
        } catch (JSONException e) {
            throw DoveAdmClientExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    // ----------------------------------------------------------------------------

    /** The responses listing as appeared in JSON array */
    private final List<DoveAdmResponse> responses;

    /** The tagged responses */
    private final Map<String, DoveAdmResponse> taggedResponses;

    /** The untagged or unsolicited responses */
    private final List<DoveAdmResponse> untaggedResponses;

    /**
     * Initializes a new {@link ParsedResponses}.
     */
    ParsedResponses(List<DoveAdmResponse> responses, Map<String, DoveAdmResponse> taggedResponses, List<DoveAdmResponse> untaggedResponses) {
        super();
        this.responses = responses;
        this.taggedResponses = taggedResponses;
        this.untaggedResponses = null == untaggedResponses ? Collections.<DoveAdmResponse> emptyList() : untaggedResponses;
    }

    /**
     * Checks if there were no responses.
     *
     * @return <code>true</code> for no responses; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return responses.isEmpty();
    }

    /**
     * An {@link Iterator} for available responses.
     *
     * @return The response iterator
     */
    @Override
    public Iterator<DoveAdmResponse> iterator() {
        return responses.iterator();
    }

    /**
     * Gets the responses (as appeared in JSON array)
     *
     * @return The responses
     */
    public List<DoveAdmResponse> getResponses() {
        return responses;
    }

    /**
     * Gets the tagged response
     *
     * @param identifier The response identifier to look-up by
     * @return The tagged response or <code>null</code>
     */
    public DoveAdmResponse getTaggedResponse(String identifier) {
        return taggedResponses.get(identifier);
    }

    /**
     * Gets the tagged responses
     *
     * @return The tagged responses
     */
    public Map<String, DoveAdmResponse> getTaggedResponses() {
        return taggedResponses;
    }

    /**
     * Gets the untagged or unsolicited responses
     *
     * @return The untagged or unsolicited responses
     */
    public List<DoveAdmResponse> getUntaggedResponses() {
        return untaggedResponses;
    }

}
