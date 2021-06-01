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


package com.openexchange.spamhandler.spamassassin.exceptions;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

public enum SpamhandlerSpamassassinExceptionCode implements DisplayableOXExceptionCode {
    /**
     * Spamd returned wrong exit code "%s"
     */
    WRONG_SPAMD_EXIT("Spamd returned wrong exit code \"%s\"", CATEGORY_ERROR, 3000),

    /**
     * Internal error: Wrong arguments are given to the tell command: "%s"
     */
    WRONG_TELL_CMD_ARGS("Internal error: Wrong arguments are given to the tell command: \"%s\"", CATEGORY_ERROR, 3001),

    /**
     * Error during communication with spamd: "%s"
     */
    COMMUNICATION_ERROR("Error during communication with spamd: \"%s\"", CATEGORY_ERROR, 3002),

    /**
     * Can't handle spam because MailService isn't available
     */
    MAILSERVICE_MISSING("Spam cannot be handled because MailService is not available", CATEGORY_ERROR, 3003),

    /**
     * Error while getting spamd provider from service: "%s"
     */
    ERROR_GETTING_SPAMD_PROVIDER("Error while getting spamd provider from service: \"%s\"", CATEGORY_ERROR, 3004);

    private Category category;

    private int detailNumber;

    private String message;
    
    private String displayMessage;

    private SpamhandlerSpamassassinExceptionCode(String message, String displayMessage, Category category, int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.detailNumber = detailNumber;
        this.category = category;
    }
    
    private SpamhandlerSpamassassinExceptionCode(String message, Category category, int detailNumber) {
        this(message, null, category, detailNumber);
    }

    @Override
    public String getPrefix() {
        return "MSG";
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
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public int getNumber() {
        return detailNumber;
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
    public OXException create(Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Throwable cause, Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
