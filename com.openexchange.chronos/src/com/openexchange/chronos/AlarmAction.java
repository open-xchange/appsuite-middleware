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

package com.openexchange.chronos;

import com.openexchange.java.EnumeratedProperty;

/**
 * {@link AlarmAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.8.6.1">RFC 5545, section 3.8.6.1</a>
 */
public class AlarmAction extends EnumeratedProperty {

    /**
     * Specifies an alarm that causes a text message to be displayed to the user.
     */
    public static final AlarmAction DISPLAY = new AlarmAction("DISPLAY");

    /**
     * Specifies an alarm that causes a sound to be played to alert the user.
     */
    public static final AlarmAction AUDIO = new AlarmAction("AUDIO");

    /**
     * Specifies an alarm that causes an electronic email message to be delivered to one or more email addresses.
     */
    public static final AlarmAction EMAIL = new AlarmAction("EMAIL");

    /**
     * Specifies an alarm that causes an SMS to be delivered to one or more phone numbers.
     */
    public static final AlarmAction SMS = new AlarmAction("X-SMS");

    /**
     * Specifies an alarm that causes a procedure or program to be invoked.
     *
     * @deprecated with <a href="https://tools.ietf.org/html/rfc5545#appendix-A.3">RFC 5545, appendix A.3</a>
     */
    @Deprecated
    public static final AlarmAction PROCEDURE = new AlarmAction("PROCEDURE");

    /**
     * Specifies a (default) alarm that does not alert the calendar.
     *
     * @see <a href="https://tools.ietf.org/html/draft-daboo-valarm-extensions-04#section-11.3">draft-daboo-valarm-extensions, section 11.3</a>
     */
    public static final AlarmAction NONE = new AlarmAction("NONE");

    /**
     * Initializes a new {@link AlarmAction}.
     *
     * @param value The action value
     */
    public AlarmAction(String value) {
        super(value);
    }

    @Override
    protected String[] getStandardValues() {
        return getValues(DISPLAY, AUDIO, EMAIL, SMS, PROCEDURE);
    }

}
