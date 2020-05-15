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

package com.openexchange.authentication.application.impl.api;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.authentication.application.AppPasswordService;
import com.openexchange.authentication.application.ajax.RestrictedAction;
import com.openexchange.authentication.application.exceptions.AppPasswordExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractAppPasswordAction}
 * Abstract ApplicationPassword action
 * Does security checks/requirements before performing action
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
@RestrictedAction(module = RestrictedAction.REQUIRES_FULL_AUTH)
public abstract class AbstractAppPasswordAction implements AJAXActionService {

    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractAppPasswordAction}.
     * 
     * @param services The service lookup
     */
    public AbstractAppPasswordAction(ServiceLookup services) {
        this.services = services;
    }

    /**
     * Gets the service from registry
     *
     * @return AppPasswordService
     * @throws OXException if the service is absent
     */
    protected AppPasswordService getService() throws OXException {
        return services.getServiceSafe(AppPasswordService.class);
    }

    /**
     * Verify the login has sufficient permission
     * Will not allow an already restricted session to change passwords
     *
     * @param session The seission
     * @throws OXException if the specified session is not authorised
     */
    protected static void checkPermission(Session session) throws OXException {
        if (session.containsParameter(Session.PARAM_RESTRICTED)) {
            throw AppPasswordExceptionCodes.NOT_AUTHORIZED.create();
        }
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        checkPermission(session);
        return doPerform(requestData, session);
    }

    /**
     * Perform the action after security check was done
     *
     * @param requestData The {@link AJAXRequestData}x
     * @param session The session
     * @return The {@link AJAXRequestResult}
     * @throws OXException if an error is occurred
     */
    protected abstract AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException;

}
