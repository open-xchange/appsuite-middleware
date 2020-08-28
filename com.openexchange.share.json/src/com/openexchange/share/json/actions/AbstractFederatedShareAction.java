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

package com.openexchange.share.json.actions;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractFederatedShareAction}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public abstract class AbstractFederatedShareAction implements AJAXActionService {

    /**
     * The {@value #LINK} parameter
     */
    protected static final String LINK = "link";

    /**
     * The {@value #SERVICE_ID} parameter
     */
    protected static final String SERVICE_ID = "service";

    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AnalyzeAction}.
     * 
     * @param services The service lookup
     */
    public AbstractFederatedShareAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        String shareLink = requestData.getParameter(LINK);
        if (Strings.isEmpty(shareLink)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(LINK);
        }
        return perform(requestData, session, shareLink);
    }

    /**
     * Performs given request.
     *
     * @param requestData The request to perform
     * @param session The session providing needed user data
     * @param shareLink The share link, never <code>null</code>
     * @return The result yielded for given request
     * @throws OXException If an error occurs
     */
    abstract AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session, String shareLink) throws OXException;

    /**
     * Get the {@value #SERVICE_ID} parameter
     *
     * @param requestData The data to get the parameter from
     * @param The name of the parameter
     * @return The parameter
     * @throws OXException If parameter is unset
     */
    protected String requireParameter(AJAXRequestData requestData, String name) throws OXException {
        String param = requestData.getParameter(name);
        if (Strings.isEmpty(param)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        return param;
    }
}
