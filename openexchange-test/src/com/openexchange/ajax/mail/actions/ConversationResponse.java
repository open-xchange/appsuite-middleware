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

package com.openexchange.ajax.mail.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.ThreadSortMailMessage;
import com.openexchange.mail.mime.dataobjects.MimeMailMessage;

/**
 * {@link ConversationResponse}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class ConversationResponse extends AbstractAJAXResponse {

    int[] columns;

    /**
     * Initializes a new {@link ConversationResponse}.
     * 
     * @param response
     */
    protected ConversationResponse(Response response) {
        super(response);
    }

    @SuppressWarnings("unchecked")
    public List<ThreadSortMailMessage> getConversations() throws Exception {
        List<ThreadSortMailMessage> result = new ArrayList<>();
        Object data = this.getData();

        if (data instanceof JSONArray) {

            for (Object jobj : ((JSONArray) data).asList()) {
                ThreadSortMailMessage threadMessage = getMailMessage((Map<String, Object>) jobj);
                result.add(threadMessage);
            }

            return result;
        }

        fail("Unable to parse response data: " + data.toString());
        return result;
    }

    @SuppressWarnings("unchecked")
    public ThreadSortMailMessage getMailMessage(Map<String, Object> messageMap) throws JSONException, AddressException, OXException {
        MimeMailMessage message = new MimeMailMessage();
        ThreadSortMailMessage result = new ThreadSortMailMessage(message);

        for (String key : messageMap.keySet()) {

            if (key.equals("thread")) {
                // handle threads
                List<Object> childMessagesJSON = (List<Object>) messageMap.get(key);
                for (Object subMap : childMessagesJSON) {
                    result.addChildMessage(getMailMessage((Map<String, Object>) subMap));
                }

                continue;
            }

            String strValue = null;
            if (messageMap.get(key) instanceof String) {
                strValue = (String) messageMap.get(key);
            }
            try {
                switch (MailListField.valueOf(key.toUpperCase())) {

                    case FROM:
                        handleInnerArrays((List<List<String>>) messageMap.get(key), message, "from");
                        break;
                    case ACCOUNT_ID:
                        message.setAccountId((int) messageMap.get(key));
                        break;
                    case ACCOUNT_NAME:
                        message.setAccountName(strValue);
                        break;
                    case COLOR_LABEL:
                        message.setColorLabel((int) messageMap.get(key));
                    case DELETED:
                        message.setFlag(MailMessage.FLAG_DELETED, true);
                        break;
                    case DISPOSITION_NOTIFICATION_TO:
                        //ignore
                        break;
                    case FLAGS:
                        message.setFlags((int) messageMap.get(key));
                        break;
                    case FOLDER:
                        message.setFolder(strValue);
                        break;
                    case NEW:
                        // ignore
                        break;
                    case ORIGINAL_FOLDER_ID:
                        message.setOriginalFolder(strValue);
                        break;
                    case ORIGINAL_ID:
                        message.setOriginalId(strValue);
                        break;
                    case PRIORITY:
                        message.setPriority((int) messageMap.get(key));
                        break;
                    case RECEIVED_DATE:
                        message.setReceivedDate(new Date((long) messageMap.get(key)));
                        break;
                    case SENT_DATE:
                        message.setSentDate(new Date((long) messageMap.get(key)));
                        break;
                    case SIZE:
                        message.setSize((int) messageMap.get(key));
                        break;
                    case SUBJECT:
                        message.setSubject(strValue);
                        break;
                    case THREAD_LEVEL:
                        message.setThreadLevel((int) messageMap.get(key));
                        break;
                    case TOTAL:
                        // ignore
                        break;
                    case UNREAD:
                        message.setUnreadMessages((int) messageMap.get(key));
                        break;
                    case ATTACHMENT:
                        // skip attachments for test classes
                        break;
                    case BCC:
                        handleInnerArrays((List<List<String>>) messageMap.get(key), message, "bcc");
                        break;
                    case CC:
                        handleInnerArrays((List<List<String>>) messageMap.get(key), message, "cc");
                        break;
                    case FLAG_SEEN:
                        message.setFlag(MailMessage.FLAG_SEEN, true);
                        break;
                    case FOLDER_ID:
                        message.setFolder(strValue);
                        break;
                    case ID:
                        message.setMailId(strValue);
                        break;
                    case MIME_TYPE:
                        message.setContentType(strValue);
                        break;
                    case MSG_REF:
                        // ignore
                        break;
                    case TO:
                        handleInnerArrays((List<List<String>>) messageMap.get(key), message, "to");
                        break;
                    default:
                        break;

                }
            } catch (IllegalArgumentException e) {
                // ignore this field
            }

        }
        return result;
    }

    private void handleInnerArrays(List<List<String>> innerArray, MimeMailMessage message, String type) throws JSONException, AddressException {
        for (int a = 0; a < innerArray.size(); a++) {
            List<String> secondInnerArray = innerArray.get(a);
            for (int x = 0; x < secondInnerArray.size(); x++) {
                String string = "";
                if (null != secondInnerArray.get(x) && !JSONObject.NULL.equals(secondInnerArray.get(0)) && !"null".equals(secondInnerArray.get(x).toString())) {
                    string = secondInnerArray.get(x);
                    if (string.contains("@")) {
                        if (type.equals("from")) {
                            message.addFrom(new InternetAddress(string));
                        } else if (type.equals("to")) {
                            message.addTo(new InternetAddress(string));
                        } else if (type.equals("cc")) {
                            message.addCc(new InternetAddress(string));
                        } else if (type.equals("bcc")) {
                            message.addBcc(new InternetAddress(string));
                        }
                    }
                }
            }
        }
    }
}
