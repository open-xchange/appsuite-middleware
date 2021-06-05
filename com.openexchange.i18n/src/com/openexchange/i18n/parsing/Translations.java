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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.openexchange.java.Strings;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Translations {

    private final Map<String, Translation> simpleTranslations; // Key -> [form1, form2, ...]
    private final Map<String, Map<String, Translation>> contextTranslations; // Context -> (Key -> [form1, form2, ...])
    private Locale locale;

    /**
     * Initializes a new {@link Translations}.
     */
    public Translations() {
        super();
        simpleTranslations = new HashMap<String, Translation>(32);
        contextTranslations = new HashMap<String, Map<String, Translation>>(32);
    }

    public String translate(String original) {
        Translation translation = simpleTranslations.get(original);
        return translation == null ? null : translation.getMessage();
    }

    public String translate(String original, int plural) {
        Translation translation = simpleTranslations.get(original);
        return translation == null ? null : translation.getMessage(I(plural));
    }

    public String translate(String context, String original) {
        Map<String, Translation> map = contextTranslations.get(context);
        return map == null ? null : map.get(original).getMessage();
    }

    public String translate(String context, String original, int plural) {
        Map<String, Translation> map = contextTranslations.get(context);
        return map == null ? null : map.get(original).getMessage(I(plural));
    }

    public void setTranslation(String key, String value) {
        if (key == null) {
            return;
        }

        Translation t = new Translation(null, key, null);
        t.setMessage(I(0), value);
        simpleTranslations.put(key, t);
    }

    public void setTranslationPlural(String key, String keyPlural, List<String> values) {
        if (key == null || values == null || values.isEmpty()) {
            return;
        }

        if (keyPlural == null && values.size() == 1) {
            setTranslation(key, values.get(0));
            return;
        }

        Translation t = new Translation(null, key, keyPlural);
        for (int i = 0; i < values.size(); i++) {
            t.setMessage(I(i), values.get(i));
        }
        simpleTranslations.put(key, t);
        if (keyPlural != null) {
            simpleTranslations.put(keyPlural, t);
        }
    }

    public void setContextTranslation(String context, String key, String value) {
        if (Strings.isEmpty(key)) {
            return;
        }

        Translation t = new Translation(context, key, null);
        t.setMessage(I(0), value);
        Map<String, Translation> map = contextTranslations.get(context);
        if (map == null) {
            map = new HashMap<String, Translation>();
            contextTranslations.put(context, map);
        }
        map.put(key, t);
    }

    public void setContextTranslationPlural(String context, String key, String keyPlural, List<String> values) {
        if (key == null || values == null || values.isEmpty()) {
            return;
        }

        if (context == null) {
            setTranslationPlural(key, keyPlural, values);
            return;
        }

        if (keyPlural == null && values.size() == 1) {
            setContextTranslation(context, key, values.get(0));
            return;
        }

        Translation t = new Translation(context, key, keyPlural);
        for (int i = 0; i < values.size(); i++) {
            t.setMessage(I(i), values.get(i));
        }
        Map<String, Translation> map = contextTranslations.get(context);
        if (map == null) {
            map = new HashMap<String, Translation>();
            contextTranslations.put(context, map);
        }
        map.put(key, t);
        if (keyPlural != null) {
            map.put(keyPlural, t);
        }
    }

    public Set<String> getKnownStrings() {
        return getKnownStrings(false);
    }

    public Set<String> getKnownStrings(boolean includeContexts) {
        if (false == includeContexts) {
            return simpleTranslations.keySet();
        }

        Set<String> retval = new HashSet<String>();
        retval.addAll(getKnownStrings());
        for (Map<String, Translation> contextMap : contextTranslations.values()) {
            retval.addAll(contextMap.keySet());
        }
        return retval;
    }

    public Set<String> getKnownStrings(String context) {
        Map<String, Translation> map = contextTranslations.get(context);
        return map == null ? Collections.<String> emptySet() : map.keySet();
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(64);
        builder.append('{');
        if (locale != null) {
            builder.append("locale=").append(locale).append(", ");
        }
        if (simpleTranslations != null) {
            builder.append("translation-map=").append(simpleTranslations);
        }
        builder.append('}');
        return builder.toString();
    }

}
