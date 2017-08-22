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
import com.openexchange.oidc.tools.OIDCTools;

public class LogoutService extends OIDCServlet{

    private static final Logger LOG = LoggerFactory.getLogger(LogoutService.class);
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 6178674444883447429L;

    public LogoutService(OIDCWebSSOProvider provider, OIDCExceptionHandler exceptionHandler) {
        super(provider, exceptionHandler);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getParameter(OIDCTools.TYPE) == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else if (request.getParameter(OIDCTools.TYPE).equalsIgnoreCase(OIDCTools.END)) {
            try {
                this.provider.logoutSSOUser(request, response);
            } catch (OXException e) {
                // TODO QS-VS: Better exception handling
                exceptionHandler.handleLogoutFailed(request, response, e);
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }
}
