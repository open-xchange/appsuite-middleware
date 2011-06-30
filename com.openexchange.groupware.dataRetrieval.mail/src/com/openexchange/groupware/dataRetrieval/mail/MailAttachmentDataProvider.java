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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.groupware.dataRetrieval.mail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.dataRetrieval.DataProvider;
import com.openexchange.groupware.dataRetrieval.DataRetrievalException;
import com.openexchange.groupware.dataRetrieval.DataRetrievalExceptionCodes;
import com.openexchange.groupware.dataRetrieval.FileMetadata;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailAttachmentDataProvider}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MailAttachmentDataProvider implements DataProvider<MailAttachmentState> {

    private static final String ID = "mail";

    public static final String PARAMETER_FOLDERID = "folder";

    public static final String PARAMETER_ID = "id";

    public static final String PARAMETER_MAILATTACHMENT = "attachment";

    private MailService mailService = null;

    public MailAttachmentDataProvider(MailService mailService) {
        this.mailService = mailService;
    }

    public String getId() {
        return ID;
    }

    public MailAttachmentState start() {
        return new MailAttachmentState();
    }

    public void close(MailAttachmentState state) {
        state.close();
    }

    public InputStream retrieve(MailAttachmentState state, Map<String, Object> specification, ServerSession session) throws AbstractOXException {
        check(specification, PARAMETER_FOLDERID, PARAMETER_ID, PARAMETER_MAILATTACHMENT);
        final String folderPath = specification.get(PARAMETER_FOLDERID).toString();
        final String uid = specification.get(PARAMETER_ID).toString();
        final String sequenceId = specification.get(PARAMETER_MAILATTACHMENT).toString();

        FullnameArgument argument = MailFolderUtility.prepareMailFolderParam(folderPath);

        int accountId = argument.getAccountId();

        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = state.getMailAccess(
            mailService,
            session,
            accountId);
        MailPart part = mailAccess.getMessageStorage().getAttachment(argument.getFullname(), uid, sequenceId);
        return part.getInputStream();
    }

    private void check(Map<String, Object> specification, String... parameters) throws DataRetrievalException {
        List<String> missing = new ArrayList<String>(parameters.length);
        for (String paramName : parameters) {
            if (!specification.containsKey(paramName)) {
                missing.add(paramName);
            }
        }
        if (!missing.isEmpty()) {
            throw DataRetrievalExceptionCodes.MISSING_PARAMETER.create(missing.toString());
        }
    }

    public FileMetadata retrieveMetadata(MailAttachmentState state, Map<String, Object> specification, ServerSession session) throws AbstractOXException {
        final String folderPath = specification.get(PARAMETER_FOLDERID).toString();
        final String uid = specification.get(PARAMETER_ID).toString();
        final String sequenceId = specification.get(PARAMETER_MAILATTACHMENT).toString();

        FullnameArgument argument = MailFolderUtility.prepareMailFolderParam(folderPath);

        int accountId = argument.getAccountId();

        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = state.getMailAccess(
            mailService,
            session,
            accountId);
        MailPart part = mailAccess.getMessageStorage().getAttachment(argument.getFullname(), uid, sequenceId);
        return new MailFileMetadata(part);
    }

    private static final class MailFileMetadata implements FileMetadata {

        private MailPart mailPart = null;

        public MailFileMetadata(MailPart mailPart) {
            this.mailPart = mailPart;
        }

        public String getFilename() {
            return mailPart.getFileName();
        }

        public long getSize() {
            return mailPart.getSize();
        }

        public String getType() {
            return mailPart.getContentType().toString(true);
        }

    }

}
