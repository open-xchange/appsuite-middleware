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

package com.openexchange.mailmapping;


/**
 * This enum represents the possible replies that a {@link MailResolver} can return alongside with a {@link ResolvedMail} instance.
 * <p>
 * Based on the order that the ResolveReply values are declared,
 * <code>ResolveReply.ACCEPT.compareTo(ResolveReply.DENY)</code> will return a positive value.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum ResolveReply {

    /**
     * The {@link MailResolver} denies further processing of passed E-Mail address.
     */
    DENY,
    /**
     * The {@link MailResolver} cannot handle passed E-Mail address, therefore delegates to the next one in chain.
     */
    NEUTRAL,
    /**
     * The {@link MailResolver} successfully handled passed E-Mail address.
     */
    ACCEPT;

}
