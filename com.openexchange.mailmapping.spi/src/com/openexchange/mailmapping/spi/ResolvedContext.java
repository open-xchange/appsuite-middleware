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

package com.openexchange.mailmapping.spi;

import com.openexchange.mailmapping.ResolveReply;
import com.openexchange.mailmapping.ResolvedMail;

/**
 * {@link ResolvedContext}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ResolvedContext {

    private final ResolveReply resolveReply;
    private final int contextID;

    /**
     * Initializes a new {@link ResolvedMail}.
     *
     * @param contextID The context identifier
     */
    public ResolvedContext(int contextID) {
        this(contextID, ResolveReply.ACCEPT);
    }

    /**
     * Initializes a new {@link ResolvedMail}.
     *
     * @param contextID The context identifier
     * @param resolveReply The resolve reply
     */
    public ResolvedContext(int contextID, ResolveReply resolveReply) {
        super();
        this.contextID = contextID;
        this.resolveReply = null == resolveReply ? ResolveReply.ACCEPT : resolveReply;
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
