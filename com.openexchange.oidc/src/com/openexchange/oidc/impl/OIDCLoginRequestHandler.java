package com.openexchange.oidc.impl;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.oidc.spi.OIDCBackend;


public class OIDCLoginRequestHandler implements LoginRequestHandler {
    
    private LoginConfiguration loginConfiguration;
    private OIDCBackend backend;
    
    public OIDCLoginRequestHandler(LoginConfiguration loginConfiguration, OIDCBackend backend) {
        this.loginConfiguration = loginConfiguration;
        this.backend = backend;
    }

    @Override
    public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        
    }

}
