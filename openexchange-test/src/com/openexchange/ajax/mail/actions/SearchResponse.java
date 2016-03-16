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

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.dataobjects.MimeMailMessage;

/**
 * {@link SearchResponse}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SearchResponse extends CommonAllResponse {

    protected SearchResponse(final Response response) {
        super(response);
    }

    public MailMessage[] getMailMessages(final int[] columns) throws JSONException, AddressException {
        final JSONArray objectsArray = (JSONArray) getData();
        final MailMessage[] messages = new MailMessage[objectsArray.length()];
        for (int i = 0; i < objectsArray.length(); i++) {
            final JSONArray oneMailAsArray = objectsArray.getJSONArray(i);
            final MailMessage message = parse(oneMailAsArray, columns);
            messages[i] = message;
        }

        return messages;
    }

    private MailMessage parse(final JSONArray mailAsArray, final int[] columns) throws JSONException, AddressException {
        final MimeMailMessage message = new MimeMailMessage();

        for (int i = 0; i < mailAsArray.length() && i < columns.length; i++) {
            // MailID
            if (columns[i] == 600) {
                message.setMailId((String) mailAsArray.get(i));
            } else if (columns[i] == 601) {
                message.setFolder((String) mailAsArray.get(i));
            } else if (columns[i] == 602) {
                message.setHasAttachment((Boolean) mailAsArray.get(i));
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
                message.setSize((Integer) mailAsArray.get(i));
                // Sent date
                // else if (columns[i] == 609) message.setSentDate(new Date((Long)mailAsArray.get(i)));
                // Received date
                // else if (columns[i] == 610) message.setReceivedDate(new Date((Long)mailAsArray.get(i)));
            }
        }

        return message;
    }

    private void handleInnerArrays(final JSONArray mailAsArray, final MimeMailMessage message, final int i, final String type) throws JSONException, AddressException {
        final JSONArray innerArray = (JSONArray) (mailAsArray.get(i));
        for (int a = 0; a < innerArray.length(); a++) {
            final JSONArray secondInnerArray = (JSONArray) innerArray.get(a);
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
