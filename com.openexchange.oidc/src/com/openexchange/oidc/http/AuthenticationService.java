package com.openexchange.oidc.http;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.oidc.OIDCWebSSOProvider;
import com.openexchange.oidc.spi.OIDCExceptionHandler;

public class AuthenticationService extends OIDCServlet{

    private static final long serialVersionUID = 7963146313895672894L;

    public AuthenticationService(OIDCWebSSOProvider provider, OIDCExceptionHandler exceptionHandler) {
        super(provider, exceptionHandler);
    }
    
    @Override
    protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {
        try {
            this.provider.authenticateUser(httpRequest, httpResponse);
        } catch (OXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
