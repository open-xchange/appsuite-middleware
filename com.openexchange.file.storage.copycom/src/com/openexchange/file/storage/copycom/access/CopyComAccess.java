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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.file.storage.copycom.access;

import java.util.Map;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.copycom.CopyComExceptionCodes;
import com.openexchange.file.storage.copycom.osgi.Services;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthService;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.session.Session;


/**
 * {@link CopyComAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class CopyComAccess {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CopyComAccess.class);

    /** The re-check threshold in seconds (45 minutes) */
    private static final long RECHECK_THRESHOLD = 2700;

    /**
     * Gets the Copy.com access for given Box account.
     *
     * @param fsAccount The Copy.com account providing credentials and settings
     * @param session The user session
     * @return The Copy.com access; either newly created or fetched from underlying registry
     * @throws OXException If a Copy.com access could not be created
     */
    public static CopyComAccess accessFor(final FileStorageAccount fsAccount, final Session session) throws OXException {
        final CopyComAccessRegistry registry = CopyComAccessRegistry.getInstance();
        final String accountId = fsAccount.getId();
        CopyComAccess boxAccess = registry.getAccess(session.getContextId(), session.getUserId(), accountId);
        if (null == boxAccess) {
            final CopyComAccess newInstance = new CopyComAccess(fsAccount, session, session.getUserId(), session.getContextId());
            boxAccess = registry.addAccess(session.getContextId(), session.getUserId(), accountId, newInstance);
            if (null == boxAccess) {
                boxAccess = newInstance;
            }
        }
        return boxAccess;
    }

    // ------------------------------------------------------------------------------------------------------------------------ //

    /** The HTTP client */
    private final DefaultHttpClient httpClient;

    /** The OAuth signer */
    private final CommonsHttpOAuthConsumer signer;

    /**
     * Initializes a new {@link CopyComAccess}.
     */
    private CopyComAccess(FileStorageAccount fsAccount, Session session, int userId, int contextId) throws OXException {
        super();

        // Get OAuth account identifier from messaging account's configuration
        int oauthAccountId;
        {
            Map<String, Object> configuration = fsAccount.getConfiguration();
            if (null == configuration) {
                throw CopyComExceptionCodes.MISSING_CONFIG.create(fsAccount.getId());
            }
            Object accountId = configuration.get("account");
            if (null == accountId) {
                throw CopyComExceptionCodes.MISSING_CONFIG.create(fsAccount.getId());
            }
            if (accountId instanceof Integer) {
                oauthAccountId = ((Integer) accountId).intValue();
            } else {
                try {
                    oauthAccountId = Integer.parseInt(accountId.toString());
                } catch (NumberFormatException e) {
                    throw CopyComExceptionCodes.MISSING_CONFIG.create(e, fsAccount.getId());
                }
            }
        }

        // Grab Copy.com OAuth account
        OAuthAccount copyComOAuthAccount;
        {
            OAuthService oAuthService = Services.getService(OAuthService.class);
            copyComOAuthAccount = oAuthService.getAccount(oauthAccountId, session, userId, contextId);
        }

        // Initialize rest
        httpClient = createClient();
        signer = createSigner(copyComOAuthAccount, session);
    }

    private DefaultHttpClient createClient() {
        return HttpClients.getHttpClient("Open-Xchange Copy.com Client");
    }

    private CommonsHttpOAuthConsumer createSigner(OAuthAccount copyComOAuthAccount, Session session) throws OXException {
        CommonsHttpOAuthConsumer tmp = new CommonsHttpOAuthConsumer(copyComOAuthAccount.getMetaData().getAPIKey(session), copyComOAuthAccount.getMetaData().getAPISecret(session));
        tmp.setTokenWithSecret(copyComOAuthAccount.getToken(), copyComOAuthAccount.getSecret());
        return tmp;
    }

    /**
     * Gets the current Copy.com client instance
     *
     * @return The Copy.com client
     */
    public DefaultHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Signs the request by using's OAuth's HTTP header authorization scheme
     * and the PLAINTEXT signature method.  As such, this should only be used
     * over secure connections (i.e. HTTPS).  Using this over regular HTTP
     * connections is completely insecure.
     */
    public void sign(final HttpRequestBase request) throws OXException {
        try {
            request.setHeader("X-Api-Version", "1");
            request.setHeader("Accept", "application/json");
            signer.sign(request);
        } catch (final OAuthCommunicationException e) {
            throw CopyComExceptionCodes.COPY_COM_ERROR.create(e, e.getMessage());
        } catch (final OAuthMessageSignerException e) {
            throw CopyComExceptionCodes.COPY_COM_ERROR.create(e, e.getMessage());
        } catch (final OAuthExpectationFailedException e) {
            throw CopyComExceptionCodes.COPY_COM_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Disposes this access instance.
     */
    public void dispose() {
        // Nothing to do
    }

}
