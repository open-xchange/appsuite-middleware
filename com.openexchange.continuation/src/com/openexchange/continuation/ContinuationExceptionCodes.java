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

package com.openexchange.continuation;

import java.util.UUID;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link ContinuationExceptionCodes} - Error codes for continuation module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public enum ContinuationExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", Category.CATEGORY_ERROR, 1),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", Category.CATEGORY_ERROR, 2),
    /**
     * Scheduled for continuation: %1$s
     */
    SCHEDULED_FOR_CONTINUATION("Scheduled for continuation: %1$s", Category.CATEGORY_ERROR, 3),
    /**
     * No such continuation for %1$s
     */
    NO_SUCH_CONTINUATION("No such continuation for %1$s", Category.CATEGORY_ERROR, 4),
    /**
     * Continuation with identifier %1$s has been canceled
     */
    CONTINUATION_CANCELED("Continuation with identifier %1$s has been canceled", Category.CATEGORY_ERROR, 5),

    ;

    /**
     * The prefix constant.
     */
    public static final String PREFIX = "CONTINUATION";

    private final Category category;
    private final int detailNumber;
    private final String message;
    private final String displayMessage;

    private ContinuationExceptionCodes(final String message, final Category category, final int detailNumber) {
        this(message, category, detailNumber, null);
    }

    private ContinuationExceptionCodes(final String message, final Category category, final int detailNumber, final String displayMessage) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    private static final Object[] EMPTY_ARGS = new Object[0];

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return create(null, null, EMPTY_ARGS);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final UUID uuid, final boolean lightWeight) {
        return lightWeight ? create(uuid, null, EMPTY_ARGS).markLightWeight() : create(uuid, null, EMPTY_ARGS);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return create(null, null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return create(null, cause, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    private OXException create(final UUID uuid, final Throwable cause, final Object... args) {
        final Category cat = null == category ? getCategory() : category;
        final OXException ret = new ContinuationException(uuid, getNumber(), getDisplayMessage(), cause, args).setLogMessage(getMessage(), args);
        return ret.addCategory(cat).setPrefix(getPrefix());
    }

    /**
     * Signal call has been scheduled for continuation.
     *
     * @param continuation The associated continuation
     * @return The appropriate {@link OXException} instance
     */
    public static <V> OXException scheduledForContinuation(final Continuation<V> continuation) {
        if (null == continuation) {
            return null;
        }
        return SCHEDULED_FOR_CONTINUATION.create(continuation.getUuid(), true);
    }

    /**
     * Signal call has been scheduled for continuation.
     *
     * @param uuid The associated UUID
     * @return The appropriate {@link OXException} instance
     */
    public static OXException scheduledForContinuation(final UUID uuid) {
        if (null == uuid) {
            return null;
        }
        return SCHEDULED_FOR_CONTINUATION.create(uuid, true);
    }

}
