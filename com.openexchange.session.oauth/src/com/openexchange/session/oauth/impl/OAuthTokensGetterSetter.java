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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.session.oauth.impl;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.lock.AccessControl;
import com.openexchange.lock.LockService;
import com.openexchange.lock.ReentrantLockAccessControl;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.oauth.OAuthTokens;

/**
 * {@link OAuthTokensGetterSetter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class OAuthTokensGetterSetter {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthTokensGetterSetter.class);

    private final ServiceLookup services;

    public OAuthTokensGetterSetter(ServiceLookup services) {
        super();
        this.services = services;
    }

    public Optional<OAuthTokens> getFromSessionAtomic(Session session) throws InterruptedException {
        return doAtomic(session, () -> getFromSession(session));
    }

    public Optional<OAuthTokens> getFromSessionAtomic(Session session, long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        return doAtomic(session, timeout, unit, (AtomicResultOperation<Optional<OAuthTokens>>) () -> getFromSession(session));
    }

    public void setInSessionAtomic(Session session, OAuthTokens tokens) throws InterruptedException {
        doAtomic(session, () -> setInSession(session, tokens));
    }

    public void setInSessionAtomic(Session session, OAuthTokens tokens, long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        doAtomic(session, timeout, unit, () -> setInSession(session, tokens));
    }

    public void removeFromSessionAtomic(Session session) throws InterruptedException {
        doAtomic(session, () -> removeFromSession(session));
    }

    public void removeFromSessionAtomic(Session session, long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        doAtomic(session, timeout, unit, () -> removeFromSession(session));
    }

    public Optional<OAuthTokens> getFromSession(Session session) {
        String accessToken = (String) session.getParameter(Session.PARAM_OAUTH_ACCESS_TOKEN);
        String expiryString = (String) session.getParameter(Session.PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE);
        String refreshToken = (String) session.getParameter(Session.PARAM_OAUTH_REFRESH_TOKEN);

        Date expiryDate = null;
        if (expiryString != null) {
            try {
                long expiryMillis = Long.parseLong(expiryString);
                expiryDate = new Date(expiryMillis);
            } catch (NumberFormatException e) {
                LOG.warn("Illegal format of session parameter '{}': {}", Session.PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE, expiryString , e);
            }
        }

        if (accessToken == null) {
            LOG.warn("Stored session doesn't contain valid oauth parameters: {}", session.getSessionID());
            return Optional.empty();
        }

        return Optional.of(new OAuthTokens(accessToken, expiryDate, refreshToken));
    }

    public void setInSession(Session session, OAuthTokens tokens) {
        session.setParameter(Session.PARAM_OAUTH_ACCESS_TOKEN, tokens.getAccessToken());
        if (tokens.hasExpiryDate()) {
            session.setParameter(Session.PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE, Long.toString(tokens.getExpiryDate().getTime()));
        }
        if (tokens.hasRefreshToken()) {
            session.setParameter(Session.PARAM_OAUTH_REFRESH_TOKEN, tokens.getRefreshToken());
        }
    }

    public void removeFromSession(Session session) {
        session.setParameter(Session.PARAM_OAUTH_ACCESS_TOKEN, null);
        session.setParameter(Session.PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE, null);
        session.setParameter(Session.PARAM_OAUTH_REFRESH_TOKEN, null);
    }

    private AccessControl getTokenLock(Session session) {
        LockService lockService = services.getService(LockService.class);
        if (null == lockService) {
            LOG.warn("Failed to acquire lock for session {}. Using global lock instead. LockService is absent.", session.getSessionID());
            return new ReentrantLockAccessControl((ReentrantLock) session.getParameter(Session.PARAM_LOCK));
        }

        int userId = session.getUserId();
        int contextId = session.getContextId();
        try {
            return lockService.getAccessControlFor(new StringBuilder(64).append("oauth-tokens-").append(session.getSessionID()).toString(), 1, userId, contextId);
        } catch (Exception e) {
            LOG.warn("Failed to acquire lock for session {}. Using global lock instead.", session.getSessionID(), e);
            return new ReentrantLockAccessControl((ReentrantLock) session.getParameter(Session.PARAM_LOCK));
        }
    }

    public static interface AtomicOperation {
        void perform() throws InterruptedException;
    }

    public static interface ThrowableAtomicOperation {
        void perform() throws InterruptedException, OXException;
    }

    public static interface AtomicResultOperation<R> {
        R perform() throws InterruptedException;
    }

    public static interface ThrowableAtomicResultOperation<R> {
        R perform() throws InterruptedException, OXException;
    }

    public void doAtomic(Session session, AtomicOperation op) throws InterruptedException {
        boolean locked = false;
        AccessControl accessControl = getTokenLock(session);
        try {
            accessControl.acquireGrant();
            locked = true;
            op.perform();
        } finally {
            releaseAndClose(accessControl, locked);
        }
    }

    public void doAtomic(Session session, long timeout, TimeUnit unit, AtomicOperation op) throws TimeoutException, InterruptedException {
        AccessControl accessControl = getTokenLock(session);
        if (accessControl.tryAcquireGrant(timeout, unit)) {
            try {
                op.perform();
            } finally {
                releaseAndClose(accessControl);
            }
        } else {
            throw new TimeoutException("Lock timeout exceeded");
        }
    }

    public void doThrowableAtomic(Session session, ThrowableAtomicOperation op) throws InterruptedException, OXException {
        boolean locked = false;
        AccessControl accessControl = getTokenLock(session);
        try {
            accessControl.acquireGrant();
            locked = true;
            op.perform();
        } finally {
            releaseAndClose(accessControl, locked);
        }
    }

    public <R> R doThrowableAtomic(Session session, long timeout, TimeUnit unit, ThrowableAtomicResultOperation<R> op) throws TimeoutException, InterruptedException, OXException {
        AccessControl accessControl = getTokenLock(session);
        if (accessControl.tryAcquireGrant(timeout, unit)) {
            try {
                return op.perform();
            } finally {
                releaseAndClose(accessControl);
            }
        }
        throw new TimeoutException("Lock timeout exceeded");
    }

    public <R> R doAtomic(Session session, AtomicResultOperation<R> op) throws InterruptedException {
        boolean locked = false;
        AccessControl accessControl = getTokenLock(session);
        try {
            accessControl.acquireGrant();
            locked = true;
            return op.perform();
        } finally {
            releaseAndClose(accessControl, locked);
        }
    }

    public <R> R doAtomic(Session session, long timeout, TimeUnit unit, AtomicResultOperation<R> op) throws TimeoutException, InterruptedException {
        AccessControl accessControl = getTokenLock(session);
        if (accessControl.tryAcquireGrant(timeout, unit)) {
            try {
                return op.perform();
            } finally {
                releaseAndClose(accessControl);
            }
        }

        throw new TimeoutException("Lock timeout exceeded");
    }

    public <R> R doAtomic(Session session, long timeout, TimeUnit unit, ThrowableAtomicResultOperation<R> op) throws TimeoutException, InterruptedException, OXException {
        AccessControl accessControl = getTokenLock(session);
        if (accessControl.tryAcquireGrant(timeout, unit)) {
            try {
                return op.perform();
            } finally {
                releaseAndClose(accessControl);
            }
        }

        throw new TimeoutException("Lock timeout exceeded");
    }

    private void releaseAndClose(AccessControl accessControl) {
        if (null != accessControl) {
            try {
                accessControl.release();
                accessControl.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private void releaseAndClose(AccessControl accessControl, boolean aquired) {
        if (null != accessControl) {
            try {
                accessControl.release(aquired);
                accessControl.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

}
