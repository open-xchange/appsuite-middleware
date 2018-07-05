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

package com.openexchange.mail.authenticity.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailField;
import com.openexchange.mail.authenticity.MailAuthenticationHandler;
import com.openexchange.mail.dataobjects.MailAuthenticationResult;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.session.Session;

/**
 * {@link TrustedDomainAuthenticationHandler}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class TrustedDomainAuthenticationHandler implements Reloadable, MailAuthenticationHandler {

    Map<String, List<TrustedDomain>> trustedDomains = new ConcurrentHashMap<>();

    private static final Logger LOG = LoggerFactory.getLogger(TrustedDomainAuthenticationHandler.class);

    private static final String PREFIX = "com.openexchange.mail.authentication.trustedDomains.";
    private static final String TENANT = "tenants";
    private static final String DOMAINS = ".domains";
    private static final String IMAGE = ".image";
    private static final List<TrustedDomain> FALLBACK_TENANT = new ArrayList<>();


    /**
     * Initializes a new {@link TrustedDomainAuthenticationHandler}.
     */
    public TrustedDomainAuthenticationHandler(ConfigurationService configurationService) {
        super();
        init(configurationService);

    }

    private TrustedDomain checkHost(String tenant, String host) {
        if(trustedDomains.containsKey(tenant)){
            List<TrustedDomain> domains = trustedDomains.get(tenant);
            for(TrustedDomain domain: domains){
                if(domain.matches(host)){
                    return domain;
                }
            }
        }

        if(FALLBACK_TENANT != null){
            for(TrustedDomain domain: FALLBACK_TENANT){
                if(domain.matches(host)){
                    return domain;
                }
            }
        }
        return null;
    }

    private void init(ConfigurationService configurationService){
        trustedDomains.clear();
        String commaSeparatedListOfTenants = configurationService.getProperty(PREFIX+TENANT, "");
        String[] tenants = Strings.splitByCommaNotInQuotes(commaSeparatedListOfTenants);
        for(String tenant: tenants){
            String commaSeparatedListOfDomains = configurationService.getProperty(PREFIX+tenant+DOMAINS, (String) null);
            if(Strings.isNotEmpty(commaSeparatedListOfDomains)){
                String[] domains = Strings.splitByCommaNotInQuotes(commaSeparatedListOfDomains);
                String image = configurationService.getProperty(PREFIX+tenant+IMAGE, (String) null);
                List<TrustedDomain> domainList = new ArrayList<>();
                for(String domain: domains){
                    domainList.add(new TrustedDomain(domain, image));
                }
                trustedDomains.put(tenant, domainList);
            }
        }

        // Add single tenant / fallback configuration
        String commaSeparatedListOfDomains = configurationService.getProperty(PREFIX + DOMAINS.substring(1), (String) null);
        if (Strings.isNotEmpty(commaSeparatedListOfDomains)) {
            String[] domains = Strings.splitByCommaNotInQuotes(commaSeparatedListOfDomains);
            String image = configurationService.getProperty(PREFIX + IMAGE.substring(1), (String) null);
            for (String domain : domains) {
                FALLBACK_TENANT.add(new TrustedDomain(domain, image));
            }
        }

    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        init(configService);
    }

    @Override
    public Interests getInterests() {
        ArrayList<String> properties = new ArrayList<>(trustedDomains.size());
        properties.add(PREFIX + TENANT);
        for(String tenant: trustedDomains.keySet()){
            properties.add(PREFIX+tenant+DOMAINS);
            properties.add(PREFIX+tenant+IMAGE);
        }
        properties.add(PREFIX+DOMAINS.substring(1));
        properties.add(PREFIX+IMAGE.substring(1));
        return DefaultInterests.builder().propertiesOfInterest(properties.toArray(new String[properties.size()])).build();
    }

    @Override
    public void handle(Session session, MailMessage mailMessage) {
        String tenant = (String) session.getParameter(Session.PARAM_HOST_NAME);
        if(tenant==null){
            LOG.warn("Missing host name session parameter. Unable to verify mail domain.");
            return;
        }
        TrustedDomain trustedDomain = checkHost(tenant, getDomain(mailMessage));
        if(trustedDomain != null){
            // TODO insert infos
            mailMessage.getAuthenticationResult();
        }
    }

    private String getDomain(MailMessage msg){
        MailAuthenticationResult authenticationResult = msg.getAuthenticationResult();
        return authenticationResult == null ? null : authenticationResult.getDomain();
    }

    @Override
    public Collection<MailField> getRequiredFields() {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getRequiredHeaders() {
        return Collections.emptyList();
    }

    @Override
    public boolean isEnabled(Session session) {
        return true;
    }

    @Override
    public int getRanking() {
        return 100;
    }

}
