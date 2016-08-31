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

package com.openexchange.ajax.advertisement.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.tools.encoding.Base64;

/**
 * {@link SetConfigRequest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class SetConfigRequest implements AJAXRequest<SetConfigResponse> {

    private String userId;
    private String userName;
    private String contextId;
    private String reseller;
    private String pack;
    private String config;
    private String login;
    private String password;

    /**
     * Initializes a new {@link SetConfigRequest}.
     * 
     * @param userId
     * @param contextId
     * @param reseller
     * @param pack
     * @param config
     */
    private SetConfigRequest(String userName, String userId, String contextId, String reseller, String pack, String config, String login, String password) {
        super();
        this.userName = userName;
        this.userId = userId;
        this.contextId = contextId;
        this.reseller = reseller;
        this.pack = pack;
        this.config = config;
        this.login=login;
        this.password=password;
    }

    /**
     * Initializes a new {@link SetConfigRequest}.
     * 
     * @param reseller
     * @param pack
     * @param config
     */
    public static SetConfigRequest create(String reseller, String pack, String config, String login, String password) {
        return new SetConfigRequest(null, null, null, reseller, pack, config, login, password);
    }

    /**
     * Initializes a new {@link SetConfigRequest}.
     * 
     * @param userId
     * @param contextId
     * @param config
     */
    public static SetConfigRequest createPreview(String userName, String userId, String contextId, String config, String login, String password) {
        return new SetConfigRequest(userName, userId, contextId, null, null, config, login, password);
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        if (config == null) {
            return Method.DELETE;
        }
        return Method.PUT;
    }

    @Override
    public String getServletPath() {
        
        if (userName != null) {
            return "/advertisement/v1/config/name";
        }

        if (userId != null) {
            return "/advertisement/v1/config/user";
        }

        if (pack != null) {
            return "/advertisement/v1/config/package";
        }

        return null;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<URLParameter> list = new ArrayList<>(3);

        if (userName != null) {
            list.add(new URLParameter("name", userName));
        }

        if (userId != null) {
            list.add(new URLParameter("userId", userId));
        }

        if (contextId != null) {
            list.add(new URLParameter("ctxId", contextId));
        }

        if (reseller != null) {
            list.add(new URLParameter("reseller", reseller));
        }

        if (pack != null) {
            list.add(new URLParameter("package", pack));
        }

        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends SetConfigResponse> getParser() {
        return new Parser(false);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return config;
    }

    @Override
    public Header[] getHeaders() {
        if (config != null) {
            return new Header[] { new Header.SimpleHeader("authorization", "Basic " + Base64.encode(login + ':' + password)), new Header.SimpleHeader("Content-Type", "application/json") };
        } else {
            return new Header[] { new Header.SimpleHeader("authorization", "Basic " + Base64.encode(login + ':' + password)) };
        }
    }

    private static final class Parser extends AbstractAJAXParser<SetConfigResponse> {

        /**
         * Initializes a new {@link Parser}.
         * 
         * @param failOnError
         */
        protected Parser(boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected SetConfigResponse createResponse(Response response) throws JSONException {
            return new SetConfigResponse(response);
        }

        @Override
        public SetConfigResponse parse(String body) throws JSONException {
            return new SetConfigResponse(new Response());
        }
    }

}
