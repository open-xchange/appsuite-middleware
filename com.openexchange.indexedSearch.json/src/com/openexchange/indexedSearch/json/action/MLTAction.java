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
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandlers;
import com.openexchange.indexedSearch.json.IndexAJAXRequest;
import com.openexchange.indexedSearch.json.ResultConverters;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link MLTAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MLTAction extends AbstractIndexAction {
    
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MLTAction.class);

    /**
     * Initializes a new {@link MLTAction}.
     * @param services
     * @param registry
     */
    public MLTAction(ServiceLookup services, ResultConverters registry) {
        super(services, registry);
    }

    @Override
    protected AJAXRequestResult perform(IndexAJAXRequest req) throws OXException, JSONException {
        long start = System.currentTimeMillis();
        ServerSession session = req.getSession();
//        int module = req.checkInt("module");
        String id = req.checkParameter("id");
        
        IndexFacadeService indexFacade = getService(IndexFacadeService.class);
        IndexAccess<MailMessage> indexAccess = indexFacade.acquireIndexAccess(Types.EMAIL, session);
        QueryParameters params = new QueryParameters.Builder()
            .setHandler(SearchHandlers.namedHandler("mlt"))
            .setSearchTerm(id)
            .setLength(10)
            .build();
        
        IndexResult<MailMessage> result = indexAccess.query(params, null);
        JSONObject responseObject = new JSONObject();
        responseObject.put("numFound", result.getNumFound());
        
        List<IndexDocument<MailMessage>> documents = result.getResults();
        JSONArray docs = new JSONArray();
        for (IndexDocument<MailMessage> document : documents) {
            MailMessage mailMessage = document.getObject();
            int accountId = mailMessage.getAccountId();
            String folder = mailMessage.getFolder();
            String mailId = mailMessage.getMailId();
            InternetAddress[] from = mailMessage.getFrom();
            InternetAddress[] to = mailMessage.getTo();
            String subject = mailMessage.getSubject();
            
            JSONObject json = new JSONObject();
            json.put("indexId", document.getDocumentId());
            
            if (accountId >= 0) {
                json.put("account", accountId);
            } else {
                json.put("account", JSONObject.NULL);
            }
            
            json.put("folder", folder);
            json.put("mailId", mailId);
            json.put("subject", subject);
            
            if (from == null) {
                json.put("from", new JSONArray());
            } else {
                JSONArray addrs = new JSONArray();
                for (InternetAddress addr : from) {
                    String personal = addr.getPersonal();
                    String address = addr.getAddress();
                    String value;
                    if (personal == null) {
                        value = address;
                    } else {
                        value = personal + " <" + address + ">";
                    }
                    
                    addrs.put(value);
                }
                
                json.put("from", addrs);
            }
            
            if (to == null) {
                json.put("to", new JSONArray());
            } else {
                JSONArray addrs = new JSONArray();
                for (InternetAddress addr : to) {
                    String personal = addr.getPersonal();
                    String address = addr.getAddress();
                    String value;
                    if (personal == null) {
                        value = address;
                    } else {
                        value = personal + " <" + address + ">";
                    }
                    
                    addrs.put(value);
                }
                
                json.put("to", addrs);
            }
            
            docs.put(json);
        }
        responseObject.put("docs", docs);

        long diff = System.currentTimeMillis() - start;
        LOG.warn("Duration: {}ms.", diff);
        
        JSONObject resultObject = new JSONObject();
        JSONObject responseHeader = new JSONObject();
        responseHeader.put("QTime", diff);
        resultObject.put("responseHeader", responseHeader);
        resultObject.put("response", responseObject);
        return new AJAXRequestResult(resultObject, "apiResponse");
    }

    @Override
    public String getAction() {
        return "mlt";
    }

}
