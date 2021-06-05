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

package com.openexchange.i18n.tools.replacement;

import java.text.DateFormat;
import java.util.EnumSet;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.i18n.tools.TemplateReplacement;
import com.openexchange.i18n.tools.TemplateToken;

/**
 * {@link StringReplacement} - An implementation of a
 * {@link TemplateReplacement template replacement} with a fixed string.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class StringReplacement implements TemplateReplacement {

    private final TemplateToken token;

    private String replacement;

    private boolean changed;

    /**
     * Initializes a new {@link StringReplacement}
     *
     * @param token The token
     * @param replacement The replacement
     */
    public StringReplacement(final TemplateToken token, final String replacement) {
        this(token, replacement, false);
    }

    /**
     * Initializes a new {@link StringReplacement}
     *
     * @param token The token
     * @param replacement The replacement
     * @param changed <code>true</code> to prepend <i>modified</i> marker
     *            <code>"> "</code>; otherwise <code>false</code>
     */
    public StringReplacement(final TemplateToken token, final String replacement, final boolean changed) {
        super();
        this.token = token;
        this.replacement = replacement;
        this.changed = changed;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public TemplateReplacement getClone() throws CloneNotSupportedException {
        return (TemplateReplacement) clone();
    }

    @Override
    public String getReplacement() {
        return replacement;
    }

    @Override
    public TemplateToken getToken() {
        return token;
    }

    @Override
    public boolean changed() {
        return changed;
    }

    private static final EnumSet<TemplateToken> IRRELEVANT = EnumSet.of(TemplateToken.FOLDER_ID, TemplateToken.FOLDER_NAME, TemplateToken.LINK, TemplateToken.UI_WEB_PATH);

    @Override
    public boolean relevantChange() {
        return !IRRELEVANT.contains(token);
    }

    @Override
    public TemplateReplacement setChanged(final boolean changed) {
        this.changed = changed;
        return this;
    }

    @Override
    public TemplateReplacement setLocale(final Locale locale) {
        return this;
    }

    @Override
    public TemplateReplacement setTimeZone(final TimeZone timeZone) {
        return this;
    }

    @Override
    public boolean merge(final TemplateReplacement other) {
        if (!StringReplacement.class.isInstance(other)) {
            /*
             * Class mismatch or null
             */
            return false;
        }
        if (!getToken().equals(other.getToken())) {
            /*
             * Token mismatch
             */
            return false;
        }
        if (!other.changed()) {
            /*
             * Other replacement does not reflect a changed value; leave
             * unchanged
             */
            return false;
        }
        final StringReplacement o = (StringReplacement) other;
        this.replacement = o.replacement;
        this.changed = true;
        return true;
    }

    @Override
    public TemplateReplacement setDateFormat(DateFormat dateFormat) {
        return this;
    }

}
