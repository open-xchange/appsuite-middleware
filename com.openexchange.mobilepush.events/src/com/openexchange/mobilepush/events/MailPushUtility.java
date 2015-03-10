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

package com.openexchange.mobilepush.events;

import java.util.List;
import java.util.Map;
import com.openexchange.mobilepush.events.storage.ContextUsers;
import com.openexchange.mobilepush.events.storage.UserToken;

/**
 * {@link MailPushUtility}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class MailPushUtility {

    public static final String KEY_SUBJECT = "subject";

    public static final String KEY_UNREAD = "unread";

    public static final String KEY_SENDER = "sender";

    public static final String KEY_PATH = "path";

    private static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";

    /**
     *
     * @param payload
     * @return
     */
    public static int getPayloadLength(String payload) {
        byte[] bytes = null;
        try {
            bytes = payload.toString().getBytes(DEFAULT_CHARACTER_ENCODING);
        } catch (Exception ex) {
            bytes = payload.toString().getBytes();
        }
        return bytes.length;
    }

    /**
     *
     * @param contextUsers
     * @param registrationId
     * @return
     */
    public static int getContextIdForToken(List<ContextUsers> contextUsers, String registrationId) {
        if (contextUsers != null && false == contextUsers.isEmpty()) {
            for (ContextUsers cu : contextUsers) {
                for (UserToken ut : cu.getUserTokens()) {
                    if (ut.getToken().equals(registrationId)) {
                        return cu.getContextId();
                    }
                }
            }
        }
        return -1;
    }

    /**
     *
     * @param messageData
     * @param bytesToCut
     */
    public static void cutMessage(Map<String, Object> messageData, int bytesToCut) {
        cutMessage(messageData, bytesToCut, 10, 35);
    }

    /**
     *
     * @param messageData
     * @param bytesToCut
     * @param fromMin
     * @param subjectMin
     */
    public static void cutMessage(Map<String, Object> messageData, int bytesToCut, int fromMin, int subjectMin) {
        if (messageData.containsKey("subject") && messageData.containsKey("sender")) {
            String subject = (String) messageData.get("subject");
            String from = (String) messageData.get("sender");

            if (bytesToCut > 0) {
                int lengthSubject = subject.length();
                int lengthFrom = from.length();
                int allowedShrinkSubject = lengthSubject - subjectMin;
                int allowedShrinkFrom = lengthFrom - fromMin;

                if (allowedShrinkSubject > 0) {
                    if (allowedShrinkSubject < bytesToCut) {
                        subject = subject.substring(0, (lengthSubject - allowedShrinkSubject));
                        bytesToCut -= allowedShrinkSubject;
                    } else {
                        subject = subject.substring(0, (lengthSubject - bytesToCut));
                        bytesToCut -= bytesToCut;
                    }
                }

                if (allowedShrinkFrom > 0 && bytesToCut > 0) {
                    if (allowedShrinkFrom < bytesToCut) {
                        from = from.substring(0, lengthFrom - allowedShrinkFrom);
                        bytesToCut -= allowedShrinkFrom;
                    } else {
                        from = from.substring(0, lengthFrom - bytesToCut);
                        bytesToCut -= bytesToCut;
                    }
                }
            }
            messageData.put("subject", subject);
            messageData.put("sender", from);
        }
    }

    /**
     *
     * @param messageData
     * @return
     */
    public static int getUnread(Map<String, Object> messageData) {
        if (messageData.containsKey(KEY_UNREAD)) {
            if (messageData.get(KEY_UNREAD) instanceof Integer) {
                return (Integer) messageData.get(KEY_UNREAD);
            }
        }
        return -1;
    }

    /**
     *
     * @param messageData
     * @return
     */
    public static String getSubject(Map<String, Object> messageData) {
        if (messageData.containsKey(KEY_SUBJECT)) {
            if (messageData.get(KEY_SUBJECT) instanceof String) {
                return (String) messageData.get(KEY_SUBJECT);
            }
        }
        return "";
    }

    /**
     *
     * @param messageData
     * @return
     */
    public static String getPath(Map<String, Object> messageData) {
        if (messageData.containsKey(KEY_PATH)) {
            if (messageData.get(KEY_PATH) instanceof String) {
                return (String) messageData.get(KEY_PATH);
            }
        }
        return "";
    }

    /**
     *
     * @param messageData
     * @return
     */
    public static String getSender(Map<String, Object> messageData) {
        if (messageData.containsKey(KEY_SENDER)) {
            if (messageData.get(KEY_SENDER) instanceof String) {
                return (String) messageData.get(KEY_SENDER);
            }
        }
        return "";
    }
}
