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

package com.openexchange.file.storage.onedrive;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.onedrive.access.OneDriveOAuthAccess;
import com.openexchange.file.storage.onedrive.osgi.Services;
import com.openexchange.java.Charsets;
import com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService;
import com.openexchange.session.Session;

/**
 * {@link AbstractOneDriveResourceAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractOneDriveResourceAccess {

    /**
     * The Jackson object mapper instance.
     */
    private static final ObjectMapper MAPPER;
    static {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER = objectMapper;
    }

    /**
     * Gets the object mapper.
     *
     * @return The object mapper
     */
    protected static ObjectMapper getObjectMapper() {
        return MAPPER;
    }

    protected static <T> T parseIntoObject(InputStream inputStream, Class<T> clazz) throws OXException {
        try {
            JsonFactory jsonFactory = new JsonFactory();
            JsonParser jp = jsonFactory.createParser(inputStream);
            return getObjectMapper().readValue(jp, clazz);
        } catch (JsonGenerationException | JsonMappingException | JsonParseException e) {
            throw FileStorageExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    // -------------------------------------------------------------------------------------------------------------- //

    /** The status code policy to obey */
    public static interface StatusCodePolicy {

        /**
         * Examines given status line
         *
         * @param httpResponse The HTTP response
         * @throws OXException If an Open-Xchange error is yielded from status
         * @throws ClientProtocolException To singal a client error
         */
        void handleStatusCode(HttpResponse httpResponse) throws OXException, ClientProtocolException;
    }

    /** The default status code policy; accepting greater than/equal to <code>200</code> and lower than <code>300</code> */
    public static final StatusCodePolicy STATUS_CODE_POLICY_DEFAULT = new StatusCodePolicy() {

        @Override
        public void handleStatusCode(HttpResponse httpResponse) throws OXException, ClientProtocolException {
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                if (404 == statusCode) {
                    throw FileStorageExceptionCodes.NOT_FOUND.create(OneDriveConstants.ID, statusCode);
                }
                String reason;
                try {
                    JSONObject jResponse = new JSONObject(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8));
                    JSONObject jError = jResponse.getJSONObject("error");
                    String code = jError.getString("code");
                    if ("resource_already_exists".equals(code)) {
                        throw new DuplicateResourceException(jError.getString("message"));
                    }
                    reason = jError.getString("message");
                } catch (DuplicateResourceException e) {
                    throw e;
                } catch (Exception e) {
                    reason = statusLine.getReasonPhrase();
                }
                throw FileStorageExceptionCodes.PROTOCOL_ERROR.create("HTTP", statusCode + " " + reason);
            }
        }
    };

    /** The status code policy; accepting greater than/equal to <code>200</code> and lower than <code>300</code> while ignoring <code>404</code> */
    public static final StatusCodePolicy STATUS_CODE_POLICY_IGNORE_NOT_FOUND = new StatusCodePolicy() {

        @Override
        public void handleStatusCode(HttpResponse httpResponse) throws OXException, ClientProtocolException {
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if ((statusCode < 200 || statusCode >= 300) && statusCode != 404) {
                String reason;
                try {
                    JSONObject jResponse = new JSONObject(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8));
                    JSONObject jError = jResponse.getJSONObject("error");
                    String code = jError.getString("code");
                    if ("resource_already_exists".equals(code)) {
                        throw new DuplicateResourceException(jError.getString("message"));
                    }
                    reason = jError.getString("message");
                } catch (DuplicateResourceException e) {
                    throw e;
                } catch (Exception e) {
                    reason = statusLine.getReasonPhrase();
                }
                throw FileStorageExceptionCodes.PROTOCOL_ERROR.create("HTTP", statusCode + " " + reason);
            }
        }
    };

    // -------------------------------------------------------------------------------------------------------------- //

    /**
     * The OneDrive base URL: <code>"https://apis.live.net/v5.0/"</code>
     */
    protected static final String URL_API_BASE = "https://apis.live.net/v5.0/";

    /** The type constants for a folder */
    private static final Set<String> TYPES_FOLDER = ImmutableSet.of(OneDriveConstants.TYPE_FOLDER, OneDriveConstants.TYPE_ALBUM);

    /**
     * Checks if specified JSON item is a folder
     *
     * @param jItem The JSON item to check
     * @return <code>true</code> if folder; otherwise <code>false</code>
     */
    protected static boolean isFolder(JSONObject jItem) {
        return TYPES_FOLDER.contains(jItem.optString("type", null));
    }

    /**
     * Checks if specified JSON item is a file
     *
     * @param jItem The JSON item to check
     * @return <code>true</code> if file; otherwise <code>false</code>
     */
    protected static boolean isFile(JSONObject jItem) {
        return false == isFolder(jItem);
    }

    protected final OneDriveOAuthAccess oneDriveAccess;
    protected final Session session;
    protected final FileStorageAccount account;
    protected String rootFolderIdentifier;
    protected final MicrosoftGraphDriveService driveService;

    /**
     * Initializes a new {@link AbstractOneDriveResourceAccess}.
     */
    protected AbstractOneDriveResourceAccess(OneDriveOAuthAccess oneDriveAccess, FileStorageAccount account, Session session) {
        super();
        this.oneDriveAccess = oneDriveAccess;
        this.account = account;
        this.session = session;
        this.driveService = Services.getService(MicrosoftGraphDriveService.class);
    }

    /**
     * Performs given closure.
     *
     * @param closure The closure to perform
     * @param httpClient The client to use
     * @return The return value
     * @throws OXException If performing closure fails
     */
    protected <R> R perform(OneDriveClosure<R> closure) throws OXException {
        return closure.perform(this, session);
    }

    /**
     * Handles authentication error.
     *
     * @param e The authentication error
     * @param session The associated session
     * @throws OXException If authentication error could not be handled
     */
    protected void handleAuthError(OXException e, Session session) throws OXException {
        try {
            oneDriveAccess.initialize();
        } catch (OXException oxe) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractOneDriveResourceAccess.class);
            logger.warn("Could not re-initialize Microsoft Graph OneDrive access", oxe);

            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, OneDriveConstants.ID, e.getMessage());
        }
    }

    /**
     * Gets the OneDrive folder identifier from given file storage folder identifier
     *
     * @param folderId The file storage folder identifier
     * @return The appropriate OneDrive folder identifier
     * @throws OXException If operation fails
     */
    protected String toOneDriveFolderId(String folderId) throws OXException {
        return FileStorageFolder.ROOT_FULLNAME.equals(folderId) ? driveService.getRootFolderId(getAccessToken()) : folderId;
    }

    /**
     * Gets the file storage folder identifier from given OneDrive folder identifier
     *
     * @param oneDriveId The OneDrive folder identifier
     * @return The appropriate file storage folder identifier
     * @throws OXException If operation fails
     */
    protected String toFileStorageFolderId(String oneDriveId) throws OXException {
        return driveService.getRootFolderId(getAccessToken()).equals(oneDriveId) ? FileStorageFolder.ROOT_FULLNAME : oneDriveId;
    }

    /**
     * 
     * @return
     */
    protected String getAccessToken() {
        return oneDriveAccess.getOAuthAccount().getToken();
    }
}
