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

package com.openexchange.mail.oauth.google;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.gmail.Gmail;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.oauth.association.AbstractOAuthAccountAssociation;
import com.openexchange.oauth.association.Module;
import com.openexchange.oauth.association.Status;
import com.openexchange.oauth.google.GoogleOAuthScope;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.session.Session;

/**
 * {@link GoogleMailOAuthAccountAssociation}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class GoogleMailOAuthAccountAssociation extends AbstractOAuthAccountAssociation {

    /**
     * The identifier for Google Mail service.
     */
    // FIXME: are there any known OX constants for that similar to GoogleDriveConstants.ID?
    // If yes feel free to replace that...
    private static final String serviceId = "googlemail";
    private final MailAccount mailAccount;

    /**
     * Initialises a new {@link GoogleMailOAuthAccountAssociation}.
     * 
     * @param accountId
     * @param userId
     * @param contextId
     */
    public GoogleMailOAuthAccountAssociation(int accountId, int userId, int contextId, MailAccount mailAccount) {
        super(accountId, userId, contextId);
        this.mailAccount = mailAccount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#getServiceId()
     */
    @Override
    public String getServiceId() {
        return serviceId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#getId()
     */
    @Override
    public String getId() {
        return Integer.toString(mailAccount.getId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return mailAccount.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#getModule()
     */
    @Override
    public String getModule() {
        return Module.MAIL.getModuleName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#optFolder()
     */
    @Override
    public String optFolder() {
        return mailAccount.getRootFolder();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#getScopes()
     */
    @Override
    public List<OAuthScope> getScopes() {
        return Collections.singletonList(GoogleOAuthScope.mail);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.AbstractOAuthAccountAssociation#getStatus(com.openexchange.session.Session)
     */
    @Override
    public Status getStatus(Session session) throws OXException {
        try {
            OAuthAccount oauthAccount = GoogleApiClients.getGoogleAccount(getOAuthAccountId(), session);
            GoogleCredential credentials = GoogleApiClients.getCredentials(oauthAccount, session);
            if (credentials == null) {
                throw new IllegalArgumentException("Cannot get credentials. Neither the oauth account nor the credentials can be 'null'.");
            }
            Gmail gmail = new Gmail.Builder(credentials.getTransport(), credentials.getJsonFactory(), credentials).setApplicationName(GoogleApiClients.getGoogleProductName(session)).build();
            gmail.users().getProfile("me").execute();
            return Status.OK;
        } catch (OXException | IOException e) {
            return Status.RECREATION_NEEDED;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.AbstractOAuthAccountAssociation#newAccess(com.openexchange.session.Session)
     */
    @Override
    protected AbstractOAuthAccess newAccess(Session session) throws OXException {
        throw new UnsupportedOperationException("No OAuthAccess for Google Mail.");
    }
}
