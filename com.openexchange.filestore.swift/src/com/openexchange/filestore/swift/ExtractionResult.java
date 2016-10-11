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

package com.openexchange.filestore.swift;

/**
 * {@link ExtractionResult} - The result for extracting association/path information from Swift URI.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ExtractionResult {

    private final int contextId;
    private final int userId;
    private final Association association;
    private final String prefix;

    /**
     * Initializes a new {@link ExtractionResult}.
     */
    public ExtractionResult(int userId, int contextId) {
        super();
        StringBuilder sb = new StringBuilder(32).append(contextId).append("ctx");
        if (userId > 0) {
            sb.append(userId).append("user");
        }
        sb.append("store");
        prefix = sb.toString();
        this.userId = userId;
        this.contextId = contextId;
        this.association = Association.CONTEXT_AND_USER;
    }

    /**
     * Initializes a new {@link ExtractionResult}.
     */
    public ExtractionResult(String prefix) {
        super();
        this.prefix = prefix;
        contextId = -1;
        userId = -1;
        this.association = Association.CUSTOM;
    }

    /**
     * Checks if this extraction result has a context/user association
     *
     * @return <code>true</code> for context/user association; otherwise <code>false</code>
     */
    public boolean hasContextUserAssociation() {
        return Association.CONTEXT_AND_USER == association;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier or <code>-1</code> if not set
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier or <code>-1</code> if not set
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the association
     *
     * @return The association
     */
    public Association getAssociation() {
        return association;
    }

    /**
     * Gets the prefix
     *
     * @return The prefix
     */
    public String getPrefix() {
        return prefix;
    }

}
