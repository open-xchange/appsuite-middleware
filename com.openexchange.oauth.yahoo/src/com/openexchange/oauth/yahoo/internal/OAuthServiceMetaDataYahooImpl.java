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

package com.openexchange.oauth.yahoo.internal;

import java.util.HashMap;
import java.util.Map;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.YahooApi;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.http.deferrer.DeferringURLService;
import com.openexchange.oauth.API;
import com.openexchange.oauth.AbstractOAuthServiceMetaData;

/**
 * {@link OAuthServiceMetaDataYahooImpl}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OAuthServiceMetaDataYahooImpl extends AbstractOAuthServiceMetaData implements com.openexchange.oauth.ScribeAware, Reloadable {

    private static final String API_KEY = "com.openexchange.oauth.yahoo.apiKey";

    private static final String API_SECRET = "com.openexchange.oauth.yahoo.apiSecret";

    private static final String[] PROPERTIES = new String[] {API_KEY, API_SECRET};

    private final DeferringURLService deferrer;

    public OAuthServiceMetaDataYahooImpl(DeferringURLService deferrer) {
        super();
        setId("com.openexchange.oauth.yahoo");
        setAPIKeyName(API_KEY);
        setAPISecretName(API_SECRET);
        setDisplayName("Yahoo");
        this.deferrer = deferrer;
    }

    @Override
    public boolean registerTokenBasedDeferrer() {
        return true;
    }

    @Override
    public boolean needsRequestToken() {
        return true;
    }

    @Override
    public API getAPI() {
        return API.YAHOO;
    }

    @Override
    public Class<? extends Api> getScribeService() {
        return YahooApi.class;
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        String apiKey = configService.getProperty(apiKeyName);
        String secretKey = configService.getProperty(apiSecretName);

        if (apiKey.isEmpty()) {
            throw new IllegalStateException("Missing following property in configuration: " + apiKeyName);
        }
        if (secretKey.isEmpty()) {
            throw new IllegalStateException("Missing following property in configuration: " + apiSecretName);
        }

        this.apiKey = apiKey;
        this.apiSecret = secretKey;
    }

    @Override
    public Map<String, String[]> getConfigFileNames() {
        Map<String, String[]> map = new HashMap<String, String[]>(1);
        map.put("yahoooauth.properties", PROPERTIES);
        return map;
    }
}
