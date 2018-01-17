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

package com.openexchange.multifactor.listener;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ActionBoundDispatcherListener;
import com.openexchange.exception.OXException;
import com.openexchange.login.multifactor.MultifactorLoginService;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MultifactorDispatcherListener}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class MultifactorDispatcherListener extends ActionBoundDispatcherListener {

    private final MultifactorLoginService mfService;
    private final String                  module;
    private final String                  action;

    /**
     * Initializes a new {@link MultifactorDispatcherListener}.
     *
     * @param serviceLookup a {@link ServiceLookup}
     */
    public MultifactorDispatcherListener(MultifactorLoginService mfService, String module, String action) {
        this.module = Objects.requireNonNull(module, "Module must not be null");
        this.mfService = Objects.requireNonNull(mfService, "Multifactor Login Service not available");
        this.action = action;
    }

    /**
     * Check if multifactor authentication has been done during this current session and since reload
     *
     * @param requestData The {@link AJAXRequestData}
     * @throws OXException
     */
    public void checkMultifactor(AJAXRequestData requestData) throws OXException {
        mfService.checkRecentMultifactorAuthentication(requestData.getSession());
    }

    @Override
    public void onRequestInitialized(AJAXRequestData requestData) throws OXException {
        checkMultifactor(requestData);
    }

    @Override
    public void onRequestPerformed(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e) {
        /* no-op */
    }

    @Override
    public void onResultReturned(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e) {
        /* no-op */
    }

    @Override
    public Set<String> getActions() {
        if (action == null || action.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<String>(Arrays.asList(action));
    }

    @Override
    public String getModule() {
        return module;
    }
}
