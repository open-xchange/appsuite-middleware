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

package com.openexchange.saml.impl;

import java.util.Collections;
import org.opensaml.saml.security.impl.SAMLMetadataSignatureSigningParametersResolver;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.SecurityConfigurationSupport;
import org.opensaml.xmlsec.SignatureSigningConfiguration;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.SignatureSigningParametersResolver;
import org.opensaml.xmlsec.criterion.SignatureSigningConfigurationCriterion;
import org.opensaml.xmlsec.impl.BasicSignatureSigningConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import com.openexchange.exception.OXException;
import com.openexchange.saml.SAMLExceptionCode;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.Criterion;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

/**
 * {@link AlgorithmUtils} - Helper class to deal with algorithms
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.4
 */
public class AlgorithmUtils {

    private static final Logger LOG = LoggerFactory.getLogger(AlgorithmUtils.class);

    /**
     * Returns the algorithm URI the given {@link Credential}s have been signed with
     * 
     * @param signingCredential - {@link Credential} the signed credentials
     * @return the signature algorithm URI used for signing
     * @throws OXException
     */
    public static String getAlgorithmURI(Credential signingCredential) throws OXException {
        SignatureSigningParametersResolver resolver = new SAMLMetadataSignatureSigningParametersResolver();
        try {
            CriteriaSet criterias = new CriteriaSet();

            BasicSignatureSigningConfiguration credentialSignatureSigningConfiguration = new BasicSignatureSigningConfiguration();
            credentialSignatureSigningConfiguration.setSigningCredentials(Collections.singletonList(signingCredential));

            SignatureSigningConfiguration globalSignatureSigningConfiguration = SecurityConfigurationSupport.getGlobalSignatureSigningConfiguration();
            Criterion signatureSigningConfigurationCriterion = new SignatureSigningConfigurationCriterion(Lists.newArrayList(globalSignatureSigningConfiguration, credentialSignatureSigningConfiguration));
            criterias.add(signatureSigningConfigurationCriterion);

            SignatureSigningParameters signatureSigningParameters = resolver.resolveSingle(criterias);
            if(null == signatureSigningParameters) {
                throw SAMLExceptionCode.INTERNAL_ERROR.create("Unable to resolve signing parameters.");
            }
            return signatureSigningParameters.getSignatureAlgorithm();
        } catch (ResolverException e) {
            LOG.error("Unable to resolve signing parameters.", e);
            throw SAMLExceptionCode.INTERNAL_ERROR.create(e.getMessage());
        }
    }
}
