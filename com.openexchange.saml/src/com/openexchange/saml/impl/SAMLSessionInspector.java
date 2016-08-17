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

package com.openexchange.saml.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.saml.SAMLSessionParameters;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;
import com.openexchange.session.inspector.Reason;
import com.openexchange.session.inspector.SessionInspectorService;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;


/**
 * Terminates SAML sessions that exceed a timeout defined by the IdP.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SAMLSessionInspector implements SessionInspectorService {

    private static final Logger LOG = LoggerFactory.getLogger(SAMLSessionInspector.class);

    private final SessiondService sessiondService;

    public SAMLSessionInspector(SessiondService sessiondService) {
        super();
        this.sessiondService = sessiondService;
    }

    @Override
    public Reply onSessionHit(Session session, HttpServletRequest request, HttpServletResponse response) throws OXException {
        Object parameter = session.getParameter(SAMLSessionParameters.SESSION_NOT_ON_OR_AFTER);
        if (parameter != null && parameter instanceof String) {
            try {
                long notOnOrAfter = Long.parseLong((String) parameter);
                if (System.currentTimeMillis() >= notOnOrAfter) {
                    sessiondService.removeSession(session.getSessionID());
                    throw SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID());
                }
            } catch (NumberFormatException e) {
                LOG.warn("Session contained parameter '{}' but its value was not a valid timestamp: {}", SAMLSessionParameters.SESSION_NOT_ON_OR_AFTER, parameter);
            }
        }

        return Reply.CONTINUE;
    }

    @Override
    public Reply onSessionMiss(String sessionId, HttpServletRequest request, HttpServletResponse response) throws OXException {
        return Reply.CONTINUE;
    }

    @Override
    public Reply onAutoLoginFailed(Reason reason, HttpServletRequest request, HttpServletResponse response) throws OXException {
        return Reply.CONTINUE;
    }

}
