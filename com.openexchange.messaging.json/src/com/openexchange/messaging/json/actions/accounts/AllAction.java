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

package com.openexchange.messaging.json.actions.accounts;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.tools.session.ServerSession;


/**
 * A class implementing the "all" action for listing messaging accounts. Optionally only accounts of a certain service
 * are returned. Parameters are:
 * <dl>
 *  <dt>messagingService</dt><dd>(optional) The ID of the messaging service. If present lists only accounts of this service.</dd>
 * </dl>
 * Returns a JSONArray of JSONObjects representing the messaging accounts.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "all", description = "Get all messaging accounts", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module.")
}, responseDescription = "A standard response object containing an array of messaging service objects.")
public class AllAction extends AbstractMessagingAccountAction {

    public AllAction(final MessagingServiceRegistry registry) {
        super(registry);
    }

    @Override
    protected AJAXRequestResult doIt(final AJAXRequestData request, final ServerSession session) throws JSONException, OXException {

        final String messagingServiceId = request.getParameter("messagingService");

        final List<MessagingService> services = new ArrayList<MessagingService>();
        if(messagingServiceId != null) {
            services.add(registry.getMessagingService(messagingServiceId, session.getUserId(), session.getContextId()));
        } else {
            services.addAll(registry.getAllServices(session.getUserId(), session.getContextId()));
        }

        final JSONArray result = new JSONArray();

        for (final MessagingService messagingService : services) {
            final boolean isMail = ("com.openexchange.messaging.mail".equals(messagingService.getId()));
            NextAccount: for (final MessagingAccount account : messagingService.getAccountManager().getAccounts(session)) {
                if (isMail && account.getId() == 0) {
                    /*
                     * The primary mail account
                     */
                    continue NextAccount;
                }
                result.put(writer.write(account));
            }
        }

        return new AJAXRequestResult(result);
    }

}
