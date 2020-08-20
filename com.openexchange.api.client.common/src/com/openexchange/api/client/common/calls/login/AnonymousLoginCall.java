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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.api.client.common.calls.login;

import static com.openexchange.api.client.common.ApiClientConstants.ACTION;
import static com.openexchange.api.client.common.ApiClientConstants.ANONYMOUS;
import static com.openexchange.api.client.common.ApiClientConstants.CLIENT;
import static com.openexchange.api.client.common.ApiClientConstants.CLIENT_VALUE;
import static com.openexchange.api.client.common.ApiClientConstants.NAME;
import static com.openexchange.api.client.common.ApiClientConstants.PASSWORD;
import static com.openexchange.api.client.common.ApiClientConstants.RAMP_UP;
import static com.openexchange.api.client.common.ApiClientConstants.SHARE;
import static com.openexchange.api.client.common.ApiClientConstants.STAY_SIGNED_IN;
import static com.openexchange.api.client.common.ApiClientConstants.TARGET;
import java.util.Map;
import java.util.Objects;
import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.Credentials;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.version.VersionService;

/**
 * {@link AnonymousLoginCall}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class AnonymousLoginCall extends AbstractLoginCall {

    private final ServiceLookup services;

    private final String share;
    private final String target;

    /**
     * Initializes a new {@link AnonymousLoginCall}.
     * 
     * @param services The service lookup to get the {@link VersionService} from
     * @param credentials The credentials
     * @param share The token of the share to access
     * @param target The path to a specific share target
     * @throws OXException In case credentials are missing
     */
    public AnonymousLoginCall(ServiceLookup services, Credentials credentials, String share, String target) throws OXException {
        super(credentials);
        this.services = services;
        this.share = Objects.requireNonNull(share);
        this.target = Objects.requireNonNull(target);

        if (null == credentials.getPassword()) {
            throw ApiClientExceptions.MISSING_CREDENTIALS.create();
        }
    }

    @Override
    protected String getAction() {
        return ANONYMOUS;
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put(SHARE, share);
        parameters.put(TARGET, target);
    }

    @Override
    @Nullable
    public HttpEntity getBody() throws OXException, JSONException {
        /*
         * Build body
         */
        JSONObject json = new JSONObject();
        json.put(ACTION, ANONYMOUS);
        json.put(NAME, null == credentials.getLogin() ? "" : credentials.getLogin());
        json.put(PASSWORD, credentials.getPassword());
        json.put(CLIENT, CLIENT_VALUE);
        json.put("locale", "en_US");
        json.put("timeout", 10000);
        json.put(RAMP_UP, false);
        json.put(SHARE, share);
        json.put(TARGET, target);
        json.put(STAY_SIGNED_IN, true);
        addVersion(json);

        return toHttpEntity(json);
    }

    private void addVersion(JSONObject json) throws JSONException {
        VersionService versionService = services.getOptionalService(VersionService.class);
        if (null != versionService) {
            String version = versionService.getVersionString();
            version = version.replace("Rev", "");
            json.put("version", version);
        }
    }

}
