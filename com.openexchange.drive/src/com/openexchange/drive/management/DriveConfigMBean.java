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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.drive.management;

import java.util.Set;

/**
 * {@link DriveConfigMBean}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface DriveConfigMBean {

    /**
     * Enables or disables diagnostics logging at <code>INFO</code>-level for all users.
     *
     * @param diagnostics <code>true</code> to enable the diagnostics log, <code>false</code> to disable it
     */
    void setDiagnostics(boolean diagnostics);

    /**
     * Gets a value indicating whether diagnostic logging for all users is enabled or not.
     *
     * @return <code>true</code> if diagnostics logging is enabled, <code>false</code>, otherwise
     */
    boolean getDiagnostics();

    /**
     * Gets a list of users where diagnostics logging is enabled.
     *
     * @return The users
     */
    Set<String> getDiagnosticsUsers();

    /**
     * Adds a user for for diagnostics logging.
     *
     * @param user The user in the format <code>username@context</code> or <code>userId@contextId</code>
     * @return <code>true</code> if the user was added successfully, <code>false</code>, otherwise
     */
    boolean addDiagnosticsUser(String user);

    /**
     * Removes a user from diagnostics logging
     *
     * @param user The user to remove
     * @return <code>true</code> if the user was removed successfully, <code>false</code>, otherwise
     */
    boolean removeDiagnosticsUser(String user);

}
