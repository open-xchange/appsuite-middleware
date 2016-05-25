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

import com.openexchange.exception.OXException;

/**
 * {@link NonAbortingDispatcherListener} - A dispatcher listener that is not supposed to abort disptacher processing.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public abstract class NonAbortingDispatcherListener implements DispatcherListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(NonAbortingDispatcherListener.class);

    /**
     * Initializes a new {@link NonAbortingDispatcherListener}.
     */
    protected NonAbortingDispatcherListener() {
        super();
    }

    /**
     * Called when a request is about being performed.
     *
     * @param requestData The associated request data
     * @throws OXException If this listener signals to abort further processing
     */
    @Override
    public void onRequestInitialized(AJAXRequestData requestData) {
        try {
            doOnRequestInitialized(requestData);
        } catch (Exception x) {
            LOG.error("Failed to execute dispatcher listener {}", this.getClass().getSimpleName(), x);
        }
    }

    /**
     * Called when a result was supposed to be created, but not yet returned to requesting client (by responsible {@link ResponseRenderer renderer}).
     *
     * @param requestData The associated request data
     * @param requestResult The request result that has been created or <code>null</code> if creation failed (in that case an exception is passed)
     * @param e The exception that caused termination, or <code>null</code> if execution completed normally (and a viable <code>requestResult</code> is given)
     * @throws OXException If this listener signals to abort further processing
     */
    @Override
    public void onRequestPerformed(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e) {
        try {
            doOnRequestPerformed(requestData, requestResult, e);
        } catch (Exception x) {
            LOG.error("Failed to execute dispatcher listener {}", this.getClass().getSimpleName(), x);
        }
    }

    /**
     * Called when a request is about being performed.
     *
     * @param requestData The associated request data
     * @throws Exception If the "request initialized" call-back cannot be successfully handled
     */
    protected abstract void doOnRequestInitialized(AJAXRequestData requestData) throws Exception;

    /**
     * Called when a result was supposed to be created, but not yet returned to requesting client (by responsible {@link ResponseRenderer renderer}).
     *
     * @param requestData The associated request data
     * @param requestResult The request result that has been created or <code>null</code> if creation failed (in that case an exception is passed)
     * @param e The exception that caused termination, or <code>null</code> if execution completed normally (and a viable <code>requestResult</code> is given)
     * @throws Exception If the "request performed" call-back cannot be successfully handled
     */
    protected abstract void doOnRequestPerformed(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e) throws Exception;

}
