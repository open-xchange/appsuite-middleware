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

package com.openexchange.messaging;

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
        called.add(new Call("copyMessages", sourceFolder, destFolder, messageIds, fast));
        return new ArrayList<String>(Arrays.asList("blupp"));
    }

    @Override
    public void deleteMessages(final String folder, final String[] messageIds, final boolean hardDelete) throws OXException {
        called.add(new Call("deleteMessages", folder, messageIds, hardDelete));
    }

    @Override
    public List<MessagingMessage> getAllMessages(final String folder, final IndexRange indexRange, final MessagingField sortField, final OrderDirection order, final MessagingField... fields) throws OXException {
        called.add(new Call("getAllMessages", folder, indexRange, sortField, order, fields));
        return new ArrayList<MessagingMessage>(Arrays.asList(templateMessage));
    }


    @Override
    public MessagingMessage getMessage(final String folder, final String id, final boolean peek) throws OXException {
        called.add(new Call("getMessage", folder, id, peek));
        return templateMessage;
    }

    @Override
    public List<MessagingMessage> getMessages(final String folder, final String[] messageIds, final MessagingField[] fields) throws OXException {
        called.add(new Call("getMessages", folder, messageIds, fields));
        return new ArrayList<MessagingMessage>(Arrays.asList(templateMessage));
    }

    @Override
    public List<String> moveMessages(final String sourceFolder, final String destFolder, final String[] messageIds, final boolean fast) throws OXException {
        called.add(new Call("moveMessages", sourceFolder, destFolder, messageIds, fast));
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

    /* (non-Javadoc)
     * @see com.openexchange.messaging.MessagingMessageAccess#resolveContent(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public MessagingContent resolveContent(final String folder, final String id, final String referenceId) throws OXException {
        // Nothing to do
        return null;
    }

}
