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
