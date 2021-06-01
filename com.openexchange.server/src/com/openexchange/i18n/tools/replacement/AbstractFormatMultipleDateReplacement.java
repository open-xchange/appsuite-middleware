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

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.i18n.tools.TemplateReplacement;

/**
 * {@link AbstractFormatMultipleDateReplacement}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public abstract class AbstractFormatMultipleDateReplacement extends AbstractMultipleDateReplacement {

    /**
     * The format string
     */
    protected String format;

    /**
     * The fallback string if replacement used in string format is empty or
     * <code>null</code>
     */
    protected String fallback;

    /**
     * Initializes a new AbstractFormatDateReplacement
     *
     * @param dates The dates
     * @param format The format string
     */
    protected AbstractFormatMultipleDateReplacement(final Date[] dates, final String format) {
        super(dates);
        this.format = format;
    }

    /**
     * Initializes a new AbstractFormatDateReplacement
     *
     * @param dates The dates
     * @param format The format string
     * @param locale The locale
     * @param timeZone The time zone
     */
    public AbstractFormatMultipleDateReplacement(final Date[] dates, final String format, final Locale locale,
            final TimeZone timeZone) {
        super(dates, locale, timeZone);
        this.format = format;
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
        final String dateRepl = super.getReplacement();
        final String result;
        if ((dates == null || dateRepl.length() == 0) && fallback != null) {
            final StringHelper sh = StringHelper.valueOf(locale == null ? Locale.ENGLISH : locale);
            result = String.format(sh.getString(format), sh.getString(fallback));
        } else {
            result = String.format(StringHelper.valueOf(locale == null ? Locale.ENGLISH : locale).getString(format),
                    dateRepl);
        }
        if (changed) {
            return new StringBuilder(PREFIX_MODIFIED.length() + result.length()).append(PREFIX_MODIFIED).append(result)
                    .toString();
        }
        return result;
    }

    @Override
    public boolean merge(final TemplateReplacement other) {
        if (!AbstractFormatMultipleDateReplacement.class.isInstance(other)) {
            /*
             * Class mismatch or null
             */
            return false;
        }
        if (super.merge(other)) {
            final AbstractFormatMultipleDateReplacement o = (AbstractFormatMultipleDateReplacement) other;
            this.format = o.format;
            this.fallback = o.fallback;
            return true;
        }
        return false;
    }
}
