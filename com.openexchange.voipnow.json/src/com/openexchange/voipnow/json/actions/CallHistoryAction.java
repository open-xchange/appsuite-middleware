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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.voipnow.json.actions;

import java.util.Map;
import java.util.Set;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.voipnow.json.VoipNowExceptionCodes;

/**
 * {@link CallHistoryAction} - Maps the action to a <tt>callhistory</tt> action.
 * <p>
 * A new call is initiated using VoipNow's HTTP API.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CallHistoryAction extends AbstractVoipNowAction {

    /**
     * The <tt>call</tt> action string.
     */
    public static final String ACTION = "callhistory";

    /**
     * Initializes a new {@link CallHistoryAction}.
     */
    public CallHistoryAction() {
        super();
    }

    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws AbstractOXException {
        try {
            /*
             * Parse parameters
             */
            final String receiverNumber = checkStringParameter("phone", request);
            final String receiverDisplayName = checkStringParameter("callerid", request);
            final int timeout = parseIntParameter("timeout", request, 10);
            /*
             * Get main extension
             */
            final String callerNumber;
            {
                final User sessionUser = session.getUser();
                final Map<String, Set<String>> attributes = sessionUser.getAttributes();
                final String attributeName = "mainExtension";
                final Set<String> set = attributes.get(attributeName);
                if (null == set || set.isEmpty()) {
                    throw VoipNowExceptionCodes.MISSING_MAIN_EXTENSION.create(
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()));
                }
                callerNumber = set.iterator().next();
            }
            final VoipNowServerSetting setting = getVoipNowServerSetting(session);
            /*
             * Perform a HTTP GET request using HttpClient
             */
            
            /*
             * Return dummy
             */
            return AJAXRequestResult.EMPTY_REQUEST_RESULT;
        } catch (final Exception e) {
            // TODO: Split cacth clauses
            throw new AjaxException(AjaxException.Code.UnexpectedError, e, e.getMessage());
        }
    }

}
