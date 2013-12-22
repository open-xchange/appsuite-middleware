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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.emig.json.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.emig.EmigService;
import com.openexchange.emig.json.EmigRequest;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SenderAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public final class SenderAction extends AbstractEmigAction {

    /**
     * Initializes a new {@link SenderAction}.
     *
     * @param services The service look-up
     */
    public SenderAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final EmigRequest req) throws OXException, JSONException {
        // Acquire services
        final EmigService emigService = getService(EmigService.class);
        if (null == emigService) {
            throw ServiceExceptionCode.absentService(EmigService.class);
        }
        final MailAccountStorageService mass = getService(MailAccountStorageService.class);
        if (null == mass) {
            throw ServiceExceptionCode.absentService(MailAccountStorageService.class);
        }

        // Get JSON body
        final JSONObject jBody = (JSONObject) req.getRequest().getData();
        if (null == jBody) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        final int accountId = jBody.optInt("account", -1);
        if (accountId < 0) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("account");
        }

        final String sender = jBody.optString("sender", null);
        if (null == sender) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("sender");
        }

        final ServerSession session = req.getSession();
        final MailAccount mailAccount = mass.getMailAccount(accountId, session.getUserId(), session.getContextId());

        final boolean emig = emigService.isEMIG_MSA(mailAccount.getTransportServer(), sender, mailAccount.getTransportLogin());

        return new AJAXRequestResult(Boolean.valueOf(emig), "boolean");
    }

}
