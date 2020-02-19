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

package com.openexchange.pns.mobile.api.facade;

import static com.openexchange.java.Autoboxing.I;
import java.util.Map;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.pns.ApnsConstants;
import com.openexchange.pns.KnownTransport;
import com.openexchange.pns.Message;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushMessageGenerator;
import com.openexchange.pns.PushNotification;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;

/**
 * {@link MobileApiFacadeMessageGenerator} - The message generator for Mobile API Facade.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.f
 */
public class MobileApiFacadeMessageGenerator implements PushMessageGenerator {

    private static final String TRANSPORT_ID_GCM = KnownTransport.GCM.getTransportId();
    private static final String TRANSPORT_ID_APNS = KnownTransport.APNS.getTransportId();
    private static final String DEFAULT_NAMESPACE = "/";

    // ------------------------------------------------------------------------------------------------------------------------

    private final ClientConfig clientConfig;
    private final ConfigViewFactory viewFactory;

    /**
     * Initializes a new {@link MobileApiFacadeMessageGenerator}.
     *
     * @param viewFactory The service to use
     */
    public MobileApiFacadeMessageGenerator(ClientConfig clientConfig, ConfigViewFactory viewFactory) {
        super();
        this.clientConfig = clientConfig;
        this.viewFactory = viewFactory;
    }

    @Override
    public String getClient() {
        return clientConfig.getClientId();
    }

    @Override
    public Message<?> generateMessageFor(String transportId, PushNotification notification) throws OXException {
        if (TRANSPORT_ID_GCM.equals(transportId)) {
            // Build the GCM/FCM message payload

            com.google.android.gcm.Message.Builder gcmMessageBuilder = new com.google.android.gcm.Message.Builder();
            Map<String, Object> messageData = notification.getMessageData();
            for (Map.Entry<String, Object> entry : messageData.entrySet()) {
                //Cast to string cause google only allows strings..
                String value = String.valueOf(entry.getValue());
                gcmMessageBuilder.addData(entry.getKey(), value);
            }

            /*-
             *
            int curlen = PushNotifications.getPayloadLength(gcmMessageBuilder.toString());
            if (curlen > GCM_MAX_PAYLOAD_SIZE) {
                int bytesToCut = curlen - GCM_MAX_PAYLOAD_SIZE;
            }
            */

            final com.google.android.gcm.Message gcmMessage = gcmMessageBuilder.build();
            return new Message<com.google.android.gcm.Message>() {

                @Override
                public com.google.android.gcm.Message getMessage() {
                    return gcmMessage;
                }
            };
        } else if (TRANSPORT_ID_APNS.equals(transportId)) {
            // Build APNS payload as expected by client
            ApnsPayloadBuilder builder = new ApnsPayloadBuilder();
            Map<String, Object> messageData = notification.getMessageData();
            try {
                String subject = MessageDataUtil.getSubject(messageData);
                String displayName = MessageDataUtil.getDisplayName(messageData);
                String senderAddress = MessageDataUtil.getSender(messageData);
                String sender = displayName.length() == 0 ? senderAddress : displayName;
                String folder = MessageDataUtil.getFolder(messageData);
                String id = MessageDataUtil.getId(messageData);
                String path = MessageDataUtil.getPath(messageData);
                int unread = MessageDataUtil.getUnread(messageData);

                if (Strings.isNotEmpty(subject) && Strings.isNotEmpty(sender)) {
                    // Non-silent push
                    StringBuilder sb = new StringBuilder(sender);
                    sb.append("\n");
                    sb.append(subject);
                    String alertMessage = sb.toString();
                    alertMessage = alertMessage.length() > ApnsConstants.APNS_MAX_ALERT_LENGTH ? alertMessage.substring(0, ApnsConstants.APNS_MAX_ALERT_LENGTH) : alertMessage;
                    builder.setAlertTitle(subject);
                    builder.setAlertBody(alertMessage);

                    MobileApiFacadePushConfiguration config = MobileApiFacadePushConfiguration.getConfigFor(notification.getUserId(), notification.getContextId(), viewFactory);

                    if (config.isApnBadgeEnabled() && unread >= 0) {
                        builder.setBadgeNumber(I(unread));
                    }

                    if (config.isApnSoundEnabled()) {
                        builder.setSound(config.getApnSoundFile());
                    }

                    builder.setCategoryName("new-message-category");
                    builder.setContentAvailable(false);
                } else {
                    // Silent push
                    builder.setContentAvailable(true);
                }
                if (Strings.isEmpty(path) && Strings.isNotEmpty(folder) && Strings.isNotEmpty(id)) {
                    path = folder + "/" + id;
                }
                if (path.length() > 0) {
                    builder.addCustomProperty("cid", path);
                } else if (folder.length() > 0) {
                    builder.addCustomProperty("folder", folder);
                }
                builder.addCustomProperty("namespace", DEFAULT_NAMESPACE);
                return new Message<String>() {

                    @Override
                    public String getMessage() {
                        return builder.buildWithMaximumLength(ApnsConstants.APNS_MAX_PAYLOAD_SIZE);
                    }

                };
            } catch (Exception e) {
                throw PushExceptionCodes.MESSAGE_GENERATION_FAILED.create(e, e.getMessage());
            }
        }

        throw PushExceptionCodes.UNSUPPORTED_TRANSPORT.create(null == transportId ? "null" : transportId);
    }

}
