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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.switchboard.exception;

import static com.openexchange.exception.OXExceptionStrings.MESSAGE;
import static com.openexchange.switchboard.exception.ZoomExceptionMessages.NO_ALL_DAY_APPOINTMENTS_MSG;
import static com.openexchange.switchboard.exception.ZoomExceptionMessages.NO_FLOATING_APPOINTMENTS_MSG;
import static com.openexchange.switchboard.exception.ZoomExceptionMessages.NO_SERIES_LONGER_THAN_A_YEAR_MSG;
import static com.openexchange.switchboard.exception.ZoomExceptionMessages.NO_SWITCH_TO_OR_FROM_SERIES_MSG;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link ZoomExceptionCodes}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.4
 */
public enum ZoomExceptionCodes implements DisplayableOXExceptionCode {

    JSON_ERROR("A JSON Error has occured.", Category.CATEGORY_ERROR, 1),
    SWITCHBOARD_ERROR("The switchboard returned with an error: %1$s", Category.CATEGORY_ERROR, 2),
    IO_ERROR("An IO error occured.", Category.CATEGORY_ERROR, 3),
    SWITCHBOARD_SERVER_ERROR("The switchboard server returned with an error: %1$s", Category.CATEGORY_ERROR, 4),

    /**
     * <li>Zoom meetings cannot be used in all-day appointments. Please use a fixed start- and end-time and try again.</li>
     * <li>Can't save all-day event with zoom meeting [id %1$s]</li>
     */
    NO_ALL_DAY_APPOINTMENTS("Can't save all-day event with zoom meeting [id %1$s]", NO_ALL_DAY_APPOINTMENTS_MSG, CATEGORY_USER_INPUT, 5),

    /**
     * <li>Zoom meetings cannot be used in appointments with floating start- or end-times. Please use a fixed start- and end-time and try again.</li>
     * <li>Can't save floating event with zoom meeting [id %1$s]</li>
     */
    NO_FLOATING_APPOINTMENTS("Can't save floating event with zoom meeting [id %1$s]", NO_FLOATING_APPOINTMENTS_MSG, CATEGORY_USER_INPUT, 6),

    /**
     * <li>Zoom meetings cannot be used in recurring appointment series spanning over more than one year. Please shorten the recurrence rule and try again.</li>
     * <li>Can't save event series with zoom meeting spanning over more than a year [id %1$s, recurrence data %2$s]</li>
     */
    NO_SERIES_LONGER_THAN_A_YEAR("Can't save event series with zoom meeting spanning over more than a year [id %1$s, recurrence data %2$s]", NO_SERIES_LONGER_THAN_A_YEAR_MSG, CATEGORY_USER_INPUT, 7),

    /**
     * <li>The same Zoom meeting can't be re-used when switching from recurring to single appointments and vice versa. Please generate a new Zoom meeting and try again.</li>
     * <li>Can't switch from/to series to single event with zoom meeting [id %1$s]</li>
     */
    NO_SWITCH_TO_OR_FROM_SERIES("Can't switch from/to series to single event with zoom meeting [id %1$s]", NO_SWITCH_TO_OR_FROM_SERIES_MSG, CATEGORY_USER_INPUT, 8),

    ;

    private static final String PREFIX = "ZOOM".intern();

    private String message;
    private String displayMessage;
    private Category category;
    private int number;

    private ZoomExceptionCodes(String message, Category category, int number) {
        this(message, null, category, number);
    }

    private ZoomExceptionCodes(String message, String displayMessage, Category category, int number) {
        this.message = message;
        this.displayMessage = null != displayMessage ? displayMessage : MESSAGE;
        this.category = category;
        this.number = number;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }

}
