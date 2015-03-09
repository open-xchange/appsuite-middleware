package com.openexchange.saml;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import com.openexchange.exception.OXException;
import com.openexchange.saml.spi.AuthnResponseHandler;

public class TestResponseHandler implements AuthnResponseHandler {

    @Override
    public boolean beforeDecode(HttpServletRequest httpRequest, HttpServletResponse httpResponse, OpenSAML openSAML) throws OXException {
        return true;
    }

    @Override
    public boolean beforeValidate(Response response, OpenSAML openSAML) throws OXException {
        return true;
    }

    @Override
    public boolean afterValidate(Response response, List<Assertion> assertions, OpenSAML openSAML) throws OXException {
        return true;
    }

    @Override
    public Principal resolvePrincipal(Response response, Assertion assertion, OpenSAML openSAML) throws OXException {
        return new Principal(0, 0);
    }

}