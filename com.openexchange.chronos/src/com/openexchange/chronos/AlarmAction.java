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

package com.openexchange.chronos;

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
