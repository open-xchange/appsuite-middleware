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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mail.autoconfig.sources;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.IndividualAutoconfig;
import com.openexchange.mail.autoconfig.tools.TrustAllAdapter;
import com.openexchange.mail.autoconfig.xmlparser.AutoconfigParser;
import com.openexchange.mail.autoconfig.xmlparser.ClientConfig;
import com.openexchange.server.ServiceLookup;

/**
 * Connects to the Mozilla ISPDB. For more information see <a
 * href="https://developer.mozilla.org/en/Thunderbird/Autoconfiguration">https://developer.mozilla.org/en/Thunderbird/Autoconfiguration</a>
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added google-common cache
 */
public class ISPDB extends AbstractConfigSource {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ISPDB.class);

    private static final String locationProperty = "com.openexchange.mail.autoconfig.ispdb";

    // -------------------------------------------------------------------------------------------------- //

    private final ServiceLookup services;
    private final Cache<String, Autoconfig> autoConfigCache;

    public ISPDB(ServiceLookup services) {
        super();
        this.services = services;
        autoConfigCache = CacheBuilder.newBuilder().initialCapacity(16).expireAfterAccess(1, TimeUnit.DAYS).maximumSize(500).build();
    }

    @Override
    public Autoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, User user, Context context) throws OXException {
        ConfigViewFactory configViewFactory = services.getService(ConfigViewFactory.class);
        ConfigView view = configViewFactory.getView(user.getId(), context.getContextId());

        String sUrl = view.get(locationProperty, String.class);
        if (sUrl == null) {
            return null;
        }
        if (!sUrl.endsWith("/")) {
            sUrl += "/";
        }
        sUrl += emailDomain;

        // Check cache
        {
            Autoconfig autoconfig = autoConfigCache.getIfPresent(sUrl);
            if (null != autoconfig) {
                return generateIndividualAutoconfig(emailLocalPart, emailDomain, autoconfig);
            }
        }

        URL url;
        try {
            url = new URL(sUrl);
        } catch (MalformedURLException e) {
            LOG.warn("Unable to parse URL: {}", sUrl, e);
            return null;
        }

        HttpClient client = new HttpClient();
        int timeout = 3000;
        client.getParams().setSoTimeout(timeout);
        client.getParams().setIntParameter("http.connection.timeout", timeout);
        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
        client.getParams().setParameter("http.protocol.single-cookie-header", Boolean.TRUE);
        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

        GetMethod getMethod;

        if (url.getProtocol().equalsIgnoreCase("https")) {
            int port = url.getPort();
            if (port == -1) {
                port = 443;
            }

            Protocol https = new Protocol("https", new TrustAllAdapter(), 443);
            client.getHostConfiguration().setHost(url.getHost(), port, https);

            getMethod = new GetMethod(url.getFile());

            getMethod.getParams().setSoTimeout(timeout);
            getMethod.setQueryString(url.getQuery());
        } else {
            getMethod = new GetMethod(sUrl);
        }

        try {

            int httpCode = client.executeMethod(getMethod);

            if (httpCode != 200) {
                LOG.info("Could not retrieve config XML. Return code was: {}", httpCode);
                return null;
            }

            ClientConfig clientConfig = new AutoconfigParser().getConfig(getMethod.getResponseBodyAsStream());

            Autoconfig autoconfig = getBestConfiguration(clientConfig, emailDomain);

            autoConfigCache.put(sUrl, autoconfig);

            return generateIndividualAutoconfig(emailLocalPart, emailDomain, autoconfig);
        } catch (HttpException e) {
            LOG.warn("Could not retrieve config XML.", e);
            return null;
        } catch (IOException e) {
            LOG.warn("Could not retrieve config XML.", e);
            return null;
        }
    }

    private Autoconfig generateIndividualAutoconfig(String emailLocalPart, String emailDomain, Autoconfig autoconfig) {
        IndividualAutoconfig retval = new IndividualAutoconfig(autoconfig);
        retval.setUsername(autoconfig.getUsername());
        replaceUsername(retval, emailLocalPart, emailDomain);
        return retval;
    }

}
