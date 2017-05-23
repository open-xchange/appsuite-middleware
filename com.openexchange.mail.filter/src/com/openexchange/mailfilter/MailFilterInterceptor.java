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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.mailfilter;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.Rule;

/**
 * {@link MailFilterInterceptor}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface MailFilterInterceptor {

    /**
     * Returns the rank of this {@link MailFilterInterceptor}. The rank defines the execution
     * order within a chain of multiple {@link MailFilterInterceptor}s.
     * 
     * @return the rank of this {@link MailFilterInterceptor}
     */
    int getRank();

    /**
     * Executes the interception process in all the {@link Rule} objects provided in the {@link List}.
     * 
     * This interception call happens right after the sieve script is read from the sieve server, but BEFORE
     * any processing begins on the middleware side.
     * 
     * @param userId the user identifier
     * @param contextId the context identifier
     * @param rules A {@link List} with {@link Rule} objects to intercept
     * @throws OXException if an error is occurred
     */
    void before(int userId, int contextId, List<Rule> rules) throws OXException;

    /**
     * Executes the interception process in all the {@link Rule} objects provided in the {@link List}.
     * 
     * This interception call happens before writing the sieve script to the sieve server, but AFTER
     * any processing happened on the middleware side.
     * 
     * @param userId the user identifier
     * @param contextId the context identifier
     * @param rules A {@link List} with {@link Rule} objects to intercept
     * @throws OXException if an error is occurred
     */
    void after(int userId, int contextId, List<Rule> rules) throws OXException;
}
