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
