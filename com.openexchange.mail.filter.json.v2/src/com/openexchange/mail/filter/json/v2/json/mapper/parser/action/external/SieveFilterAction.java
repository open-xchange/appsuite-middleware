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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.action.external;

import java.util.Hashtable;
import java.util.Set;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SieveFilterAction} Interface for an exterenal sieve plugin filter action
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public interface SieveFilterAction {

    /**
     * Return the name for this action
     *
     * @return The json name of this action
     */
    String getJsonName();

    /**
     * Check that the command is supported. Check against capabilities
     *
     * @param capabilities List of Sieve capabilities
     * @return <code>true</code> if supported, <code>false</code> otherwise
     * @throws OXException
     */
    boolean isCommandSupported(Set<String> capabilities) throws OXException;

    /**
     * Checks whether the given filter {@link TestCommand} is applicable or not
     *
     * @param actionCommand to check
     * @return <code>true</code> if applicable, <code>false</code> otherwise
     * @throws OXException
     */
    boolean isApplicable(ActionCommand actionCommand) throws OXException;

    /**
     * Parse the actionCommand and return the jsonObject with proper name.
     * Should only call if isApplicable returned <code>true</code> for the action command
     *
     * @param jsonObject to populate
     * @param actionCommand the {@link ActionCommand} to parse
     * @throws OXException
     */
    void parse(JSONObject jsonObject, ActionCommand actionCommand) throws OXException;

    /**
     * Checks whether the jsonObject refers to this action
     *
     * @param jsonObject to parse
     * @return <code>true</code> if applicable, <code>false</code> otherwise
     * @throws OXException
     */
    boolean isApplicable(JSONObject jsonObject) throws OXException;

    /**
     * Parses the ActionCommand from the given jsonObject
     *
     * @param jsonObject containing ID and other parameters
     * @param session current session
     * @return The action command
     * @throws OXException
     */
    ActionCommand parse(JSONObject jsonObject, ServerSession session) throws OXException;

    /**
     * Gets the tag arguments for the action
     *
     * @return HashTable of tag arguments
     */
    Hashtable<String, Integer> getTagArgs();

}
