/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
