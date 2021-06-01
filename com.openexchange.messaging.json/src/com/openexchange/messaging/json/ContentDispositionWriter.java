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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.ContentDisposition;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.generic.internet.MimeContentDisposition;
import com.openexchange.tools.session.ServerSession;

/**
 * Writes a content-disposition in the long form.
 *
 * @see ContentDispositionParser
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContentDispositionWriter implements MessagingHeaderWriter {

    /**
     * Initializes a new {@link ContentDispositionWriter}.
     */
    public ContentDispositionWriter() {
        super();
    }

    @Override
    public int getRanking() {
        return 0;
    }

    @Override
    public boolean handles(final Entry<String, Collection<MessagingHeader>> entry) {
        return "content-disposition".equalsIgnoreCase(entry.getKey());
    }

    @Override
    public String writeKey(final Entry<String, Collection<MessagingHeader>> entry) throws JSONException, OXException {
        return "Content-Disposition";
    }

    @Override
    public Object writeValue(final Entry<String, Collection<MessagingHeader>> entry, final ServerSession session) throws JSONException, OXException {
        final ContentDisposition cDisp = toCType(entry.getValue().iterator().next());
        final JSONObject jsonCType = new JSONObject();
        /*
         * Put disposition
         */
        jsonCType.put("type", cDisp.getDisposition());
        /*
         * Put parameters
         */
        final Iterator<String> names = cDisp.getParameterNames();
        if (names.hasNext()) {
            final JSONObject params = new JSONObject();
            do {
                final String name = names.next();
                final String value = cDisp.getParameter(name);
                params.put(name, value);
            } while (names.hasNext());
            jsonCType.put("params", params);
        }
        /*
         * Return JSON
         */
        return jsonCType;
    }

    private ContentDisposition toCType(final MessagingHeader header) throws OXException {
        if (ContentDisposition.class.isInstance(header)) {
            return (ContentDisposition) header;
        }
        return new MimeContentDisposition(header.getValue());
    }

}
