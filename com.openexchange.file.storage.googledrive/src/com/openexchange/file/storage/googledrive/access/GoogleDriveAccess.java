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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.drive.Drive;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.googledrive.GoogleDriveExceptionCodes;
import com.openexchange.google.api.client.GoogleApiClients;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.session.Session;

/**
 * {@link GoogleDriveAccess} - Initializes and provides Google Drive access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GoogleDriveAccess {

    /** The re-check threshold in seconds (45 minutes) */
    private static final long RECHECK_THRESHOLD = 2700;

    /**
     * Gets the Google Drive access for given Google Drive account.
     *
     * @param fsAccount The Google Drive account providing credentials and settings
     * @param session The user session
     * @return The Google Drive access; either newly created or fetched from underlying registry
     * @throws OXException If a Google Drive access could not be created
     */
    public static GoogleDriveAccess accessFor(FileStorageAccount fsAccount, Session session) throws OXException {
        final GoogleDriveAccessRegistry registry = GoogleDriveAccessRegistry.getInstance();
        final String accountId = fsAccount.getId();
        GoogleDriveAccess googleDriveAccess = registry.getAccess(session.getContextId(), session.getUserId(), accountId);
        if (null == googleDriveAccess) {
            final GoogleDriveAccess newInstance = new GoogleDriveAccess(fsAccount, session);
            googleDriveAccess = registry.addAccess(session.getContextId(), session.getUserId(), accountId, newInstance);
            if (null == googleDriveAccess) {
                googleDriveAccess = newInstance;
            }
        } else {
            googleDriveAccess.ensureNotExpired(session);
        }
        return googleDriveAccess;
    }

    // -------------------------------------------------------------------------------------------------------------------- //

    /** The Google Drive API client. */
    private final AtomicReference<Drive> driveRef;

    /** The Google OAuth account */
    private volatile OAuthAccount googleAccount;

    /** The last-accessed time stamp */
    private volatile long lastAccessed;

    /**
     * Initializes a new {@link FacebookMessagingResource}.
     *
     * @param fsAccount The Google Drive account providing credentials and settings
     * @param session The session
     * @throws OXException
     */
    private GoogleDriveAccess(FileStorageAccount fsAccount, Session session) throws OXException {
        super();

        // Get OAuth account identifier
        final int oauthAccountId;
        {
            final Map<String, Object> configuration = fsAccount.getConfiguration();
            if (null == configuration) {
                throw GoogleDriveExceptionCodes.MISSING_CONFIG.create(fsAccount.getId());
            }
            final Object accountId = configuration.get("account");
            if (null == accountId) {
                throw GoogleDriveExceptionCodes.MISSING_CONFIG.create(fsAccount.getId());
            }
            if (accountId instanceof Integer) {
                oauthAccountId = ((Integer) accountId).intValue();
            } else {
                try {
                    oauthAccountId = Integer.parseInt(accountId.toString());
                } catch (final NumberFormatException e) {
                    throw GoogleDriveExceptionCodes.MISSING_CONFIG.create(e, fsAccount.getId());
                }
            }
        }

        // Grab Google OAuth account
        OAuthAccount googleAccount = GoogleApiClients.getGoogleAccount(oauthAccountId, session);
        this.googleAccount = googleAccount;

        // Generate appropriate credentials for it
        GoogleCredential credentials = GoogleApiClients.getCredentials(googleAccount, session);

        // Establish Drive instance
        Drive drive = new Drive.Builder(credentials.getTransport(), credentials.getJsonFactory(), credentials).setApplicationName(GoogleApiClients.getGoogleProductName()).build();
        driveRef = new AtomicReference<Drive>(drive);
        lastAccessed = System.nanoTime();
    }

    /**
     * Ensures this access is not expired
     *
     * @param session The associated session
     * @return The non-expired access
     * @throws OXException If check fails
     */
    private GoogleDriveAccess ensureNotExpired(Session session) throws OXException {
        long now = System.nanoTime();
        if (TimeUnit.NANOSECONDS.toSeconds(now - lastAccessed) > RECHECK_THRESHOLD) {
            synchronized (this) {
                OAuthAccount newAccount = GoogleApiClients.ensureNonExpiredGoogleAccount(googleAccount, session);
                if (newAccount != null) {
                    this.googleAccount = newAccount;

                    // Generate appropriate credentials for it
                    GoogleCredential credentials = GoogleApiClients.getCredentials(newAccount, session);

                    // Establish Drive instance
                    Drive drive = new Drive.Builder(credentials.getTransport(), credentials.getJsonFactory(), credentials).setApplicationName(GoogleApiClients.getGoogleProductName()).build();
                    driveRef.set(drive);
                    lastAccessed = System.nanoTime();
                }
            }
        }
        return this;
    }

    /**
     * Gets the Drive reference
     *
     * @return The Drive reference
     */
    public Drive getDrive() {
        return driveRef.get();
    }

    /**
     * Disposes this access
     */
    public void dispose() {
        // Nothing to do
    }

}
