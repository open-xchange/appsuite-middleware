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

package com.openexchange.messaging;

import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingHeader.KnownHeader;

/**
 * {@link MessagingMessageGetSwitch}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class MessagingMessageGetSwitch implements MessagingMessageSwitcher {

    /**
     * Initializes a new {@link MessagingMessageGetSwitch}.
     */
    public MessagingMessageGetSwitch() {
        super();
    }

    @Override
    public Object accountName(final Object... args) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Object bcc(final Object... args) throws OXException {
        return header(args[0], KnownHeader.BCC.toString());
    }

    @Override
    public Object body(final Object... args) throws OXException {
        final MessagingMessage message = (MessagingMessage) args[0];
        return message.getContent();
    }

    @Override
    public Object cc(final Object... args) throws OXException {
        return header(args[0], KnownHeader.CC.toString());
    }

    @Override
    public Object colorLabel(final Object... args) throws OXException {
        return Integer.valueOf(((MessagingMessage) args[0]).getColorLabel());
    }

    @Override
    public Object contentType(final Object... args) throws OXException {
        return header(args[0], KnownHeader.CONTENT_TYPE.toString());
    }

    @Override
    public Object dispositionNotificationTo(final Object... args) throws OXException {
        return header(args[0], KnownHeader.DISPOSITION_NOTIFICATION_TO.toString());
    }

    @Override
    public Object flags(final Object... args) throws OXException {
        return Integer.valueOf(((MessagingMessage) args[0]).getFlags());
    }

    @Override
    public Object folderId(final Object... args) {
        return ((MessagingMessage) args[0]).getFolder();
    }

    @Override
    public Object from(final Object... args) throws OXException {
        return header(args[0], KnownHeader.FROM.toString());
    }

    @Override
    public Object full(final Object... args) {
        return args[0];
    }

    @Override
    public Object headers(final Object... args) throws OXException {
        return ((MessagingMessage) args[0]).getHeaders();
    }

    @Override
    public Object id(final Object... args) {
        return ((MessagingMessage) args[0]).getId();
    }

    @Override
    public Object priority(final Object... args) throws OXException {
        return header(args[0], KnownHeader.PRIORITY.toString());
    }

    @Override
    public Object receivedDate(final Object... args) {
        return Long.valueOf(((MessagingMessage) args[0]).getReceivedDate());
    }

    @Override
    public Object sentDate(final Object... args) throws OXException {
        return header(args[0], KnownHeader.SENT_DATE.toString());
    }

    @Override
    public Object size(final Object... args) throws OXException {
        return Long.valueOf(((MessagingMessage) args[0]).getSize());
    }

    @Override
    public Object subject(final Object... args) throws OXException {
        return header(args[0], KnownHeader.SUBJECT.toString());
    }

    @Override
    public Object threadLevel(final Object... args) {
        return Integer.valueOf(((MessagingMessage) args[0]).getThreadLevel());
    }

    @Override
    public Object to(final Object... args) throws OXException {
        return header(args[0], KnownHeader.TO.toString());
    }

    private Object header(final Object object, final String header) throws OXException {
        return ((MessagingMessage) object).getHeader(header);
    }

    @Override
    public Object picture(final Object... args) {
        return ((MessagingMessage) args[0]).getPicture();
    }

    @Override
    public Object url(final Object... args) throws OXException {
        return ((MessagingMessage) args[0]).getUrl();
    }

}
