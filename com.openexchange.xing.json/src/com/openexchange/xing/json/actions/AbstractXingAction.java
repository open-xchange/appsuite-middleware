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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.xing.json.actions;

import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.xing.access.XingExceptionCodes;
import com.openexchange.xing.access.XingOAuthAccess;
import com.openexchange.xing.access.XingOAuthAccessProvider;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.exception.XingUnlinkedException;
import com.openexchange.xing.json.XingRequest;

/**
 * {@link AbstractXingAction} - The abstract XING action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractXingAction implements AJAXActionService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractXingAction}.
     */
    protected AbstractXingAction(final ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the service of specified type
     *
     * @param clazz The service's class
     * @return The service or <code>null</code> if absent
     */
    protected <S> S getService(final Class<? extends S> clazz) {
        return services.getOptionalService(clazz);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        if (checkCapability() && !hasXingCapability(session)) {
            throw OXException.noPermissionForModule("xing");
        }
        try {
            return perform(new XingRequest(requestData, session));
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final XingUnlinkedException e) {
            throw XingExceptionCodes.UNLINKED_ERROR.create();
        } catch (final XingException e) {
            throw XingExceptionCodes.XING_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw XingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private boolean hasXingCapability(final ServerSession session) throws OXException {
        final CapabilityService capabilityService = getService(CapabilityService.class);
        return (null != capabilityService && capabilityService.getCapabilities(session).contains("xing"));
    }

    /**
     * Checks if capability should be checked prior to serving action.
     *
     * @return <code>true</code> to check; otherwise <code>false</code>
     */
    protected boolean checkCapability() {
        return true;
    }

    /**
     * Gets the XING OAuth access.
     *
     * @param req The associated XING request
     * @return The XING OAuth access
     * @throws OXException If XING OAuth access cannot be returned
     */
    protected XingOAuthAccess getXingOAuthAccess(final XingRequest req) throws OXException {
        final XingOAuthAccessProvider provider = services.getService(XingOAuthAccessProvider.class);
        if (null == provider) {
            throw ServiceExceptionCode.absentService(XingOAuthAccessProvider.class);
        }

        final ServerSession session = req.getSession();
        final int xingOAuthAccount = provider.getXingOAuthAccount(session);
        return provider.accessFor(xingOAuthAccount, session);
    }

    /**
     * Performs specified XING request.
     *
     * @param req The XING request
     * @return The result
     * @throws OXException If an error occurs
     * @throws JSONException If a JSON error occurs
     * @throws XingException If XING API error occurs
     */
    protected abstract AJAXRequestResult perform(XingRequest req) throws OXException, JSONException, XingException;

}
