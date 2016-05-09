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

package com.openexchange.ajax.requesthandler;


/**
 * {@link DispatcherListener} - A listener which receives various call-backs during a {@link Dispatcher} processing.
 * <p>
 * The call-backs happen in the following order:
 * <ol>
 * <li>{@link #onRequestInitialized(AJAXRequestData)}<br>Request has been completely parsed. but not yet performed<br>&nbsp;</li>
 * <li>{@link #onRequestPerformed(AJAXRequestData, AJAXRequestResult, Exception)}<br>Either result has been successfully created or an exception is given<br>&nbsp;</li>
 * <li>{@link #onResultReturned(AJAXRequestData, AJAXRequestResult, Exception)}<br>The result is ensured to be successfully created and is about being returned to client. If output to client failed the exception argument in non-<code>null</code></li>
 * </ol>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public interface DispatcherListener {

    /**
     * Checks whether this dispatcher listener wants to receive call-backs for given request data.
     *
     * @param requestData
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     */
    boolean applicable(AJAXRequestData requestData);

    /**
     * Called when a request is about being performed.
     *
     * @param requestData The associated request data
     */
    void onRequestInitialized(AJAXRequestData requestData);

    /**
     * Called when a result was supposed to be created, but not yet returned to requesting client (by responsible {@link ResponseRenderer renderer}).
     *
     * @param requestData The associated request data
     * @param requestResult The request result that has been created or <code>null</code> if creation failed (in that case an exception is passed)
     * @param e The exception that caused termination, or <code>null</code> if execution completed normally (and a viable <code>requestResult</code> is given)
     */
    void onRequestPerformed(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e);

    /**
     * Called when a result has been successfully created and an attempt was made returning it to requesting client (by responsible {@link ResponseRenderer renderer}).
     *
     * @param requestData The associated request data
     * @param requestResult The request result that has been returned
     * @param e The exception that caused termination, or <code>null</code> if execution completed normally
     */
    void onResultReturned(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e);
}
