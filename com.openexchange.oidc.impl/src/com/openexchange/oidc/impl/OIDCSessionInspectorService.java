package com.openexchange.oidc.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.osgi.OIDCBackendRegistry;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;
import com.openexchange.session.inspector.Reason;
import com.openexchange.session.inspector.SessionInspectorService;
import com.openexchange.session.oauth.RefreshResult;
import com.openexchange.session.oauth.RefreshResult.FailReason;
import com.openexchange.session.oauth.SessionOAuthTokenService;
import com.openexchange.session.oauth.TokenRefreshConfig;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link OIDCSessionInspectorService} Is triggered on each Request, that comes
 * with a {@link Session} parameter. Is used to check on expired OAuth tokens, if storage of
 * those is enabled for the current backend.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class OIDCSessionInspectorService implements SessionInspectorService{

    private static final Logger LOG = LoggerFactory.getLogger(OIDCSessionInspectorService.class);

    private final OIDCBackendRegistry oidcBackends;

    private final SessionOAuthTokenService tokenService;

    public OIDCSessionInspectorService(OIDCBackendRegistry oidcBackendRegistry, SessionOAuthTokenService tokenService) {
        super();
        this.oidcBackends = oidcBackendRegistry;
        this.tokenService = tokenService;
    }

    @Override
    public Reply onSessionHit(Session session, HttpServletRequest request, HttpServletResponse response) throws OXException {
        if (session.getParameter(OIDCTools.IDTOKEN) == null) {
            // session not managed by us
            LOG.debug("Skipping unmanaged session: {}", session.getSessionID());
            return Reply.NEUTRAL;
        }

        OIDCBackend backend = this.loadBackendForSession(session);
        if (null == backend) {
            LOG.warn("Unable to load OIDC backend for session due to missing path parameter: {}", session.getSessionID());
            return Reply.NEUTRAL;
        }

        try {
            OIDCBackendConfig config = backend.getBackendConfig();
            OIDCTokenRefresher refresher = new OIDCTokenRefresher(backend, session);
            TokenRefreshConfig refreshConfig = OIDCTools.getTokenRefreshConfig(config);
            RefreshResult result = tokenService.checkOrRefreshTokens(session, refresher, refreshConfig);
            if (result.isSuccess()) {
                LOG.debug("Returning neutral reply for session '{}' due to successful token refresh result: {}", session.getSessionID(), result.getSuccessReason().name());
                return Reply.NEUTRAL;
            }

            return handleErrorResult(session, result);
        } catch (OXException e) {
            LOG.error("Error while checking oauth tokens for session '{}'", session.getSessionID(), e);
            // try to perform request anyway on best effort
            return Reply.NEUTRAL;
        } catch (InterruptedException e) {
            LOG.warn("Thread was interrupted while checking session oauth tokens");
            // keep interrupted state
            Thread.currentThread().interrupt();
            return Reply.STOP;
        }
    }

    private Reply handleErrorResult(Session session, RefreshResult result) throws OXException {
        RefreshResult.FailReason failReason = result.getFailReason();
        if (failReason == FailReason.INVALID_REFRESH_TOKEN || failReason == FailReason.PERMANENT_ERROR) {
            if (result.hasException()) {
                LOG.info("Terminating session '{}' due to oauth token refresh error: {} ({})", session.getSessionID(), failReason.name(), result.getErrorDesc(), result.getException());
            } else {
                LOG.info("Terminating session '{}' due to oauth token refresh error: {} ({})", session.getSessionID(), failReason.name(), result.getErrorDesc());
            }
            SessiondService sessiondService = Services.getService(SessiondService.class);
            sessiondService.removeSession(session.getSessionID());
            throw SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID());
        }

        // try to perform request anyway on best effort
        if (result.hasException()) {
            LOG.warn("Error while refreshing oauth tokens for session '{}': {}", session.getSessionID(), result.getErrorDesc(), result.getException());
        } else {
            LOG.warn("Error while refreshing oauth tokens for session '{}': {}", session.getSessionID(), result.getErrorDesc());
        }
        return Reply.NEUTRAL;
    }

    private OIDCBackend loadBackendForSession(Session session) throws OXException{
        String backendPath = (String) session.getParameter(OIDCTools.BACKEND_PATH);
        if (null == backendPath) {
            return null;
        }

        for (OIDCBackend backend : this.oidcBackends.getAllRegisteredBackends()) {
            if (backend.getPath().equals(backendPath)) {
                return backend;
            }
        }
        throw OIDCExceptionCode.UNABLE_TO_FIND_BACKEND_FOR_SESSION.create(backendPath);
    }

    @Override
    public Reply onSessionMiss(String sessionId, HttpServletRequest request, HttpServletResponse response) throws OXException {
        return Reply.NEUTRAL;
    }

    @Override
    public Reply onAutoLoginFailed(Reason reason, HttpServletRequest request, HttpServletResponse response) throws OXException {
        return Reply.NEUTRAL;
    }

}
