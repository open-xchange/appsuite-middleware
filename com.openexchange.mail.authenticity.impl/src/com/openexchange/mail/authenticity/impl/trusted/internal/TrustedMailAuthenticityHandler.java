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

package com.openexchange.mail.authenticity.impl.trusted.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.naming.ConfigurationException;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Interests;
import com.openexchange.config.PropertyFilter;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.authenticity.MailAuthenticityExceptionCodes;
import com.openexchange.mail.authenticity.MailAuthenticityResultKey;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.impl.trusted.Icon;
import com.openexchange.mail.authenticity.impl.trusted.TrustedMailService;
import com.openexchange.mail.authenticity.mechanism.AuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanism;
import com.openexchange.mail.authenticity.mechanism.SimplePassFailResult;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.session.Session;

/**
 * {@link TrustedMailAuthenticityHandler}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class TrustedMailAuthenticityHandler implements ForcedReloadable, TrustedMailService {

    private static final Logger LOG = LoggerFactory.getLogger(TrustedMailAuthenticityHandler.class);

    /*
     * Property keys
     */
    private static final String PREFIX = "com.openexchange.mail.authenticity.trusted.";
    private static final String TENANT = "tenants";
    private static final String CONFIG = ".config";
    private static final String IMAGE = ".image.";
    private static final String FALLBACK_IMAGE = ".fallbackImage";

    static final MailAuthenticityMechanism TRUSTED_MAIL_MECHANISM = new MailAuthenticityMechanism() {

        @Override
        public Class<? extends AuthenticityMechanismResult> getResultType() {
            return SimplePassFailResult.class;
        }

        @Override
        public String getDisplayName() {
            return "TrustedMail";
        }

        @Override
        public String getTechnicalName() {
            return "TrustedMail";
        }

        @Override
        public String toString() {
            return getDisplayName();
        }

        @Override
        public int getCode() {
            return 0;
        }
    };

    private final Map<String, List<TrustedMail>> trustedMailAddressesPerTenant;
    private final List<TrustedMail> fallbackTenant;

    /**
     * Initializes a new {@link TrustedMailAuthenticityHandler}.
     *
     * @throws OXException
     */
    public TrustedMailAuthenticityHandler(ConfigurationService configurationService) throws OXException {
        super();
        this.trustedMailAddressesPerTenant = new ConcurrentHashMap<>();
        this.fallbackTenant = new CopyOnWriteArrayList<>();
        init(configurationService);
    }

    @Override
    public void handle(Session session, MailMessage mailMessage) {
        String tenant = (String) session.getParameter(Session.PARAM_HOST_NAME);
        if (tenant == null) {
            LOG.warn("Missing host name session parameter. Unable to verify mail.");
            return;
        }

        MailAuthenticityResult authenticityResult = mailMessage.getAuthenticityResult();

        if (MailAuthenticityStatus.PASS.equals(authenticityResult.getStatus())) {
            String mailAddress = getMailAddress(mailMessage);
            TrustedMail trustedDomain = checkMail(tenant, mailAddress);
            if (trustedDomain != null) {
                authenticityResult.setStatus(MailAuthenticityStatus.TRUSTED);
                if (trustedDomain.getImage() != null) {
                    authenticityResult.addAttribute(MailAuthenticityResultKey.IMAGE, trustedDomain.getImage().getUID());
                }
            }
        }
    }

    private TrustedMail checkMail(String tenant, String mailAddress) {
        if (trustedMailAddressesPerTenant.containsKey(tenant)) {
            List<TrustedMail> trustedMails = trustedMailAddressesPerTenant.get(tenant);
            for (TrustedMail trustedMail : trustedMails) {
                if (trustedMail.matches(mailAddress)) {
                    return trustedMail;
                }
            }
        }

        if (fallbackTenant != null) {
            for (TrustedMail trustedMail : fallbackTenant) {
                if (trustedMail.matches(mailAddress)) {
                    return trustedMail;
                }
            }
        }
        return null;
    }

    private void init(ConfigurationService configurationService) throws OXException {
        String commaSeparatedListOfTenants = configurationService.getProperty(PREFIX + TENANT, "");
        if (!Strings.isEmpty(commaSeparatedListOfTenants)) {
            String[] tenants = Strings.splitByCommaNotInQuotes(commaSeparatedListOfTenants);
            for (String tenant : tenants) {
                String commaSeparatedListOfMailAddresses = configurationService.getProperty(PREFIX + tenant + CONFIG, (String) null);
                if (Strings.isNotEmpty(commaSeparatedListOfMailAddresses)) {
                    String[] mailAddresses = Strings.splitByCommaNotInQuotes(commaSeparatedListOfMailAddresses);
                    String fallbackImageStr = configurationService.getProperty(PREFIX + tenant + FALLBACK_IMAGE, (String) null);
                    Icon fallbackImage = null;
                    if (!Strings.isEmpty(fallbackImageStr)) {
                        fallbackImage = getIcon(fallbackImageStr, tenant);
                    }
                    Map<String, String> images = configurationService.getProperties(new PropertyFilter() {

                        @Override
                        public boolean accept(String name, String value) throws OXException {
                            return name.startsWith(PREFIX + tenant + IMAGE);
                        }
                    });
                    List<TrustedMail> trustedMailList = new ArrayList<>();
                    for (String mailAddress : mailAddresses) {
                        trustedMailList.add(getTrustedMail(mailAddress, images, fallbackImage, tenant));
                    }
                    trustedMailAddressesPerTenant.put(tenant, trustedMailList);
                }
            }
        }

        // Add single tenant / fall-back configuration
        String commaSeparatedListOfMailAddresses = configurationService.getProperty(PREFIX + CONFIG.substring(1), (String) null);
        if (Strings.isNotEmpty(commaSeparatedListOfMailAddresses)) {
            String[] mailAddresses = Strings.splitByCommaNotInQuotes(commaSeparatedListOfMailAddresses);
            String fallbackImageStr = configurationService.getProperty(PREFIX + FALLBACK_IMAGE.substring(1), (String) null);
            Icon fallbackImage = null;
            if (!Strings.isEmpty(fallbackImageStr)) {
                fallbackImage = getIcon(fallbackImageStr, (String) null);
            }

            Map<String, String> images = configurationService.getProperties(new PropertyFilter() {

                @Override
                public boolean accept(String name, String value) throws OXException {
                    return name.startsWith(PREFIX + IMAGE.substring(1));
                }
            });
            for (String mailAddress : mailAddresses) {
                fallbackTenant.add(getTrustedMail(mailAddress, images, fallbackImage, null));
            }
        }
    }

    /**
     *
     * @param mailAddress
     * @param images
     * @param tenant
     * @return
     * @throws OXException
     */
    private TrustedMail getTrustedMail(String mailAddress, Map<String, String> images, Icon fallbackImage, String tenant) throws OXException {
        if (mailAddress.indexOf(":") > 0) {
            String[] trustedMailConfig = Strings.splitByColon(mailAddress);
            if (trustedMailConfig.length != 2) {
                throw new OXException(new ConfigurationException("Unable to to parse trusted mail address config. Only one colon is allowed: " + mailAddress));
            }
            String image = null;
            if (tenant == null) {
                image = images.get(PREFIX + IMAGE.substring(1) + trustedMailConfig[1]);
            } else {
                image = images.get(PREFIX + tenant + IMAGE + trustedMailConfig[1]);
            }
            if (image != null) {
                return new TrustedMail(trustedMailConfig[0], getIcon(image, tenant));
            } else {
                return new TrustedMail(trustedMailConfig[0], fallbackImage);
            }
        }
        return new TrustedMail(mailAddress, fallbackImage);
    }

    private Icon getIcon(String image, String tenant) {
        Icon icon = null;
        if (!Strings.isEmpty(image)) {
            if (UrlValidator.getInstance().isValid(image)) {
                try {
                    icon = new ImageIcon(new URL(image));
                } catch (IOException e) {
                    if (tenant != null) {
                        LOG.error("Unable to resolve configured trusted mail address image for tenant {}: {}", tenant, image, e);
                    } else {
                        LOG.error("Unable to resolve configured trusted mail address fallback image: {}", image, e);
                    }
                }
            } else {
                File f = new File(image);
                if (f.exists() && !f.isDirectory()) {
                    try {
                        icon = new ImageIcon(f);
                    } catch (IOException e) {
                        if (tenant != null) {
                            LOG.error("Unable to resolve configured trusted mail address image for tenant {}: {}", tenant, image, e);
                        } else {
                            LOG.error("Unable to resolve configured trusted mail address fallback image: {}", image, e);
                        }
                    }
                } else {
                    if (tenant != null) {
                        LOG.error("Unable to resolve configured trusted mail address image for tenant {}: {}", tenant, image);
                    } else {
                        LOG.error("Unable to resolve configured trusted mail address fallback image: {}", image);
                    }
                }
            }
        }
        return icon;
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        trustedMailAddressesPerTenant.clear();
        fallbackTenant.clear();
        try {
            init(configService);
        } catch (OXException e) {
            LOG.error("Error during config reload: {}", e.getMessage(), e);
        }
    }

    @Override
    public Icon getIcon(Session session, String uri) throws OXException {
        String tenant = (String) session.getParameter(Session.PARAM_HOST_NAME);
        if (tenant == null) {
            LOG.warn("Missing host name session parameter. Falling back to fallback tenant.");
        } else {
            if (trustedMailAddressesPerTenant.containsKey(tenant)) {
                for (TrustedMail trustedMail : trustedMailAddressesPerTenant.get(tenant)) {
                    if (trustedMail.getImage() != null && trustedMail.getImage().getUID().equals(uri)) {
                        return trustedMail.getImage();
                    }
                }
            }
        }

        for (TrustedMail trustedMail : fallbackTenant) {
            if (trustedMail.getImage() != null && trustedMail.getImage().getUID().equals(uri)) {
                return trustedMail.getImage();
            }
        }

        throw MailAuthenticityExceptionCodes.INVALID_IMAGE_UID.create();
    }

    @Override
    public Interests getInterests() {
        return Reloadables.getInterestsForAll();
    }

    private String getMailAddress(MailMessage msg) {
        MailAuthenticityResult authenticationResult = msg.getAuthenticityResult();
        return authenticationResult == null ? null : authenticationResult.getAttribute(MailAuthenticityResultKey.TRUSTED_SENDER).toString();
    }

}
