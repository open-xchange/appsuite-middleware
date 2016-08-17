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

package com.openexchange.client.onboarding.plist.internal;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSProcessableFile;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.util.Store;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.client.onboarding.plist.PListSigner;
import com.openexchange.client.onboarding.plist.osgi.Services;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;

/**
 * {@link PListSignerImpl}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public final class PListSignerImpl implements PListSigner {

    /**
     * Initializes a new {@link PListSignerImpl}.
     */
    public PListSignerImpl() {
        super();
    }

    @Override
    public IFileHolder signPList(IFileHolder toSign, Session session) throws OXException {
        return signPList(toSign, session.getUserId(), session.getContextId());
    }

    @Override
    public IFileHolder signPList(IFileHolder toSign, int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        if (null == viewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }
        ConfigView view = viewFactory.getView(userId, contextId);
        ConfigurationService configService = Services.getService(ConfigurationService.class);
        if (null == configService) {
            throw ServiceExceptionCode.absentService(ConfigurationService.class);
        }

        // Check if enabled
        boolean enabled = configService.getBoolProperty("com.openexchange.client.onboarding.plist.signature.enabled", false);
        if (false == enabled) {
            return toSign;
        }

        // Get & check needed parameters
        String storeName = configService.getProperty("com.openexchange.client.onboarding.plist.pkcs12store.filename");
        String password = configService.getProperty("com.openexchange.client.onboarding.plist.pkcs12store.password");
        String alias = view.get("com.openexchange.client.onboarding.plist.signkey.alias", String.class);
        if (Strings.isEmpty(storeName) || Strings.isEmpty(password) || Strings.isEmpty(alias)) {
            return toSign;
        }

        return signPList(toSign, storeName, password, alias);
    }

    private IFileHolder signPList(IFileHolder toSign, String storeName, String password, String alias) throws OXException {
        IFileHolder input = toSign;

        ThresholdFileHolder sink = null;
        boolean error = true;
        try {
            PrivateKey privKey = getPrivateKey(storeName, password, alias);
            Certificate[] certChain = getCertificateChain(storeName, password, alias);
            X509Certificate cert = (X509Certificate) certChain[0];
            Store<?> certs = new JcaCertStore(Arrays.asList(certChain));

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            JcaSimpleSignerInfoGeneratorBuilder builder = new JcaSimpleSignerInfoGeneratorBuilder();
            gen.addSignerInfoGenerator(builder.build("SHA256withRSA", privKey, cert));
            gen.addCertificates(certs);

            CMSTypedData data;
            {
                // Ensure to deal with an instance of ThresholdFileHolder to yield appropriate processable data
                if (input instanceof ThresholdFileHolder) {
                    ThresholdFileHolder tfh = (ThresholdFileHolder) input;
                    data = toProcessableData(tfh);
                } else {
                    ThresholdFileHolder tfh = new ThresholdFileHolder(input);
                    input.close();
                    input = tfh;
                    data = toProcessableData(tfh);
                }
            }

            // Sign it
            CMSSignedData signed = gen.generate(data, true);
            ContentInfo contentInfo = signed.toASN1Structure();

            // Flush signed content to a new ThresholdFileHolder
            sink = new ThresholdFileHolder();
            sink.setContentType(input.getContentType());
            sink.setDisposition(input.getDisposition());
            sink.setName(input.getName());
            ASN1OutputStream aOut = new ASN1OutputStream(sink.asOutputStream());
            aOut.writeObject(contentInfo);
            aOut.flush();
            error = false; // Avoid preliminary closing
            return sink;
        } catch (OXException e) {
            throw e;
        } catch (Exception e) {
            throw OnboardingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (error) {
                Streams.close(sink);
            }
            Streams.close(input);
        }
    }

    private CMSTypedData toProcessableData(ThresholdFileHolder tfh) throws OXException {
        File tempFile = tfh.getTempFile();
        return null == tempFile ? new CMSProcessableByteArray(tfh.toByteArray()) : new CMSProcessableFile(tempFile);
    }

    private Certificate[] getCertificateChain(String storeName, String password, String alias) throws Exception {
        KeyStore store = KeyStore.getInstance("PKCS12");
        FileInputStream fis = new FileInputStream(storeName);
        try {
            store.load(fis, password.toCharArray());
            return store.getCertificateChain(alias);
        } finally {
            Streams.close(fis);
        }
    }

    private PrivateKey getPrivateKey(String storeName, String password, String alias) throws Exception {
        KeyStore store = KeyStore.getInstance("PKCS12");
        FileInputStream fis = new FileInputStream(storeName);
        try {
            store.load(fis, password.toCharArray());
            return (PrivateKey) store.getKey(alias, password.toCharArray());
        } finally {
            Streams.close(fis);
        }
    }

}
