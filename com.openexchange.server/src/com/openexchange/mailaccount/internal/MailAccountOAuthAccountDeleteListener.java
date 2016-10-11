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

package com.openexchange.mailaccount.internal;

import static com.openexchange.ajax.requesthandler.AJAXRequestDataBuilder.request;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.DispatcherServlet;
import com.openexchange.ajax.requesthandler.Dispatchers;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptions;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * {@link MailAccountOAuthAccountDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class MailAccountOAuthAccountDeleteListener implements OAuthAccountDeleteListener {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(MailAccountOAuthAccountDeleteListener.class);

    private static final String DEFAULT_ID = Integer.toString(MailAccount.DEFAULT_ID);

    /**
     * Initializes a new {@link MailAccountOAuthAccountDeleteListener}.
     */
    public MailAccountOAuthAccountDeleteListener() {
        super();
    }

    @Override
    public void onBeforeOAuthAccountDeletion(int id, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException {
        // Ignore
    }

    @Override
    public void onAfterOAuthAccountDeletion(int id, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException {
        MailAccountStorageService mass = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
        if (null != mass) {
            MailAccount[] userMailAccounts = mass.getUserMailAccounts(user, cid, con);
            for (MailAccount mailAccount : userMailAccounts) {
                if (false == mailAccount.isDefaultAccount() && false == isUnifiedINBOXAccount(mailAccount)) {
                    boolean deleted = false;
                    if (mailAccount.isMailOAuthAble()) {
                        if (mailAccount.getMailOAuthId() == id) {
                            mass.deleteMailAccount(mailAccount.getId(), Collections.<String, Object> emptyMap(), user, cid, false, con);
                            deleted = true;
                        }
                    }
                    if (!deleted && mailAccount.isTransportOAuthAble()) {
                        if (mailAccount.getTransportOAuthId() == id) {
                            mass.deleteTransportAccount(mailAccount.getId(), user, cid, con);
                        }
                    }
                }
            }
        }

        Dispatcher ox = DispatcherServlet.getDispatcher();
        if (null == ox) {
            return;
        }

        ServerSession session = ServerSessionAdapter.valueOf(new FakeSession(null, user, cid));

        Object result;
        {
            String columns = Attribute.ID_LITERAL.getId() + "," + Attribute.MAIL_URL_LITERAL.getId() + "," + Attribute.MAIL_OAUTH_LITERAL.getId() + "," + Attribute.TRANSPORT_OAUTH_LITERAL.getId();
            AJAXRequestData requestData = request().session(session).module("account").action("all").format("json").params("columns", columns).build();
            result = perform(requestData, session, ox);
        }

        if (null != result) {
            try {
                JSONArray jAccounts = (JSONArray) result;
                List<String> toDelete = new ArrayList<>(jAccounts.length());
                for (int i = 0, k = jAccounts.length(); k-- > 0; i++) {
                    JSONArray jAccount = jAccounts.getJSONArray(i);
                    String accountId = jAccount.getString(0);
                    if (false == DEFAULT_ID.equals(accountId)) {
                        String url = jAccount.getString(1);
                        if (!url.startsWith(UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX)) {
                            int mailOAuthId = jAccount.getInt(2);
                            boolean deleted = false;
                            if (mailOAuthId == id) {
                                toDelete.add(accountId);
                                deleted = true;
                            }

                            int transportOAuthId = jAccount.getInt(4);
                            if (!deleted && transportOAuthId == id) {
                                toDelete.add(accountId);
                            }
                        }
                    }
                }

                if (false == toDelete.isEmpty()) {
                    JSONArray jIds = new JSONArray(toDelete.size());
                    for (String accId : toDelete) {
                        jIds.put(accId);
                    }
                    AJAXRequestData requestData = request().session(session).module("account").action("delete").format("json").data(jIds, "json").build();
                    perform(requestData, session, ox);
                }
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        }
    }

    private static <V> V perform(AJAXRequestData requestData, ServerSession session, Dispatcher ox) {
        try {
            Dispatchers.perform(requestData, session, ox);
        } catch (OXException e) {
            if (OXExceptions.isCategory(Category.CATEGORY_PERMISSION_DENIED, e)) {
                LOG.debug("Permission error", e);
            } else {
                LOG.error("Error", e);
            }
        }

        return null;
    }

    private static boolean isUnifiedINBOXAccount(final MailAccount mailAccount) {
        return isUnifiedINBOXAccount(mailAccount.getMailProtocol());
    }

    private static boolean isUnifiedINBOXAccount(final String mailProtocol) {
        return UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(mailProtocol);
    }

}
