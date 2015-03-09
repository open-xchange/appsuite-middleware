package com.openexchange.saml;

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import com.openexchange.exception.OXException;
import com.openexchange.saml.SAMLConfig.Binding;
import com.openexchange.saml.spi.AuthnResponseHandler;
import com.openexchange.saml.spi.Principal;
import com.openexchange.saml.validation.ValidationResult;

public class TestResponseHandler implements AuthnResponseHandler {

    @Override
    public Principal resolvePrincipal(Response response, Assertion assertion, OpenSAML openSAML) throws OXException {
        return new Principal(0, 0);
    }

    @Override
    public ValidationResult validate(Response response, Binding binding) {
        // TODO Auto-generated method stub
        return null;
    }

}