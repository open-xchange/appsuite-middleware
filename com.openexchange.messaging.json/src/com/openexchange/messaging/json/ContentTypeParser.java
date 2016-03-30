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

package com.openexchange.messaging.json;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.generic.internet.MimeContentType;

/**
 * Parses the long and short forms of the json version of the content-type header.
 * <p>
 * <code>
 * {&nbsp;"type":"text/plain",&nbsp;"params":&nbsp;{"name":"something.txt","charset":"UTF-8"}&nbsp;}
 * </code><br>
 * &nbsp;&nbsp;or<br>
 * <code>
 * "text/plain;charset=UTF-8;name=something.txt"
 * </code>
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContentTypeParser implements MessagingHeaderParser {

    /**
     * Initializes a new {@link ContentTypeParser}.
     */
    public ContentTypeParser() {
        super();
    }

    @Override
    public int getRanking() {
        return 1;
    }

    @Override
    public boolean handles(final String key, final Object value) {
        return MimeContentType.getContentTypeName().equalsIgnoreCase(key);
    }

    @Override
    public void parseAndAdd(final Map<String, Collection<MessagingHeader>> headers, final String key, final Object value) throws JSONException, OXException {
        if (JSONObject.class.isInstance(value)) {
            parseObject(headers, (JSONObject) value);
        } else if (String.class.isInstance(value)) {
            parseString(headers, (String) value);
        }
    }

    private void parseString(final Map<String, Collection<MessagingHeader>> headers, final String value) throws OXException {
        final MimeContentType contentType = new MimeContentType(value);
        headers.put(MimeContentType.getContentTypeName(), Arrays.asList((MessagingHeader) contentType));
    }

    private void parseObject(final Map<String, Collection<MessagingHeader>> headers, final JSONObject value) throws OXException, JSONException {
        final MimeContentType contentType = new MimeContentType();
        final JSONObject jsonCType = value;
        contentType.setBaseType(jsonCType.getString("type"));
        if (jsonCType.has("params")) {
            final JSONObject params = jsonCType.getJSONObject("params");
            if (params.has("charset")) {
                contentType.setCharsetParameter(params.getString("charset"));
            }
            if (params.has("name")) {
                contentType.setNameParameter(params.getString("name"));
            }
        }
        headers.put(MimeContentType.getContentTypeName(), Arrays.asList((MessagingHeader) contentType));
    }

}
