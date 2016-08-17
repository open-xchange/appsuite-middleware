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

package com.openexchange.filestore.swift.impl.token;

import java.util.Date;
import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.swift.SwiftExceptionCode;
import com.openexchange.java.Strings;

/**
 * {@link TokenParser}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface TokenParser {

    /** The logger constant */
    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(TokenParser.class);

    /**
     * Parses the token from specified JSON response.
     *
     * @param jResponse The JSON response as received from identifier end-point
     * @param response The associated HTTP response
     * @return The parsed token
     * @throws OXException If parsing the token fails
     */
    Token parseTokenFrom(JSONObject jResponse, HttpResponse response) throws OXException;

    /** The parser for <a href="http://developer.openstack.org/api-ref-identity-v2.html">Identity API v2</a> */
    public static final TokenParser TOKEN_PARSER_V2 = new TokenParser() {

        @Override
        public Token parseTokenFrom(JSONObject jResponse, HttpResponse response) throws OXException {
            if (null == jResponse) {
                return null;
            }

            try {
                JSONObject jAccess = jResponse.getJSONObject("access");
                JSONObject jToken = jAccess.getJSONObject("token");
                String id = jToken.getString("id");
                Date expires = TokenParserUtil.parseExpiryDate(jToken.getString("expires"));
                return new Token(id, expires);
            } catch (JSONException e) {
                throw SwiftExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    };

    /** The parser for <a href="http://developer.openstack.org/api-ref-identity-v3.html">Identity API v3</a> */
    public static final TokenParser TOKEN_PARSER_V3 = new TokenParser() {

        @Override
        public Token parseTokenFrom(JSONObject jResponse, HttpResponse response) throws OXException {
            if (null == jResponse) {
                return null;
            }

            try {
                // The authentication token is provided by "X-Subject-Token" header rather than in the response body.
                String id = response.getFirstHeader("X-Subject-Token").getValue();
                if (Strings.isEmpty(id)) {
                    LOGGER.warn("Missing \"X-Subject-Token\" response header!");
                }

                // Expiration date is in JSON response body
                JSONObject jToken = jResponse.getJSONObject("token");
                Date expires = TokenParserUtil.parseExpiryDate(jToken.getString("expires_at"));
                return new Token(id, expires);
            } catch (JSONException e) {
                throw SwiftExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    };

}
