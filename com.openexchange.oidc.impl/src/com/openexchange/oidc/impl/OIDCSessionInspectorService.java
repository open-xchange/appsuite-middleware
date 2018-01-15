package com.openexchange.oidc.impl;

import java.util.Map.Entry;
import java.util.SortedMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
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
    private final BundleContext context;

    public OIDCSessionInspectorService(OIDCBackendRegistry oidcBackends, BundleContext context) {
        this.oidcBackends = oidcBackends;
        this.context = context;
    }

    @Override
    public Reply onSessionHit(Session session, HttpServletRequest request, HttpServletResponse response) throws OXException {
        OIDCBackend backend = this.loadBackendForSession(session);
        if (null == backend) {
            return Reply.NEUTRAL;
        }
        if (backend.isTokenExpired(session)) {
            if(!backend.updateOauthTokens(session)) {
                backend.logoutCurrentUser(session, request, response);
            }
        }
        return Reply.NEUTRAL;
    }

    private OIDCBackend loadBackendForSession(Session session) throws OXException{
        SortedMap<ServiceReference<OIDCBackend>,OIDCBackend> tracked = this.oidcBackends.getTracked();
        String backendPath = (String) session.getParameter(OIDCTools.BACKEND_PATH);
        if (null == backendPath) {
            return null;
        }
        String sessionBackendPath = backendPath;
        for (Entry<ServiceReference<OIDCBackend>, OIDCBackend> entry : tracked.entrySet()) {
            OIDCBackend backend = this.context.getService(entry.getKey());
            if (backend.getPath().equals(sessionBackendPath)) {
                return backend;
            }
        }
        throw OIDCExceptionCode.UNABLE_TO_FIND_BACKEND_FOR_SESSION.create(sessionBackendPath);
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
