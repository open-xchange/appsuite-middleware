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

package com.openexchange.mail.authenticity.impl.handler.domain.internal;

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
import com.openexchange.mail.authenticity.DefaultMailAuthenticityResultKey;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.TrustedDomainResultKey;
import com.openexchange.mail.authenticity.impl.handler.domain.Icon;
import com.openexchange.mail.authenticity.impl.handler.domain.TrustedDomainService;
import com.openexchange.mail.authenticity.mechanism.AbstractAuthMechResult;
import com.openexchange.mail.authenticity.mechanism.AuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanism;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.SimplePassFailResult;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.session.Session;

/**
 * {@link TrustedDomainAuthenticityHandler}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class TrustedDomainAuthenticityHandler implements ForcedReloadable, TrustedDomainService {

    private static final Logger LOG = LoggerFactory.getLogger(TrustedDomainAuthenticityHandler.class);

    /*
     * Property keys
     */
    private static final String PREFIX = "com.openexchange.mail.authenticity.trustedDomains.";
    private static final String TENANT = "tenants";
    private static final String DOMAINS = ".domains";
    private static final String IMAGE = ".image.";
    private static final String FALLBACK_IMAGE = ".fallbackImage";

    static final MailAuthenticityMechanism TRUSTED_DOMAIN_MECHANISM = new MailAuthenticityMechanism() {

        @Override
        public Class<? extends AuthenticityMechanismResult> getResultType() {
            return SimplePassFailResult.class;
        }

        @Override
        public String getDisplayName() {
            return "TrustedDomain";
        }

        @Override
        public String getTechnicalName() {
            return "TrustedDomain";
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
    };

    private final Map<String, List<TrustedDomain>> trustedDomainsPerTenant;
    private final List<TrustedDomain> fallbackTenant;

    /**
     * Initializes a new {@link TrustedDomainAuthenticityHandler}.
     *
     * @throws OXException
     */
    public TrustedDomainAuthenticityHandler(ConfigurationService configurationService) throws OXException {
        super();
        this.trustedDomainsPerTenant = new ConcurrentHashMap<>();
        this.fallbackTenant = new CopyOnWriteArrayList<>();
        init(configurationService);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(Session session, MailMessage mailMessage) {
        String tenant = (String) session.getParameter(Session.PARAM_HOST_NAME);
        if (tenant == null) {
            LOG.warn("Missing host name session parameter. Unable to verify mail domain.");
            return;
        }

        MailAuthenticityResult authenticityResult = mailMessage.getAuthenticityResult();

        if (MailAuthenticityStatus.PASS.equals(authenticityResult.getStatus())) {
            String domain = getDomain(mailMessage);
            TrustedDomain trustedDomain = checkHost(tenant, domain);
            if (trustedDomain != null) {
                List<MailAuthenticityMechanismResult> results = authenticityResult.getAttribute(DefaultMailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class);
                results.add(new TrustedDomainResult(domain, null, SimplePassFailResult.PASS));
                authenticityResult.addAttribute(TrustedDomainResultKey.TRUSTED_DOMAIN, true);
            }
        }
    }

    private TrustedDomain checkHost(String tenant, String host) {
        if (trustedDomainsPerTenant.containsKey(tenant)) {
            List<TrustedDomain> domains = trustedDomainsPerTenant.get(tenant);
            for (TrustedDomain domain : domains) {
                if (domain.matches(host)) {
                    return domain;
                }
            }
        }

        if (fallbackTenant != null) {
            for (TrustedDomain domain : fallbackTenant) {
                if (domain.matches(host)) {
                    return domain;
                }
            }
        }
        return null;
    }

    private void init(ConfigurationService configurationService) throws OXException {
        String commaSeparatedListOfTenants = configurationService.getProperty(PREFIX + TENANT, "");
        String[] tenants = Strings.splitByCommaNotInQuotes(commaSeparatedListOfTenants);
        for (String tenant : tenants) {
            String commaSeparatedListOfDomains = configurationService.getProperty(PREFIX + tenant + DOMAINS, (String) null);
            if (Strings.isNotEmpty(commaSeparatedListOfDomains)) {
                String[] domains = Strings.splitByCommaNotInQuotes(commaSeparatedListOfDomains);
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
                List<TrustedDomain> domainList = new ArrayList<>();
                for (String domain : domains) {
                    domainList.add(getTrustedDomain(domain, images, fallbackImage, tenant));
                }
                trustedDomainsPerTenant.put(tenant, domainList);
            }
        }

        // Add single tenant / fall-back configuration
        String commaSeparatedListOfDomains = configurationService.getProperty(PREFIX + DOMAINS.substring(1), (String) null);
        if (Strings.isNotEmpty(commaSeparatedListOfDomains)) {
            String[] domains = Strings.splitByCommaNotInQuotes(commaSeparatedListOfDomains);
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
            for (String domain : domains) {
                fallbackTenant.add(getTrustedDomain(domain, images, fallbackImage, null));
            }
        }
    }

    /**
     *
     * @param domain
     * @param images
     * @param tenant
     * @return
     * @throws OXException
     */
    private TrustedDomain getTrustedDomain(String domain, Map<String, String> images, Icon fallbackImage, String tenant) throws OXException {
        if (domain.indexOf(":") > 0) {
            String[] domainConfig = Strings.splitByColon(domain);
            if (domainConfig.length != 2) {
                throw new OXException(new ConfigurationException("Unable to to parse trusted domain config. Only one colon is allowed: " + domain));
            }
            String image = null;
            if (tenant == null) {
                image = images.get(PREFIX + IMAGE.substring(1) + domainConfig[1]);
            } else {
                image = images.get(PREFIX + tenant + IMAGE + domainConfig[1]);
            }
            if (image != null) {
                return new TrustedDomain(domainConfig[0], getIcon(image, tenant));
            } else {
                return new TrustedDomain(domainConfig[0], fallbackImage);
            }
        }
        return new TrustedDomain(domain, fallbackImage);
    }

    private Icon getIcon(String image, String tenant) {
        Icon icon = null;
        if (!Strings.isEmpty(image)) {
            if (UrlValidator.getInstance().isValid(image)) {
                try {
                    icon = new ImageIcon(new URL(image));
                } catch (IOException e) {
                    if (tenant != null) {
                        LOG.error("Unable to resolve configured trusted domain image for tenant {}: {}", tenant, image, e);
                    } else {
                        LOG.error("Unable to resolve configured trusted domain fallback image: {}", image, e);
                    }
                }
            } else {
                File f = new File(image);
                if (f.exists() && !f.isDirectory()) {
                    try {
                        icon = new ImageIcon(f);
                    } catch (IOException e) {
                        if (tenant != null) {
                            LOG.error("Unable to resolve configured trusted domain image for tenant {}: {}", tenant, image, e);
                        } else {
                            LOG.error("Unable to resolve configured trusted domain fallback image: {}", image, e);
                        }
                    }
                } else {
                    if (tenant != null) {
                        LOG.error("Unable to resolve configured trusted domain image for tenant {}: {}", tenant, image);
                    } else {
                        LOG.error("Unable to resolve configured trusted domain fallback image: {}", image);
                    }
                }
            }
        }
        return icon;
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        trustedDomainsPerTenant.clear();
        fallbackTenant.clear();
        try {
            init(configService);
        } catch (OXException e) {
            LOG.error("Error during config reload: {}", e.getMessage(), e);
        }
    }

    @Override
    public Icon getIcon(String domain, Session session) {
        String tenant = (String) session.getParameter(Session.PARAM_HOST_NAME);
        if (tenant == null) {
            LOG.warn("Missing host name session parameter. Unable to verify mail domain.");
            return null;
        }
        List<TrustedDomain> list = trustedDomainsPerTenant.get(tenant);
        for (TrustedDomain dom : list) {
            if (dom.matches(domain)) {
                return dom.getImage();
            }
        }

        for (TrustedDomain dom : fallbackTenant) {
            if (dom.matches(domain)) {
                return dom.getImage();
            }
        }
        return null;
    }

    @Override
    public Interests getInterests() {
        return Reloadables.getInterestsForAll();
    }

    private String getDomain(MailMessage msg) {
        MailAuthenticityResult authenticationResult = msg.getAuthenticityResult();
        return authenticationResult == null ? null : authenticationResult.getAttribute(DefaultMailAuthenticityResultKey.FROM_DOMAIN).toString();
    }

    private static class TrustedDomainResult extends AbstractAuthMechResult {

        /**
         * Initializes a new {@link TrustedDomainResult}.
         *
         * @param domain
         * @param clientIP
         * @param result
         */
        public TrustedDomainResult(String domain, String clientIP, AuthenticityMechanismResult result) {
            super(domain, clientIP, result);
        }

        @Override
        public MailAuthenticityMechanism getMechanism() {
            return TRUSTED_DOMAIN_MECHANISM;
        }
    }

}
