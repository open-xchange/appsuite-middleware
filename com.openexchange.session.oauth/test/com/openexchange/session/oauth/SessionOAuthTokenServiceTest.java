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

package com.openexchange.session.oauth;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.util.UUIDs;
import com.openexchange.lock.LockService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.oauth.RefreshResult.FailReason;
import com.openexchange.session.oauth.RefreshResult.SuccessReason;
import com.openexchange.session.oauth.TokenRefreshResponse.Error;
import com.openexchange.session.oauth.TokenRefreshResponse.ErrorType;
import com.openexchange.session.oauth.impl.DefaultSessionOAuthTokenService;
import com.openexchange.session.oauth.mocks.SimLockService;
import com.openexchange.session.oauth.mocks.SimSessionStorageService;
import com.openexchange.session.oauth.mocks.SimSessiondService;
import com.openexchange.sessiond.DefaultAddSessionParameter;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.SessionStorageService;

/**
 * {@link SessionOAuthTokenServiceTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class SessionOAuthTokenServiceTest {

    private static final long DEFAULT_TOKEN_EXPIRY_MILLIS = 1000L;

    private ServiceLookup services;
    private SessiondService sessiondService;
    private SessionStorageService sessionStorageService;
    private Session session;
    private DefaultSessionOAuthTokenService tokenService;

    @Before
    public void setUp() throws OXException {
        SimLockService lockService = new SimLockService();
        sessiondService = Mockito.spy(new SimSessiondService());
        sessionStorageService = new SimSessionStorageService();

        services = Mockito.mock(ServiceLookup.class);
        Mockito.when(services.getService(Mockito.any())).thenAnswer((invocation) -> {
            Class<?> clazz = invocation.getArgument(0);
            if (clazz.equals(LockService.class)) {
                return lockService;
            } else if (clazz.equals(SessiondService.class)) {
                return sessiondService;
            } else if (clazz.equals(SessionStorageService.class)) {
                return sessionStorageService;
            }
            return null;
        });
        Mockito.when(services.getOptionalService(SessiondService.class)).thenReturn(sessiondService);

        tokenService = new DefaultSessionOAuthTokenService(services);

        session = createSession();
    }

    @Test
    public void testNoTokensInSession() throws Exception {
        TokenRefreshConfig refreshConfig = TokenRefreshConfig.newBuilder()
            .setLockTimeout(100L, TimeUnit.MILLISECONDS)
            .setRefreshThreshold(100L, TimeUnit.MILLISECONDS)
            .build();

        RefreshResult result = tokenService.checkOrRefreshTokens(session, new AlwaysSucceedRefresher(), refreshConfig);
        assertFailureResult(FailReason.PERMANENT_ERROR, result);
    }

    @Test
    public void testNonExpired() throws Exception {
        OAuthTokens oldTokens = createTokens(expiresInMillis(100L));
        tokenService.setInSession(session, oldTokens);

        TokenRefreshConfig refreshConfig = TokenRefreshConfig.newBuilder()
            .setLockTimeout(100L, TimeUnit.MILLISECONDS)
            .setRefreshThreshold(1L, TimeUnit.MILLISECONDS)
            .build();

        RefreshResult result = tokenService.checkOrRefreshTokens(session, new AlwaysSucceedRefresher(), refreshConfig);
        assertSuccessResult(SuccessReason.NON_EXPIRED, result);

        Optional<OAuthTokens> tokensAfterCheck = tokenService.getFromSession(session);
        assertEquals(oldTokens, tokensAfterCheck.get());
    }

    @Test
    public void testBasicRefresh() throws Exception {
        OAuthTokens oldTokens = createTokens(expiresInMillis(0L));
        tokenService.setInSession(session, oldTokens);

        TokenRefreshConfig refreshConfig = TokenRefreshConfig.newBuilder()
            .setLockTimeout(100L, TimeUnit.MILLISECONDS)
            .setRefreshThreshold(100L, TimeUnit.MILLISECONDS)
            .build();

        RefreshResult result = tokenService.checkOrRefreshTokens(session, new AlwaysSucceedRefresher(), refreshConfig);
        assertSuccessResult(SuccessReason.REFRESHED, result);

        Optional<OAuthTokens> tokensAfterCheck = tokenService.getFromSession(session);
        assertNotEquals(oldTokens, tokensAfterCheck.get());

        Mockito.verify(sessiondService, Mockito.times(1)).storeSession(session.getSessionID(), false);
    }

    @Test
    public void testRethrowRefresherException() throws Exception {
        OAuthTokens oldTokens = createTokens(expiresInMillis(0L));
        tokenService.setInSession(session, oldTokens);

        TokenRefreshConfig refreshConfig = TokenRefreshConfig.newBuilder()
            .setLockTimeout(100L, TimeUnit.MILLISECONDS)
            .setRefreshThreshold(100L, TimeUnit.MILLISECONDS)
            .build();

        boolean thrown = false;
        try {
            tokenService.checkOrRefreshTokens(session, new ThrowExeptionRefresher(), refreshConfig);
        } catch (OXException e) {
            assertEquals(ThrowExeptionRefresher.EXCEPTION, e);
            thrown = true;
        }
        assertTrue(thrown);

        Optional<OAuthTokens> tokensAfterCheck = tokenService.getFromSession(session);
        assertEquals(oldTokens, tokensAfterCheck.get());
    }

    @Test
    public void testInvalidRefreshToken() throws InterruptedException, OXException {
        OAuthTokens oldTokens = createTokens(expiresInMillis(0L));
        tokenService.setInSession(session, oldTokens);

        TokenRefreshConfig refreshConfig = TokenRefreshConfig.newBuilder()
            .setLockTimeout(100L, TimeUnit.MILLISECONDS)
            .setRefreshThreshold(100L, TimeUnit.MILLISECONDS)
            .build();

        RefreshResult result = tokenService.checkOrRefreshTokens(session, new InvalidRefreshTokenRefresher(UUIDs.getUnformattedStringFromRandom()), refreshConfig);
        assertFailureResult(FailReason.INVALID_REFRESH_TOKEN, result);

        // invalid tokens must have been removed from session
        Optional<OAuthTokens> tokensAfterCheck = tokenService.getFromSession(session);
        assertFalse(tokensAfterCheck.isPresent());
    }

    @Test
    public void testRefresherReturnsTemporaryError() throws InterruptedException, OXException {
        OAuthTokens oldTokens = createTokens(expiresInMillis(0L));
        tokenService.setInSession(session, oldTokens);

        TokenRefreshConfig refreshConfig = TokenRefreshConfig.newBuilder()
            .setLockTimeout(100L, TimeUnit.MILLISECONDS)
            .setRefreshThreshold(100L, TimeUnit.MILLISECONDS)
            .build();

        RefreshResult result = tokenService.checkOrRefreshTokens(session, new TemporaryErrorRefresher(), refreshConfig);
        assertFailureResult(FailReason.TEMPORARY_ERROR, result);

        Optional<OAuthTokens> tokensAfterCheck = tokenService.getFromSession(session);
        assertEquals(oldTokens, tokensAfterCheck.get());
    }

    @Test
    public void testRefresherReturnsPermanentError() throws InterruptedException, OXException {
        OAuthTokens oldTokens = createTokens(expiresInMillis(0L));
        tokenService.setInSession(session, oldTokens);

        TokenRefreshConfig refreshConfig = TokenRefreshConfig.newBuilder()
            .setLockTimeout(100L, TimeUnit.MILLISECONDS)
            .setRefreshThreshold(100L, TimeUnit.MILLISECONDS)
            .build();

        RefreshResult result = tokenService.checkOrRefreshTokens(session, new PermanentErrorRefresher(), refreshConfig);
        assertFailureResult(FailReason.PERMANENT_ERROR, result);

        Optional<OAuthTokens> tokensAfterCheck = tokenService.getFromSession(session);
        assertEquals(oldTokens, tokensAfterCheck.get());
    }

    @Test
    public void testRefresherReturnsTooSoonExpiryDate() throws InterruptedException, OXException {
        OAuthTokens oldTokens = createTokens(expiresInMillis(0L));
        tokenService.setInSession(session, oldTokens);

        TokenRefreshConfig refreshConfig = TokenRefreshConfig.newBuilder()
            .setLockTimeout(100L, TimeUnit.MILLISECONDS)
            .setRefreshThreshold(2L, TimeUnit.SECONDS)
            .build();

        // new expiry is lower than refresh threshold (99ms vs. 100ms)
        RefreshResult result = tokenService.checkOrRefreshTokens(session, new AlwaysSucceedRefresher(1000l), refreshConfig);
        assertFailureResult(FailReason.PERMANENT_ERROR, result);

        Optional<OAuthTokens> tokensAfterCheck = tokenService.getFromSession(session);
        assertEquals(oldTokens, tokensAfterCheck.get());
    }

    /**
     * Two threads try to refresh token concurrently. First thread "other" gets the lock and
     * refreshes (takes ~200ms). Lock timeout is 100ms, so second thread (main thread) is supposed to
     * timeout.
     */
    @Test
    public void testLockTimeout() throws Exception {
        OAuthTokens oldTokens = createTokens(expiresInMillis(0L));
        tokenService.setInSession(session, oldTokens);

        TokenRefreshConfig refreshConfig = TokenRefreshConfig.newBuilder()
            .setLockTimeout(100L, TimeUnit.MILLISECONDS)
            .setRefreshThreshold(100L, TimeUnit.MILLISECONDS)
            .build();

        CyclicBarrier barrier = new CyclicBarrier(2);

        RefreshThread other = new RefreshThread(tokenService, session, new BlockingSucceedRefresher(200L, barrier), refreshConfig);
        other.start();

        // the other thread is now holding the lock
        barrier.await(1L, TimeUnit.SECONDS);

        RefreshResult result = tokenService.checkOrRefreshTokens(session, new AlwaysSucceedRefresher(), refreshConfig);
        RefreshResult otherResult = other.awaitResult();

        assertSuccessResult(SuccessReason.REFRESHED, otherResult);
        assertFailureResult(FailReason.LOCK_TIMEOUT, result);
    }

    /**
     * Two threads try to acquire the same lock to refresh the token. The first thread "other" succeeds while
     * holding the lock for ~100ms. The second thread "main" must not return with {@link SuccessReason#REFRESHED},
     * but any other success status.
     */
    @Test
    public void testNoConcurrentRefresh() throws Exception {
        OAuthTokens oldTokens = createTokens(expiresInMillis(0L));
        tokenService.setInSession(session, oldTokens);

        TokenRefreshConfig refreshConfig = TokenRefreshConfig.newBuilder()
            .setLockTimeout(200L, TimeUnit.MILLISECONDS)
            .setRefreshThreshold(100L, TimeUnit.MILLISECONDS)
            .build();

        CyclicBarrier barrier = new CyclicBarrier(2);

        RefreshThread other = new RefreshThread(tokenService, session, new BlockingSucceedRefresher(100L, barrier), refreshConfig);
        other.start();

        barrier.await(1L, TimeUnit.SECONDS);

        // the other thread is now holding the lock
        RefreshResult result = tokenService.checkOrRefreshTokens(session, new AlwaysSucceedRefresher(), refreshConfig);
        assertAnySuccessResultBut(SuccessReason.CONCURRENT_REFRESH, result);

        RefreshResult otherResult = other.awaitResult();
        assertSuccessResult(SuccessReason.REFRESHED, otherResult);
    }

    /**
     * Token refresh fails due to invalid refresh token. With 'tryRecoverStoredTokens = true' a
     * non-expired token is found in a stored session.
     */
    @Test
    public void testEnableTryRecoverStoredTokens_Success() throws Exception {
        OAuthTokens oldTokens = createTokens(expiresInMillis(0L));
        tokenService.setInSession(session, oldTokens);

        // add new tokens to stored session
        OAuthTokens storedTokens = createTokens(expiresInMillis(1000L));
        tokenService.setInSession(sessionStorageService.lookupSession(session.getSessionID()), storedTokens);

        TokenRefreshConfig refreshConfig = TokenRefreshConfig.newBuilder()
            .setLockTimeout(100L, TimeUnit.MILLISECONDS)
            .setRefreshThreshold(100L, TimeUnit.MILLISECONDS)
            .enableTryRecoverStoredTokens()
            .build();

        RefreshResult result = tokenService.checkOrRefreshTokens(session, new InvalidRefreshTokenRefresher(UUIDs.getUnformattedStringFromRandom()), refreshConfig);
        assertSuccessResult(SuccessReason.CONCURRENT_REFRESH, result);
    }

    /**
     * Token refresh fails due to invalid refresh token. With 'tryRecoverStoredTokens = true' no
     * non-expired token is found in a stored session => {@link FailReason#INVALID_REFRESH_TOKEN}
     * is returned.
     */
    @Test
    public void testEnableTryRecoverStoredTokens_Failure() throws Exception {
        OAuthTokens oldTokens = createTokens(expiresInMillis(0L));
        tokenService.setInSession(session, oldTokens);

        // store new parameters
        sessionStorageService.addSession(session);

        TokenRefreshConfig refreshConfig = TokenRefreshConfig.newBuilder()
            .setLockTimeout(100L, TimeUnit.MILLISECONDS)
            .setRefreshThreshold(100L, TimeUnit.MILLISECONDS)
            .enableTryRecoverStoredTokens()
            .build();

        RefreshResult result = tokenService.checkOrRefreshTokens(session, new InvalidRefreshTokenRefresher(UUIDs.getUnformattedStringFromRandom()), refreshConfig);
        assertFailureResult(FailReason.INVALID_REFRESH_TOKEN, result);
    }

    private static void assertSuccessResult(SuccessReason reason, RefreshResult result) {
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(reason, result.getSuccessReason());
        assertNull(result.getFailReason());
        assertFalse(result.isFail());
        assertFalse(result.hasException());
        assertNull(result.getException());
    }

    private static void assertAnySuccessResultBut(SuccessReason nonReason, RefreshResult result) {
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotEquals(nonReason, result.getSuccessReason());
        assertNull(result.getFailReason());
        assertFalse(result.isFail());
        assertFalse(result.hasException());
        assertNull(result.getException());
    }

    private static void assertFailureResult(FailReason reason, RefreshResult result) {
        assertNotNull("Expected failure result " + reason + " but was null", result);
        assertFalse("Expected failure result " + reason + " but was " + result.getSuccessReason(), result.isSuccess());
        assertTrue("Expected failure result " + reason + " but was " + result.getSuccessReason(), result.isFail());
        assertEquals(reason, result.getFailReason());
        assertNull(result.getSuccessReason());
    }

    static Date expiresInMillis(long inMillis) {
        return new Date(System.currentTimeMillis() + inMillis);
    }

    private Session createSession() throws OXException {
        DefaultAddSessionParameter param = new DefaultAddSessionParameter();
        param.setUserId(1);
        Context context = Mockito.mock(Context.class);
        Mockito.when(I(context.getContextId())).thenReturn(I(1));
        param.setContext(context);
        param.setFullLogin("testuser@example.com");
        Session session = sessiondService.addSession(param);
        sessionStorageService.addSession(session);
        return session;
    }

    static OAuthTokens createTokens(Date expiry) {
        return new OAuthTokens(UUIDs.getUnformattedStringFromRandom(), expiry, UUIDs.getUnformattedStringFromRandom());
    }

    private static final class AlwaysSucceedRefresher implements TokenRefresher {

        private final long expiryMillis;

        public AlwaysSucceedRefresher() {
            this(DEFAULT_TOKEN_EXPIRY_MILLIS);
        }

        public AlwaysSucceedRefresher(long expiryMillis) {
            super();
            this.expiryMillis = expiryMillis;
        }

        @Override
        public TokenRefreshResponse execute(OAuthTokens currentTokens) throws OXException {
            return new TokenRefreshResponse(createTokens(expiresInMillis(expiryMillis)));
        }
    }

    private static final class ThrowExeptionRefresher implements TokenRefresher {

        static final OXException EXCEPTION = OXException.general("test failure");

        public ThrowExeptionRefresher() {
            super();

        }

        @Override
        public TokenRefreshResponse execute(OAuthTokens currentTokens) throws OXException {
            throw EXCEPTION;
        }
    }

    private static class PredicateRefresher implements TokenRefresher {

        private final Predicate<OAuthTokens> predicate;
        private final TokenRefreshResponse errorResponse;

        PredicateRefresher(Predicate<OAuthTokens> predicate, TokenRefreshResponse errorResponse) {
            this.predicate = predicate;
            this.errorResponse = errorResponse;
        }

        @Override
        public TokenRefreshResponse execute(OAuthTokens currentTokens) throws OXException {
            if (predicate.test(currentTokens)) {
                return new TokenRefreshResponse(createTokens(expiresInMillis(DEFAULT_TOKEN_EXPIRY_MILLIS)));
            }

            return errorResponse;
        }
    }

    private static final class InvalidRefreshTokenRefresher extends PredicateRefresher {

        InvalidRefreshTokenRefresher(String expectedToken) {
            super((t) -> expectedToken.equals(t.getRefreshToken()),
                new TokenRefreshResponse(new TokenRefreshResponse.Error(ErrorType.INVALID_REFRESH_TOKEN, "invalid_grant", "")));
        }
    }

    private static final class TemporaryErrorRefresher implements TokenRefresher {

        public TemporaryErrorRefresher() {
            super();
        }

        @Override
        public TokenRefreshResponse execute(OAuthTokens currentTokens) throws OXException {
            Error error = new TokenRefreshResponse.Error(ErrorType.TEMPORARY, "error", "Temporary Error");
            return new TokenRefreshResponse(error);
        }
    }

    private static final class PermanentErrorRefresher implements TokenRefresher {

        public PermanentErrorRefresher() {
            super();
        }

        @Override
        public TokenRefreshResponse execute(OAuthTokens currentTokens) throws OXException {
            Error error = new TokenRefreshResponse.Error(ErrorType.PERMANENT, "error", "Permanent Error");
            return new TokenRefreshResponse(error);
        }
    }

    private static final class BlockingSucceedRefresher implements TokenRefresher {

        private final long blockMillis;
        private final CyclicBarrier barrier;

        BlockingSucceedRefresher(long blockMillis, CyclicBarrier barrier) {
            this.blockMillis = blockMillis;
            this.barrier = barrier;
        }

        @Override
        public TokenRefreshResponse execute(OAuthTokens currentTokens) throws OXException, InterruptedException {
            try {
                barrier.await(1L, TimeUnit.SECONDS);
            } catch (BrokenBarrierException e) {
                throw new IllegalStateException(e);
            } catch (TimeoutException e) {
                throw new IllegalStateException(e);
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(blockMillis));
            return new TokenRefreshResponse(createTokens(expiresInMillis(DEFAULT_TOKEN_EXPIRY_MILLIS)));
        }
    }

    private static final class RefreshThread extends Thread {

        private final SessionOAuthTokenService tokenService;
        private final Session session;
        private final TokenRefresher refresher;
        private final TokenRefreshConfig refreshConfig;
        private final CompletableFuture<RefreshResult> result;

        RefreshThread(SessionOAuthTokenService tokenService, Session session, TokenRefresher refresher, TokenRefreshConfig refreshConfig) {
            this.tokenService = tokenService;
            this.session = session;
            this.refresher = refresher;
            this.refreshConfig = refreshConfig;
            result = new CompletableFuture<RefreshResult>();
        }

        @Override
        public void run() {
            try {
                result.complete(tokenService.checkOrRefreshTokens(session, refresher, refreshConfig));
            } catch (InterruptedException | OXException e) {
                result.completeExceptionally(e);
            }
        }

        public RefreshResult awaitResult() throws InterruptedException, OXException, TimeoutException {
            try {
                return result.get();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof InterruptedException) {
                    throw (InterruptedException) cause;
                } else if (cause instanceof TimeoutException) {
                    throw (TimeoutException) cause;
                } else if (cause instanceof OXException) {
                    throw (OXException) cause;
                }

                throw new IllegalStateException(cause);
            }
        }

    }

}
