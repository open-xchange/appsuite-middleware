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

package com.openexchange.chronos.provider.google.access;

import org.json.JSONException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.calendar.Calendar;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.google.GoogleCalendarConfigField;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.oauth.OAuthAccount;
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
public class GoogleOAuthAccess extends AbstractOAuthAccess {

    private final CalendarAccount acc;

    /**
     * Initializes a new {@link GoogleOAuthAccess}.
     */
    public GoogleOAuthAccess(CalendarAccount account, Session session) throws OXException {
        super(session);
        this.acc = account;
    }

    @Override
    public void initialize() throws OXException {
        synchronized (this) {
            // Grab Google OAuth account
            int oauthAccountId = getAccountId();
            OAuthAccount oauthAccount = GoogleApiClients.getGoogleAccount(oauthAccountId, getSession(), false);
            verifyAccount(oauthAccount, OXScope.calendar_ro);
            setOAuthAccount(oauthAccount);

            {
                OAuthAccount newAccount = GoogleApiClients.ensureNonExpiredGoogleAccount(oauthAccount, getSession());
                if (null != newAccount) {
                    oauthAccount = newAccount;
                    setOAuthAccount(newAccount);
                }
            }

            // Generate appropriate credentials for it
            GoogleCredential credentials = GoogleApiClients.getCredentials(oauthAccount, getSession());

            // Establish Drive instance
            setOAuthClient(new OAuthClient<Calendar>(new Calendar.Builder(credentials.getTransport(), credentials.getJsonFactory(), credentials).setApplicationName(GoogleApiClients.getGoogleProductName(getSession())).build(), getOAuthAccount().getToken()));
        }
    }

    @Override
    public boolean ping() throws OXException {
        try {
            Calendar cal = this.<Calendar> getClient().client;
            return true;
//        } catch (final HttpResponseException e) {
//            if (401 == e.getStatusCode() || 403 == e.getStatusCode()) {
//                return false;
//            }
////            throw FileStorageExceptionCodes.PROTOCOL_ERROR.create(e, "HTTP", Integer.valueOf(e.getStatusCode()) + " " + e.getStatusMessage());
//        } catch (final IOException e) {
////            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
//            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        return false;
    }

    @Override
    public int getAccountId() throws OXException {
        try {
            return acc.getUserConfiguration().getInt(GoogleCalendarConfigField.oauthId.name());
        } catch (IllegalArgumentException e) {
//            throw FileStorageExceptionCodes.MISSING_CONFIG.create(GoogleDriveConstants.ID, fsAccount.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
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
