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
 * {@link FormatLocalizedStringReplacement} - An implementation of a
 * {@link TemplateReplacement replacement} enhanced with a {@link Locale locale}
 * and capable to prepend <i>changed</i> marker.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class FormatLocalizedStringReplacement implements TemplateReplacement {

    private final TemplateToken token;

    private String format;

    private String replacement;

    private Locale locale;

    private boolean changed;

    private StringHelper stringHelper;

    /**
     * Initializes a new {@link FormatLocalizedStringReplacement}
     *
     * @param token The token
     * @param format The format string; leave to <code>null</code> to avoid
     *            formatting
     * @param replacement The replacement
     */
    public FormatLocalizedStringReplacement(final TemplateToken token, final String format, final String replacement) {
        super();
        this.token = token;
        this.replacement = replacement;
        this.format = format;
    }

    @Override
    public TemplateReplacement setLocale(final Locale locale) {
        if (locale == null || locale.equals(this.locale)) {
            return this;
        }
        this.locale = locale;
        stringHelper = null;
        return this;
    }

    protected StringHelper getStringHelper() {
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
    public TemplateReplacement setChanged(final boolean changed) {
        this.changed = changed;
        return this;
    }

    @Override
    public String getReplacement() {
        if (format == null) {
            return replacement;
        }
        final String result = String.format(getStringHelper().getString(format), getStringHelper().getString(replacement));
        if (changed) {
            return new StringBuilder(result.length() + PREFIX_MODIFIED.length()).append(PREFIX_MODIFIED).append(result)
                .toString();
        }
        return result;
    }

    /**
     * Gets the sole replacement
     *
     * @return The sole replacement
     */
    protected final String getSoleReplacement() {
        return replacement;
    }

    @Override
    public TemplateToken getToken() {
        return token;
    }

    @Override
    public TemplateReplacement setTimeZone(final TimeZone timeZone) {
        return this;
    }

    @Override
    public TemplateReplacement getClone() throws CloneNotSupportedException {
        return (TemplateReplacement) clone();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final FormatLocalizedStringReplacement clone = (FormatLocalizedStringReplacement) super.clone();
        clone.locale = (Locale) this.locale.clone();
        return clone;
    }

    @Override
    public boolean merge(final TemplateReplacement other) {
        if (!FormatLocalizedStringReplacement.class.isInstance(other)) {
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
        final FormatLocalizedStringReplacement o = (FormatLocalizedStringReplacement) other;
        this.replacement = o.replacement;
        this.format = o.format;
        this.changed = true;
        return true;
    }

    @Override
    public TemplateReplacement setDateFormat(DateFormat dateFormat) {
        return this;
    }

}
