
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

package com.openexchange.authentication.application.impl;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AbstractAJAXActionAnnotationProcessor;
import com.openexchange.authentication.application.ajax.RestrictedAction;
import com.openexchange.authentication.application.exceptions.AppPasswordExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AppPasswordAnnotationProcessor}
 * Checks session for restricted authentication. If present, verifies scope
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class AppPasswordAnnotationProcessor extends AbstractAJAXActionAnnotationProcessor<RestrictedAction> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AppPasswordAnnotationProcessor.class);

    /**
     * Initializes a new {@link AppPasswordAnnotationProcessor}.
     */
    public AppPasswordAnnotationProcessor() {
        super();
    }

    @Override
    protected Class<RestrictedAction> getAnnotation() {
        return RestrictedAction.class;
    }

    @Override
    protected void doProcess(RestrictedAction annotation, AJAXActionService action, AJAXRequestData requestData, ServerSession session) throws OXException {
        Object restrParam = session.getParameter(Session.PARAM_RESTRICTED);
        if (restrParam == null) {
            return; // Not a restricted session, just return
        }
        RestrictedAction restrAction = action.getClass().getAnnotation(RestrictedAction.class);
        String requiredScope = restrAction.type().val() + restrAction.module();

        // Check if this action requires full auth.  If so, reject now
        if (RestrictedAction.REQUIRES_FULL_AUTH.equals(restrAction.module())) {
            throw AppPasswordExceptionCodes.NOT_AUTHORIZED.create(requiredScope);
        }

        // Return if grant all
        if (RestrictedAction.GRANT_ALL.equals(restrAction.module())) {
            return;
        }

        // Check the scopes of authentication for this session
        if (false == (restrParam instanceof String[])) {
            throw AppPasswordExceptionCodes.APPLICATION_PASSWORD_GENERIC_ERROR.create("Unkown restricted session type");
        }
        List<String> restrictedScopes = Arrays.asList((String[]) restrParam);
        LOG.debug("Restricted session hit for module " + requestData.getModule() + " action:" + requestData.getAction() + " required:" + requiredScope);
        if (RestrictedAction.GRANT_ALL.equals(requiredScope)) {
            return;
        }
        if (!restrictedScopes.contains(requiredScope)) {
            throw AppPasswordExceptionCodes.NOT_AUTHORIZED.create(requiredScope);
        }
    }
}
