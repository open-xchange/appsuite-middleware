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

package com.openexchange.indexedSearch.json.converter;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexDocument;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.mail.json.writer.MessageWriter.MailFieldWriter;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SearchResult2JSONConverter}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SearchResult2JSONConverter implements ResultConverter {
    
    private static MailListField[] DEFAULT_MAIL_FIELDS = new MailListField[] 
        {
        MailListField.ID,
        MailListField.ACCOUNT_ID,
        MailListField.FOLDER_ID,
        MailListField.FROM,
        MailListField.TO,
        MailListField.CC,
        MailListField.BCC,
        MailListField.SUBJECT,
        MailListField.SIZE,
        MailListField.SENT_DATE,
        MailListField.RECEIVED_DATE,
        MailListField.ATTACHMENT,
        MailListField.FLAGS,
        MailListField.COLOR_LABEL
    };

    @Override
    public String getInputFormat() {
        return "searchResult";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        Object resultObject = result.getResultObject();
        if (resultObject == null) {
            result.setResultObject(JSONObject.NULL, "json");
            return;
        }
        
        SearchResult searchResult = (SearchResult) resultObject;
        OXJSONWriter resultWriter = new OXJSONWriter();
        try {
            resultWriter.object();
            resultWriter.key("numFound");
            resultWriter.value(searchResult.getNumFound());
            resultWriter.key("duration");
            resultWriter.value(searchResult.getDuration());
            
            JSONObject documentsJSON = new JSONObject();
            List<IndexDocument<MailMessage>> mailDocuments = searchResult.getMailResults();
            if (mailDocuments != null) {
                MailListField[] fields;
                int[] requestedFields = searchResult.getMailFields();
                if (requestedFields == null) {
                    fields = DEFAULT_MAIL_FIELDS;
                } else {
                    fields = MailListField.getFields(requestedFields);
                }
                
                JSONArray mailJSON = convertMails(session, mailDocuments, fields);
                documentsJSON.put("mail", mailJSON);
            }
            
            resultWriter.key("documents");
            resultWriter.value(documentsJSON);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } finally {
            try {
                resultWriter.endObject();
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        }
        
        result.setResultObject(resultWriter.getObject(), "json");
    }

    private JSONArray convertMails(ServerSession session, List<IndexDocument<MailMessage>> mails, MailListField[] fields) throws JSONException, OXException {
        if (mails.isEmpty()) {
            return new JSONArray();
        }
        
        MailFieldWriter[] writers = MessageWriter.getMailFieldWriter(fields);
        OXJSONWriter jsonWriter = new OXJSONWriter();
        jsonWriter.array();
        try {
            int userId = session.getUserId();
            int contextId = session.getContextId();
            for (IndexDocument<MailMessage> mailDocument : mails) {
                MailMessage mail = mailDocument.getObject();
                if (mail != null) {
                    JSONObject documentJSON = new JSONObject();
                    documentJSON.put("documentId", mailDocument.getDocumentId());
                    
                    JSONObject mailJSON = new JSONObject();
                    int accountID = mail.getAccountId();
                    for (int j = 0; j < writers.length; j++) {
                        writers[j].writeField(mailJSON, mail, 0, true, accountID, userId, contextId);
                    }
                    documentJSON.put("data", mailJSON);
                    jsonWriter.value(documentJSON);
                }
            }
        } finally {
            jsonWriter.endArray();
        }
        
        return (JSONArray) jsonWriter.getObject();
    }

}
