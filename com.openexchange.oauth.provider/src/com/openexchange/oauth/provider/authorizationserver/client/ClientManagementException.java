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

package com.openexchange.oauth.provider.authorizationserver.client;


/**
 * This Exception is thrown on client provisioning errors.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ClientManagementException extends Exception {

    private static final long serialVersionUID = 578991491960268465L;

    public static enum Reason {

        /**
         * An internal error occurred: %1$s
         */
        INTERNAL_ERROR("An internal error occurred: %1$s"),
        /**
         * Invalid client data: %1$s
         */
        INVALID_CLIENT_DATA("Invalid client data: %1$s"),
        /**
         * The client ID '%1$s' is invalid.
         */
        INVALID_CLIENT_ID("The client ID '%1$s' is invalid."),
        /**
         * A client with name '$1%s' does already exist.
         */
        DUPLICATE_NAME("A client with name '%1$s' does already exist in context group '%2$s'."),
        /**
         * The client storage threw an error: %1$s
         */
        STORAGE_ERROR("The client storage threw an error: %1$s");

        private final String baseMessage;

        private Reason(String baseMessage) {
            this.baseMessage = baseMessage;
        }

        public String getBaseMessage() {
            return baseMessage;
        }

    }

    private final Reason reason;

    /**
     * Creates a new {@link ClientManagementException}. The message is computed
     * from the reasons base message and the passed parameters.
     *
     * @param reason The reason
     * @param params The parameters
     */
    public ClientManagementException(Reason reason, String... params) {
        super(compileMessage(reason, (Object[]) params));
        this.reason = reason;
    }

    /**
     * Creates a new {@link ClientManagementException}. The message is computed
     * from the reasons base message and the passed parameters.
     *
     * @param cause The cause
     * @param reason The reason
     * @param params The parameters
     */
    public ClientManagementException(Throwable cause, Reason reason, String... params) {
        super(compileMessage(reason, (Object[]) params), cause);
        this.reason = reason;
    }

    private ClientManagementException(Reason reason, String rawMessage) {
        super(rawMessage);
        this.reason = reason;
    }

    /**
     * Creates an instance of {@link ClientManagementException}. The message is not based on
     * the reason but will be overridden by the passed one.
     *
     * @param reason The reason
     * @param message The message
     * @return The exception
     */
    public static ClientManagementException forMessage(Reason reason, String message) {
        return new ClientManagementException(reason, message);
    }

    /**
     * Gets the reason
     *
     * @return The reason
     */
    public Reason getReason() {
        return reason;
    }

    private static String compileMessage(Reason reason, Object... params) {
        return String.format(reason.getBaseMessage(), params);
    }

}
