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
import com.openexchange.mail.FullnameArgument;
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
    public ThreadSortMailMessage(MailMessage delegatee) {
        super();
        this.delegatee = delegatee;
        childMessages = new ArrayList<ThreadSortMailMessage>(8);
    }

    @Override
    public void addReplyTo(InternetAddress addr) {
        delegatee.addReplyTo(addr);
    }

    @Override
    public void addReplyTo(InternetAddress[] addrs) {
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
    public void setHeader(String name, String value) {
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
    public void setMessageId(String messageId) {
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
    public void setReferences(String sReferences) {
        delegatee.setReferences(sReferences);
    }

    @Override
    public void setReferences(String[] references) {
        delegatee.setReferences(references);
    }

    @Override
    public void addBcc(InternetAddress addr) {
        delegatee.addBcc(addr);
    }

    @Override
    public void addBcc(InternetAddress[] addrs) {
        delegatee.addBcc(addrs);
    }

    @Override
    public void addCc(InternetAddress addr) {
        delegatee.addCc(addr);
    }

    @Override
    public void addCc(InternetAddress[] addrs) {
        delegatee.addCc(addrs);
    }

    @Override
    public void addFrom(InternetAddress addr) {
        delegatee.addFrom(addr);
    }

    @Override
    public void addFrom(InternetAddress[] addrs) {
        delegatee.addFrom(addrs);
    }

    @Override
    public void addHeader(String name, String value) {
        delegatee.addHeader(name, value);
    }

    @Override
    public void addHeaders(HeaderCollection headers) {
        delegatee.addHeaders(headers);
    }

    @Override
    public void addTo(InternetAddress addr) {
        delegatee.addTo(addr);
    }

    @Override
    public void addTo(InternetAddress[] addrs) {
        delegatee.addTo(addrs);
    }

    @Override
    public void addUserFlag(String userFlag) {
        delegatee.addUserFlag(userFlag);
    }

    @Override
    public void addUserFlags(String[] userFlags) {
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
    public boolean containsHeader(String name) {
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
    public boolean equals(Object obj) {
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
    public MailPart getEnclosedMailPart(int index) throws OXException {
        return delegatee.getEnclosedMailPart(index);
    }

    @Override
    public String getFileName() {
        return delegatee.getFileName();
    }

    @Override
    public String getFirstHeader(String name) {
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
    public String getHeader(String name, String delimiter) {
        return delegatee.getHeader(name, delimiter);
    }

    @Override
    public String getHeader(String name, char delimiter) {
        return delegatee.getHeader(name, delimiter);
    }

    @Override
    public String[] getHeader(String name) {
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
    public Iterator<Entry<String, String>> getMatchingHeaders(String[] matchingHeaders) {
        return delegatee.getMatchingHeaders(matchingHeaders);
    }

    @Override
    public MailPath getMsgref() {
        return delegatee.getMsgref();
    }

    @Override
    public Iterator<Entry<String, String>> getNonMatchingHeaders(String[] nonMatchingHeaders) {
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
    public boolean isHasAttachment() {
        return delegatee.isHasAttachment();
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
    public void removeHeader(String name) {
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
    public void setAccountId(int accountId) {
        delegatee.setAccountId(accountId);
    }

    @Override
    public void setAccountName(String accountName) {
        delegatee.setAccountName(accountName);
    }

    @Override
    public void setAppendVCard(boolean appendVCard) {
        delegatee.setAppendVCard(appendVCard);
    }

    @Override
    public void setColorLabel(int colorLabel) {
        delegatee.setColorLabel(colorLabel);
    }

    @Override
    public void setContentDisposition(ContentDisposition disposition) {
        delegatee.setContentDisposition(disposition);
    }

    @Override
    public void setContentDisposition(String disposition) throws OXException {
        delegatee.setContentDisposition(disposition);
    }

    @Override
    public void setContentId(String contentId) {
        delegatee.setContentId(contentId);
    }

    @Override
    public void setContentType(ContentType contentType) {
        delegatee.setContentType(contentType);
    }

    @Override
    public void setContentType(String contentType) throws OXException {
        delegatee.setContentType(contentType);
    }

    @Override
    public void setDispositionNotification(InternetAddress dispositionNotification) {
        delegatee.setDispositionNotification(dispositionNotification);
    }

    @Override
    public void setFileName(String fileName) {
        delegatee.setFileName(fileName);
    }

    @Override
    public void setFlag(int flag, boolean enable) throws OXException {
        delegatee.setFlag(flag, enable);
    }

    @Override
    public void setFlags(int flags) {
        delegatee.setFlags(flags);
    }

    @Override
    public void setFolder(String folder) {
        delegatee.setFolder(folder);
    }

    @Override
    public void setHasAttachment(boolean hasAttachment) {
        delegatee.setHasAttachment(hasAttachment);
    }

    @Override
    public void setMailId(String id) {
        delegatee.setMailId(id);
    }

    @Override
    public void setMsgref(MailPath msgref) {
        delegatee.setMsgref(msgref);
    }

    @Override
    public void setPrevSeen(boolean prevSeen) {
        delegatee.setPrevSeen(prevSeen);
    }

    @Override
    public void setPriority(int priority) {
        delegatee.setPriority(priority);
    }

    @Override
    public void setReceivedDate(Date receivedDate) {
        delegatee.setReceivedDate(receivedDate);
    }

    @Override
    public void setSentDate(Date sentDate) {
        delegatee.setSentDate(sentDate);
    }

    @Override
    public void setSequenceId(String sequenceId) {
        delegatee.setSequenceId(sequenceId);
    }

    @Override
    public void setSize(long size) {
        delegatee.setSize(size);
    }

    @Override
    public void setSubject(String subject) {
        delegatee.setSubject(subject);
    }

    @Override
    public void setSubject(String subject, boolean decoded) {
        delegatee.setSubject(subject, decoded);
    }

    @Override
    public void setThreadLevel(int threadLevel) {
        delegatee.setThreadLevel(threadLevel);
    }

    @Override
    public void setUnreadMessages(int unreadMessages) {
        delegatee.setUnreadMessages(unreadMessages);
    }

    @Override
    public String toString() {
        return delegatee.toString();
    }

    @Override
    public void writeTo(OutputStream out) throws OXException {
        delegatee.writeTo(out);
    }

    @Override
    public boolean hasHeaders(String... names) {
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
    public void setRecentCount(int recentCount) {
        delegatee.setRecentCount(recentCount);
    }

    @Override
    public void addFrom(Collection<InternetAddress> addrs) {
        delegatee.addFrom(addrs);
    }

    @Override
    public void removeFromPersonals() {
        delegatee.removeFromPersonals();
    }

    @Override
    public void addTo(Collection<InternetAddress> addrs) {
        delegatee.addTo(addrs);
    }

    @Override
    public void removeToPersonals() {
        delegatee.removeToPersonals();
    }

    @Override
    public void addCc(Collection<InternetAddress> addrs) {
        delegatee.addCc(addrs);
    }

    @Override
    public void removeCcPersonals() {
        delegatee.removeCcPersonals();
    }

    @Override
    public void addBcc(Collection<InternetAddress> addrs) {
        delegatee.addBcc(addrs);
    }

    @Override
    public void removeBccPersonals() {
        delegatee.removeBccPersonals();
    }

    @Override
    public InternetAddress[] getAllRecipients() {
        return delegatee.getAllRecipients();
    }

    @Override
    public void addReplyTo(Collection<InternetAddress> addrs) {
        delegatee.addReplyTo(addrs);
    }

    @Override
    public boolean isSubjectDecoded() {
        return delegatee.isSubjectDecoded();
    }

    @Override
    public void addUserFlags(Collection<String> userFlags) {
        delegatee.addUserFlags(userFlags);
    }

    @Override
    public FullnameArgument getOriginalFolder() {
        return delegatee.getOriginalFolder();
    }

    @Override
    public boolean containsOriginalFolder() {
        return delegatee.containsOriginalFolder();
    }

    @Override
    public void removeOriginalFolder() {
        delegatee.removeOriginalFolder();
    }

    @Override
    public void setOriginalFolder(FullnameArgument originalFolder) {
        delegatee.setOriginalFolder(originalFolder);
    }

    @Override
    public String getTextPreview() {
        return delegatee.getTextPreview();
    }

    @Override
    public boolean containsTextPreview() {
        return delegatee.containsTextPreview();
    }

    @Override
    public void removeTextPreview() {
        delegatee.removeTextPreview();
    }

    @Override
    public void setTextPreview(String textPreview) {
        delegatee.setTextPreview(textPreview);
    }

    @Override
    public String getOriginalId() {
        return delegatee.getOriginalId();
    }

    @Override
    public boolean containsOriginalId() {
        return delegatee.containsOriginalId();
    }

    @Override
    public void removeOriginalId() {
        delegatee.removeOriginalId();
    }

    @Override
    public void setOriginalId(String originalId) {
        delegatee.setOriginalId(originalId);
    }

    @Override
    public String[] getReferencesOrInReplyTo() {
        return delegatee.getReferencesOrInReplyTo();
    }

    @Override
    public void setSecurityInfo(SecurityInfo securityInfo) {
        delegatee.setSecurityInfo(securityInfo);
    }

    @Override
    public SecurityInfo getSecurityInfo() {
        return delegatee.getSecurityInfo();
    }

    @Override
    public boolean containsSecurityInfo() {
        return delegatee.containsSecurityInfo();
    }

    @Override
    public void removeSecurityInfo() {
        delegatee.removeSecurityInfo();
    }

    @Override
    public void setSecurityResult(SecurityResult result) {
        delegatee.setSecurityResult(result);
    }

    @Override
    public SecurityResult getSecurityResult() {
        return delegatee.getSecurityResult();
    }

    @Override
    public boolean hasSecurityResult() {
        return delegatee.hasSecurityResult();
    }

    @Override
    public boolean containsSecurityResult() {
        return delegatee.containsSecurityResult();
    }

    @Override
    public void removeSecurityResult() {
        delegatee.removeSecurityResult();
    }

    @Override
    public void setAuthenticityResult(MailAuthenticityResult authenticationResult) {
        delegatee.setAuthenticityResult(authenticationResult);
    }

    @Override
    public MailAuthenticityResult getAuthenticityResult() {
        return delegatee.getAuthenticityResult();
    }

    @Override
    public boolean hasAuthenticityResult() {
        return delegatee.hasAuthenticityResult();
    }

    @Override
    public boolean containsAuthenticityResult() {
        return delegatee.containsAuthenticityResult();
    }

    @Override
    public void removeAuthenticityResult() {
        delegatee.removeAuthenticityResult();
    }

    @Override
    public boolean isAlternativeHasAttachment() {
        return delegatee.isAlternativeHasAttachment();
    }

    @Override
    public boolean containsAlternativeHasAttachment() {
        return delegatee.containsAlternativeHasAttachment();
    }

    @Override
    public void removeAlternativeHasAttachment() {
        delegatee.removeAlternativeHasAttachment();
    }

    @Override
    public void setAlternativeHasAttachment(boolean hasAttachment) {
        delegatee.setAlternativeHasAttachment(hasAttachment);
    }

    /**
     * Sets specified child messages.
     *
     * @param mailMessages The child messages
     */
    public void setChildMessages(Collection<ThreadSortMailMessage> mailMessages) {
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
    public void addChildMessage(ThreadSortMailMessage mailMessage) {
        childMessages.add(mailMessage);
    }

    /**
     * Adds specified child messages.
     *
     * @param mailMessages The child messages
     */
    public void addChildMessages(Collection<ThreadSortMailMessage> mailMessages) {
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
