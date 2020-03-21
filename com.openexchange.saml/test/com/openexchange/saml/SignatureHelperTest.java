/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.saml;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Matchers;
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
import org.powermock.reflect.Whitebox;
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
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(SigningUtil.class);
        PowerMockito.mockStatic(SignatureValidator.class);
        this.credentials = new ArrayList<>();
        this.credentials.add(credOne);
        this.credentials.add(credTwo);
        Mockito.when(object.getSignature()).thenReturn(signature);
    }

    @Test
    public void testValidateSignature_TwoSigPass() throws Exception {
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
