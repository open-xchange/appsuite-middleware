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

package com.openexchange.mail.compose.impl.storage.db.mapping;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.database.BooleanMapping;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.groupware.tools.mappings.database.VarCharMapping;
import com.openexchange.mail.compose.Address;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.Message;
import com.openexchange.mail.compose.Message.ContentType;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.MessageField;
import com.openexchange.mail.compose.Meta;
import com.openexchange.mail.compose.Security;
import com.openexchange.mail.compose.SharedAttachmentsInfo;

/**
 * {@link MessageMapper}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class MessageMapper extends DefaultDbMapper<MessageDescription, MessageField> {

    private static final MessageMapper INSTANCE = new MessageMapper();

    /**
     * Gets the <code>MessageMapper</code> instance.
     *
     * @return The instance
     */
    public static MessageMapper getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private MessageMapper() {
        super();
    }

    @Override
    public MessageDescription newInstance() {
        return new MessageDescription();
    }

    @Override
    public MessageField[] newArray(int size) {
        return new MessageField[size];
    }

    @Override
    protected EnumMap<MessageField, DbMapping<? extends Object, MessageDescription>> createMappings() {
        EnumMap<MessageField, DbMapping<? extends Object, MessageDescription>> mappings = new EnumMap<>(MessageField.class);

        mappings.put(MessageField.SENDER, new VarCharJsonAddressMapping<MessageDescription>("senderAddr", "Sender") {

            @Override
            public boolean isSet(MessageDescription object) {
                return object.containsSender();
            }

            @Override
            public void set(MessageDescription object, Address value) throws OXException {
                object.setSender(value);
            }

            @Override
            public Address get(MessageDescription object) {
                return object.getSender();
            }

            @Override
            public void remove(MessageDescription object) {
                object.removeSender();
            }

        });

        mappings.put(MessageField.FROM, new VarCharJsonAddressMapping<MessageDescription>("fromAddr", "From") {

            @Override
            public boolean isSet(MessageDescription object) {
                return object.containsFrom();
            }

            @Override
            public void set(MessageDescription object, Address value) throws OXException {
                object.setFrom(value);
            }

            @Override
            public Address get(MessageDescription object) {
                return object.getFrom();
            }

            @Override
            public void remove(MessageDescription object) {
                object.removeFrom();
            }

        });

        mappings.put(MessageField.TO, new VarCharJsonAddressArrayMapping<MessageDescription>("toAddr", "To") {

            @Override
            public boolean isSet(MessageDescription object) {
                return object.containsTo();
            }

            @Override
            public void set(MessageDescription object, List<Address> value) throws OXException {
                object.setTo(value);
            }

            @Override
            public List<Address> get(MessageDescription object) {
                return object.getTo();
            }

            @Override
            public void remove(MessageDescription object) {
                object.removeTo();
            }

        });

        mappings.put(MessageField.CC, new VarCharJsonAddressArrayMapping<MessageDescription>("ccAddr", "CC") {

            @Override
            public boolean isSet(MessageDescription object) {
                return object.containsCc();
            }

            @Override
            public void set(MessageDescription object, List<Address> value) throws OXException {
                object.setCc(value);
            }

            @Override
            public List<Address> get(MessageDescription object) {
                return object.getCc();
            }

            @Override
            public void remove(MessageDescription object) {
                object.removeCc();
            }

        });

        mappings.put(MessageField.BCC, new VarCharJsonAddressArrayMapping<MessageDescription>("bccAddr", "BCC") {

            @Override
            public boolean isSet(MessageDescription object) {
                return object.containsBcc();
            }

            @Override
            public void set(MessageDescription object, List<Address> value) throws OXException {
                object.setBcc(value);
            }

            @Override
            public List<Address> get(MessageDescription object) {
                return object.getBcc();
            }

            @Override
            public void remove(MessageDescription object) {
                object.removeBcc();
            }

        });

        mappings.put(MessageField.SUBJECT, new VarCharMapping<MessageDescription>("subject", "Subject") {

            @Override
            public boolean isSet(MessageDescription object) {
                return object.containsSubject();
            }

            @Override
            public void set(MessageDescription object, String value) throws OXException {
                object.setSubject(value);
            }

            @Override
            public String get(MessageDescription object) {
                return object.getSubject();
            }

            @Override
            public void remove(MessageDescription object) {
                object.removeSubject();
            }

        });

        mappings.put(MessageField.CONTENT, new VarCharMapping<MessageDescription>("content", "Content") {

            @Override
            public boolean isSet(MessageDescription object) {
                return object.containsContent();
            }

            @Override
            public void set(MessageDescription object, String value) throws OXException {
                object.setContent(value);
            }

            @Override
            public String get(MessageDescription object) {
                return object.getContent();
            }

            @Override
            public void remove(MessageDescription object) {
                object.removeContent();
            }

        });

        mappings.put(MessageField.CONTENT_TYPE, new VarCharMapping<MessageDescription>("contentType", "Content Type") {

            @Override
            public boolean isSet(MessageDescription object) {
                return object.containsContentType();
            }

            @Override
            public void set(MessageDescription object, String value) throws OXException {
                for (Message.ContentType ct : EnumSet.allOf(Message.ContentType.class)) {
                    if (value.equals(ct.getId())) {
                        object.setContentType(ct);
                        break;
                    }
                }
            }

            @Override
            public String get(MessageDescription object) {
                ContentType contentType = object.getContentType();
                return null == contentType ? null : contentType.getId();
            }

            @Override
            public void remove(MessageDescription object) {
                object.removeContentType();
            }

        });

        mappings.put(MessageField.META, new VarCharJsonMetaMapping<MessageDescription>("meta", "Meta Data") {

            @Override
            public boolean isSet(MessageDescription object) {
                return object.containsMeta();
            }

            @Override
            public void set(MessageDescription object, Meta value) throws OXException {
                object.setMeta(value);
            }

            @Override
            public Meta get(MessageDescription object) {
                return object.getMeta();
            }

            @Override
            public void remove(MessageDescription object) {
                object.removeMeta();
            }

        });

        mappings.put(MessageField.PRIORITY, new VarCharMapping<MessageDescription>("priority", "Priority") {

            @Override
            public boolean isSet(MessageDescription object) {
                return object.containsPriority();
            }

            @Override
            public void set(MessageDescription object, String value) throws OXException {
                Message.Priority priority = Message.Priority.priorityFor(value);
                object.setPriority(priority);
            }

            @Override
            public String get(MessageDescription object) {
                return object.getPriority().getId();
            }

            @Override
            public void remove(MessageDescription object) {
                object.removePriority();
            }

        });

        mappings.put(MessageField.REQUEST_READ_RECEIPT, new BooleanMapping<MessageDescription>("requestReadReceipt", "Request read receipt") {

            @Override
            public boolean isSet(MessageDescription object) {
                return object.containsRequestReadReceipt();
            }

            @Override
            public void set(MessageDescription object, Boolean value) throws OXException {
                object.setRequestReadReceipt(value.booleanValue());
            }

            @Override
            public Boolean get(MessageDescription object) {
                return Boolean.valueOf(object.isRequestReadReceipt());
            }

            @Override
            public void remove(MessageDescription object) {
                object.removeRequestReadReceipt();
            }

        });

        mappings.put(MessageField.CONTENT_ENCRYPTED, new BooleanMapping<MessageDescription>("contentEncrypted", "Content encrypted") {

            @Override
            public boolean isSet(MessageDescription object) {
                // Consider as always set
                return true;
            }

            @Override
            public void set(MessageDescription object, Boolean value) throws OXException {
                object.setContentEncrypted(value.booleanValue());
            }

            @Override
            public Boolean get(MessageDescription object) {
                return object.containsContentEncrypted() ? Boolean.valueOf(object.isContentEncrypted()) : Boolean.FALSE;
            }

            @Override
            public void remove(MessageDescription object) {
                object.removeContentEncrypted();
            }

        });

        mappings.put(MessageField.SECURITY, new VarCharJsonSecurityMapping<MessageDescription>("security", "Security settings") {

            @Override
            public boolean isSet(MessageDescription object) {
                return object.containsSecurity();
            }

            @Override
            public void set(MessageDescription object, Security value) throws OXException {
                object.setSecurity(value);
            }

            @Override
            public Security get(MessageDescription object) {
                return object.getSecurity();
            }

            @Override
            public void remove(MessageDescription object) {
                object.removeSecurity();
            }

        });

        mappings.put(MessageField.SHARED_ATTACCHMENTS_INFO, new VarCharJsonSharedAttachmentsInfoMapping<MessageDescription>("sharedAttachments", "Shared Attachments Information") {

            @Override
            public boolean isSet(MessageDescription object) {
                return object.containsSharedAttachmentsInfo();
            }

            @Override
            public void set(MessageDescription object, SharedAttachmentsInfo value) throws OXException {
                object.setsharedAttachmentsInfo(value);
            }

            @Override
            public SharedAttachmentsInfo get(MessageDescription object) {
                return object.getSharedAttachmentsInfo();
            }

            @Override
            public void remove(MessageDescription object) {
                object.removeSharedAttachmentsInfo();
            }

        });

        mappings.put(MessageField.ATTACHMENTS, new VarCharJsonAttachmentsArrayMapping<MessageDescription>("attachments", "Attachments") {

            @Override
            public boolean isSet(MessageDescription object) {
                return object.containsAttachments();
            }

            @Override
            public void set(MessageDescription object, List<Attachment> value) throws OXException {
                object.setAttachments(value);
            }

            @Override
            public List<Attachment> get(MessageDescription object) {
                return object.getAttachments();
            }

            @Override
            public void remove(MessageDescription object) {
                object.removeAttachments();
            }

        });

        return mappings;
    }

}
