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

package com.openexchange.indexedSearch.json.action;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.indexedSearch.json.IndexAJAXRequest;
import com.openexchange.indexedSearch.json.ResultConverters;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.index.MailIndexField;
import com.openexchange.server.ServiceLookup;


/**
 * {@link PersonsAndTopicsAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class PersonsAndTopicsAction extends AbstractIndexAction {

    /**
     * Initializes a new {@link PersonsAndTopicsAction}.
     * @param services
     * @param registry
     */
    public PersonsAndTopicsAction(ServiceLookup services, ResultConverters registry) {
        super(services, registry);
    }

    @Override
    protected AJAXRequestResult perform(IndexAJAXRequest req) throws OXException, JSONException {
        String searchTerm = req.checkParameter("searchTerm");
        int maxPersons = req.optInt("maxPersons") == IndexAJAXRequest.NOT_FOUND ? 10 : req.optInt("maxPersons");
        int maxTopics = req.optInt("maxTopics") == IndexAJAXRequest.NOT_FOUND ? 10 : req.optInt("maxTopics");
        
        IndexFacadeService indexFacade = getService(IndexFacadeService.class);
        IndexAccess<MailMessage> indexAccess = indexFacade.acquireIndexAccess(Types.EMAIL, req.getSession());
        QueryParameters params = new QueryParameters.Builder()
            .setHandler(SearchHandler.PERSONS_AND_TOPICS)
            .setSearchTerm(searchTerm)
            .setLength(maxPersons + maxTopics)
            .build();
        
        IndexResult<MailMessage> result = indexAccess.query(params, null);
        List<IndexDocument<MailMessage>> documents = result.getResults();
        Set<String> persons = new LinkedHashSet<String>();
        Set<String> topics = new LinkedHashSet<String>();
        for (IndexDocument<MailMessage> document : documents) {
            MailMessage mailMessage = document.getObject();
            Map<IndexField, List<String>> highlighting = document.getHighlighting();
            if (highlighting.containsKey(MailIndexField.FROM)) {
                addInternetAddresses(mailMessage.getFrom(), persons);
            }
            if (highlighting.containsKey(MailIndexField.TO)) {
                addInternetAddresses(mailMessage.getTo(), persons);
            }
            if (highlighting.containsKey(MailIndexField.SUBJECT)) {
                String subject = mailMessage.getSubject();
                if (subject != null) {
                    topics.add(subject);
                }
            }
            if (highlighting.containsKey(MailIndexField.CC)) {
                addInternetAddresses(mailMessage.getCc(), persons);
            }
            if (highlighting.containsKey(MailIndexField.BCC)) {
                addInternetAddresses(mailMessage.getBcc(), persons);
            }
        }
        
        JSONArray personsArray = new JSONArray();
        int i = 0;
        for (String person : persons) {
            if (++i > maxPersons) {
                break;
            }
            
            JSONObject json = new JSONObject();
            json.put("value", person);
            personsArray.put(json);
        }
        
        JSONArray topicsArray = new JSONArray();
        i = 0;
        for (String topic : topics) {
            if (++i > maxTopics) {
                break;
            }
            
            JSONObject json = new JSONObject();
            json.put("value", topic);
            topicsArray.put(json);
        }
        
        JSONObject resultObject = new JSONObject();
        resultObject.put("persons", personsArray);
        resultObject.put("topics", topicsArray);
        
        return new AJAXRequestResult(resultObject, "json");
    }
    
    private static void addInternetAddresses(InternetAddress[] addrs, Set<String> resultSet) {
        if (addrs == null || addrs.length == 0) {
            return;
        }
        
        for (InternetAddress addr : addrs) {
            resultSet.add(addr.toUnicodeString());
        }
    }

    @Override
    public String getAction() {
        return "spotlight";
    }

}
