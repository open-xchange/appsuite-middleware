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

package com.openexchange.share.impl.xctx;

import static com.openexchange.java.Autoboxing.I;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.openexchange.ajax.login.LoginRequestImpl;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Reference;
import com.openexchange.java.util.UUIDs;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.core.tools.ShareToken;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.share.subscription.XctxSessionManager;

/**
 * {@link XctxSessionCache}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.org">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class XctxSessionCache implements XctxSessionManager {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(XctxSessionCache.class);

    private final ServiceLookup services;
    private final Cache<String, String> guestSessionCache;

    /**
     * Initializes a new {@link XctxSessionCache}.
     * 
     * @param services A service lookup reference
     */
    public XctxSessionCache(ServiceLookup services) {
        super();
        this.services = services;
        this.guestSessionCache = CacheBuilder.newBuilder()
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .removalListener(new RemovalListener<String, String>() {

                @Override
                public void onRemoval(RemovalNotification<String, String> notification) {
                    doLogout(notification.getValue());
                }
            })
        .build();
    }

    @Override
    public Session getGuestSession(Session session, String baseToken, String password) throws OXException {
        ShareToken shareToken = new ShareToken(baseToken);
        if (session.getContextId() == shareToken.getContextID()) {
            /*
             * Allow context internal shares for anonymous links
             */
            GuestInfo guestInfo = services.getServiceSafe(ShareService.class).resolveGuest(baseToken);
            if (false == RecipientType.ANONYMOUS.equals(guestInfo.getRecipientType())) {
                throw ShareExceptionCodes.INVALID_TOKEN.create(baseToken);
            }
        }
        Reference<Session> newGuestSession = new Reference<Session>();
        String key = getKey(session, baseToken, password);
        String guestSessionId;
        try {
            guestSessionId = guestSessionCache.get(key, new Callable<String>() {

                @Override
                public String call() throws Exception {
                    Session guestSession = doLogin(session, baseToken, password);
                    newGuestSession.setValue(guestSession);
                    return guestSession.getSessionID();
                }
            });
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (null != cause && OXException.class.isInstance(e.getCause())) {
                throw (OXException) cause;
            }
            throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
        }
        if (newGuestSession.hasValue()) {
            /*
             * new guest login was performed in this request
             */
            return newGuestSession.getValue();
        }
        /*
         * session identifier looked up successfully in cache; get & implicitly validate session from service
         */
        Session guestSession = services.getServiceSafe(SessiondService.class).getSession(guestSessionId, false);
        if (null == guestSession) {
            /*
             * invalidate cache entry & login again if no session could be looked up
             */
            guestSessionCache.asMap().remove(key, guestSessionId);
            return getGuestSession(session, baseToken, password);
        }
        return guestSession;
    }
    
    /**
     * Performs the login.
     * 
     * @param session The user session spawning the guest session
     * @param baseToken The base token of the guest user
     * @param password The optional password of the guest account
     * @return The guest session
     */
    Session doLogin(Session session, String baseToken, String password) throws OXException {
        XctxLoginMethod loginMethod = new XctxLoginMethod(services, baseToken, password);
        LoginRequestImpl loginRequest = new LoginRequestImpl.Builder()
            .login(baseToken)
            .password(password)
            .authId(UUIDs.getUnformattedStringFromRandom())
            .tranzient(true)
            .client(session.getClient())
            .iface(Interface.HTTP_JSON)
            .clientIP(session.getLocalIp())
            .hash(session.getHash())
            .userAgent((String) session.getParameter(Session.PARAM_USER_AGENT))
        .build();
        LoginResult loginResult = LoginPerformer.getInstance().doLogin(loginRequest, new HashMap<String, Object>(), loginMethod);
        if (null == loginResult || null == loginResult.getSession()) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create("no session from login result");
        }
        LOG.debug("Successful login for share {} with guest user {} in context {}, using session {}.", 
            baseToken, I(loginResult.getUser().getId()), I(loginResult.getContext().getContextId()), loginResult.getSession().getSessionID());
        return loginResult.getSession();
    }

    /**
     * Performs the logout.
     *
     * @param session The session to log-out
     */
    void doLogout(String sessionId) {
        LOG.debug("Cross-context guest logout: {}...", sessionId);
        try {
            Session removedSession = LoginPerformer.getInstance().doLogout(sessionId);
            if (null != removedSession) {
                LOG.debug("Removed cross-context guest session {}", removedSession);
            } else {
                LOG.debug("Cross-context guest session {} not removed.", sessionId);
            }
        } catch (OXException e) {
            LOG.warn("Error removing cross-context guest session", e);
        }
    }

    private static String getKey(Session session, String baseToken, String password) {
        return new StringBuilder().append(session.getSessionID()).append(baseToken).append((null == password ? "" : password).hashCode()).toString();
    }

}
