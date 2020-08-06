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
            return signatureSigningParameters.getSignatureAlgorithm();
        } catch (ResolverException e) {
            LOG.error("Unable while trying to resolve signing parameters.", e);
            throw SAMLExceptionCode.INTERNAL_ERROR.create(e.getMessage());
        }
    }
}
