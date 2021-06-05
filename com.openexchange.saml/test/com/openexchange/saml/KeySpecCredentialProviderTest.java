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

import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.crypto.SigningUtil;
import com.openexchange.saml.spi.KeySpecCredentialProvider;
import com.openexchange.saml.spi.KeySpecCredentialProvider.Algorithm;
import com.openexchange.saml.spi.KeySpecCredentialProvider.SpecContainer;
import net.shibboleth.utilities.java.support.codec.Base64Support;


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
        X509EncodedKeySpec idpPublicKeySpec = new X509EncodedKeySpec(Base64Support.decode(B64_DSA_512_DER_PUBLIC_KEY));
        PKCS8EncodedKeySpec idpPrivateKeySpec = new PKCS8EncodedKeySpec(Base64Support.decode(B64_DSA_512_DER_PRIVATE_KEY));
        KeySpecCredentialProvider credentialProvider = KeySpecCredentialProvider.newInstance(new SpecContainer(idpPublicKeySpec, null, Algorithm.DSA), new SpecContainer(null, idpPrivateKeySpec, Algorithm.DSA), null);
        Assert.assertTrue(credentialProvider.hasValidationCredentials());
        List<Credential> validationCredentials = credentialProvider.getValidationCredentials();
        Assert.assertNotNull(validationCredentials);
        Assert.assertTrue(credentialProvider.hasSigningCredential());
        Credential signingCredential = credentialProvider.getSigningCredential();
        Assert.assertNotNull(signingCredential);

        byte[] signature = SigningUtil.sign(signingCredential, "SHA1withDSA", false, SIGNING_INPUT.getBytes());
        Assert.assertNotNull(signature);
        Assert.assertTrue(signature.length > 0);
        SigningUtil.verify(validationCredentials.get(0), "SHA1withDSA", false, signature, SIGNING_INPUT.getBytes());
    }

}
