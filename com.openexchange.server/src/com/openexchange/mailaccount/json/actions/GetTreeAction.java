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

package com.openexchange.mailaccount.json.actions;

import org.json.JSONException;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetTreeAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GetTreeAction extends AbstractMailAccountTreeAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GetTreeAction.class);

    public static final String ACTION = "get_tree";

    /**
     * Initializes a new {@link GetTreeAction}.
     */
    public GetTreeAction() {
        super();
    }

    @Override
    protected AJAXRequestResult innerPerform(final AJAXRequestData requestData, final ServerSession session, final JSONValue jVoid) throws OXException, JSONException {
        final int id = parseIntParameter(AJAXServlet.PARAMETER_ID, requestData);

        final MailAccountStorageService storageService =
            ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);

        final MailAccount mailAccount = storageService.getMailAccount(id, session.getUserId(), session.getContextId());

        if (isUnifiedINBOXAccount(mailAccount)) {
            // Treat as no hit
            throw MailAccountExceptionCodes.NOT_FOUND.create(
                Integer.valueOf(id),
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(session.getContextId()));
        }

        if (!session.getUserPermissionBits().isMultipleMailAccounts() && !isDefaultMailAccount(mailAccount)) {
            throw
                MailAccountExceptionCodes.NOT_ENABLED.create(
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(session.getContextId()));
        }

        // Create a mail access instance
        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, mailAccount.getId());
        return new AJAXRequestResult(actionValidateTree0(mailAccess, session));
    }

}
