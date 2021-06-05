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
 * {@link ResolvedMail}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 */
public class ResolvedMail {

    /**
     * Creates a new {@link ResolvedMail} instance indicating {@link ResolveReply#DENY DENY} result.
     *
     * @return A new {@link ResolvedMail} instance indicating {@link ResolveReply#DENY DENY} result
     */
    public static ResolvedMail DENY() {
        return new ResolvedMail(-1, -1, ResolveReply.DENY);
    }

    /**
     * Creates a new {@link ResolvedMail} instance indicating {@link ResolveReply#NEUTRAL NEUTRAL} result.
     *
     * @return A new {@link ResolvedMail} instance indicating {@link ResolveReply#NEUTRAL NEUTRAL} result
     */
    public static ResolvedMail NEUTRAL() {
        return new ResolvedMail(-1, -1, ResolveReply.NEUTRAL);
    }

    /**
     * Creates a new {@link ResolvedMail} instance for given user/context identifier; hence indicating {@link ResolveReply#ACCEPT ACCEPT} result.
     *
     * @param userID The user identifier
     * @param contextID The context identifier
     * @return A new {@link ResolvedMail} instance for given user/context identifier; hence indicating {@link ResolveReply#ACCEPT ACCEPT} result
     */
    public static ResolvedMail ACCEPT(int userID, int contextID) {
        return new ResolvedMail(userID, contextID, ResolveReply.ACCEPT);
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    private final ResolveReply resolveReply;
    private final int userID;
    private final int contextID;

    /**
     * Initializes a new {@link ResolvedMail}.
     *
     * @param userID The user identifier
     * @param contextID The context identifier
     */
    public ResolvedMail(int userID, int contextID) {
        this(userID, contextID, ResolveReply.ACCEPT);
    }

    /**
     * Initializes a new {@link ResolvedMail}.
     *
     * @param userID The user identifier
     * @param contextID The context identifier
     * @param resolveReply The resolve reply
     */
    public ResolvedMail(int userID, int contextID, ResolveReply resolveReply) {
        super();
        this.userID = userID;
        this.contextID = contextID;
        this.resolveReply = null == resolveReply ? ResolveReply.ACCEPT : resolveReply;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier or <code>-1</code> if unknown
     *         (typically alongside with resolve type set to {@link ResolveReply#NEUTRAL} or {@link ResolveReply#DENY})
     */
    public int getUserID() {
        return userID;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier or <code>-1</code> if unknown
     *         (typically alongside with resolve type set to {@link ResolveReply#NEUTRAL} or {@link ResolveReply#DENY})
     */
    public int getContextID() {
        return contextID;
    }

    /**
     * Gets the resolve reply
     * <ul>
     * <li>DENY - The {@code MailResolver} denies further processing of passed E-Mail address.
     * <li>NEUTRAL - The {@code MailResolver} cannot handle passed E-Mail address, therefore delegates to the next one in chain.
     * <li>ACCEPT - The {@code MailResolver} successfully handled passed E-Mail address.
     * </ul>
     *
     * @return The resolve reply
     */
    public ResolveReply getResolveReply() {
        return resolveReply;
    }

}
