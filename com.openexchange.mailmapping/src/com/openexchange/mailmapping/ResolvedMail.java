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
