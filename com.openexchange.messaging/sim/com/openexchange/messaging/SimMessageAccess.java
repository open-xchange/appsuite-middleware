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

import static com.openexchange.java.Autoboxing.B;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link SimMessageAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimMessageAccess implements MessagingMessageAccess {

    public static final class Call {
        private final String name;
        private final Object[] args;

        public Call(final String name, final Object...args) {
            this.name = name;
            this.args = args;
        }


        public String getName() {
            return name;
        }

        public Object[] getArgs() {
            return args;
        }
    }

    private final List<Call> called = new ArrayList<Call>();
    private MessagingMessage templateMessage;
    private MessagingPart templatePart;

    public List<Call> getCalls() {
        return called;
    }

    public void setTemplateMessage(final MessagingMessage templateMessage) {
        this.templateMessage = templateMessage;
    }

    @Override
    public MessagingPart getAttachment(final String folder, final String messageId, final String sectionId) throws OXException {
        called.add(new Call("getAttachment", folder, messageId, sectionId));
        return templatePart;
    }

    @Override
    public void appendMessages(final String folder, final MessagingMessage[] messages) throws OXException {
        called.add(new Call("appendMessages", folder, messages));
    }

    @Override
    public List<String> copyMessages(final String sourceFolder, final String destFolder, final String[] messageIds, final boolean fast) throws OXException {
        called.add(new Call("copyMessages", sourceFolder, destFolder, messageIds, B(fast)));
        return new ArrayList<String>(Arrays.asList("blupp"));
    }

    @Override
    public void deleteMessages(final String folder, final String[] messageIds, final boolean hardDelete) throws OXException {
        called.add(new Call("deleteMessages", folder, messageIds, B(hardDelete)));
    }

    @Override
    public List<MessagingMessage> getAllMessages(final String folder, final IndexRange indexRange, final MessagingField sortField, final OrderDirection order, final MessagingField... fields) throws OXException {
        called.add(new Call("getAllMessages", folder, indexRange, sortField, order, fields));
        return new ArrayList<MessagingMessage>(Arrays.asList(templateMessage));
    }


    @Override
    public MessagingMessage getMessage(final String folder, final String id, final boolean peek) throws OXException {
        called.add(new Call("getMessage", folder, id, B(peek)));
        return templateMessage;
    }

    @Override
    public List<MessagingMessage> getMessages(final String folder, final String[] messageIds, final MessagingField[] fields) throws OXException {
        called.add(new Call("getMessages", folder, messageIds, fields));
        return new ArrayList<MessagingMessage>(Arrays.asList(templateMessage));
    }

    @Override
    public List<String> moveMessages(final String sourceFolder, final String destFolder, final String[] messageIds, final boolean fast) throws OXException {
        called.add(new Call("moveMessages", sourceFolder, destFolder, messageIds, B(fast)));
        return null;
    }

    @Override
    public MessagingMessage perform(final String folder, final String id, final String action) throws OXException {
        called.add(new Call("perform", folder, id, action));
        return templateMessage;
    }

    @Override
    public MessagingMessage perform(final String action) throws OXException {
        called.add(new Call("perform", action));
        return templateMessage;
    }

    @Override
    public MessagingMessage perform(final MessagingMessage message, final String action) throws OXException {
        called.add(new Call("perform", message, action));
        return templateMessage;
    }

    @Override
    public List<MessagingMessage> searchMessages(final String folder, final IndexRange indexRange, final MessagingField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MessagingField[] fields) throws OXException {
        called.add(new Call("searchMessages", folder, indexRange, sortField, order, searchTerm, fields));
        return new ArrayList<MessagingMessage>(Arrays.asList(templateMessage));
    }

    @Override
    public void updateMessage(final MessagingMessage message, final MessagingField[] fields) throws OXException {
        called.add(new Call("updateMessage", message, fields));
    }

    @Override
    public MessagingContent resolveContent(final String folder, final String id, final String referenceId) throws OXException {
        // Nothing to do
        return null;
    }

}
