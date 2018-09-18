package com.openexchange.oidc.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.osgi.OIDCBackendRegistry;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;
import com.openexchange.session.inspector.Reason;
import com.openexchange.session.inspector.SessionInspectorService;

/**
 * {@link OIDCSessionInspectorService} Is triggered on each Request, that comes
 * with a {@link Session} parameter. Is used to check on expired OAuth tokens, if storage of
 * those is enabled for the current backend.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class OIDCSessionInspectorService implements SessionInspectorService{

    private final OIDCBackendRegistry oidcBackends;

    public OIDCSessionInspectorService(OIDCBackendRegistry oidcBackendRegistry) {
        super();
        this.oidcBackends = oidcBackendRegistry;
    }

    @Override
    public Reply onSessionHit(Session session, HttpServletRequest request, HttpServletResponse response) throws OXException {
        OIDCBackend backend = this.loadBackendForSession(session);
        if (null == backend) {
            return Reply.NEUTRAL;
        }
        if (backend.isTokenExpired(session) && !backend.updateOauthTokens(session)) {
            backend.logoutCurrentUser(session, request, response);
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
