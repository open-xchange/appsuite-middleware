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
import java.util.Map;
import org.json.JSONException;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingHeader;


/**
 * A pair of {@link MessagingHeaderParser} and {@link MessagingHeaderWriter} are used for customizing header reading and writing. Instances
 * of those classes can be registered in a given {@link MessagingMessageWriter}.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MessagingHeaderParser {

    /**
     * Returns true if this parser feels responsible for the given header. Usually this will orient itself along the given key.
     */
    public boolean handles(String key, Object value);

    /**
     * Parses the header and adds it to the given map.
     */
    public void parseAndAdd(Map<String, Collection<MessagingHeader>> headers, String key, Object value) throws JSONException, OXException;

    /**
     * If more than one parser feels responsible for a given header, the one with the highest ranking wins.
     */
    public int getRanking();
}
