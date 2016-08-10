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

package com.openexchange.file.storage.googledrive.access;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.drive.Drive;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.googledrive.GoogleDriveConstants;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.oauth.AbstractOAuthAccess;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthClient;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.session.Session;

/**
 * {@link GoogleDriveOAuthAccess} - Initialises and provides Google Drive access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GoogleDriveOAuthAccess extends AbstractOAuthAccess {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleDriveOAuthAccess.class);

    /** The Google Drive API client. */
    private final AtomicReference<OAuthClient<Drive>> oauthClientRef;

    /** The last-accessed time stamp */
    private volatile long lastAccessed;

    private FileStorageAccount fsAccount;

    private Session session;

    /**
     * Initialises a new {@link GoogleDriveOAuthAccess}.
     */
    public GoogleDriveOAuthAccess(FileStorageAccount fsAccount, Session session) throws OXException {
        super();
        this.fsAccount = fsAccount;
        this.session = session;

        int oauthAccountId = getAccountId();
        // Grab Google OAuth account
        OAuthAccount googleAccount = GoogleApiClients.getGoogleAccount(oauthAccountId, session, false);
        setOAuthAccount(googleAccount);

        // Establish Drive reference
        oauthClientRef = new AtomicReference<OAuthClient<Drive>>();
        lastAccessed = System.nanoTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#initialise()
     */
    @Override
    public void initialise() throws OXException {
        synchronized (this) {
            OAuthClient<Drive> oAuthClient = oauthClientRef.get();
            if (null == oAuthClient) {
                OAuthAccount oauthAccount = getOAuthAccount();
                {
                    OAuthAccount newAccount = GoogleApiClients.ensureNonExpiredGoogleAccount(oauthAccount, session);
                    if (null != newAccount) {
                        oauthAccount = newAccount;
                        setOAuthAccount(newAccount);
                    }
                }

                // Generate appropriate credentials for it
                GoogleCredential credentials = GoogleApiClients.getCredentials(oauthAccount, session);

                // Establish Drive instance
                oAuthClient = new OAuthClient<>(new Drive.Builder(credentials.getTransport(), credentials.getJsonFactory(), credentials).setApplicationName(GoogleApiClients.getGoogleProductName()).build(), getOAuthAccount().getToken());
                oauthClientRef.set(oAuthClient);
                lastAccessed = System.nanoTime();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#revoke()
     */
    @Override
    public void revoke() throws OXException {
        // No Java API call
        // More information here: https://developers.google.com/identity/protocols/OAuth2WebServer#tokenrevoke
        try {
            DefaultHttpClient httpClient = HttpClients.getHttpClient("Open-Xchange OneDrive Client");
            HttpGet request = new HttpGet("https://accounts.google.com/o/oauth2/revoke?token=" + getOAuthAccount().getToken());
            HttpResponse httpResponse = httpClient.execute(request);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return;
            } else {
                LOG.warn("The Dropbox OAuth token couldn't not be revoked for user '{}' in context '{}'. Status Code: {}, {}", session.getUserId(), session.getContextId(), statusCode, httpResponse.getStatusLine().getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", e.getMessage());
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#ping()
     */
    @Override
    public boolean ping() throws OXException {
        try {
            Drive drive = oauthClientRef.get().client;
            drive.about().get().execute();
            return true;
        } catch (final HttpResponseException e) {
            if (401 == e.getStatusCode() || 403 == e.getStatusCode()) {
                return false;
            }
            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", Integer.valueOf(e.getStatusCode()) + " " + e.getStatusMessage());
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#getClient()
     */
    @Override
    public OAuthClient<?> getClient() throws OXException {
        OAuthClient<Drive> oAuthClient = oauthClientRef.get();
        if (oAuthClient == null) {
            initialise();
            oAuthClient = oauthClientRef.get();
        }
        return oAuthClient;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#getAccountId()
     */
    @Override
    public int getAccountId() throws OXException {
        try {
            return getAccountId(fsAccount.getConfiguration());
        } catch (IllegalArgumentException e) {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create(GoogleDriveConstants.ID, fsAccount.getId());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccess#ensureNotExpired()
     */
    @Override
    public OAuthAccess ensureNotExpired() throws OXException {
        if (null == oauthClientRef.get()) {
            return this;
        }

        if (isExpired()) {
            synchronized (this) {
                if (isExpired()) {
                    OAuthAccount newAccount = GoogleApiClients.ensureNonExpiredGoogleAccount(getOAuthAccount(), session);
                    if (newAccount != null) {
                        setOAuthAccount(newAccount);

                        // Generate appropriate credentials for it
                        GoogleCredential credentials = GoogleApiClients.getCredentials(newAccount, session);

                        // Establish Drive instance
                        OAuthClient<Drive> oauthClient = new OAuthClient<>(new Drive.Builder(credentials.getTransport(), credentials.getJsonFactory(), credentials).setApplicationName(GoogleApiClients.getGoogleProductName()).build(), getOAuthAccount().getToken());
                        oauthClientRef.set(oauthClient);
                        lastAccessed = System.nanoTime();
                    }
                }
            }
        }
        return this;
    }
}
