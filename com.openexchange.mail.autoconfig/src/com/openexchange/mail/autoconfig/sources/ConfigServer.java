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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.StringAllocator;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.xmlparser.AutoconfigParser;
import com.openexchange.mail.autoconfig.xmlparser.ClientConfig;

/**
 * {@link ConfigServer}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ConfigServer extends AbstractConfigSource {

    static final org.apache.commons.logging.Log LOG = com.openexchange.log.LogFactory.getLog(ConfigServer.class);

    @Override
    public Autoconfig getAutoconfig(final String emailLocalPart, final String emailDomain, final String password, final User user, final Context context) throws OXException {
        // New HTTP client
        final HttpClient client = new HttpClient();
        final int timeout = 3000;
        client.getParams().setSoTimeout(timeout);
        client.getParams().setIntParameter("http.connection.timeout", timeout);
        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
        client.getParams().setParameter("http.protocol.single-cookie-header", Boolean.TRUE);
        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

        // GET method
        String uri = new StringAllocator("http://autoconfig.").append(emailDomain).append("/mail/config-v1.1.xml").toString();
        final GetMethod getMethod = new GetMethod(uri);
        try {
            // Name-value-pairs
            final List<NameValuePair> pairs = new ArrayList<NameValuePair>(4);
            {
                final NameValuePair pair = new NameValuePair("emailaddress", new StringAllocator(emailLocalPart).append('@').append(emailDomain).toString());
                pairs.add(pair);
            }
            getMethod.setQueryString(pairs.toArray(new NameValuePair[0]));

            // Execute GET request
            int statusCode = client.executeMethod(getMethod);
            if (statusCode != 200) {
                LOG.info("Could not retrieve config XML from autoconfig server. Return code was: " + statusCode);
                // Try 2nd URL
                uri = new StringAllocator(64).append("http://").append(emailDomain).append("/.well-known/autoconfig/mail/config-v1.1.xml").toString();
                getMethod.setURI(new URI(uri, false));
                statusCode = client.executeMethod(getMethod);
                if (statusCode != 200) {
                    LOG.info("Could not retrieve config XML from main domain. Return code was: " + statusCode);
                    return null;
                }
            }

            ClientConfig clientConfig = new AutoconfigParser().getConfig(getMethod.getResponseBodyAsStream());

            Autoconfig autoconfig = getBestConfiguration(clientConfig, emailDomain);
            replaceUsername(autoconfig, emailLocalPart, emailDomain);
            return autoconfig;

        } catch (OXException e) {
            if (3 != e.getCode() || !"MAIL-AUTOCONFIG".equals(e.getPrefix())) {
                throw e;
            }
            // No valid XML received...
            LOG.info("No valid XML received from URI: " + uri, e.getCause());
        } catch (HttpException e) {
            LOG.warn("Could not retrieve config XML.", e);
        } catch (final java.net.UnknownHostException e) {
            // Obviously that host does not exist
            LOG.debug("Could not retrieve config XML, because of an unknown host for URL: " + uri, e);
        } catch (final IOException e) {
            LOG.warn("Could not retrieve config XML.", e);
        }
        return null;
    }
}

