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
import java.util.Map.Entry;
import org.json.JSONException;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.tools.session.ServerSession;

/**
 * A pair of {@link MessagingHeaderParser} and {@link MessagingHeaderWriter} are used for customizing header reading and writing. Instances
 * of those classes can be registered in a given {@link MessagingMessageWriter}.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MessagingHeaderWriter {

    /**
     * Checks if this writer feels responsible for specified header map entry. Will usually orient itself along the key of the entry
     * {@link Entry#getKey()}
     *
     * @return <code>true</code> if this writer is responsible for header map entry; otherwise <code>false</code>
     */
    boolean handles(Entry<String, Collection<MessagingHeader>> entry);

    /**
     * Gets this writer priority.
     * <p>
     * If multiple header writers feel responsible for a certain header, the one with the highest ranking will win.
     *
     * @return The priority
     */
    int getRanking();

    /**
     * Writes the JSON key for the given entry
     */
    String writeKey(Entry<String, Collection<MessagingHeader>> entry) throws JSONException, OXException;

    /**
     * Writes the JSON value for the given entry
     */
    Object writeValue(Entry<String, Collection<MessagingHeader>> entry, ServerSession session) throws JSONException, OXException;

}
