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

package com.openexchange.mail.dataobjects;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.activation.DataHandler;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.HeaderCollection;

/**
 * {@link ThreadSortMailMessage} - Extends {@link MailMessage} by capability to carry child messages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadSortMailMessage extends MailMessage {

    private static final long serialVersionUID = 7632979796213310250L;

    private final List<ThreadSortMailMessage> childMessages;

    private final MailMessage delegatee;

    /**
     * Initializes a new {@link ThreadSortMailMessage}.
     *
     * @param delegatee
     */
    public ThreadSortMailMessage(final MailMessage delegatee) {
        super();
        this.delegatee = delegatee;
        childMessages = new ArrayList<ThreadSortMailMessage>(8);
    }

    @Override
    public void addReplyTo(final InternetAddress addr) {
        delegatee.addReplyTo(addr);
    }

    @Override
    public void addReplyTo(final InternetAddress[] addrs) {
        delegatee.addReplyTo(addrs);
    }

    @Override
    public boolean containsReplyTo() {
        return delegatee.containsReplyTo();
    }

    @Override
    public void removeReplyTo() {
        delegatee.removeReplyTo();
    }

    @Override
    public InternetAddress[] getReplyTo() {
        return delegatee.getReplyTo();
    }

    @Override
    public boolean isUnseen() {
        return delegatee.isUnseen();
    }

    @Override
    public void setHeader(final String name, final String value) {
        delegatee.setHeader(name, value);
    }

    @Override
    public String getMessageId() {
        return delegatee.getMessageId();
    }

    @Override
    public boolean containsMessageId() {
        return delegatee.containsMessageId();
    }

    @Override
    public void removeMessageId() {
        delegatee.removeMessageId();
    }

    @Override
    public void setMessageId(final String messageId) {
        delegatee.setMessageId(messageId);
    }

    @Override
    public String getInReplyTo() {
        return delegatee.getInReplyTo();
    }

    @Override
    public String[] getReferences() {
        return delegatee.getReferences();
    }

    @Override
    public boolean containsReferences() {
        return delegatee.containsReferences();
    }

    @Override
    public void removeReferences() {
        delegatee.removeReferences();
    }

    @Override
    public void setReferences(final String sReferences) {
        delegatee.setReferences(sReferences);
    }

    @Override
    public void setReferences(final String[] references) {
        delegatee.setReferences(references);
    }

    @Override
    public void addBcc(final InternetAddress addr) {
        delegatee.addBcc(addr);
    }

    @Override
    public void addBcc(final InternetAddress[] addrs) {
        delegatee.addBcc(addrs);
    }

    @Override
    public void addCc(final InternetAddress addr) {
        delegatee.addCc(addr);
    }

    @Override
    public void addCc(final InternetAddress[] addrs) {
        delegatee.addCc(addrs);
    }

    @Override
    public void addFrom(final InternetAddress addr) {
        delegatee.addFrom(addr);
    }

    @Override
    public void addFrom(final InternetAddress[] addrs) {
        delegatee.addFrom(addrs);
    }

    @Override
    public void addHeader(final String name, final String value) {
        delegatee.addHeader(name, value);
    }

    @Override
    public void addHeaders(final HeaderCollection headers) {
        delegatee.addHeaders(headers);
    }

    @Override
    public void addTo(final InternetAddress addr) {
        delegatee.addTo(addr);
    }

    @Override
    public void addTo(final InternetAddress[] addrs) {
        delegatee.addTo(addrs);
    }

    @Override
    public void addUserFlag(final String userFlag) {
        delegatee.addUserFlag(userFlag);
    }

    @Override
    public void addUserFlags(final String[] userFlags) {
        delegatee.addUserFlags(userFlags);
    }

    @Override
    public Object clone() {
        return delegatee.clone();
    }

    @Override
    public boolean containsAccountId() {
        return delegatee.containsAccountId();
    }

    @Override
    public boolean containsAccountName() {
        return delegatee.containsAccountName();
    }

    @Override
    public boolean containsAppendVCard() {
        return delegatee.containsAppendVCard();
    }

    @Override
    public boolean containsBcc() {
        return delegatee.containsBcc();
    }

    @Override
    public boolean containsCc() {
        return delegatee.containsCc();
    }

    @Override
    public boolean containsColorLabel() {
        return delegatee.containsColorLabel();
    }

    @Override
    public boolean containsContentDisposition() {
        return delegatee.containsContentDisposition();
    }

    @Override
    public boolean containsContentId() {
        return delegatee.containsContentId();
    }

    @Override
    public boolean containsContentType() {
        return delegatee.containsContentType();
    }

    @Override
    public boolean containsDispositionNotification() {
        return delegatee.containsDispositionNotification();
    }

    @Override
    public boolean containsFileName() {
        return delegatee.containsFileName();
    }

    @Override
    public boolean containsFlags() {
        return delegatee.containsFlags();
    }

    @Override
    public boolean containsFolder() {
        return delegatee.containsFolder();
    }

    @Override
    public boolean containsFrom() {
        return delegatee.containsFrom();
    }

    @Override
    public boolean containsHasAttachment() {
        return delegatee.containsHasAttachment();
    }

    @Override
    public boolean containsHeader(final String name) {
        return delegatee.containsHeader(name);
    }

    @Override
    public boolean containsHeaders() {
        return delegatee.containsHeaders();
    }

    @Override
    public boolean containsMsgref() {
        return delegatee.containsMsgref();
    }

    @Override
    public boolean containsPrevSeen() {
        return delegatee.containsPrevSeen();
    }

    @Override
    public boolean containsPriority() {
        return delegatee.containsPriority();
    }

    @Override
    public boolean containsReceivedDate() {
        return delegatee.containsReceivedDate();
    }

    @Override
    public boolean containsSentDate() {
        return delegatee.containsSentDate();
    }

    @Override
    public boolean containsSequenceId() {
        return delegatee.containsSequenceId();
    }

    @Override
    public boolean containsSize() {
        return delegatee.containsSize();
    }

    @Override
    public boolean containsSubject() {
        return delegatee.containsSubject();
    }

    @Override
    public boolean containsThreadLevel() {
        return delegatee.containsThreadLevel();
    }

    @Override
    public boolean containsTo() {
        return delegatee.containsTo();
    }

    @Override
    public boolean containsUserFlags() {
        return delegatee.containsUserFlags();
    }

    @Override
    public boolean equals(final Object obj) {
        return delegatee.equals(obj);
    }

    @Override
    public int getAccountId() {
        return delegatee.getAccountId();
    }

    @Override
    public String getAccountName() {
        return delegatee.getAccountName();
    }

    @Override
    public InternetAddress[] getBcc() {
        return delegatee.getBcc();
    }

    @Override
    public InternetAddress[] getCc() {
        return delegatee.getCc();
    }

    @Override
    public int getColorLabel() {
        return delegatee.getColorLabel();
    }

    @Override
    public Object getContent() throws OXException {
        return delegatee.getContent();
    }

    @Override
    public ContentDisposition getContentDisposition() {
        return delegatee.getContentDisposition();
    }

    @Override
    public String getContentId() {
        return delegatee.getContentId();
    }

    @Override
    public ContentType getContentType() {
        return delegatee.getContentType();
    }

    @Override
    public DataHandler getDataHandler() throws OXException {
        return delegatee.getDataHandler();
    }

    @Override
    public InternetAddress getDispositionNotification() {
        return delegatee.getDispositionNotification();
    }

    @Override
    public int getEnclosedCount() throws OXException {
        return delegatee.getEnclosedCount();
    }

    @Override
    public MailPart getEnclosedMailPart(final int index) throws OXException {
        return delegatee.getEnclosedMailPart(index);
    }

    @Override
    public String getFileName() {
        return delegatee.getFileName();
    }

    @Override
    public String getFirstHeader(final String name) {
        return delegatee.getFirstHeader(name);
    }

    @Override
    public int getFlags() {
        return delegatee.getFlags();
    }

    @Override
    public String getFolder() {
        return delegatee.getFolder();
    }

    @Override
    public InternetAddress[] getFrom() {
        return delegatee.getFrom();
    }

    @Override
    public String getHeader(final String name, final String delimiter) {
        return delegatee.getHeader(name, delimiter);
    }

    @Override
    public String getHeader(final String name, final char delimiter) {
        return delegatee.getHeader(name, delimiter);
    }

    @Override
    public String[] getHeader(final String name) {
        return delegatee.getHeader(name);
    }

    @Override
    public HeaderCollection getHeaders() {
        return delegatee.getHeaders();
    }

    @Override
    public Iterator<Entry<String, String>> getHeadersIterator() {
        return delegatee.getHeadersIterator();
    }

    @Override
    public int getHeadersSize() {
        return delegatee.getHeadersSize();
    }

    @Override
    public InputStream getInputStream() throws OXException {
        return delegatee.getInputStream();
    }

    @Override
    public String getMailId() {
        return delegatee.getMailId();
    }

    @Override
    public MailPath getMailPath() {
        return delegatee.getMailPath();
    }

    @Override
    public Iterator<Entry<String, String>> getMatchingHeaders(final String[] matchingHeaders) {
        return delegatee.getMatchingHeaders(matchingHeaders);
    }

    @Override
    public MailPath getMsgref() {
        return delegatee.getMsgref();
    }

    @Override
    public Iterator<Entry<String, String>> getNonMatchingHeaders(final String[] nonMatchingHeaders) {
        return delegatee.getNonMatchingHeaders(nonMatchingHeaders);
    }

    @Override
    public int getPriority() {
        return delegatee.getPriority();
    }

    @Override
    public Date getReceivedDate() {
        return delegatee.getReceivedDate();
    }

    @Override
    public Date getSentDate() {
        return delegatee.getSentDate();
    }

    @Override
    public String getSequenceId() {
        return delegatee.getSequenceId();
    }

    @Override
    public long getSize() {
        return delegatee.getSize();
    }

    @Override
    public String getSource() throws OXException {
        return delegatee.getSource();
    }

    @Override
    public byte[] getSourceBytes() throws OXException {
        return delegatee.getSourceBytes();
    }

    @Override
    public String getSubject() {
        return delegatee.getSubject();
    }

    @Override
    public int getThreadLevel() {
        return delegatee.getThreadLevel();
    }

    @Override
    public InternetAddress[] getTo() {
        return delegatee.getTo();
    }

    @Override
    public int getUnreadMessages() {
        return delegatee.getUnreadMessages();
    }

    @Override
    public String[] getUserFlags() {
        return delegatee.getUserFlags();
    }

    @Override
    public boolean hasAttachment() {
        return delegatee.hasAttachment();
    }

    @Override
    public boolean hasEnclosedParts() throws OXException {
        return delegatee.hasEnclosedParts();
    }

    @Override
    public int hashCode() {
        return delegatee.hashCode();
    }

    @Override
    public boolean isAnswered() {
        return delegatee.isAnswered();
    }

    @Override
    public boolean isAppendVCard() {
        return delegatee.isAppendVCard();
    }

    @Override
    public boolean isDeleted() {
        return delegatee.isDeleted();
    }

    @Override
    public boolean isDraft() {
        return delegatee.isDraft();
    }

    @Override
    public boolean isFlagged() {
        return delegatee.isFlagged();
    }

    @Override
    public boolean isForwarded() {
        return delegatee.isForwarded();
    }

    @Override
    public boolean isPrevSeen() {
        return delegatee.isPrevSeen();
    }

    @Override
    public boolean isReadAcknowledgment() {
        return delegatee.isReadAcknowledgment();
    }

    @Override
    public boolean isRecent() {
        return delegatee.isRecent();
    }

    @Override
    public boolean isSeen() {
        return delegatee.isSeen();
    }

    @Override
    public boolean isSpam() {
        return delegatee.isSpam();
    }

    @Override
    public boolean isUser() {
        return delegatee.isUser();
    }

    @Override
    public void loadContent() throws OXException {
        delegatee.loadContent();
    }

    @Override
    public void prepareForCaching() {
        delegatee.prepareForCaching();
    }

    @Override
    public void removeAccountId() {
        delegatee.removeAccountId();
    }

    @Override
    public void removeAccountName() {
        delegatee.removeAccountName();
    }

    @Override
    public void removeAppendVCard() {
        delegatee.removeAppendVCard();
    }

    @Override
    public void removeBcc() {
        delegatee.removeBcc();
    }

    @Override
    public void removeCc() {
        delegatee.removeCc();
    }

    @Override
    public void removeColorLabel() {
        delegatee.removeColorLabel();
    }

    @Override
    public void removeContentDisposition() {
        delegatee.removeContentDisposition();
    }

    @Override
    public void removeContentId() {
        delegatee.removeContentId();
    }

    @Override
    public void removeContentType() {
        delegatee.removeContentType();
    }

    @Override
    public void removeDispositionNotification() {
        delegatee.removeDispositionNotification();
    }

    @Override
    public void removeFileName() {
        delegatee.removeFileName();
    }

    @Override
    public void removeFlags() {
        delegatee.removeFlags();
    }

    @Override
    public void removeFolder() {
        delegatee.removeFolder();
    }

    @Override
    public void removeFrom() {
        delegatee.removeFrom();
    }

    @Override
    public void removeHasAttachment() {
        delegatee.removeHasAttachment();
    }

    @Override
    public void removeHeader(final String name) {
        delegatee.removeHeader(name);
    }

    @Override
    public void removeHeaders() {
        delegatee.removeHeaders();
    }

    @Override
    public void removeMsgref() {
        delegatee.removeMsgref();
    }

    @Override
    public void removePrevSeen() {
        delegatee.removePrevSeen();
    }

    @Override
    public void removePriority() {
        delegatee.removePriority();
    }

    @Override
    public void removeReceivedDate() {
        delegatee.removeReceivedDate();
    }

    @Override
    public void removeSentDate() {
        delegatee.removeSentDate();
    }

    @Override
    public void removeSequenceId() {
        delegatee.removeSequenceId();
    }

    @Override
    public void removeSize() {
        delegatee.removeSize();
    }

    @Override
    public void removeSubject() {
        delegatee.removeSubject();
    }

    @Override
    public void removeThreadLevel() {
        delegatee.removeThreadLevel();
    }

    @Override
    public void removeTo() {
        delegatee.removeTo();
    }

    @Override
    public void removeUserFlags() {
        delegatee.removeUserFlags();
    }

    @Override
    public void setAccountId(final int accountId) {
        delegatee.setAccountId(accountId);
    }

    @Override
    public void setAccountName(final String accountName) {
        delegatee.setAccountName(accountName);
    }

    @Override
    public void setAppendVCard(final boolean appendVCard) {
        delegatee.setAppendVCard(appendVCard);
    }

    @Override
    public void setColorLabel(final int colorLabel) {
        delegatee.setColorLabel(colorLabel);
    }

    @Override
    public void setContentDisposition(final ContentDisposition disposition) {
        delegatee.setContentDisposition(disposition);
    }

    @Override
    public void setContentDisposition(final String disposition) throws OXException {
        delegatee.setContentDisposition(disposition);
    }

    @Override
    public void setContentId(final String contentId) {
        delegatee.setContentId(contentId);
    }

    @Override
    public void setContentType(final ContentType contentType) {
        delegatee.setContentType(contentType);
    }

    @Override
    public void setContentType(final String contentType) throws OXException {
        delegatee.setContentType(contentType);
    }

    @Override
    public void setDispositionNotification(final InternetAddress dispositionNotification) {
        delegatee.setDispositionNotification(dispositionNotification);
    }

    @Override
    public void setFileName(final String fileName) {
        delegatee.setFileName(fileName);
    }

    @Override
    public void setFlag(final int flag, final boolean enable) throws OXException {
        delegatee.setFlag(flag, enable);
    }

    @Override
    public void setFlags(final int flags) {
        delegatee.setFlags(flags);
    }

    @Override
    public void setFolder(final String folder) {
        delegatee.setFolder(folder);
    }

    @Override
    public void setHasAttachment(final boolean hasAttachment) {
        delegatee.setHasAttachment(hasAttachment);
    }

    @Override
    public void setMailId(final String id) {
        delegatee.setMailId(id);
    }

    @Override
    public void setMsgref(final MailPath msgref) {
        delegatee.setMsgref(msgref);
    }

    @Override
    public void setPrevSeen(final boolean prevSeen) {
        delegatee.setPrevSeen(prevSeen);
    }

    @Override
    public void setPriority(final int priority) {
        delegatee.setPriority(priority);
    }

    @Override
    public void setReceivedDate(final Date receivedDate) {
        delegatee.setReceivedDate(receivedDate);
    }

    @Override
    public void setSentDate(final Date sentDate) {
        delegatee.setSentDate(sentDate);
    }

    @Override
    public void setSequenceId(final String sequenceId) {
        delegatee.setSequenceId(sequenceId);
    }

    @Override
    public void setSize(final long size) {
        delegatee.setSize(size);
    }

    @Override
    public void setSubject(final String subject) {
        delegatee.setSubject(subject);
    }

    @Override
    public void setThreadLevel(final int threadLevel) {
        delegatee.setThreadLevel(threadLevel);
    }

    @Override
    public void setUnreadMessages(final int unreadMessages) {
        delegatee.setUnreadMessages(unreadMessages);
    }

    @Override
    public String toString() {
        return delegatee.toString();
    }

    @Override
    public void writeTo(final OutputStream out) throws OXException {
        delegatee.writeTo(out);
    }

    @Override
    public boolean hasHeaders(final String... names) {
        return delegatee.hasHeaders(names);
    }

    @Override
    public Date getReceivedDateDirect() {
        return delegatee.getReceivedDateDirect();
    }

    @Override
    public int getRecentCount() {
        return delegatee.getRecentCount();
    }

    @Override
    public boolean containsRecentCount() {
        return delegatee.containsRecentCount();
    }

    @Override
    public void removeRecentCount() {
        delegatee.removeRecentCount();
    }

    @Override
    public void setRecentCount(final int recentCount) {
        delegatee.setRecentCount(recentCount);
    }

    /**
     * Sets specified child messages.
     *
     * @param mailMessages The child messages
     */
    public void setChildMessages(final Collection<ThreadSortMailMessage> mailMessages) {
        childMessages.clear();
        if (null != mailMessages && !mailMessages.isEmpty()) {
            childMessages.addAll(mailMessages);
        }
    }

    /**
     * Adds specified child message.
     *
     * @param mailMessage The child message
     */
    public void addChildMessage(final ThreadSortMailMessage mailMessage) {
        childMessages.add(mailMessage);
    }

    /**
     * Adds specified child messages.
     *
     * @param mailMessages The child messages
     */
    public void addChildMessages(final Collection<ThreadSortMailMessage> mailMessages) {
        childMessages.addAll(mailMessages);
    }

    /**
     * Gets child messages.
     *
     * @return The child messages
     */
    public List<ThreadSortMailMessage> getChildMessages() {
        return childMessages;
    }

    /**
     * Gets the original message.
     *
     * @return The original message
     */
    public MailMessage getOriginalMessage() {
        return delegatee;
    }

}
