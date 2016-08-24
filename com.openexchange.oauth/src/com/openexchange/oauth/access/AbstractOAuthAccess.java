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

package com.openexchange.oauth.access;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthClient;

/**
 * {@link AbstractOAuthAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractOAuthAccess implements OAuthAccess {

    /** The re-check threshold in seconds (45 minutes) */
    private static final long RECHECK_THRESHOLD = 2700;

    /** The {@link OAuthAccount} */
    private volatile OAuthAccount oauthAccount;

    /** The associated OAuth client */
    private final AtomicReference<OAuthClient<?>> oauthClientRef;

    /** The last-accessed time stamp */
    private volatile long lastAccessed;

    /**
     * Initializes a new {@link AbstractOAuthAccess}.
     */
    protected AbstractOAuthAccess() {
        super();
        oauthClientRef = new AtomicReference<OAuthClient<?>>();
    }

    @Override
    public void dispose() {
        // Empty by default
    }

    @Override
    public OAuthAccount getOAuthAccount() {
        return oauthAccount;
    }

    @Override
    public <T> OAuthClient<T> getClient() throws OXException {
        OAuthClient<?> client = oauthClientRef.get();
        if (client == null) {
            // Exclusive initialization for OAuth client
            synchronized (this) {
                client = oauthClientRef.get();
                if (client == null) {
                    initialize();
                    client = oauthClientRef.get();
                }
            }
        }
        return (OAuthClient<T>) client;
    }

    /**
     * Verifies whether the OAuth token and the associated {@link OAuthClient} are expired.
     *
     * @return <code>true</code> if expired; <code>false</code> otherwise
     */
    protected boolean isExpired() {
        long now = System.nanoTime();
        return TimeUnit.NANOSECONDS.toSeconds(now - lastAccessed) > RECHECK_THRESHOLD;
    }

    /**
     * Sets the {@link OAuthClient}. Updates the last accessed time stamp
     *
     * @param client The {@link OAuthClient} to set
     */
    protected <T> void setOAuthClient(OAuthClient<T> client) {
        lastAccessed = System.nanoTime();
        oauthClientRef.set(client);
    }

    /**
     * Gets the {@link OAuthClient} reference w/o any initializations before-hand.
     *
     * @return The {@link OAuthClient} instance or <code>null</code>
     */
    protected <T> OAuthClient<T> getOAuthClient() {
        return (OAuthClient<T>) oauthClientRef.get();
    }

    /**
     * Sets the {@link OAuthAccount}
     *
     * @param oauthAccount The {@link OAuthAccount} to set
     */
    protected void setOAuthAccount(OAuthAccount oauthAccount) {
        this.oauthAccount = oauthAccount;
    }

    /**
     * Returns the OAuth account identifier from associated account's configuration
     *
     * @param configuration The configuration
     * @return The account identifier
     * @throws IllegalArgumentException If the configuration is <code>null</code>, or if the account identifier is not present, or is present but cannot be parsed as an integer
     */
    protected int getAccountId(Map<String, Object> configuration) {
        if (null == configuration) {
            throw new IllegalArgumentException("The configuration cannot be 'null'");
        }

        Object accountId = configuration.get("account");
        if (null == accountId) {
            throw new IllegalArgumentException("The account identifier is missing from the configuration");
        }

        if (accountId instanceof Integer) {
            return ((Integer) accountId).intValue();
        }

        try {
            return Integer.parseInt(accountId.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The account identifier '" + accountId.toString() + "' cannot be parsed as an integer.", e);
        }
    }

}
