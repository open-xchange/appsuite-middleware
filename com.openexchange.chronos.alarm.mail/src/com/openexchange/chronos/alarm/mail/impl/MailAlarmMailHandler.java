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

package com.openexchange.chronos.alarm.mail.impl;

import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.User;

/**
 * {@link MailAlarmMailHandler}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public class MailAlarmMailHandler {

    private final ServiceLookup services;

    public MailAlarmMailHandler(ServiceLookup services) {
        super();
        this.services = services;
    }

    public void send(Event event, User user, int contextId, int accountId, long trigger) throws OXException {
        TransportProvider transportProvider = getTransportProvider();
        ComposedMailMessage mail = MailAlarmMailGenerator.init(event, user, contextId, accountId, trigger, services).compose();

        sendMail(transportProvider.createNewNoReplyTransport(contextId, true), mail);
    }

    private TransportProvider getTransportProvider() {
        return TransportProviderRegistry.getTransportProvider("smtp");
    }

    private static void sendMail(MailTransport transport, ComposedMailMessage mail) throws OXException {
        try {
            transport.sendMailMessage(mail, ComposeType.NEW);
        } finally {
            try {
                transport.close();
            } catch (@SuppressWarnings("unused") OXException e) {
                // ignore
            }
        }
    }
}
