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

package com.openexchange.ajax.share.actions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.framework.Params;


/**
 * {@link RedeemRequest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.2
 */
public class RedeemRequest implements AJAXRequest<RedeemResponse> {
    
    private final String token;
    private final boolean failOnError;
    
    public RedeemRequest(String token) {
        super();
        this.token = token;
        this.failOnError = false;
    }
    
    public RedeemRequest(String token, boolean failOnError) {
        super();
        this.token = token;
        this.failOnError = failOnError;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    @Override
    public String getServletPath() {
        return "/ajax/share/redeem/token";
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        return new Params("token", token).toArray();
    }

    @Override
    public AbstractAJAXParser<? extends RedeemResponse> getParser() {
        return new RedeemParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }
    
    private static final class RedeemParser extends AbstractAJAXParser<RedeemResponse> {
        
        public RedeemParser(boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected RedeemResponse createResponse(Response response) throws JSONException {
            if (!response.hasError()) {
                Map<String, String> properties = new HashMap<String, String>();
                JSONObject data = (JSONObject) response.getJSON();
                if (data.hasAndNotNull("share")) {
                    properties.put("share", data.getString("share"));
                }
                if (data.hasAndNotNull("login_type")) {
                    properties.put("login_type", data.getString("login_type"));
                }
                if (data.hasAndNotNull("message_type")) {
                    properties.put("message_type", data.getString("message_type"));
                }
                if (data.hasAndNotNull("message")) {
                    properties.put("message", data.getString("message"));
                }
                if (data.hasAndNotNull("target")) {
                    properties.put("target", data.getString("target"));
                }
                return new RedeemResponse(response, properties);
            }
            return new RedeemResponse(response, null);
        }
        
    }

}
