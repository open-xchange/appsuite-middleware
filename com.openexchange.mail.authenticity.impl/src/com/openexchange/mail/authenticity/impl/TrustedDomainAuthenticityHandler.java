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

package com.openexchange.mail.authenticity.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.java.Strings;
import com.openexchange.mail.authenticity.DefaultMailAuthenticityResultKey;
import com.openexchange.mail.authenticity.MailAuthenticityResultKey;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
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
public class TrustedDomainAuthenticityHandler implements Reloadable {

    private static final Logger LOG = LoggerFactory.getLogger(TrustedDomainAuthenticityHandler.class);

    /*
     * Property keys
     */
    private static final String PREFIX = "com.openexchange.mail.authenticity.trustedDomains.";
    private static final String TENANT = "tenants";
    private static final String DOMAINS = ".domains";
    private static final String IMAGE = ".image";

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
        public String toString() {
            return getDisplayName();
        }
    };

    private final Map<String, List<TrustedDomain>> trustedDomainsPerTenant;
    private final List<TrustedDomain> fallbackTenant;

    /**
     * Initializes a new {@link TrustedDomainAuthenticityHandler}.
     */
    public TrustedDomainAuthenticityHandler(ConfigurationService configurationService) {
        super();
        trustedDomainsPerTenant = new ConcurrentHashMap<>();
        fallbackTenant = new CopyOnWriteArrayList<>();
        init(configurationService);
    }

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
                if (trustedDomain.getImage() != null) {
                    authenticityResult.addAttribute(new MailAuthenticityResultKey() {

                        @Override
                        public String getKey() {
                            return "image";
                        }
                    }, trustedDomain.getImage());
                }
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

    private void init(ConfigurationService configurationService) {
        String commaSeparatedListOfTenants = configurationService.getProperty(PREFIX + TENANT, "");
        String[] tenants = Strings.splitByCommaNotInQuotes(commaSeparatedListOfTenants);
        for (String tenant : tenants) {
            String commaSeparatedListOfDomains = configurationService.getProperty(PREFIX + tenant + DOMAINS, (String) null);
            if (Strings.isNotEmpty(commaSeparatedListOfDomains)) {
                String[] domains = Strings.splitByCommaNotInQuotes(commaSeparatedListOfDomains);
                String image = configurationService.getProperty(PREFIX + tenant + IMAGE, (String) null);
                List<TrustedDomain> domainList = new ArrayList<>();
                for (String domain : domains) {
                    domainList.add(new TrustedDomain(domain, image));
                }
                trustedDomainsPerTenant.put(tenant, domainList);
            }
        }

        // Add single tenant / fall-back configuration
        String commaSeparatedListOfDomains = configurationService.getProperty(PREFIX + DOMAINS.substring(1), (String) null);
        if (Strings.isNotEmpty(commaSeparatedListOfDomains)) {
            String[] domains = Strings.splitByCommaNotInQuotes(commaSeparatedListOfDomains);
            String image = configurationService.getProperty(PREFIX + IMAGE.substring(1), (String) null);
            for (String domain : domains) {
                fallbackTenant.add(new TrustedDomain(domain, image));
            }
        }
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        trustedDomainsPerTenant.clear();
        fallbackTenant.clear();
        init(configService);
    }

    @Override
    public Interests getInterests() {
        List<String> properties = new LinkedList<>();
        properties.add(PREFIX + TENANT);
        for (String tenant : trustedDomainsPerTenant.keySet()) {
            properties.add(PREFIX + tenant + DOMAINS);
            properties.add(PREFIX + tenant + IMAGE);
        }
        properties.add(PREFIX + DOMAINS.substring(1));
        properties.add(PREFIX + IMAGE.substring(1));
        return DefaultInterests.builder().propertiesOfInterest(properties.toArray(new String[properties.size()])).build();
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

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("TrustedDomainResult [mechanism=").append(getMechanism()).append(", domain=").append(getDomain()).append(", result=").append(getResult()).append("]");
            return builder.toString();
        }
    }

}
