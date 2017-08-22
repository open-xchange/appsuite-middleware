
package com.openexchange.oidc.http;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.oidc.OIDCWebSSOProvider;
import com.openexchange.oidc.spi.OIDCExceptionHandler;

public class AuthenticationService extends OIDCServlet {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);
    private static final long serialVersionUID = 7963146313895672894L;

    public AuthenticationService(OIDCWebSSOProvider provider, OIDCExceptionHandler exceptionHandler) {
        super(provider, exceptionHandler);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            this.provider.authenticateUser(request, response);
        } catch (OXException e) {
            exceptionHandler.handleAuthenticationFailed(request, response, e);
            LOG.error(e.getLocalizedMessage());
        }
    }

}
