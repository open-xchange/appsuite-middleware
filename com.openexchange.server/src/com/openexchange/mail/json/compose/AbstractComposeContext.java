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

package com.openexchange.mail.json.compose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ComposedMailPart;
import com.openexchange.mail.dataobjects.compose.DataMailPart;
import com.openexchange.mail.dataobjects.compose.InfostoreDocumentMailPart;
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.dataobjects.compose.UploadFileMailPart;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.tools.session.ServerSession;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * {@link AbstractComposeContext} - A compose context; storing necessary state information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public abstract class AbstractComposeContext {

    /* Final members */
    private final int accountId;
    private final ServerSession session;
    private final TransportProvider provider;
    private final TIntObjectMap<MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>> mailAccesses;

    /* State variables */
    private ComposedMailMessage sourceMessage;
    private TextBodyMailPart textPart;
    private List<ReferencedMailPart> referencedParts;
    private List<DataMailPart> dataParts;
    private List<UploadFileMailPart> uploadedParts;
    private List<InfostoreDocumentMailPart> driveParts;

    /**
     * Initializes a new {@link AbstractComposeContext}.
     *
     * @param request The compose request associated with this context
     * @throws OXException If initialization fails
     */
    protected AbstractComposeContext(ComposeRequest request) throws OXException {
        this(request.getAccountId(), request.getSession());
    }

    /**
     * Initializes a new {@link AbstractComposeContext}.
     *
     * @throws OXException If initialization fails
     */
    protected AbstractComposeContext(int accountId, ServerSession session) throws OXException {
        super();
        this.accountId = accountId;
        this.session = session;
        provider = TransportProviderRegistry.getTransportProviderBySession(session, accountId);
        mailAccesses = new TIntObjectHashMap<>(2);
    }

    /**
     * Invoked when specified part is supposed to be added to this compose context.
     *
     * @param part The added part
     * @param info The {@link ComposedMailPart} view for the specified part
     * @throws OXException If part cannot be accepted
     */
    protected abstract void onPartAdd(MailPart part, ComposedMailPart info) throws OXException;

    /**
     * Sets the source compose message providing basic information.
     *
     * @param sourceMessage The compose message to set
     */
    public void setSourceMessage(ComposedMailMessage sourceMessage) {
        this.sourceMessage = sourceMessage;
    }

    /**
     * Gets the source compose message providing basic information.
     *
     * @return The compose message
     */
    public ComposedMailMessage getSourceMessage() {
        return sourceMessage;
    }

    /**
     * Gets the transport provider
     *
     * @return The transport provider
     */
    public TransportProvider getProvider() {
        return provider;
    }

    /**
     * Gets the account identifier
     *
     * @return The account identifier
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public ServerSession getSession() {
        return session;
    }

    /**
     * Gets the text part
     *
     * @return The text part
     */
    public TextBodyMailPart getTextPart() {
        return textPart;
    }

    /**
     * Sets the text part
     *
     * @param textPart The text part to set
     */
    public void setTextPart(TextBodyMailPart textPart) {
        this.textPart = textPart;
    }

    /**
     * Gets a listing of all parts in this context.
     *
     * @return All parts
     */
    public List<MailPart> getAllParts() {
        List<MailPart> parts = new LinkedList<>();
        parts.addAll(getReferencedParts());
        parts.addAll(getDataParts());
        parts.addAll(getUploadParts());
        parts.addAll(getDriveParts());
        return parts;
    }

    /**
     * Adds specified referenced part
     *
     * @param referencedPart The referenced part
     * @throws OXException If part cannot be added
     */
    public void addReferencedPart(ReferencedMailPart referencedPart) throws OXException {
        if (null != referencedPart) {
            List<ReferencedMailPart> referencedParts = this.referencedParts;
            if (null == referencedParts) {
                referencedParts = new ArrayList<>(4);
                this.referencedParts = referencedParts;
            }
            onPartAdd(referencedPart, referencedPart);
            referencedParts.add(referencedPart);
        }
    }

    /**
     * Gets the referenced parts
     *
     * @return The referenced parts or an empty list
     */
    public List<ReferencedMailPart> getReferencedParts() {
        return null == referencedParts ? Collections.<ReferencedMailPart> emptyList() : referencedParts;
    }

    /**
     * Adds specified data part
     *
     * @param dataPart The data part
     * @throws OXException If part cannot be added
     */
    public void addDataPart(DataMailPart dataPart) throws OXException {
        if (null != dataPart) {
            List<DataMailPart> dataParts = this.dataParts;
            if (null == dataParts) {
                dataParts = new ArrayList<>(4);
                this.dataParts = dataParts;
            }
            onPartAdd(dataPart, dataPart);
            dataParts.add(dataPart);
        }
    }

    /**
     * Gets the data parts
     *
     * @return The data parts or an empty list
     */
    public List<DataMailPart> getDataParts() {
        return null == dataParts ? Collections.<DataMailPart> emptyList() : dataParts;
    }

    /**
     * Adds specified upload part
     *
     * @param uploadPart The upload part
     * @throws OXException If part cannot be added
     */
    public void addUploadPart(UploadFileMailPart uploadPart) throws OXException {
        if (null != uploadPart) {
            List<UploadFileMailPart> uploadedParts = this.uploadedParts;
            if (null == uploadedParts) {
                uploadedParts = new ArrayList<>(4);
                this.uploadedParts = uploadedParts;
            }
            onPartAdd(uploadPart, uploadPart);
            uploadedParts.add(uploadPart);
        }
    }

    /**
     * Gets the upload parts
     *
     * @return The upload parts or an empty list
     */
    public List<UploadFileMailPart> getUploadParts() {
        return null == uploadedParts ? Collections.<UploadFileMailPart> emptyList() : uploadedParts;
    }

    /**
     * Adds specified drive part
     *
     * @param drivePart The drive part
     * @throws OXException If part cannot be added
     */
    public void addDrivePart(InfostoreDocumentMailPart drivePart) throws OXException {
        if (null != drivePart) {
            List<InfostoreDocumentMailPart> driveParts = this.driveParts;
            if (null == driveParts) {
                driveParts = new ArrayList<>(4);
                this.driveParts = driveParts;
            }
            onPartAdd(drivePart, drivePart);
            driveParts.add(drivePart);
        }
    }

    /**
     * Gets the drive parts
     *
     * @return The drive parts or an empty list
     */
    public List<InfostoreDocumentMailPart> getDriveParts() {
        return null == driveParts ? Collections.<InfostoreDocumentMailPart> emptyList() : driveParts;
    }

    /**
     * Gets the connected mail access associated with this context.
     *
     * @param accountId The identifier of the target account for which to return a connected mail access
     * @return The connected mail access
     * @throws OXException If mail access cannot be returned
     */
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getConnectedMailAccess(int accountId) throws OXException {
        return getMailAccess0(accountId, false);
    }

    /**
     * Reconnects the mail access associated with specified account identifier.
     *
     * @param accountId The identifier of the target account for which to return a connected mail access
     * @return The (reconnected) mail access
     * @throws OXException If mail access cannot be returned
     */
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> reconnectMailAccess(int accountId) throws OXException {
        return getMailAccess0(accountId, true);
    }

    private MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getMailAccess0(int accountId, boolean reconnect) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = mailAccesses.get(accountId);

        if (null == mailAccess) {
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccesses.put(accountId, mailAccess);
            mailAccess.connect();
            return mailAccess;
        }

        if (reconnect) {
            mailAccess = MailAccess.reconnect(mailAccess);
            mailAccesses.put(accountId, mailAccess);
        }

        return mailAccess;
    }

    /**
     * Disposes this context.
     */
    public void dispose() {
        TIntObjectIterator<MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>> it = mailAccesses.iterator();
        for (int i = mailAccesses.size(); i-- > 0;) {
            it.advance();
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = it.value();
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

}
