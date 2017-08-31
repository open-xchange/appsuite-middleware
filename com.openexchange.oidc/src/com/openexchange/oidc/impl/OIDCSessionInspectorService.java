package com.openexchange.oidc.impl;

import java.util.Map.Entry;
import java.util.SortedMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.openexchange.exception.OXException;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.osgi.OIDCBackendRegistry;
import com.openexchange.oidc.spi.OIDCBackend;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;
import com.openexchange.session.inspector.Reason;
import com.openexchange.session.inspector.SessionInspectorService;

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
        if (backend.tokensExpired(session)) {
            if(!backend.updateOauthTokens(session)) {
                backend.logoutCurrentUser(session, request, response, null);
            }
        }
        return Reply.NEUTRAL;
    }

    private OIDCBackend loadBackendForSession(Session session) throws OXException{
        SortedMap<ServiceReference<OIDCBackend>,OIDCBackend> tracked = oidcBackends.getTracked();
        String sessionBackendPath = (String) session.getParameter(OIDCTools.BACKEND_PATH);
        for (Entry<ServiceReference<OIDCBackend>, OIDCBackend> entry : tracked.entrySet()) {
            OIDCBackend backend = context.getService(entry.getKey());
            if (sessionBackendPath != null && backend.getPath().equals(sessionBackendPath)) {
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
