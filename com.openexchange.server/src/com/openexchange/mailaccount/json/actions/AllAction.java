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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountFacade;
import com.openexchange.mailaccount.json.writer.MailAccountWriter;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AllAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "all", description = "Get all mail accounts", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "columns", description = "A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for mail account's are defined in mail account data.")
}, responseDescription = "An array with attachment data. Each array element describes one mail account and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.")
public final class AllAction extends AbstractMailAccountAction {

    public static final String ACTION = AJAXServlet.ACTION_ALL;

    /**
     * Initializes a new {@link AllAction}.
     */
    public AllAction() {
        super();
    }

    @Override
    protected AJAXRequestResult innerPerform(final AJAXRequestData requestData, final ServerSession session, final JSONValue jVoid) throws OXException {
        final String colString = requestData.getParameter(AJAXServlet.PARAMETER_COLUMNS);

        final List<Attribute> attributes = getColumns(colString);

        final MailAccountFacade mailAccountFacade = getMailAccountFacade();
        MailAccount[] userMailAccounts = mailAccountFacade.getUserMailAccounts(session.getUserId(), session.getContextId());

        final boolean multipleEnabled = session.getUserPermissionBits().isMultipleMailAccounts();
        final List<MailAccount> tmp = new ArrayList<MailAccount>(userMailAccounts.length);

        for (final MailAccount userMailAccount : userMailAccounts) {
            final MailAccount mailAccount = userMailAccount;
            if (!isUnifiedINBOXAccount(mailAccount) && (multipleEnabled || isDefaultMailAccount(mailAccount))) {
                tmp.add(mailAccount);
                // tmp.add(checkFullNames(mailAccount, storageService, session));
            }
        }
        userMailAccounts = tmp.toArray(new MailAccount[tmp.size()]);

        final JSONArray jsonArray = MailAccountWriter.writeArray(userMailAccounts, attributes, session);
        return new AJAXRequestResult(jsonArray);
    }

}
