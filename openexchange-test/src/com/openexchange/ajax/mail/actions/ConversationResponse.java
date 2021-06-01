/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.mail.dataobjects.MailAuthenticityResult;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.ThreadSortMailMessage;
import com.openexchange.mail.mime.dataobjects.MimeMailMessage;
import com.openexchange.mail.utils.MailFolderUtility;

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
                        message.setAccountId(((Integer) messageMap.get(key)).intValue());
                        break;
                    case ACCOUNT_NAME:
                        message.setAccountName(strValue);
                        break;
                    case COLOR_LABEL:
                        message.setColorLabel(((Integer) messageMap.get(key)).intValue());
                        break;
                    case DELETED:
                        message.setFlag(MailMessage.FLAG_DELETED, true);
                        break;
                    case DISPOSITION_NOTIFICATION_TO:
                        //ignore
                        break;
                    case FLAGS:
                        message.setFlags(((Integer) messageMap.get(key)).intValue());
                        break;
                    case FOLDER:
                        message.setFolder(strValue);
                        break;
                    case NEW:
                        // ignore
                        break;
                    case ORIGINAL_FOLDER_ID:
                        message.setOriginalFolder(MailFolderUtility.prepareMailFolderParam(strValue));
                        break;
                    case ORIGINAL_ID:
                        message.setOriginalId(strValue);
                        break;
                    case TEXT_PREVIEW_IF_AVAILABLE:
                        // fall-through
                    case TEXT_PREVIEW:
                        message.setTextPreview(strValue);
                        break;
                    case AUTHENTICATION_OVERALL_RESULT:
                        message.setAuthenticityResult((MailAuthenticityResult) messageMap.get(key));
                        break;
                    case AUTHENTICATION_MECHANISM_RESULTS:
                        message.setAuthenticityResult((MailAuthenticityResult) messageMap.get(key));
                        break;
                    case PRIORITY:
                        message.setPriority(((Integer) messageMap.get(key)).intValue());
                        break;
                    case RECEIVED_DATE:
                        message.setReceivedDate(new Date(((Long) messageMap.get(key)).longValue()));
                        break;
                    case SENT_DATE:
                        message.setSentDate(new Date(((Long) messageMap.get(key)).longValue()));
                        break;
                    case SIZE:
                        message.setSize(((Integer) messageMap.get(key)).intValue());
                        break;
                    case SUBJECT:
                        message.setSubject(strValue);
                        break;
                    case THREAD_LEVEL:
                        message.setThreadLevel(((Integer) messageMap.get(key)).intValue());
                        break;
                    case TOTAL:
                        // ignore
                        break;
                    case UNREAD:
                        message.setUnreadMessages(((Integer) messageMap.get(key)).intValue());
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

    private void handleInnerArrays(List<List<String>> innerArray, MimeMailMessage message, String type) throws AddressException {
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
