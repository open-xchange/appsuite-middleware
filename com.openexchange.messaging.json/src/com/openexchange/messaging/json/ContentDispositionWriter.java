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
