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

package com.openexchange.i18n.tools;

import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * {@link TemplateReplacement} - Defines how a {@link TemplateToken token} is
 * supposed to be replaced by a replacement string.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface TemplateReplacement extends Cloneable {

    public static final String PREFIX_MODIFIED = "* ";

    /**
     * Gets the token occurring in a template which is supposed to be replaced
     *
     * @return The token occurring in a template which is supposed to be
     *         replaced
     */
    public TemplateToken getToken();

    /**
     * Gets the replacement string for the token occurring in a template
     *
     * @return The replacement string for the token occurring in a template
     */
    public String getReplacement();

    /**
     * Indicates if this replacement reflects a changed value; default is
     * <code>false</code>
     *
     * @return <code>true</code> if this replacement reflects a changed value;
     *         otherwise <code>false</code>
     */
    public boolean changed();

    /**
     * Checks if this replacement is a relevant change to notify about.
     *
     * @return <code>true</code> if relevant; otherwise <code>false</code>
     */
    public boolean relevantChange();

    /**
     * Sets whether this replacement is marked as being changed or not.
     *
     * <code>true</code> to mark this replacement as being changed; otherwise
     * <code>false</code>
     *
     * @return This replacement with new changed status applied
     */
    public TemplateReplacement setChanged(boolean changed);

    /**
     * Sets specified locale.
     * <p>
     * If not applicable, given locale is <code>null</code> or equal to already
     * applied locale, this method is a no-op and this replacement is returned
     * unchanged.
     *
     * @param locale The locale to set
     * @return This replacement with specified locale applied
     */
    public TemplateReplacement setLocale(Locale locale);

    /**
     * Sets specified time zone.
     * <p>
     * If not applicable, given time zone is <code>null</code> or equal to
     * already applied time zone, this method is a no-op and this replacement is
     * returned unchanged.
     *
     * @param timeZone The time zone to set
     * @return This replacement with specified time zone applied
     */
    public TemplateReplacement setTimeZone(TimeZone timeZone);

    /**
     * 
     *
     * @param dateFormat
     * @return
     */
    public TemplateReplacement setDateFormat(DateFormat dateFormat);

    /**
     * Should delegate to {@link java.lang.Object#clone()}
     *
     * @return The clone
     * @throws CloneNotSupportedException If {@link Cloneable} interface is not
     *             implemented
     */
    public TemplateReplacement getClone() throws CloneNotSupportedException;

    /**
     * Merges this replacement with specified replacement provided that classes
     * and {@link TemplateToken tokens} of both replacements are equal:
     * <ul>
     * <li>If other replacement's {@link #changed()} returns <code>true</code>,
     * the replacement is taken from other replacement</li>
     * <li>Otherwise this replacement is left unchanged</li>
     * </ul>
     * <b>Note</b>:<br>
     * If specified replacement is <code>null</code> this method is treated as a
     * no-op.
     *
     * @param other The other replacement to merge with
     * @return <code>true</code> if this replacement is merged with specified
     *         replacement; otherwise <code>false</code> is this replacement is
     *         left unchanged
     */
    public boolean merge(TemplateReplacement other);
}
