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

package com.openexchange.mail.json.compose.share;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.DataMailPart;
import com.openexchange.mail.dataobjects.compose.InfostoreDocumentMailPart;
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.json.compose.ComposeContext;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ForwardingComposeContext}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class ForwardingComposeContext implements ComposeContext {

    private final ComposeContext composeContext;
    private List<MailPart> allParts;

    /**
     * Initializes a new {@link ForwardingComposeContext}.
     */
    public ForwardingComposeContext(ComposeContext composeContext) {
        super();
        this.composeContext = composeContext;
    }

    /**
     * Sets all parts of this compose context.
     *
     * @param allParts All parts
     */
    public void setAllParts(List<MailPart> allParts) {
        this.allParts = allParts;
    }

    @Override
    public void setSourceMessage(ComposedMailMessage sourceMessage) {
        composeContext.setSourceMessage(sourceMessage);
    }

    @Override
    public ComposedMailMessage getSourceMessage() {
        return composeContext.getSourceMessage();
    }

    @Override
    public TransportProvider getProvider() {
        return composeContext.getProvider();
    }

    @Override
    public int getAccountId() {
        return composeContext.getAccountId();
    }

    @Override
    public ServerSession getSession() {
        return composeContext.getSession();
    }

    @Override
    public boolean isPlainText() {
        return composeContext.isPlainText();
    }

    @Override
    public TextBodyMailPart getTextPart() {
        return composeContext.getTextPart();
    }

    @Override
    public void setTextPart(TextBodyMailPart textPart) {
        composeContext.setTextPart(textPart);
    }

    @Override
    public List<MailPart> getAllParts() {
        return allParts == null ? composeContext.getAllParts() : allParts;
    }

    @Override
    public boolean hasAnyPart() {
        return allParts == null ? composeContext.hasAnyPart() : !allParts.isEmpty();
    }

    @Override
    public void addReferencedPart(ReferencedMailPart referencedPart) throws OXException {
        composeContext.addReferencedPart(referencedPart);
    }

    @Override
    public List<ReferencedMailPart> getReferencedParts() {
        return composeContext.getReferencedParts();
    }

    @Override
    public void addDataPart(DataMailPart dataPart) throws OXException {
        composeContext.addDataPart(dataPart);
    }

    @Override
    public List<DataMailPart> getDataParts() {
        return composeContext.getDataParts();
    }

    @Override
    public void addUploadPart(MailPart uploadPart) throws OXException {
        composeContext.addUploadPart(uploadPart);
    }

    @Override
    public List<MailPart> getUploadParts() {
        return composeContext.getUploadParts();
    }

    @Override
    public void addDrivePart(InfostoreDocumentMailPart drivePart) throws OXException {
        composeContext.addDrivePart(drivePart);
    }

    @Override
    public List<InfostoreDocumentMailPart> getDriveParts() {
        return composeContext.getDriveParts();
    }

    @Override
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getConnectedMailAccess(int accountId) throws OXException {
        return composeContext.getConnectedMailAccess(accountId);
    }

    @Override
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> reconnectMailAccess(int accountId) throws OXException {
        return composeContext.reconnectMailAccess(accountId);
    }

    @Override
    public void dispose() {
        composeContext.dispose();
    }


}
