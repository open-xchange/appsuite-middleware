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

package com.openexchange.pns;

import java.util.Map;
import com.openexchange.java.Charsets;

/**
 * {@link PushNotifications} - The utility class for push notification module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class PushNotifications {

    private PushNotifications() {
        super();
    }

    /**
     * Gets the length in bytes for specified payload string.
     *
     * @param payload The payload
     * @return The length in bytes
     */
    public static int getPayloadLength(String payload) {
        if (null == payload) {
            return 0;
        }

        byte[] bytes;
        try {
            bytes = payload.getBytes(Charsets.UTF_8);
        } catch (Exception ex) {
            bytes = payload.getBytes();
        }
        return bytes.length;
    }

    /**
     * Checks if specified push notification appears to be a refresh
     *
     * @param notification The push notification to examine
     * @return <code>true</code> if push notification appears is a refresh; otherwise <code>false</code>
     */
    public static boolean isRefresh(PushNotification notification) {
        Map<String, Object> messageData = notification.getMessageData();
        if (null == messageData) {
            return false;
        }

        return "refresh".equals(messageData.get(PushNotificationField.MESSAGE.getId()));
    }

    /**
     * Gets the value from notification's data associated with specified field.
     *
     * @param field The field
     * @param notification The notification to grab from
     * @return The value or <code>null</code>
     */
    public static <V> V getValueFor(PushNotificationField field, PushNotification notification) {
        if (null == field) {
            return null;
        }
        return getValueFor(field.getId(), notification);
    }

    /**
     * Gets the value from notification's data associated with specified field.
     *
     * @param field The field
     * @param notification The notification to grab from
     * @return The value or <code>null</code>
     */
    public static <V> V getValueFor(String field, PushNotification notification) {
        if (null == field || null == notification) {
            return null;
        }
        return (V) notification.getMessageData().get(field);
    }

    // -----------------------------------------------------------------------------------------------------------

    /**
     * Cuts given notification by specified number of bytes.
     *
     * @param notification The notification
     * @param numBytesToCut The number of bytes to cut by
     */
    public static void cutNotification(PushNotification notification, int numBytesToCut) {
        switch (notification.getAffiliation()) {
            case MAIL:
                cutMailNotification(notification, numBytesToCut, 10, 35);
                break;
            default:
                break;
        }
    }

    /**
     * Cuts specified mail push notification by the specified number of bytes
     *
     * @param notification The push notification with mail affiliation
     * @param numBytesToCut The number of bytes to cut by
     * @param senderMin The minimum length to preserve for sender
     * @param subjectMin The minimum length to preserve for subject
     * @throws IllegalArgumentException If affiliation is not mail
     */
    public static void cutMailNotification(PushNotification notification, int numBytesToCut, int senderMin, int subjectMin) {
        if (null == notification || numBytesToCut <= 0) {
            return;
        }

        if (PushAffiliation.MAIL != notification.getAffiliation()) {
            throw new IllegalArgumentException("Invalid affilitation: " + notification.getAffiliation());
        }

        Map<String, Object> messageData = notification.getMessageData();
        int toCut = numBytesToCut;

        // First, cut from subject
        {
            String subject = (String) messageData.get(PushNotificationField.MAIL_SUBJECT.getId());
            if (null != subject) {
                int lengthSubject = subject.length();
                int allowedShrinkSubject = lengthSubject - subjectMin;
                if (allowedShrinkSubject > 0) {
                    if (allowedShrinkSubject < toCut) {
                        subject = subject.substring(0, (lengthSubject - allowedShrinkSubject));
                        toCut -= allowedShrinkSubject;
                    } else {
                        subject = subject.substring(0, (lengthSubject - toCut));
                        toCut = 0;
                    }
                }

                messageData.put(PushNotificationField.MAIL_SUBJECT.getId(), subject);
            }
        }

        // Then from sender
        if (toCut > 0) {
            String sender = (String) messageData.get(PushNotificationField.MAIL_SENDER.getId());
            if (null != sender) {
                int lengthFrom = sender.length();
                int allowedShrinkFrom = lengthFrom - senderMin;
                if (allowedShrinkFrom > 0) {
                    if (allowedShrinkFrom < toCut) {
                        sender = sender.substring(0, lengthFrom - allowedShrinkFrom);
                        toCut -= allowedShrinkFrom;
                    } else {
                        sender = sender.substring(0, lengthFrom - toCut);
                        toCut = 0;
                    }
                }

                messageData.put(PushNotificationField.MAIL_SENDER.getId(), sender);
            }
        }
    }

}
