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
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.i18n.tools.TemplateReplacement;
import com.openexchange.i18n.tools.TemplateToken;

/**
 * {@link LocalizedStringReplacement}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public abstract class LocalizedStringReplacement implements TemplateReplacement {

    private StringHelper stringHelper;

    private final TemplateToken token;

    private String replacement;

    protected Locale locale;

    protected boolean changed;

    /**
     * Initializes a new {@link LocalizedStringReplacement}
     */
    public LocalizedStringReplacement(final TemplateToken token, final String replacement) {
        super();
        this.token = token;
        this.replacement = replacement;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final LocalizedStringReplacement clone = (LocalizedStringReplacement) super.clone();
        clone.stringHelper = null;
        clone.locale = (Locale) (this.locale == null ? null : this.locale.clone());
        return clone;
    }

    @Override
    public TemplateReplacement getClone() throws CloneNotSupportedException {
        return (TemplateReplacement) clone();
    }

    protected final StringHelper getStringHelper() {
        if (stringHelper == null) {
            if (locale == null) {
                stringHelper = StringHelper.valueOf(Locale.ENGLISH);
            } else {
                stringHelper = StringHelper.valueOf(locale);
            }
        }
        return stringHelper;
    }

    @Override
    public boolean changed() {
        return changed;
    }

    @Override
    public boolean relevantChange() {
        return changed();
    }

    @Override
    public String getReplacement() {
        return getStringHelper().getString(replacement);
    }

    @Override
    public TemplateToken getToken() {
        return token;
    }

    @Override
    public TemplateReplacement setChanged(final boolean changed) {
        this.changed = changed;
        return this;
    }

    @Override
    public final TemplateReplacement setLocale(final Locale locale) {
        if (locale == null || locale.equals(this.locale)) {
            return this;
        }
        this.locale = locale;
        stringHelper = null;
        return this;
    }

    @Override
    public TemplateReplacement setTimeZone(final TimeZone timeZone) {
        // Not applicable
        return this;
    }

    @Override
    public boolean merge(final TemplateReplacement other) {
        if (!LocalizedStringReplacement.class.isInstance(other)) {
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
        final LocalizedStringReplacement o = (LocalizedStringReplacement) other;
        this.replacement = o.replacement;
        this.changed = true;
        return true;
    }

    @Override
    public TemplateReplacement setDateFormat(DateFormat dateFormat) {
        return this;
    }
}
