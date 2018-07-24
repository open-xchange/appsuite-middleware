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

package com.openexchange.mail.dataobjects;

/**
 * {@link MailFilterResult} - Indicates whether a filter was successfully applied to a message.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class MailFilterResult {

    private final String id;
    private final String errors;
    private final String warnings;

    /**
     * Initializes a new {@link MailFilterResult}.
     *
     * @param uid The optional ID or <code>null</code>
     * @param errors The possible errors
     * @param warnings The possible warnings
     */
    public MailFilterResult(String id, String errors, String warnings) {
        super();
        this.id = id;
        this.errors = errors;
        this.warnings = warnings;
    }

    /**
     * Checks if the filter was applied successfully.
     *
     * @return <code>true</code> if filter was applied successfully; otherwise <code>false</code>
     */
    public boolean isOK() {
        return null == errors && null == warnings;
    }

    /**
     * Checks if the filter was applied successfully, but there were one or more warnings produced by the filter.
     *
     * @return <code>true</code> if warnings exist; otherwise <code>false</code>
     */
    public boolean hasWarnings() {
        return null != warnings;
    }

    /**
     * Checks if application of the filter failed for some reason.
     *
     * @return <code>true</code> if filter failed; otherwise <code>false</code>
     */
    public boolean hasErrors() {
        return null != errors;
    }

    /**
     * Gets the ID
     *
     * @return The ID or <code>null</code>
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the a human-readable descriptive text listing the encountered errors
     *
     * @return The errors
     */
    public String getErrors() {
        return errors;
    }

    /**
     * Gets the a human-readable descriptive text listing the produced warnings.
     *
     * @return The warnings
     */
    public String getWarnings() {
        return warnings;
    }

}
