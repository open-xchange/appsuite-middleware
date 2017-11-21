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

package com.openexchange.mail.authentication.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.java.Strings;
import com.openexchange.mail.authentication.TrustedDomain;
import com.openexchange.mail.authentication.TrustedMailDomainService;

/**
 * {@link TrustedMailDomainServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class TrustedMailDomainServiceImpl implements TrustedMailDomainService, Reloadable{

    Map<String, List<TrustedDomain>> trustedDomains = new ConcurrentHashMap<>();

    private static final String PREFIX = "com.openexchange.mail.authentication.trustedDomains.";
    private static final String TENANT = "tenants";
    private static final String DOMAINS = ".domains";
    private static final String IMAGE = ".image";


    /**
     * Initializes a new {@link TrustedMailDomainServiceImpl}.
     */
    public TrustedMailDomainServiceImpl(ConfigurationService configurationService) {
        super();
        init(configurationService);

    }

    @Override
    public TrustedDomain getTrustedDomain(String tenant, String host) {
        if(trustedDomains.containsKey(tenant)){
            List<TrustedDomain> domains = trustedDomains.get(tenant);
            for(TrustedDomain domain: domains){
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
                String image = configurationService.getProperty(PREFIX+tenant+DOMAINS, (String) null);
                List<TrustedDomain> domainList = new ArrayList<>();
                for(String domain: domains){
                    domainList.add(new TrustedDomain(domain, image));
                }
                trustedDomains.put(tenant, domainList);

            }
        }
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        init(configService);
    }

    @Override
    public Interests getInterests() {
        String [] properties = new String[1 + trustedDomains.size() * 2];
        properties[0] = PREFIX + TENANT;
        int x=1;
        for(String tenant: trustedDomains.keySet()){
            properties[x++] = PREFIX+tenant+DOMAINS;
            properties[x++] = PREFIX+tenant+IMAGE;
        }
        return DefaultInterests.builder().propertiesOfInterest(properties).build();
    }

}
