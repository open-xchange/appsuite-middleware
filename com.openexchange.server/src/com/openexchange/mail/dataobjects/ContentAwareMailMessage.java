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
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.activation.DataHandler;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.HeaderCollection;

/**
 * {@link ContentAwareMailMessage} - Enhances {@link MailMessage} by {@link #getPrimaryContent()} method.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContentAwareMailMessage extends MailMessage {

    private static final long serialVersionUID = -4272526344614740235L;

    private final MailMessage message;

    private final String primaryContent;

    /**
     * Initializes a new {@link ContentAwareMailMessage}.
     *
     * @param primaryContent The primary content
     * @param message The delegate message
     */
    public ContentAwareMailMessage(String primaryContent, MailMessage message) {
        super();
        this.message = message;
        this.primaryContent = primaryContent;
    }

    @Override
    public String getMessageId() {
        return message.getMessageId();
    }

    @Override
    public boolean containsMessageId() {
        return message.containsMessageId();
    }

    @Override
    public void removeMessageId() {
        message.removeMessageId();
    }

    @Override
    public void setMessageId(String messageId) {
        message.setMessageId(messageId);
    }

    @Override
    public String getInReplyTo() {
        return message.getInReplyTo();
    }

    @Override
    public String[] getReferences() {
        return message.getReferences();
    }

    @Override
    public boolean containsReferences() {
        return message.containsReferences();
    }

    @Override
    public void removeReferences() {
        message.removeReferences();
    }

    @Override
    public void setReferences(String sReferences) {
        message.setReferences(sReferences);
    }

    @Override
    public void setReferences(String[] references) {
        message.setReferences(references);
    }

    @Override
    public void addReplyTo(InternetAddress addr) {
        message.addReplyTo(addr);
    }

    @Override
    public void addReplyTo(InternetAddress[] addrs) {
        message.addReplyTo(addrs);
    }

    @Override
    public boolean containsReplyTo() {
        return message.containsReplyTo();
    }

    @Override
    public void removeReplyTo() {
        message.removeReplyTo();
    }

    @Override
    public InternetAddress[] getReplyTo() {
        return message.getReplyTo();
    }

    @Override
    public boolean isUnseen() {
        return message.isUnseen();
    }

    /**
     * Gets the primary content
     *
     * @return The primary content or <code>null</code> if absent
     */
    public String getPrimaryContent() {
        return primaryContent;
    }

    @Override
    public ContentType getContentType() {
        return message.getContentType();
    }

    @Override
    public boolean containsContentType() {
        return message.containsContentType();
    }

    @Override
    public void removeContentType() {
        message.removeContentType();
    }

    @Override
    public void setContentType(ContentType contentType) {
        message.setContentType(contentType);
    }

    @Override
    public void setContentType(String contentType) throws OXException {
        message.setContentType(contentType);
    }

    @Override
    public ContentDisposition getContentDisposition() {
        return message.getContentDisposition();
    }

    @Override
    public boolean containsContentDisposition() {
        return message.containsContentDisposition();
    }

    @Override
    public void removeContentDisposition() {
        message.removeContentDisposition();
    }

    @Override
    public void setContentDisposition(String disposition) throws OXException {
        message.setContentDisposition(disposition);
    }

    @Override
    public void setContentDisposition(ContentDisposition disposition) {
        message.setContentDisposition(disposition);
    }

    @Override
    public String toString() {
        return message.toString();
    }

    @Override
    public String getFileName() {
        return message.getFileName();
    }

    @Override
    public boolean containsFileName() {
        return message.containsFileName();
    }

    @Override
    public void removeFileName() {
        message.removeFileName();
    }

    @Override
    public void setFileName(String fileName) {
        message.setFileName(fileName);
    }

    @Override
    public void addHeader(String name, String value) {
        message.addHeader(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
        message.setHeader(name, value);
    }

    @Override
    public void addHeaders(HeaderCollection headers) {
        message.addHeaders(headers);
    }

    @Override
    public boolean containsHeaders() {
        return message.containsHeaders();
    }

    @Override
    public void removeHeaders() {
        message.removeHeaders();
    }

    @Override
    public int getHeadersSize() {
        return message.getHeadersSize();
    }

    @Override
    public Iterator<Entry<String, String>> getHeadersIterator() {
        return message.getHeadersIterator();
    }

    @Override
    public boolean containsHeader(String name) {
        return message.containsHeader(name);
    }

    @Override
    public void addFrom(InternetAddress addr) {
        message.addFrom(addr);
    }

    @Override
    public String[] getHeader(String name) {
        return message.getHeader(name);
    }

    @Override
    public void addFrom(InternetAddress[] addrs) {
        message.addFrom(addrs);
    }

    @Override
    public String getFirstHeader(String name) {
        return message.getFirstHeader(name);
    }

    @Override
    public boolean containsFrom() {
        return message.containsFrom();
    }

    @Override
    public String getHeader(String name, String delimiter) {
        return message.getHeader(name, delimiter);
    }

    @Override
    public void removeFrom() {
        message.removeFrom();
    }

    @Override
    public InternetAddress[] getFrom() {
        return message.getFrom();
    }

    @Override
    public String getHeader(String name, char delimiter) {
        return message.getHeader(name, delimiter);
    }

    @Override
    public void addTo(InternetAddress addr) {
        message.addTo(addr);
    }

    @Override
    public void addTo(InternetAddress[] addrs) {
        message.addTo(addrs);
    }

    @Override
    public HeaderCollection getHeaders() {
        return message.getHeaders();
    }

    @Override
    public boolean containsTo() {
        return message.containsTo();
    }

    @Override
    public Iterator<Entry<String, String>> getNonMatchingHeaders(String[] nonMatchingHeaders) {
        return message.getNonMatchingHeaders(nonMatchingHeaders);
    }

    @Override
    public void removeTo() {
        message.removeTo();
    }

    @Override
    public InternetAddress[] getTo() {
        return message.getTo();
    }

    @Override
    public Iterator<Entry<String, String>> getMatchingHeaders(String[] matchingHeaders) {
        return message.getMatchingHeaders(matchingHeaders);
    }

    @Override
    public void removeHeader(String name) {
        message.removeHeader(name);
    }

    @Override
    public void addCc(InternetAddress addr) {
        message.addCc(addr);
    }

    @Override
    public boolean hasHeaders(String... names) {
        return message.hasHeaders(names);
    }

    @Override
    public void addCc(InternetAddress[] addrs) {
        message.addCc(addrs);
    }

    @Override
    public long getSize() {
        return message.getSize();
    }

    @Override
    public boolean containsCc() {
        return message.containsCc();
    }

    @Override
    public boolean containsSize() {
        return message.containsSize();
    }

    @Override
    public void removeSize() {
        message.removeSize();
    }

    @Override
    public void removeCc() {
        message.removeCc();
    }

    @Override
    public void setSize(long size) {
        message.setSize(size);
    }

    @Override
    public InternetAddress[] getCc() {
        return message.getCc();
    }

    @Override
    public String getContentId() {
        return message.getContentId();
    }

    @Override
    public boolean containsContentId() {
        return message.containsContentId();
    }

    @Override
    public void addBcc(InternetAddress addr) {
        message.addBcc(addr);
    }

    @Override
    public void removeContentId() {
        message.removeContentId();
    }

    @Override
    public void setContentId(String contentId) {
        message.setContentId(contentId);
    }

    @Override
    public void addBcc(InternetAddress[] addrs) {
        message.addBcc(addrs);
    }

    @Override
    public String getSequenceId() {
        return message.getSequenceId();
    }

    @Override
    public boolean containsSequenceId() {
        return message.containsSequenceId();
    }

    @Override
    public boolean containsBcc() {
        return message.containsBcc();
    }

    @Override
    public void removeSequenceId() {
        message.removeSequenceId();
    }

    @Override
    public void setSequenceId(String sequenceId) {
        message.setSequenceId(sequenceId);
    }

    @Override
    public void removeBcc() {
        message.removeBcc();
    }

    @Override
    public InternetAddress[] getBcc() {
        return message.getBcc();
    }

    @Override
    public MailPath getMsgref() {
        return message.getMsgref();
    }

    @Override
    public int getFlags() {
        return message.getFlags();
    }

    @Override
    public boolean containsMsgref() {
        return message.containsMsgref();
    }

    @Override
    public boolean isAnswered() {
        return message.isAnswered();
    }

    @Override
    public void removeMsgref() {
        message.removeMsgref();
    }

    @Override
    public boolean isDeleted() {
        return message.isDeleted();
    }

    @Override
    public void setMsgref(MailPath msgref) {
        message.setMsgref(msgref);
    }

    @Override
    public boolean isDraft() {
        return message.isDraft();
    }

    @Override
    public boolean isFlagged() {
        return message.isFlagged();
    }

    @Override
    public boolean isRecent() {
        return message.isRecent();
    }

    @Override
    public boolean isSeen() {
        return message.isSeen();
    }

    @Override
    public boolean isSpam() {
        return message.isSpam();
    }

    @Override
    public boolean hasEnclosedParts() throws OXException {
        return message.hasEnclosedParts();
    }

    @Override
    public boolean isForwarded() {
        return message.isForwarded();
    }

    @Override
    public boolean isReadAcknowledgment() {
        return message.isReadAcknowledgment();
    }

    @Override
    public Object getContent() throws OXException {
        return message.getContent();
    }

    @Override
    public boolean isUser() {
        return message.isUser();
    }

    @Override
    public boolean containsFlags() {
        return message.containsFlags();
    }

    @Override
    public DataHandler getDataHandler() throws OXException {
        return message.getDataHandler();
    }

    @Override
    public void removeFlags() {
        message.removeFlags();
    }

    @Override
    public void setFlags(int flags) {
        message.setFlags(flags);
    }

    @Override
    public InputStream getInputStream() throws OXException {
        return message.getInputStream();
    }

    @Override
    public void setFlag(int flag, boolean enable) throws OXException {
        message.setFlag(flag, enable);
    }

    @Override
    public int getEnclosedCount() throws OXException {
        return message.getEnclosedCount();
    }

    @Override
    public boolean isPrevSeen() {
        return message.isPrevSeen();
    }

    @Override
    public MailPart getEnclosedMailPart(int index) throws OXException {
        return message.getEnclosedMailPart(index);
    }

    @Override
    public boolean containsPrevSeen() {
        return message.containsPrevSeen();
    }

    @Override
    public void loadContent() throws OXException {
        message.loadContent();
    }

    @Override
    public void removePrevSeen() {
        message.removePrevSeen();
    }

    @Override
    public void setPrevSeen(boolean prevSeen) {
        message.setPrevSeen(prevSeen);
    }

    @Override
    public int getThreadLevel() {
        return message.getThreadLevel();
    }

    @Override
    public void writeTo(OutputStream out) throws OXException {
        message.writeTo(out);
    }

    @Override
    public boolean containsThreadLevel() {
        return message.containsThreadLevel();
    }

    @Override
    public void removeThreadLevel() {
        message.removeThreadLevel();
    }

    @Override
    public void setThreadLevel(int threadLevel) {
        message.setThreadLevel(threadLevel);
    }

    @Override
    public String getSubject() {
        return message.getSubject();
    }

    @Override
    public String getSource() throws OXException {
        return message.getSource();
    }

    @Override
    public boolean containsSubject() {
        return message.containsSubject();
    }

    @Override
    public byte[] getSourceBytes() throws OXException {
        return message.getSourceBytes();
    }

    @Override
    public void removeSubject() {
        message.removeSubject();
    }

    @Override
    public void setSubject(String subject) {
        message.setSubject(subject);
    }

    @Override
    public void setSubject(String subject, boolean decoded) {
        message.setSubject(subject, decoded);
    }

    @Override
    public void prepareForCaching() {
        message.prepareForCaching();
    }

    @Override
    public Date getSentDate() {
        return message.getSentDate();
    }

    @Override
    public boolean containsSentDate() {
        return message.containsSentDate();
    }

    @Override
    public void removeSentDate() {
        message.removeSentDate();
    }

    @Override
    public void setSentDate(Date sentDate) {
        message.setSentDate(sentDate);
    }

    @Override
    public Date getReceivedDate() {
        return message.getReceivedDate();
    }

    @Override
    public Date getReceivedDateDirect() {
        return message.getReceivedDateDirect();
    }

    @Override
    public boolean containsReceivedDate() {
        return message.containsReceivedDate();
    }

    @Override
    public void removeReceivedDate() {
        message.removeReceivedDate();
    }

    @Override
    public void setReceivedDate(Date receivedDate) {
        message.setReceivedDate(receivedDate);
    }

    @Override
    public void addUserFlag(String userFlag) {
        message.addUserFlag(userFlag);
    }

    @Override
    public void addUserFlags(String[] userFlags) {
        message.addUserFlags(userFlags);
    }

    @Override
    public boolean containsUserFlags() {
        return message.containsUserFlags();
    }

    @Override
    public void removeUserFlags() {
        message.removeUserFlags();
    }

    @Override
    public String[] getUserFlags() {
        return message.getUserFlags();
    }

    @Override
    public int getColorLabel() {
        return message.getColorLabel();
    }

    @Override
    public boolean containsColorLabel() {
        return message.containsColorLabel();
    }

    @Override
    public void removeColorLabel() {
        message.removeColorLabel();
    }

    @Override
    public void setColorLabel(int colorLabel) {
        message.setColorLabel(colorLabel);
    }

    @Override
    public int getPriority() {
        return message.getPriority();
    }

    @Override
    public boolean containsPriority() {
        return message.containsPriority();
    }

    @Override
    public void removePriority() {
        message.removePriority();
    }

    @Override
    public void setPriority(int priority) {
        message.setPriority(priority);
    }

    @Override
    public InternetAddress getDispositionNotification() {
        return message.getDispositionNotification();
    }

    @Override
    public boolean containsDispositionNotification() {
        return message.containsDispositionNotification();
    }

    @Override
    public void removeDispositionNotification() {
        message.removeDispositionNotification();
    }

    @Override
    public void setDispositionNotification(InternetAddress dispositionNotification) {
        message.setDispositionNotification(dispositionNotification);
    }

    @Override
    public String getFolder() {
        return message.getFolder();
    }

    @Override
    public boolean containsFolder() {
        return message.containsFolder();
    }

    @Override
    public void removeFolder() {
        message.removeFolder();
    }

    @Override
    public void setFolder(String folder) {
        message.setFolder(folder);
    }

    @Override
    public int getAccountId() {
        return message.getAccountId();
    }

    @Override
    public boolean containsAccountId() {
        return message.containsAccountId();
    }

    @Override
    public void removeAccountId() {
        message.removeAccountId();
    }

    @Override
    public void setAccountId(int accountId) {
        message.setAccountId(accountId);
    }

    @Override
    public String getAccountName() {
        return message.getAccountName();
    }

    @Override
    public boolean containsAccountName() {
        return message.containsAccountName();
    }

    @Override
    public void removeAccountName() {
        message.removeAccountName();
    }

    @Override
    public void setAccountName(String accountName) {
        message.setAccountName(accountName);
    }

    @Override
    public boolean hasAttachment() {
        return message.hasAttachment();
    }

    @Override
    public boolean isHasAttachment() {
        return message.isHasAttachment();
    }

    @Override
    public boolean containsHasAttachment() {
        return message.containsHasAttachment();
    }

    @Override
    public void removeHasAttachment() {
        message.removeHasAttachment();
    }

    @Override
    public void setHasAttachment(boolean hasAttachment) {
        message.setHasAttachment(hasAttachment);
    }

    @Override
    public boolean isAlternativeHasAttachment() {
        return message.isAlternativeHasAttachment();
    }

    @Override
    public boolean containsAlternativeHasAttachment() {
        return message.containsAlternativeHasAttachment();
    }

    @Override
    public void removeAlternativeHasAttachment() {
        message.removeAlternativeHasAttachment();
    }

    @Override
    public void setAlternativeHasAttachment(boolean hasAttachment) {
        message.setAlternativeHasAttachment(hasAttachment);
    }

    @Override
    public Object clone() {
        return message.clone();
    }

    @Override
    public boolean isAppendVCard() {
        return message.isAppendVCard();
    }

    @Override
    public boolean containsAppendVCard() {
        return message.containsAppendVCard();
    }

    @Override
    public void removeAppendVCard() {
        message.removeAppendVCard();
    }

    @Override
    public void setAppendVCard(boolean appendVCard) {
        message.setAppendVCard(appendVCard);
    }

    @Override
    public int getRecentCount() {
        return message.getRecentCount();
    }

    @Override
    public boolean containsRecentCount() {
        return message.containsRecentCount();
    }

    @Override
    public void removeRecentCount() {
        message.removeRecentCount();
    }

    @Override
    public void setRecentCount(int recentCount) {
        message.setRecentCount(recentCount);
    }

    @Override
    public MailPath getMailPath() {
        return message.getMailPath();
    }

    @Override
    public String getMailId() {
        return message.getMailId();
    }

    @Override
    public void setMailId(String id) {
        message.setMailId(id);
    }

    @Override
    public int getUnreadMessages() {
        return message.getUnreadMessages();
    }

    @Override
    public void setUnreadMessages(int unreadMessages) {
        message.setUnreadMessages(unreadMessages);
    }

}
