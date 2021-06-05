/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.saml;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.crypto.SigningUtil;
import org.opensaml.xmlsec.signature.SignableXMLObject;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.saml.tools.SignatureHelper;
import com.openexchange.saml.validation.ValidationError;

/**
 * 
 * {@link SignatureHelperTest}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.1
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ SignatureHelper.class, SignatureValidator.class, SigningUtil.class })
public class SignatureHelperTest {

    private List<Credential> credentials;

    @Mock
    SignableXMLObject object;

    @Mock
    Credential credOne;

    @Mock
    Credential credTwo;

    @Mock
    Signature signature;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(SigningUtil.class);
        PowerMockito.mockStatic(SignatureValidator.class);
        this.credentials = new ArrayList<>();
        this.credentials.add(credOne);
        this.credentials.add(credTwo);
        Mockito.when(object.getSignature()).thenReturn(signature);
    }

    @Test
    public void testValidateSignature_TwoSigPass() {
        ValidationError error = SignatureHelper.validateSignature(object, credentials);
        assertTrue("Expected no validation failure, but did fail", error == null);
    }

    @Test
    public void testValidateSignature_OneSigPass() throws Exception {
        PowerMockito.doThrow(new SignatureException()).when(SignatureValidator.class, "validate", signature, credOne);
        PowerMockito.doNothing().when(SignatureValidator.class, "validate", signature, credTwo);

        ValidationError error = SignatureHelper.validateSignature(object, credentials);
        assertTrue("Expected no validation failure, but did fail", error == null);
    }
}
