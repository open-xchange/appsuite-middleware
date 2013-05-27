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

package com.openexchange.oauth.xing;

import java.net.MalformedURLException;
import java.net.URL;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.XingApi;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.StringAllocator;
import com.openexchange.java.Strings;
import com.openexchange.oauth.API;
import com.openexchange.oauth.AbstractOAuthServiceMetaData;
import com.openexchange.session.Session;

/**
 * {@link XingOAuthServiceMetaData}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class XingOAuthServiceMetaData extends AbstractOAuthServiceMetaData implements com.openexchange.oauth.ScribeAware {

    private final String domain;

    /**
     * Initializes a new {@link XingOAuthServiceMetaData}.
     *
     * @param configService The configuration service
     * @throws IllegalStateException If either API key or secret is missing
     */
    public XingOAuthServiceMetaData(final ConfigurationService configService) {
        super();
        id = "com.openexchange.oauth.xing";
        displayName = "XING";

        final String apiKey = configService.getProperty("com.openexchange.oauth.xing.apiKey");
        if (Strings.isEmpty(apiKey)) {
            throw new IllegalStateException("Missing following property in configuration: com.openexchange.oauth.xing.apiKey");
        }
        this.apiKey = apiKey;

        final String apiSecret = configService.getProperty("com.openexchange.oauth.xing.apiSecret");
        if (Strings.isEmpty(apiSecret)) {
            throw new IllegalStateException("Missing following property in configuration: com.openexchange.oauth.xing.apiSecret");
        }
        this.apiSecret = apiSecret;

        final String domain = configService.getProperty("com.openexchange.oauth.xing.domain");
        if (Strings.isEmpty(domain)) {
            this.domain = null;
        } else {
            this.domain = domain;
        }
    }

    @Override
    public String modifyCallbackURL(final String callbackUrl, final Session session) {
        if (null == callbackUrl || null == domain) {
            return super.modifyCallbackURL(callbackUrl, session);
        }
        try {
            final URL url = new URL(callbackUrl);
            final String host = url.getHost();
            if (domain.equals(host)) {
                return callbackUrl;
            }
            final StringAllocator sb = new StringAllocator(callbackUrl.length());
            final String protocol = toLowerCase(url.getProtocol());
            sb.append(protocol).append("://");
            sb.append(domain);
            final int port = url.getPort();
            if (port >= 0) {
                if ("http".equals(protocol)) {
                    if (port != 80) {
                        sb.append(':').append(url.getPort());
                    }
                } else if ("https".equals(protocol)) {
                    if (port != 443) {
                        sb.append(':').append(url.getPort());
                    }
                } else {
                    sb.append(':').append(port);
                }
            }
            {
                final String path = url.getPath();
                if (!Strings.isEmpty(path)) {
                    sb.append(path);
                }
            }
            {
                final String query = url.getQuery();
                if (!Strings.isEmpty(query)) {
                    sb.append('?').append(query);
                }
            }
            {
                final String ref = url.getRef();
                if (!Strings.isEmpty(ref)) {
                    sb.append('#').append(ref);
                }
            }
            return sb.toString();
        } catch (final MalformedURLException e) {
            return callbackUrl;
        }
    }

    @Override
    public API getAPI() {
        return API.XING;
    }

    @Override
    public Class<? extends Api> getScribeService() {
        return XingApi.class;
    }

    /** ASCII-wise to lower-case */
    private String toLowerCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringAllocator builder = new StringAllocator(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

}
