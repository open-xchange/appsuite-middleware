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

package com.openexchange.eventsystem.provisioning;

/**
 * {@link ProviosioningEventConstants} - Provides constants for provisioning-related events.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ProviosioningEventConstants {

    // ----------------------------------------------------------------------------------------------------- //
    // --------------------------------------    TOPICS    ------------------------------------------------- //
    // ----------------------------------------------------------------------------------------------------- //

    /**
     * The name of the topic to notify about a created context.
     */
    public static final String TOPIC_CONTEXT_CREATE = "com/openexchange/provisioning/context/create";

    /**
     * The name of the topic to notify about a changed/updated context.
     */
    public static final String TOPIC_CONTEXT_UPDATE = "com/openexchange/provisioning/context/update";

    /**
     * The name of the topic to notify about a deleted context.
     */
    public static final String TOPIC_CONTEXT_DELETE = "com/openexchange/provisioning/context/delete";

    // ----------------------------------------------------------------------------------------------------- //

    /**
     * The name of the topic to notify about a created user.
     */
    public static final String TOPIC_USER_CREATE = "com/openexchange/provisioning/user/create";

    /**
     * The name of the topic to notify about a changed/updated user.
     */
    public static final String TOPIC_USER_UPDATE = "com/openexchange/provisioning/user/update";

    /**
     * The name of the topic to notify about a deleted user.
     */
    public static final String TOPIC_USER_DELETE = "com/openexchange/provisioning/user/delete";

    // ----------------------------------------------------------------------------------------------------- //
    // --------------------------------------  PROPERTIES  ------------------------------------------------- //
    // ----------------------------------------------------------------------------------------------------- //

    /**
     * The name of the property providing the context identifier.
     */
    public static final String PROP_CONTEXT_ID = "contextId";

    /**
     * The name of the property providing the user identifier.
     */
    public static final String PROP_USER_ID = "userId";

    /**
     * The name of the property providing the user login name.
     */
    public static final String PROP_USER_NAME = "name";

}
