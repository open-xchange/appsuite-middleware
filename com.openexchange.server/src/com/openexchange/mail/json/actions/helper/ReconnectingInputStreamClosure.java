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
        } catch (com.sun.mail.util.FolderClosedIOException e) {
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
        ThresholdFileHolder newTfh = null;
        try {
            ma = MailAccess.getInstance(session, fa.getAccountId());
            ma.connect(false);

            newTfh = new ThresholdFileHolder();
            if (image) {
                newTfh.write(ma.getMessageStorage().getImageAttachment(fa.getFullName(), uid, id).getInputStream());
            } else {
                newTfh.write(ma.getMessageStorage().getAttachment(fa.getFullName(), uid, id).getInputStream());
            }
            this.tfh = newTfh;
            InputStream stream = newTfh.getStream();
            newTfh = null;
            return stream;
        } finally {
            if (null != ma) {
                ma.close(true);
            }
            Streams.close(newTfh);
        }
    }
}