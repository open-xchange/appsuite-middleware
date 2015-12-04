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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.onboarding.signature;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.util.Store;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.onboarding.OnboardingExceptionCodes;
import com.openexchange.onboarding.osgi.Services;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;

/**
 * {@link PListSigner}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public final class PListSigner {

    private final ThresholdFileHolder fileHolder;

    /**
     * Initializes a new {@link PListSigner}.
     */
    public PListSigner(ThresholdFileHolder fileHolder) {
        super();
        this.fileHolder = fileHolder;
    }

    public ThresholdFileHolder signPList(Session session) throws OXException {
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        if (null == viewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        ConfigurationService configService = Services.getService(ConfigurationService.class);
        if (null == configService) {
            throw ServiceExceptionCode.absentService(ConfigurationService.class);
        }

        boolean enabled = configService.getBoolProperty("com.openexchange.onboarding.plist.signature.enabled", false);
        String storeName = configService.getProperty("com.openexchange.onboarding.plist.pkcs12store.filename");
        String password = configService.getProperty("com.openexchange.onboarding.plist.pkcs12store.password");
        String alias = view.get("com.openexchange.onboarding.plist.signkey.alias", String.class);
        if (!enabled || Strings.isEmpty(storeName) || Strings.isEmpty(password) || Strings.isEmpty(alias)) {
            return fileHolder;
        }
        byte[] signed = signPList(fileHolder.toByteArray(), storeName, password, alias);
        fileHolder.reset();
        fileHolder.write(signed);
        return fileHolder;
    }

    private byte[] signPList(byte[] in, String storeName, String password, String alias) throws OXException {
        try {
            PrivateKey privKey = getPrivateKey(storeName, password, alias);
            X509Certificate cert = getCertificate(storeName, password, alias);
            List<X509Certificate> certList = Collections.singletonList(cert);
            Store certs = new JcaCertStore(certList);
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            JcaSimpleSignerInfoGeneratorBuilder builder = new JcaSimpleSignerInfoGeneratorBuilder();
            gen.addSignerInfoGenerator(builder.build("SHA1withRSA", privKey, cert));
            gen.addCertificates(certs);
            CMSTypedData data = new CMSProcessableByteArray(in);
            CMSSignedData signed = gen.generate(data, true);
            return signed.getEncoded();
        } catch (Exception e) {
            throw OnboardingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private X509Certificate getCertificate(String storeName, String password, String alias) throws Exception {
        KeyStore store = KeyStore.getInstance("PKCS12");
        store.load(new FileInputStream(storeName), password.toCharArray());
        return (X509Certificate) store.getCertificate(alias);
    }

    private PrivateKey getPrivateKey(String storeName, String password, String alias) throws Exception {
        KeyStore store = KeyStore.getInstance("PKCS12");
        store.load(new FileInputStream(storeName), password.toCharArray());
        return (PrivateKey) store.getKey(alias, password.toCharArray());
    }

}
