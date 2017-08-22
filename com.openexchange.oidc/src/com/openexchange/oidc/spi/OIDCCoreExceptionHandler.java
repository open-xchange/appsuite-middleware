package com.openexchange.oidc.spi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;


public class OIDCCoreExceptionHandler implements OIDCExceptionHandler {

    @Override
    public void handleAuthenticationFailed(HttpServletRequest request, HttpServletResponse response, OXException exception) {

    }

    @Override
    public void handleLogoutFailed(HttpServletRequest request, HttpServletResponse response, OXException exception) {

    }

    @Override
    public void handleLoginFailed(HttpServletRequest request, HttpServletResponse response, OXException exception) {

    }

}
