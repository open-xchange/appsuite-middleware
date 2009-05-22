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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.event;

import com.openexchange.session.Session;

/**
 * {@link CommonEvent} - Interface for common event distributed by OSGi's event admin.
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public interface CommonEvent {

    /**
     * The event key for common events.
     */
    public static final String EVENT_KEY = "OX_EVENT";

    /**
     * Constant for insert action.
     */
    public static final int INSERT = 1;

    /**
     * Constant for update action.
     */
    public static final int UPDATE = 2;

    /**
     * Constant for delete action.
     */
    public static final int DELETE = 3;

    /**
     * Constant for move action.
     */
    public static final int MOVE = 4;

    /**
     * Constant for confirm-accepted action.
     */
    public static final int CONFIRM_ACCEPTED = 5;

    /**
     * Constant for confirm-declined action.
     */
    public static final int CONFIRM_DECLINED = 6;

    /**
     * Constant for confirm-tentative action.
     */
    public static final int CONFIRM_TENTATIVE = 7;

    /**
     * Gets the context ID.
     * 
     * @return The context ID
     */
    public int getContextId();

    /**
     * Gets the user ID.
     * 
     * @return The user ID
     */
    public int getUserId();

    /**
     * Gets the module.
     * 
     * @return The module
     */
    public int getModule();

    /**
     * Gets the action object.
     * 
     * @return The action object
     */
    public Object getActionObj();

    /**
     * Gets the old object.
     * 
     * @return The old object
     */
    public Object getOldObj();

    /**
     * Gets the source folder object.
     * 
     * @return The source folder object
     */
    public Object getSourceFolder();

    /**
     * Gets the destination folder object.
     * 
     * @return The destination folder object
     */
    public Object getDestinationFolder();

    /**
     * Gets the action.
     * 
     * @return The action
     */
    public int getAction();

    /**
     * Gets the session.
     * 
     * @return he session
     */
    public Session getSession();
}
