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

package com.openexchange.mail.structure.parser;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.activation.DataHandler;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.session.Session;

/**
 * {@link ComposedMailWrapper}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class ComposedMailWrapper extends ComposedMailMessage {

    private static final long serialVersionUID = -3283856474686683383L;

    private final MailMessage mail;

    /**
     * Initializes a new {@link ComposedMailWrapper}.
     *
     * @param session The session
     * @param ctx The context
     */
    public ComposedMailWrapper(final MailMessage mail, final Session session, final Context ctx) {
        super(session, ctx);
        this.mail = mail;
    }

    @Override
    public void setHeader(final String name, final String value) {
        mail.setHeader(name, value);
    }

    @Override
    public String getMessageId() {
        return mail.getMessageId();
    }

    @Override
    public boolean containsMessageId() {
        return mail.containsMessageId();
    }

    @Override
    public void removeMessageId() {
        mail.removeMessageId();
    }

    @Override
    public void setMessageId(final String messageId) {
        mail.setMessageId(messageId);
    }

    @Override
    public String getInReplyTo() {
        return mail.getInReplyTo();
    }

    @Override
    public String[] getReferences() {
        return mail.getReferences();
    }

    @Override
    public boolean containsReferences() {
        return mail.containsReferences();
    }

    @Override
    public void removeReferences() {
        mail.removeReferences();
    }

    @Override
    public void setReferences(final String sReferences) {
        mail.setReferences(sReferences);
    }

    @Override
    public void setReferences(final String[] references) {
        mail.setReferences(references);
    }

    @Override
    public int hashCode() {
        return mail.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return mail.equals(obj);
    }

    @Override
    public ContentType getContentType() {
        return mail.getContentType();
    }

    @Override
    public boolean containsContentType() {
        return mail.containsContentType();
    }

    @Override
    public void removeContentType() {
        mail.removeContentType();
    }

    @Override
    public void setContentType(final ContentType contentType) {
        mail.setContentType(contentType);
    }

    @Override
    public void setContentType(final String contentType) throws OXException {
        mail.setContentType(contentType);
    }

    @Override
    public ContentDisposition getContentDisposition() {
        return mail.getContentDisposition();
    }

    @Override
    public boolean containsContentDisposition() {
        return mail.containsContentDisposition();
    }

    @Override
    public void removeContentDisposition() {
        mail.removeContentDisposition();
    }

    @Override
    public void setContentDisposition(final String disposition) throws OXException {
        mail.setContentDisposition(disposition);
    }

    @Override
    public String toString() {
        return mail.toString();
    }

    @Override
    public void setContentDisposition(final ContentDisposition disposition) {
        mail.setContentDisposition(disposition);
    }

    @Override
    public String getFileName() {
        return mail.getFileName();
    }

    @Override
    public boolean containsFileName() {
        return mail.containsFileName();
    }

    @Override
    public void removeFileName() {
        mail.removeFileName();
    }

    @Override
    public void setFileName(final String fileName) {
        mail.setFileName(fileName);
    }

    @Override
    public void addHeader(final String name, final String value) {
        mail.addHeader(name, value);
    }

    @Override
    public void addHeaders(final HeaderCollection headers) {
        mail.addHeaders(headers);
    }

    @Override
    public boolean containsHeaders() {
        return mail.containsHeaders();
    }

    @Override
    public void removeHeaders() {
        mail.removeHeaders();
    }

    @Override
    public int getHeadersSize() {
        return mail.getHeadersSize();
    }

    @Override
    public Iterator<Entry<String, String>> getHeadersIterator() {
        return mail.getHeadersIterator();
    }

    @Override
    public boolean containsHeader(final String name) {
        return mail.containsHeader(name);
    }

    @Override
    public String[] getHeader(final String name) {
        return mail.getHeader(name);
    }

    @Override
    public void addFrom(final InternetAddress addr) {
        mail.addFrom(addr);
    }

    @Override
    public String getFirstHeader(final String name) {
        return mail.getFirstHeader(name);
    }

    @Override
    public void addFrom(final InternetAddress[] addrs) {
        mail.addFrom(addrs);
    }

    @Override
    public String getHeader(final String name, final String delimiter) {
        return mail.getHeader(name, delimiter);
    }

    @Override
    public boolean containsFrom() {
        return mail.containsFrom();
    }

    @Override
    public void removeFrom() {
        mail.removeFrom();
    }

    @Override
    public String getHeader(final String name, final char delimiter) {
        return mail.getHeader(name, delimiter);
    }

    @Override
    public InternetAddress[] getFrom() {
        return mail.getFrom();
    }

    @Override
    public HeaderCollection getHeaders() {
        return mail.getHeaders();
    }

    @Override
    public void addTo(final InternetAddress addr) {
        mail.addTo(addr);
    }

    @Override
    public Iterator<Entry<String, String>> getNonMatchingHeaders(final String[] nonMatchingHeaders) {
        return mail.getNonMatchingHeaders(nonMatchingHeaders);
    }

    @Override
    public void addTo(final InternetAddress[] addrs) {
        mail.addTo(addrs);
    }

    @Override
    public boolean containsTo() {
        return mail.containsTo();
    }

    @Override
    public Iterator<Entry<String, String>> getMatchingHeaders(final String[] matchingHeaders) {
        return mail.getMatchingHeaders(matchingHeaders);
    }

    @Override
    public void removeTo() {
        mail.removeTo();
    }

    @Override
    public InternetAddress[] getTo() {
        return mail.getTo();
    }

    @Override
    public void removeHeader(final String name) {
        mail.removeHeader(name);
    }

    @Override
    public boolean hasHeaders(final String... names) {
        return mail.hasHeaders(names);
    }

    @Override
    public void addCc(final InternetAddress addr) {
        mail.addCc(addr);
    }

    @Override
    public long getSize() {
        return mail.getSize();
    }

    @Override
    public boolean containsSize() {
        return mail.containsSize();
    }

    @Override
    public void addCc(final InternetAddress[] addrs) {
        mail.addCc(addrs);
    }

    @Override
    public void removeSize() {
        mail.removeSize();
    }

    @Override
    public void setSize(final long size) {
        mail.setSize(size);
    }

    @Override
    public void addReplyTo(final InternetAddress addr) {
        mail.addReplyTo(addr);
    }

    @Override
    public void addReplyTo(final InternetAddress[] addrs) {
        mail.addReplyTo(addrs);
    }

    @Override
    public boolean containsReplyTo() {
        return mail.containsReplyTo();
    }

    @Override
    public void removeReplyTo() {
        mail.removeReplyTo();
    }

    @Override
    public InternetAddress[] getReplyTo() {
        return mail.getReplyTo();
    }

    @Override
    public boolean isUnseen() {
        return mail.isUnseen();
    }

    @Override
    public boolean containsCc() {
        return mail.containsCc();
    }

    @Override
    public String getContentId() {
        return mail.getContentId();
    }

    @Override
    public void removeCc() {
        mail.removeCc();
    }

    @Override
    public InternetAddress[] getCc() {
        return mail.getCc();
    }

    @Override
    public boolean containsContentId() {
        return mail.containsContentId();
    }

    @Override
    public void removeContentId() {
        mail.removeContentId();
    }

    @Override
    public void setContentId(final String contentId) {
        mail.setContentId(contentId);
    }

    @Override
    public void addBcc(final InternetAddress addr) {
        mail.addBcc(addr);
    }

    @Override
    public String getSequenceId() {
        return mail.getSequenceId();
    }

    @Override
    public boolean containsSequenceId() {
        return mail.containsSequenceId();
    }

    @Override
    public void removeSequenceId() {
        mail.removeSequenceId();
    }

    @Override
    public void addBcc(final InternetAddress[] addrs) {
        mail.addBcc(addrs);
    }

    @Override
    public void setSequenceId(final String sequenceId) {
        mail.setSequenceId(sequenceId);
    }

    @Override
    public MailPath getMsgref() {
        return mail.getMsgref();
    }

    @Override
    public boolean containsBcc() {
        return mail.containsBcc();
    }

    @Override
    public void removeBcc() {
        mail.removeBcc();
    }

    @Override
    public InternetAddress[] getBcc() {
        return mail.getBcc();
    }

    @Override
    public boolean containsMsgref() {
        return mail.containsMsgref();
    }

    @Override
    public void removeMsgref() {
        mail.removeMsgref();
    }

    @Override
    public int getFlags() {
        return mail.getFlags();
    }

    @Override
    public void setMsgref(final MailPath msgref) {
        mail.setMsgref(msgref);
    }

    @Override
    public boolean isAnswered() {
        return mail.isAnswered();
    }

    @Override
    public boolean isDeleted() {
        return mail.isDeleted();
    }

    @Override
    public boolean isDraft() {
        return mail.isDraft();
    }

    @Override
    public boolean isFlagged() {
        return mail.isFlagged();
    }

    @Override
    public boolean isRecent() {
        return mail.isRecent();
    }

    @Override
    public boolean hasEnclosedParts() throws OXException {
        return mail.hasEnclosedParts();
    }

    @Override
    public boolean isSeen() {
        return mail.isSeen();
    }

    @Override
    public Object getContent() throws OXException {
        return mail.getContent();
    }

    @Override
    public boolean isSpam() {
        return mail.isSpam();
    }

    @Override
    public boolean isForwarded() {
        return mail.isForwarded();
    }

    @Override
    public boolean isReadAcknowledgment() {
        return mail.isReadAcknowledgment();
    }

    @Override
    public DataHandler getDataHandler() throws OXException {
        return mail.getDataHandler();
    }

    @Override
    public boolean isUser() {
        return mail.isUser();
    }

    @Override
    public boolean containsFlags() {
        return mail.containsFlags();
    }

    @Override
    public InputStream getInputStream() throws OXException {
        return mail.getInputStream();
    }

    @Override
    public void removeFlags() {
        mail.removeFlags();
    }

    @Override
    public void setFlags(final int flags) {
        mail.setFlags(flags);
    }

    @Override
    public int getEnclosedCount() throws OXException {
        return mail.getEnclosedCount();
    }

    @Override
    public void setFlag(final int flag, final boolean enable) throws OXException {
        mail.setFlag(flag, enable);
    }

    @Override
    public MailPart getEnclosedMailPart(final int index) throws OXException {
        return mail.getEnclosedMailPart(index);
    }

    @Override
    public boolean isPrevSeen() {
        return mail.isPrevSeen();
    }

    @Override
    public void loadContent() throws OXException {
        mail.loadContent();
    }

    @Override
    public boolean containsPrevSeen() {
        return mail.containsPrevSeen();
    }

    @Override
    public void removePrevSeen() {
        mail.removePrevSeen();
    }

    @Override
    public void setPrevSeen(final boolean prevSeen) {
        mail.setPrevSeen(prevSeen);
    }

    @Override
    public void writeTo(final OutputStream out) throws OXException {
        mail.writeTo(out);
    }

    @Override
    public int getThreadLevel() {
        return mail.getThreadLevel();
    }

    @Override
    public boolean containsThreadLevel() {
        return mail.containsThreadLevel();
    }

    @Override
    public void removeThreadLevel() {
        mail.removeThreadLevel();
    }

    @Override
    public void setThreadLevel(final int threadLevel) {
        mail.setThreadLevel(threadLevel);
    }

    @Override
    public String getSource() throws OXException {
        return mail.getSource();
    }

    @Override
    public String getSubject() {
        return mail.getSubject();
    }

    @Override
    public boolean containsSubject() {
        return mail.containsSubject();
    }

    @Override
    public byte[] getSourceBytes() throws OXException {
        return mail.getSourceBytes();
    }

    @Override
    public void removeSubject() {
        mail.removeSubject();
    }

    @Override
    public void setSubject(final String subject) {
        mail.setSubject(subject);
    }

    @Override
    public void prepareForCaching() {
        mail.prepareForCaching();
    }

    @Override
    public Date getSentDate() {
        return mail.getSentDate();
    }

    @Override
    public boolean containsSentDate() {
        return mail.containsSentDate();
    }

    @Override
    public void removeSentDate() {
        mail.removeSentDate();
    }

    @Override
    public void setSentDate(final Date sentDate) {
        mail.setSentDate(sentDate);
    }

    @Override
    public Date getReceivedDate() {
        return mail.getReceivedDate();
    }

    @Override
    public Date getReceivedDateDirect() {
        return mail.getReceivedDateDirect();
    }

    @Override
    public boolean containsReceivedDate() {
        return mail.containsReceivedDate();
    }

    @Override
    public void removeReceivedDate() {
        mail.removeReceivedDate();
    }

    @Override
    public void setReceivedDate(final Date receivedDate) {
        mail.setReceivedDate(receivedDate);
    }

    @Override
    public void addUserFlag(final String userFlag) {
        mail.addUserFlag(userFlag);
    }

    @Override
    public void addUserFlags(final String[] userFlags) {
        mail.addUserFlags(userFlags);
    }

    @Override
    public boolean containsUserFlags() {
        return mail.containsUserFlags();
    }

    @Override
    public void removeUserFlags() {
        mail.removeUserFlags();
    }

    @Override
    public String[] getUserFlags() {
        return mail.getUserFlags();
    }

    @Override
    public int getColorLabel() {
        return mail.getColorLabel();
    }

    @Override
    public boolean containsColorLabel() {
        return mail.containsColorLabel();
    }

    @Override
    public void removeColorLabel() {
        mail.removeColorLabel();
    }

    @Override
    public void setColorLabel(final int colorLabel) {
        mail.setColorLabel(colorLabel);
    }

    @Override
    public int getPriority() {
        return mail.getPriority();
    }

    @Override
    public boolean containsPriority() {
        return mail.containsPriority();
    }

    @Override
    public void removePriority() {
        mail.removePriority();
    }

    @Override
    public void setPriority(final int priority) {
        mail.setPriority(priority);
    }

    @Override
    public InternetAddress getDispositionNotification() {
        return mail.getDispositionNotification();
    }

    @Override
    public boolean containsDispositionNotification() {
        return mail.containsDispositionNotification();
    }

    @Override
    public void removeDispositionNotification() {
        mail.removeDispositionNotification();
    }

    @Override
    public void setDispositionNotification(final InternetAddress dispositionNotification) {
        mail.setDispositionNotification(dispositionNotification);
    }

    @Override
    public String getFolder() {
        return mail.getFolder();
    }

    @Override
    public boolean containsFolder() {
        return mail.containsFolder();
    }

    @Override
    public void removeFolder() {
        mail.removeFolder();
    }

    @Override
    public void setFolder(final String folder) {
        mail.setFolder(folder);
    }

    @Override
    public int getAccountId() {
        return mail.getAccountId();
    }

    @Override
    public boolean containsAccountId() {
        return mail.containsAccountId();
    }

    @Override
    public void removeAccountId() {
        mail.removeAccountId();
    }

    @Override
    public void setAccountId(final int accountId) {
        mail.setAccountId(accountId);
    }

    @Override
    public String getAccountName() {
        return mail.getAccountName();
    }

    @Override
    public boolean containsAccountName() {
        return mail.containsAccountName();
    }

    @Override
    public void removeAccountName() {
        mail.removeAccountName();
    }

    @Override
    public void setAccountName(final String accountName) {
        mail.setAccountName(accountName);
    }

    @Override
    public boolean hasAttachment() {
        return mail.hasAttachment();
    }

    @Override
    public boolean containsHasAttachment() {
        return mail.containsHasAttachment();
    }

    @Override
    public void removeHasAttachment() {
        mail.removeHasAttachment();
    }

    @Override
    public void setHasAttachment(final boolean hasAttachment) {
        mail.setHasAttachment(hasAttachment);
    }

    @Override
    public Object clone() {
        return mail.clone();
    }

    @Override
    public boolean isAppendVCard() {
        return mail.isAppendVCard();
    }

    @Override
    public boolean containsAppendVCard() {
        return mail.containsAppendVCard();
    }

    @Override
    public void removeAppendVCard() {
        mail.removeAppendVCard();
    }

    @Override
    public void setAppendVCard(final boolean appendVCard) {
        mail.setAppendVCard(appendVCard);
    }

    @Override
    public int getRecentCount() {
        return mail.getRecentCount();
    }

    @Override
    public boolean containsRecentCount() {
        return mail.containsRecentCount();
    }

    @Override
    public void removeRecentCount() {
        mail.removeRecentCount();
    }

    @Override
    public void setRecentCount(final int recentCount) {
        mail.setRecentCount(recentCount);
    }

    @Override
    public MailPath getMailPath() {
        return mail.getMailPath();
    }

    @Override
    public String getMailId() {
        return mail.getMailId();
    }

    @Override
    public void setMailId(final String id) {
        mail.setMailId(id);
    }

    @Override
    public int getUnreadMessages() {
        return mail.getUnreadMessages();
    }

    @Override
    public void setUnreadMessages(final int unreadMessages) {
        mail.setUnreadMessages(unreadMessages);
    }

    @Override
    public void setBodyPart(final TextBodyMailPart mailPart) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TextBodyMailPart getBodyPart() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MailPart removeEnclosedPart(final int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addEnclosedPart(final MailPart part) {
        throw new UnsupportedOperationException();
    }

}
