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
