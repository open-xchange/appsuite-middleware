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

package com.openexchange.groupware.importexport.csv;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

public enum CsvExceptionCodes implements DisplayableOXExceptionCode {

    /** Broken CSV file: Lines have different number of cells, line #1 has %d, line #%d has %d. Is this really a CSV file? */
    BROKEN_CSV("Broken CSV file: Lines have different number of cells, line #1 has %d, line #%d has %d. Is this really a CSV file?",
        CsvExceptionMessages.BROKEN_CSV_MSG, CATEGORY_USER_INPUT, 1000),
    
    /** Illegal state: Found data after presumed last line. */
    DATA_AFTER_LAST_LINE("Illegal state: Found data after presumed last line.", CATEGORY_ERROR, 1001),
    
    /** Cannot find an importer for format %s into folders %s */
    LOADING_FOLDER_FAILED("Could not load folder %s", CATEGORY_ERROR, 204),
    
    /** Cannot load folder (not found, no rights, who knows? used to be I_E 204 */
    UTF8_ENCODE_FAILED("Could not encode as UTF-8", CATEGORY_ERROR, 104),
    
    /** Could not encode as UTF-8 */
    IOEXCEPTION_WHILE_CONVERTING("Encountered IO error while trying to read stream", CATEGORY_ERROR, 1002),
    
    /** Parsing %1$s to a number failed. */
    NUMBER_FAILED("Parsing %1$s to a number failed.", CATEGORY_ERROR, 207),

    /** Error at row %1$s: %2$s */
    NESTED_ERROR("Error at row %1$s: %2$s",CsvExceptionMessages.NESTED_ERROR_MSG, CATEGORY_USER_INPUT, 1003);

    public static String PREFIX = "CSV";

    private String message;
    private String displayMessage;
    private Category category;
    private int number;
    
    private CsvExceptionCodes(String message, Category category, int number) {
        this(message, null, category, number);
    }

    private CsvExceptionCodes(String message, String displayMessage, Category category, int number) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
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
