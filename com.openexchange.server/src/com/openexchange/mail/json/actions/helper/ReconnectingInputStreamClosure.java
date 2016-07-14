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

package com.openexchange.mail.json.actions.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ReconnectingInputStreamClosure}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.0
 */
public final class ReconnectingInputStreamClosure implements IFileHolder.InputStreamClosure {

    private final ServerSession session;
    private final String id;
    private final String uid;
    private final MailPart mailPart;
    private final String folderPath;
    private final boolean image;
    private volatile ThresholdFileHolder tfh;

    public ReconnectingInputStreamClosure(MailPart mailPart, String folderPath, String uid, String id, boolean image, ServerSession session) {
        super();
        this.session = session;
        this.id = id;
        this.uid = uid;
        this.mailPart = mailPart;
        this.folderPath = folderPath;
        this.image = image;
    }

    @Override
    public InputStream newStream() throws OXException, IOException {
        {
            ThresholdFileHolder tfh = this.tfh;
            if (null != tfh) {
                return tfh.getStream();
            }
        }

        InputStream partStream = null;
        boolean close = true;
        try {
            partStream = mailPart.getInputStream();
            PushbackInputStream in = new PushbackInputStream(partStream);
            // Check if readable...
            final int check = in.read();
            if (check < 0) {
                return Streams.EMPTY_INPUT_STREAM;
            }
            // ... then push back to stream
            in.unread(check);
            close = false;
            return in;
        } catch (final com.sun.mail.util.FolderClosedIOException e) {
            // Need to reconnect
            return reconnectAndGetStream();
        } finally {
            if (close) {
                Streams.close(partStream);
            }
        }
    }

    private InputStream reconnectAndGetStream() throws OXException {
        FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(folderPath);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> ma = null;
        try {
            ma = MailAccess.getInstance(session, fa.getAccountId());
            ma.connect(false);
            final ThresholdFileHolder newTfh = new ThresholdFileHolder();
            if (image) {
                newTfh.write(ma.getMessageStorage().getImageAttachment(fa.getFullName(), uid, id).getInputStream());
            } else {
                newTfh.write(ma.getMessageStorage().getAttachment(fa.getFullName(), uid, id).getInputStream());
            }
            this.tfh = newTfh;
            return newTfh.getStream();
        } finally {
            if (null != ma) {
                ma.close(true);
            }
        }
    }
}