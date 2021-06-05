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

package com.openexchange.chronos.provider.google.access;

import java.io.IOException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.calendar.Calendar;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.google.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthClient;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.session.Session;

/**
 *
 * {@link GoogleOAuthAccess}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
//FIXME: Maybe consolidate with GoogleDriveOAuthAccess
public class GoogleOAuthAccess extends AbstractOAuthAccess {

    private final int accountId;

    /**
     * Initializes a new {@link GoogleOAuthAccess}.
     */
    public GoogleOAuthAccess(int accountId, Session session) {
        super(session);
        this.accountId = accountId;
    }

    @Override
    public void initialize() throws OXException {
        synchronized (this) {
            // Grab Google OAuth account
            int oauthAccountId = getAccountId();
            OAuthAccount oauthAccount = GoogleApiClients.getGoogleAccount(oauthAccountId, getSession(), false);
            setOAuthAccount(oauthAccount);

            {
                OAuthAccount newAccount = GoogleApiClients.ensureNonExpiredGoogleAccount(oauthAccount, getSession());
                if (null != newAccount) {
                    oauthAccount = newAccount;
                    setOAuthAccount(newAccount);
                }
            }
            verifyAccount(oauthAccount, Services.getService(OAuthService.class), OXScope.calendar_ro);

            // Generate appropriate credentials for it
            GoogleCredential credentials = GoogleApiClients.getCredentials(oauthAccount, getSession());

            // Establish Calendar instance
            setOAuthClient(new OAuthClient<Calendar>(new Calendar.Builder(credentials.getTransport(), credentials.getJsonFactory(), credentials).setApplicationName(GoogleApiClients.getGoogleProductName(getSession())).build(), getOAuthAccount().getToken()));
        }
    }

    @Override
    public boolean ping() throws OXException {
        Calendar client = this.<Calendar> getClient().client;
        try {
            client.calendarList().list().execute();
            return true;
        } catch (HttpResponseException e) {
            if (401 == e.getStatusCode() || 403 == e.getStatusCode()) {
                return false;
            }
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
        } catch (IOException e) {
            throw CalendarExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
        }
    }

    @Override
    public int getAccountId() throws OXException {
      return accountId;
    }

    @Override
    public OAuthAccess ensureNotExpired() throws OXException {
        if (isExpired()) {
            synchronized (this) {
                if (isExpired()) {
                    initialize();
                }
            }
        }
        return this;
    }
}
