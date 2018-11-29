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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.authenticity.MailAuthenticityExceptionCodes;
import com.openexchange.mail.authenticity.MailAuthenticityResultKey;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.impl.trusted.Icon;
import com.openexchange.mail.authenticity.impl.trusted.TrustedMailService;
import com.openexchange.mail.authenticity.impl.trusted.internal.fetcher.TrustedMailIconFetcher;
import com.openexchange.mail.authenticity.impl.trusted.internal.fetcher.TrustedMailIconFileFetcher;
import com.openexchange.mail.authenticity.impl.trusted.internal.fetcher.TrustedMailIconURLFetcher;
import com.openexchange.mail.authenticity.mechanism.AuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanism;
import com.openexchange.mail.authenticity.mechanism.SimplePassFailResult;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.timer.TimerService;

/**
 * {@link TrustedMailServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class TrustedMailServiceImpl implements ForcedReloadable, TrustedMailService {

    private static final Logger LOG = LoggerFactory.getLogger(TrustedMailServiceImpl.class);

    private static final String TRUSTED_MAIL_NAME = "TrustedMail";

    static final MailAuthenticityMechanism TRUSTED_MAIL_MECHANISM = new MailAuthenticityMechanism() {

        @Override
        public Class<? extends AuthenticityMechanismResult> getResultType() {
            return SimplePassFailResult.class;
        }

        @Override
        public String getDisplayName() {
            return TRUSTED_MAIL_NAME;
        }

        @Override
        public String getTechnicalName() {
            return TRUSTED_MAIL_NAME;
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

    private enum Protocol {
        file,
        http,
        https;
    }

    private final Map<Protocol, TrustedMailIconFetcher> fetchers;

    /**
     * Initializes a new {@link TrustedMailServiceImpl}.
     */
    public TrustedMailServiceImpl(ServiceLookup services) {
        super();
        this.trustedMailAddressesPerTenant = new ConcurrentHashMap<>(4);
        this.fallbackTenant = new CopyOnWriteArrayList<>();

        fetchers = initialiseFetchers();

        ConfigurationService configurationService = services.getService(ConfigurationService.class);
        TimerService timerService = services.getService(TimerService.class);
        timerService.schedule(() -> init(configurationService), 0);
    }

    /**
     * Initialises the fetchers
     * 
     * @return An {@link ImmutableMap} with the fetchers
     */
    private ImmutableMap<Protocol, TrustedMailIconFetcher> initialiseFetchers() {
        ImmutableMap.Builder<Protocol, TrustedMailIconFetcher> builder = ImmutableMap.builder();
        TrustedMailIconURLFetcher fetcher = new TrustedMailIconURLFetcher();
        builder.put(Protocol.http, fetcher);
        builder.put(Protocol.https, fetcher);
        builder.put(Protocol.file, new TrustedMailIconFileFetcher());
        return builder.build();
    }

    /**
     * Initialises the service
     * 
     * @param configurationService The {@link ConfigurationService}
     */
    private boolean init(ConfigurationService configurationService) {
        String commaSeparatedListOfTenants = configurationService.getProperty("com.openexchange.mail.authenticity.trusted.tenants", "");
        if (Strings.isNotEmpty(commaSeparatedListOfTenants)) {
            String[] tenants = Strings.splitByCommaNotInQuotes(commaSeparatedListOfTenants);
            for (String tenant : tenants) {
                String commaSeparatedListOfMailAddresses = configurationService.getProperty("com.openexchange.mail.authenticity.trusted." + tenant + ".config");
                if (Strings.isNotEmpty(commaSeparatedListOfMailAddresses)) {
                    String[] mailAddresses = Strings.splitByCommaNotInQuotes(commaSeparatedListOfMailAddresses);
                    String fallbackImageStr = configurationService.getProperty("com.openexchange.mail.authenticity.trusted." + tenant + ".fallbackImage");
                    Icon fallbackImage = null;
                    if (Strings.isNotEmpty(fallbackImageStr)) {
                        fallbackImage = getIcon(fallbackImageStr, tenant);
                    }
                    List<TrustedMail> trustedMailList;

                    try {
                        Map<String, String> images = configurationService.getProperties((name, value) -> name.startsWith("com.openexchange.mail.authenticity.trusted." + tenant + ".image."));

                        trustedMailList = new ArrayList<>(mailAddresses.length);
                        for (String mailAddress : mailAddresses) {
                            TrustedMail trustedMail = getTrustedMail(mailAddress, images, fallbackImage, tenant);
                            if (trustedMail == null) {
                                throw MailAuthenticityExceptionCodes.INVALID_PROPERTY.create("com.openexchange.mail.authenticity.trusted." + tenant + ".config");
                            }
                            trustedMailList.add(trustedMail);
                        }
                        trustedMailAddressesPerTenant.put(tenant, trustedMailList);
                    } catch (OXException e) {
                        LOG.error("The configuration for bundle 'com.openexchange.mail.authenticity' is invalid. Please check your configuration: {}", e.getMessage(), e);
                        return false;
                    }
                }
            }
        }

        // Add single tenant / fall-back configuration
        String commaSeparatedListOfMailAddresses = configurationService.getProperty("com.openexchange.mail.authenticity.trusted.config");
        if (Strings.isNotEmpty(commaSeparatedListOfMailAddresses)) {
            String[] mailAddresses = Strings.splitByCommaNotInQuotes(commaSeparatedListOfMailAddresses);
            String fallbackImageStr = configurationService.getProperty("com.openexchange.mail.authenticity.trusted.fallbackImage");
            Icon fallbackImage = null;
            if (Strings.isNotEmpty(fallbackImageStr)) {
                fallbackImage = getIcon(fallbackImageStr, (String) null);
            }

            try {
                Map<String, String> images = configurationService.getProperties((name, value) -> name.startsWith("com.openexchange.mail.authenticity.trusted.image."));

                for (String mailAddress : mailAddresses) {
                    fallbackTenant.add(getTrustedMail(mailAddress, images, fallbackImage, null));
                }
            } catch (OXException e) {
                LOG.error("The configuration for bundle 'com.openexchange.mail.authenticity' is invalid. Please check your configuration: {}", e.getMessage(), e);
                return false;
            }
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mail.authenticity.impl.trusted.TrustedMailService#handle(com.openexchange.session.Session, com.openexchange.mail.dataobjects.MailMessage)
     */
    @Override
    public void handle(Session session, MailMessage mailMessage) {
        if (null == mailMessage) {
            return;
        }

        String tenant = (String) session.getParameter(Session.PARAM_HOST_NAME);
        if (tenant == null) {
            LOG.warn("Missing host name session parameter. Unable to verify mail.");
            return;
        }

        MailAuthenticityResult authenticityResult = mailMessage.getAuthenticityResult();

        if (authenticityResult == null) {
            LOG.warn("Unable to verify trusted domain without authentication result.");
            return;
        }

        if (false == MailAuthenticityStatus.PASS.equals(authenticityResult.getStatus())) {
            return;
        }

        String mailAddress = getMailAddress(mailMessage);
        if (Strings.isEmpty(mailAddress)) {
            return;
        }
        TrustedMail trustedDomain = checkMail(tenant, mailAddress);
        if (trustedDomain == null) {
            return;
        }
        authenticityResult.setStatus(MailAuthenticityStatus.TRUSTED);
        if (trustedDomain.getImage() == null) {
            return;

        }
        authenticityResult.addAttribute(MailAuthenticityResultKey.IMAGE, trustedDomain.getImage().getUID());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.config.Reloadable#reloadConfiguration(com.openexchange.config.ConfigurationService)
     */
    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        trustedMailAddressesPerTenant.clear();
        fallbackTenant.clear();
        if (!init(configService)) {
            LOG.error("Error(s) during config reload!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mail.authenticity.impl.trusted.TrustedMailService#getIcon(com.openexchange.session.Session, java.lang.String)
     */
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

    /////////////////////////////// HELPERS ///////////////////////////////

    /**
     * @param tenant the tenant
     * @param mailAddress The mail address
     * @return
     */
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

    /**
     * Gets the {@link TrustedMail}
     * 
     * @param mailAddress A mail address or a mail address configuration
     * @param images The map of configured images
     * @param fallbackImage The fall-back image
     * @param tenant An optional tenant
     * @return the {@link TrustedMail} or null in case the mail address configuration is invalid
     */
    private TrustedMail getTrustedMail(String mailAddress, Map<String, String> images, Icon fallbackImage, String tenant) {
        if (mailAddress.indexOf(':') <= 0) {
            return new TrustedMail(mailAddress, fallbackImage);
        }

        String[] trustedMailConfig = Strings.splitByColon(mailAddress);
        if (trustedMailConfig.length != 2) {
            return null;
        }

        String imageKey = (tenant == null ? "com.openexchange.mail.authenticity.trusted.image." : "com.openexchange.mail.authenticity.trusted." + tenant + ".image.") + trustedMailConfig[1];
        String image = images.get(imageKey);
        return new TrustedMail(trustedMailConfig[0], null == image ? fallbackImage : getIcon(image, tenant));
    }

    /**
     * Gets the {@link Icon} from the specified resource URL for the specified tenant
     * 
     * @param resourceUrl The resource URL that points to the image
     * @param tenant The tenant
     * @return The {@link Icon} or <code>null</code> if the specified resource URL points to
     *         a non existing resource, or if the resource is <code>null</code> or empty.
     */
    private Icon getIcon(String resourceUrl, String tenant) {
        if (Strings.isEmpty(resourceUrl)) {
            return null;
        }

        TrustedMailIconFetcher iconFetcher = getIconFetcher(resourceUrl);
        if (iconFetcher == null) {
            LOG.error("No appropriate icon fetcher found for '{}'", resourceUrl);
            return null;
        }
        if (!iconFetcher.exists(resourceUrl)) {
            logErrorWithTenant(tenant, resourceUrl);
            return null;
        }

        byte[] fetch = iconFetcher.fetch(resourceUrl);
        if (fetch == null) {
            logErrorWithTenant(tenant, resourceUrl);
            return null;
        }
        return new ImageIcon(fetch);
    }

    /**
     * Returns the appropriate {@link TrustedMailIconFetcher} for the specified resource URL
     * 
     * @param resourceUrl The resource URL
     * @return the appropriate {@link TrustedMailIconFetcher} for the specified resource URL
     *         or <code>null</code> if none found
     */
    private TrustedMailIconFetcher getIconFetcher(String resourceUrl) {
        try {
            URL u = new URL(resourceUrl);
            Protocol p = Protocol.valueOf(u.getProtocol().toLowerCase());
            return fetchers.get(p);
        } catch (MalformedURLException e) {
            LOG.error("A malformed URL was detected for resource '{}'", resourceUrl, e);
            return null;
        } catch (IllegalArgumentException e) {
            LOG.error("The protocol defined in the resource url '{}' is not supported.", resourceUrl);
            return null;
        }
    }

    /**
     * Logs an error for the specified resource URL and tenant
     * 
     * @param tenant The tenant
     * @param resourceURL The resource URL
     */
    private void logErrorWithTenant(String tenant, String resourceURL) {
        if (Strings.isNotEmpty(tenant)) {
            LOG.error("Unable to resolve configured trusted mail address image for tenant {}: {}", tenant, resourceURL);
        } else {
            LOG.error("Unable to resolve configured trusted mail address fallback image: {}", resourceURL);
        }
    }

    /**
     * Returns the value of the {@link MailAuthenticityResultKey#TRUSTED_SENDER} attribute from the specified
     * {@link MailMessage}
     * 
     * @param msg The {@link MailMessage}
     * @return the value of the {@link MailAuthenticityResultKey#TRUSTED_SENDER} attribute or <code>null</code>
     *         if the key is either absent or is assigned a <code>null</code> value.
     */
    private String getMailAddress(MailMessage msg) {
        MailAuthenticityResult authenticationResult = msg.getAuthenticityResult();
        return authenticationResult == null ? null : authenticationResult.getAttribute(MailAuthenticityResultKey.TRUSTED_SENDER).toString();
    }

}
