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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.antivirus.exceptions;

import static com.openexchange.exception.OXExceptionStrings.MESSAGE;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link AntiVirusServiceExceptionCodes}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public enum AntiVirusServiceExceptionCodes implements DisplayableOXExceptionCode {
    /**
     * <li>We were unable to scan your file for viruses.</li>
     * <li>An I/O error occurred: %1$s</li>
     */
    IO_ERROR("An I/O error occurred: %1$s", AntiVirusServiceExceptionMessages.UNABLE_TO_SCAN, CATEGORY_ERROR, 1),
    /**
     * <li>We were unable to scan your file for viruses.</li>
     * <li>An unexpected error occurred: %1$s</li>
     */
    UNEXPECTED_ERROR("An unexpected error occurred: %1$s", AntiVirusServiceExceptionMessages.UNABLE_TO_SCAN, CATEGORY_ERROR, 2),
    /**
     * <li>We were unable to scan your file for viruses.</li>
     * <li>The remote service is unavailable at the moment: %1$s. Please try again later.</li>
     */
    REMOTE_SERVICE_UNAVAILABLE("The remote service is unavailable at the moment: %1$s. Please try again later.", AntiVirusServiceExceptionMessages.UNABLE_TO_SCAN, CATEGORY_CONNECTIVITY, 3),
    /**
     * <li>We were unable to scan your file for viruses.</li>
     * <li>A remote internal server error occurred: %1$s</li>
     */
    REMOTE_INTERNAL_SERVER_ERROR("A remote internal server error occurred: %1$s", AntiVirusServiceExceptionMessages.UNABLE_TO_SCAN, CATEGORY_ERROR, 4),
    /**
     * <li>We were unable to scan your file for viruses.</li>
     * <li>A remote server error occurred: %1$s</li>
     */
    REMOTE_SERVER_ERROR("A remote server error occurred: %1$s", AntiVirusServiceExceptionMessages.UNABLE_TO_SCAN, CATEGORY_ERROR, 5),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>The configuration is missing from the '%1$s' property. Please configure the AntiVirus service properly.</li>
     */
    CONFIGURATION_MISSING("The configuration is missing from the '%1$s' property. Please configure the AntiVirus service properly.", CATEGORY_CONFIGURATION, 6),
    /**
     * <li>We were unable to scan your file for viruses.</li>
     * <li>Cannot establish connection to the remote Anti-Virus server. Please try again layer.</li>
     */
    CANNOT_ESTABLISH_CONNECTION("Cannot establish connection to the remote Anti-Virus server. Please try again layer.", AntiVirusServiceExceptionMessages.UNABLE_TO_SCAN, CATEGORY_CONNECTIVITY, 7),
    /**
     * <li>The file '%1$s' you are trying to scan does not exist</li>
     * <li>File '%1$s' does not exist</li>
     */
    FILE_NOT_EXISTS("File '%1$s' does not exist", AntiVirusServiceExceptionMessages.FILE_NOT_EXISTS, CATEGORY_USER_INPUT, 8),
    /**
     * <li>The file you are trying to scan exceeds the maximum allowed file size of %1$s.</li>
     * <li>File too big to scan</li>
     */
    FILE_TOO_BIG("File too big to scan", AntiVirusServiceExceptionMessages.FILE_TOO_BIG, CATEGORY_USER_INPUT, 9),
    /**
     * JSON error: %s
     */
    JSON_ERROR("JSON error: %s", MESSAGE, Category.CATEGORY_ERROR, 10),
    /**
     * <li>The file '%1$s' you are trying to download seems to be infected with '%2$s'.</li>
     * <li>The file '%1$s' is infected with '%2$s'.</li>
     */
    FILE_INFECTED("The file '%1$s' is infected with '%2$s'.", AntiVirusServiceExceptionMessages.FILE_INFECTED, CATEGORY_USER_INPUT, 11),
    /**
     * <li>We were unable to scan your file for viruses.</li>
     * <li>The 'antivirus' capability is disabled for user '%1$s' in context '%2$s'</li>
     */
    CAPABILITY_DISABLED("The 'antivirus' capability is disabled for user '%1$s' in context '%2$s'", AntiVirusServiceExceptionMessages.UNABLE_TO_SCAN, CATEGORY_PERMISSION_DENIED, 12),
    /**
     * <li>We were unable to scan your file for viruses.</li>
     * <li>The Anti-Virus service is disabled for user '%1$s' in context '%2$s'</li>
     */
    ANTI_VIRUS_SERVICE_DISABLED("The Anti-Virus service is disabled for user '%1$s' in context '%2$s'", AntiVirusServiceExceptionMessages.UNABLE_TO_SCAN, CATEGORY_PERMISSION_DENIED, 13),
    /**
     * <li>The Anti-Virus service is unavailable at the moment. Please try again later.</li>
     * <li>The Anti-Virus service is absent.</li>
     */
    ANTI_VIRUS_SERVICE_ABSENT("The Anti-Virus service is absent.", AntiVirusServiceExceptionMessages.ANTI_VIRUS_SERVICE_UNAVAILABLE, CATEGORY_SERVICE_DOWN, 14),
    ;

    public static final String PREFIX = "ANTI-VIRUS-SERVICE";

    private String message;
    private String displayMessage;
    private Category category;
    private int number;

    /**
     * Initialises a new {@link AntiVirusServiceExceptionCodes}.
     * 
     * @param message The exception message
     * @param category The {@link Category}
     * @param number The error number
     */
    private AntiVirusServiceExceptionCodes(String message, Category category, int number) {
        this(message, null, category, number);
    }

    /**
     * Initialises a new {@link AntiVirusServiceExceptionCodes}.
     * 
     * @param message The exception message
     * @param displayMessage The display message
     * @param category The {@link Category}
     * @param number The error number
     */
    private AntiVirusServiceExceptionCodes(String message, String displayMessage, Category category, int number) {
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
