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

package com.openexchange.indexedSearch.json.action;

import java.util.List;
import javax.mail.internet.InternetAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.indexedSearch.json.IndexAJAXRequest;
import com.openexchange.indexedSearch.json.ResultConverters;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link MailSearchAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public abstract class MailSearchAction extends AbstractIndexAction {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailSearchAction.class);

    private final SearchHandler searchHandler;

    private final String action;

    /**
     * Initializes a new {@link MailSearchAction}.
     * @param services
     * @param registry
     */
    protected MailSearchAction(String action, ServiceLookup services, ResultConverters registry, SearchHandler searchHandler) {
        super(services, registry);
        this.searchHandler = searchHandler;
        this.action = action;
    }

    @Override
    protected AJAXRequestResult perform(IndexAJAXRequest req) throws OXException, JSONException {
        ServerSession session = req.getSession();
        String searchTerm = req.checkParameter("searchTerm");

        IndexFacadeService indexFacade = getService(IndexFacadeService.class);
        IndexAccess<MailMessage> indexAccess = indexFacade.acquireIndexAccess(Types.EMAIL, session);
        QueryParameters params = new QueryParameters.Builder()
            .setHandler(searchHandler)
            .setSearchTerm(searchTerm)
            .build();

        IndexResult<MailMessage> result = indexAccess.query(params, null);
        List<IndexDocument<MailMessage>> documents = result.getResults();
        JSONArray jsonResult = new JSONArray();
        for (IndexDocument<MailMessage> document : documents) {
            MailMessage mail = document.getObject();
            JSONObject json = new JSONObject();
            int accountId = mail.getAccountId();
            String folder = mail.getFolder();
            String fullname = MailFolderUtility.prepareFullname(accountId, folder);
            json.put(FolderChildFields.FOLDER_ID, fullname == null ? JSONObject.NULL : fullname);

            String id = mail.getMailId();
            json.put(DataFields.ID, id == null ? JSONObject.NULL : id);

            addAddresses(mail.getFrom(), MailJSONField.FROM, json);
            addAddresses(mail.getTo(), MailJSONField.RECIPIENT_TO, json);
            addAddresses(mail.getCc(), MailJSONField.RECIPIENT_CC, json);
            addAddresses(mail.getBcc(), MailJSONField.RECIPIENT_BCC, json);

            String subject = mail.getSubject();
            json.put(MailJSONField.SUBJECT.getKey(), subject == null ? JSONObject.NULL : subject);
            jsonResult.put(json);
        }

        return new AJAXRequestResult(jsonResult, "json");
    }

    private void addAddresses(InternetAddress[] addrs, MailJSONField field, JSONObject json) throws JSONException {
        if (addrs == null || addrs.length == 0) {
            return;
        }

        json.put(field.getKey(), MessageWriter.getAddressesAsArray(addrs));
    }

    @Override
    public String getAction() {
        return action;
    }

}
