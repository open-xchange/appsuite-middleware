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

package com.openexchange.share.servlet.handler;


/**
 * {@link ShareHandlerReply} - An enumeration for replies as a share passes the {@link ShareHandler share handler} chain.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public enum ShareHandlerReply {

    /**
     * The handling for a share is denied. Further handling for that request is supposed to be aborted.
     */
    DENY,
    /**
     * The handling for a share is supposed to get passed to the next handler in chain.
     */
    NEUTRAL,
    /**
     * The handling for a share has been completed. No further handling is supposed to take place.
     */
    ACCEPT;

}
