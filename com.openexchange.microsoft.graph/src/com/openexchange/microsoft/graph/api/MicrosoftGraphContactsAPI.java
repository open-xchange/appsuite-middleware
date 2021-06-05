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

package com.openexchange.microsoft.graph.api;

import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpHeaders;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.microsoft.graph.api.client.MicrosoftGraphRESTClient;
import com.openexchange.microsoft.graph.api.client.MicrosoftGraphRESTEndPoint;
import com.openexchange.microsoft.graph.api.exception.MicrosoftGraphContactClientExceptionCodes;
import com.openexchange.rest.client.v2.RESTMethod;
import com.openexchange.rest.client.v2.RESTResponse;

/**
 * {@link MicrosoftGraphContactsAPI}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
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
        checkResponseBody(restResponse, contactId);
        return byte[].class.cast(restResponse.getResponseBody());
    }

    /**
     * Checks whether the response body contains a byte array
     * 
     * @param restResponse The {@link RESTResponse} to check
     * @throws OXException if the response body does not contain a byte array
     */
    private void checkResponseBody(RESTResponse restResponse, String contactId) throws OXException {
        String contentType = restResponse.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentType.isEmpty()) {
            throw MicrosoftGraphContactClientExceptionCodes.NO_CONTACT_PHOTO_EMPTY_CONTENT_TYPE.create(contactId);
        }
        if (false == contentType.startsWith("image")) {
            throw MicrosoftGraphContactClientExceptionCodes.NO_CONTACT_PHOTO_WRONG_CONTENT_TYPE.create(contactId, contentType);
        }
        if (restResponse.getResponseBody() == null) {
            throw MicrosoftGraphContactClientExceptionCodes.NO_CONTACT_PHOTO_EMPTY_RESPONSE_BODY.create(contactId);
        }
        if (false == (restResponse.getResponseBody() instanceof byte[])) {
            throw MicrosoftGraphContactClientExceptionCodes.NO_CONTACT_PHOTO_WRONG_RESPONSE_BODY.create(restResponse.getResponseBody().getClass().getName());
        }
    }
}
