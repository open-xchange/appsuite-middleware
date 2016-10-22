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

package com.openexchange.ajax.requesthandler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * {@link DispatcherNotes} - The action annotation provides the default format for an {@link AJAXActionService}.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DispatcherNotes {

    /**
     * Gets the default format.
     *
     * @return The default format
     */
    String defaultFormat() default "apiResponse";

    /**
     * Indicates whether this action allows falling back to the public session cookie for session retrieval. This is useful
     * if you don't want varying URLs between sessions. The trade-off is less stability for your requests in problematic infrastructures.
     * @return Whether to allow access using the fallback session or not
     */
    boolean allowPublicSession() default false;

    /**
     * Indicates whether this action allows authentication via public session identifier.
     * @return Whether to allow authentication via public session identifier or not
     */
    boolean publicSessionAuth() default false;

    /**
     * Indicates that this action may be called without a session
     * @return whether to allow access to this action without a session
     */
	boolean noSession() default false;

	/**
     * Indicates whether this action is allowed to miss the associated secret cookie, because it is meant as a callback.
     * @return Whether to allow access without secret
     */
	boolean noSecretCallback() default false;

	/**
     * Indicates whether this action prefers reading/parsing request body stream by itself.
     * @return Whether to prefer reading/parsing request body stream by itself
     */
    boolean preferStream() default false;
}
