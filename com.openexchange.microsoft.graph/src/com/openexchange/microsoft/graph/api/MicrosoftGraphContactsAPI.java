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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.microsoft.graph.api;

import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpHeaders;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.microsoft.graph.api.client.MicrosoftGraphRESTClient;
import com.openexchange.microsoft.graph.api.client.MicrosoftGraphRESTEndPoint;
import com.openexchange.rest.client.v2.RESTMethod;
import com.openexchange.rest.client.v2.RESTResponse;

/**
 * {@link MicrosoftGraphContactsAPI}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class MicrosoftGraphContactsAPI extends AbstractMicrosoftGraphAPI {

    /**
     * Initialises a new {@link MicrosoftGraphContactsAPI}.
     * 
     * @param client the {@link MicrosoftGraphRESTClient}
     */
    public MicrosoftGraphContactsAPI(MicrosoftGraphRESTClient client) {
        super(client);
    }

    /**
     * Retrieves all contacts of the current user.
     * 
     * Required OAuth scopes:
     * <ul>
     * <li>Contacts.Read</li>
     * </ul>
     * 
     * @param accessToken The OAuth access token previously acquired by the OAuthService
     * @return a {@link JSONObject} with all the user's contacts
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/v1.0/api/user_list_contacts">List Contacts</a>
     */
    public JSONObject getContacts(String accessToken) throws OXException {
        return getContacts(accessToken, 0, 0);
    }

    /**
     * Retrieves the contacts of the current user starting from the specified offset and skipping to the specified offset
     * 
     * Required OAuth scopes:
     * <ul>
     * <li>Contacts.Read</li>
     * </ul>
     * 
     * @param accessToken The OAuth access token previously acquired by the OAuthService
     * @return a {@link JSONObject} with all the user's contacts
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/paging">Paging</a>
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/v1.0/api/user_list_contacts">List Contacts</a>
     */
    public JSONObject getContacts(String accessToken, int startOffset, int skip) throws OXException {
        String path = "/me" + MicrosoftGraphRESTEndPoint.contacts.getAbsolutePath();
        Map<String, String> queryParams = new HashMap<>(2);
        if (startOffset > 0) {
            queryParams.put("$top", Integer.toString(startOffset));
        }
        if (skip > 0) {
            queryParams.put("$skip", Integer.toString(skip));
        }
        return getResource(accessToken, path, queryParams);
    }

    /**
     * Retrieves the metadata of the photo of the contact with the specified identifier
     * 
     * @param contactId The contact identifier
     * @param accessToken The OAuth access token previously acquired by the OAuthService
     * @return a {@link JSONObject} with the photo's metadata
     * @throws OXException if an error is occurred
     */
    public JSONObject getContactPhotoMetadata(String contactId, String accessToken) throws OXException {
        return getResource(accessToken, "/me" + MicrosoftGraphRESTEndPoint.contacts.getAbsolutePath() + "/" + contactId + "/photo");
    }

    /**
     * Retrieves the photo of the contact with the specified identifier
     * 
     * @param contactId The contact identifier
     * @param accessToken The OAuth access token previously acquired by the OAuthService
     * @return The byte array with the photo's contents
     * @throws OXException if an error is occurred
     */
    public byte[] getContactPhoto(String contactId, String accessToken) throws OXException {
        String path = "/me" + MicrosoftGraphRESTEndPoint.contacts.getAbsolutePath() + "/" + contactId + "/photo/$value";
        RESTResponse restResponse = client.execute(createRequest(RESTMethod.GET, accessToken, path));
        checkResponseBody(restResponse);
        return byte[].class.cast(restResponse.getResponseBody());
    }

    /**
     * Checks whether the response body contains a byte array
     * 
     * @param restResponse The {@link RESTResponse} to check
     * @throws OXException if the response body does not contain a byte array
     */
    private void checkResponseBody(RESTResponse restResponse) throws OXException {
        String contentType = restResponse.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentType.isEmpty()) {
            throw new OXException(666, "No contact photo found.");
        }
        if (!contentType.startsWith("image") || restResponse.getResponseBody() == null) {
            throw new OXException(666, "No contact photo found.");
        }
        if (!(restResponse.getResponseBody() instanceof byte[])) {
            throw new OXException(666, "No contact photo found.");
        }
    }
}
