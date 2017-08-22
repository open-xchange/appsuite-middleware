package com.openexchange.oidc.spi;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;


public class OIDCCoreExceptionHandler implements OIDCExceptionHandler {

    @Override
    public void handleAuthenticationFailed(HttpServletRequest request, HttpServletResponse response, OXException exception) throws IOException {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Override
    public void handleLogoutFailed(HttpServletRequest request, HttpServletResponse response, OXException exception) throws IOException {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

}
