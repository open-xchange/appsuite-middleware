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

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.dataobjects.MimeMailMessage;

/**
 * @author <a href="karsten.will@open-xchange.com">Karsten Will</a>
 */
public class AllResponse extends CommonAllResponse {

    protected AllResponse(final Response response) {
        super(response);
    }

    public MailMessage[] getMailMessages(int[] columns) throws JSONException, AddressException {
        final JSONArray objectsArray = (JSONArray) getData();
        MailMessage[] messages = new MailMessage[objectsArray.length()];
        for (int i = 0; i < objectsArray.length(); i++) {
            JSONArray oneMailAsArray = objectsArray.getJSONArray(i);
            MailMessage message = parse(oneMailAsArray, columns);
            messages[i] = message;
        }

        return messages;
    }

    private MailMessage parse(JSONArray mailAsArray, int[] columns) throws JSONException, AddressException {
        MimeMailMessage message = new MimeMailMessage();

        for (int i = 0; i < mailAsArray.length() && i < columns.length; i++) {
            // MailID
            if (columns[i] == 600) {
                message.setMailId((String) mailAsArray.get(i));
            } else if (columns[i] == 601) {
                message.setFolder((String) mailAsArray.get(i));
            } else if (columns[i] == 602) {
                message.setHasAttachment(((Boolean) mailAsArray.get(i)).booleanValue());
            } else if (columns[i] == 603) {
                handleInnerArrays(mailAsArray, message, i, "from");
            } else if (columns[i] == 604) {
                handleInnerArrays(mailAsArray, message, i, "to");
            } else if (columns[i] == 605) {
                handleInnerArrays(mailAsArray, message, i, "cc");
            } else if (columns[i] == 606) {
                handleInnerArrays(mailAsArray, message, i, "bcc");
            } else if (columns[i] == 607) {
                message.setSubject((String) mailAsArray.get(i));
            } else if (columns[i] == 608) {
                message.setSize(((Integer) mailAsArray.get(i)).intValue());
                // Sent date
                //else if (columns[i] == 609) message.setSentDate(new Date((Long)mailAsArray.get(i)));
                // Received date
                //else if (columns[i] == 610) message.setReceivedDate(new Date((Long)mailAsArray.get(i)));
            }
        }

        return message;
    }

    private void handleInnerArrays(JSONArray mailAsArray, MimeMailMessage message, int i, String type) throws JSONException, AddressException {
        JSONArray innerArray = (JSONArray) (mailAsArray.get(i));
        for (int a = 0; a < innerArray.length(); a++) {
            JSONArray secondInnerArray = (JSONArray) innerArray.get(a);
            for (int x = 0; x < secondInnerArray.length(); x++) {
                String string = "";
                if (null != secondInnerArray.getString(x) && !"null".equals(secondInnerArray.getString(x))) {
                    string = secondInnerArray.getString(x);
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
