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

package com.openexchange.i18n.parsing;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Translation}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class Translation {

    private String context;
    private String id;
    private String idPlural;
    private final Map<Integer, String> messages;

    public Translation(String context, String id, String idPlural) {
        setContext(context);
        setId(id);
        setIdPlural(idPlural);
        messages = new HashMap<Integer, String>();
    }

    public String getMessage() {
        return getMessage(I(0));
    }

    /**
     * Returns the plural form of the message.
     *
     * @param plural
     * @return The plural form. The singular form if 0.
     */
    public String getMessage(Integer plural) {
        return messages.containsKey(plural) ? messages.get(plural) : messages.get(Collections.max(messages.keySet()));
    }

    /**
     * Sets one message (sinular or plural).
     *
     * @param plural The singular version (0) or one of the plural versions (1..)
     * @param message
     */
    public void setMessage(Integer plural, String message) {
        messages.put(plural, message);
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdPlural() {
        return idPlural;
    }

    public void setIdPlural(String idPlural) {
        this.idPlural = idPlural;
    }

}
