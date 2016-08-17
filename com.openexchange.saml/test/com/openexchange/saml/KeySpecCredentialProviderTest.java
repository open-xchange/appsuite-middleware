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

import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.junit.Assert;
import org.junit.Test;
import org.opensaml.xml.security.SigningUtil;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.util.Base64;
import com.openexchange.saml.spi.KeySpecCredentialProvider;
import com.openexchange.saml.spi.KeySpecCredentialProvider.Algorithm;
import com.openexchange.saml.spi.KeySpecCredentialProvider.SpecContainer;


/**
 * {@link KeySpecCredentialProviderTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class KeySpecCredentialProviderTest {

    private static final String B64_DSA_512_DER_PUBLIC_KEY =
        "MIHwMIGoBgcqhkjOOAQBMIGcAkEA39Kczu16QbtFpUA5/HlTVXqnW8ZrMXxHSp5v88xXgTmc8AAB" +
        "x7YmCHL6ip6Ah8c8doKt9jTRtpEYwN5OGRCBQQIVAM2LX3ApM7Tbh7STh9RWV95GVSCvAkAYJuFP" +
        "VRpvUtTLpNsZsl5dLjVi25pWzCuewY9RelqA6yaq2w4CszQq+ZKU65aLGpeEKtc66yzY/PTzzVeA" +
        "Uv1mA0MAAkBYSdNpYO/E4Ysw7IvglZJGhObTZ8dkzoAyALA9svlOXm+hoOmUDKP9lb3rFreED1ze" +
        "4a0gV0ZMqDApIA0zKcxz";

    private static final String B64_DSA_512_DER_PRIVATE_KEY =
        "MIHGAgEAMIGoBgcqhkjOOAQBMIGcAkEA39Kczu16QbtFpUA5/HlTVXqnW8ZrMXxHSp5v88xXgTmc" +
        "8AABx7YmCHL6ip6Ah8c8doKt9jTRtpEYwN5OGRCBQQIVAM2LX3ApM7Tbh7STh9RWV95GVSCvAkAY" +
        "JuFPVRpvUtTLpNsZsl5dLjVi25pWzCuewY9RelqA6yaq2w4CszQq+ZKU65aLGpeEKtc66yzY/PTz" +
        "zVeAUv1mBBYCFE3W3pI6SVFrpBZyi1aJTrWVmztU";

    private static final String SIGNING_INPUT = "IamABunchOfTextPossiblyXMLOrSo";

    @Test
    public void testSigningWithRawDERKeys() throws Exception {
        X509EncodedKeySpec idpPublicKeySpec = new X509EncodedKeySpec(Base64.decode(B64_DSA_512_DER_PUBLIC_KEY));
        PKCS8EncodedKeySpec idpPrivateKeySpec = new PKCS8EncodedKeySpec(Base64.decode(B64_DSA_512_DER_PRIVATE_KEY));
        KeySpecCredentialProvider credentialProvider = KeySpecCredentialProvider.newInstance(new SpecContainer(idpPublicKeySpec, null, Algorithm.DSA), new SpecContainer(null, idpPrivateKeySpec, Algorithm.DSA), null);
        Assert.assertTrue(credentialProvider.hasValidationCredential());
        Credential validationCredential = credentialProvider.getValidationCredential();
        Assert.assertNotNull(validationCredential);
        Assert.assertTrue(credentialProvider.hasSigningCredential());
        Credential signingCredential = credentialProvider.getSigningCredential();
        Assert.assertNotNull(signingCredential);

        byte[] signature = SigningUtil.sign(signingCredential, "SHA1withDSA", false, SIGNING_INPUT.getBytes());
        Assert.assertNotNull(signature);
        Assert.assertTrue(signature.length > 0);
        SigningUtil.verify(validationCredential, "SHA1withDSA", false, signature, SIGNING_INPUT.getBytes());
    }

}
