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
