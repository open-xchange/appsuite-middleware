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

package com.openexchange.mail.conversion;

import static com.openexchange.mail.mime.utils.MimeMessageUtility.shouldRetry;
import java.io.InputStream;
import com.openexchange.conversion.DataSource;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.virtual.osgi.Services;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.crypto.CryptographicAwareMailAccessFactory;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.session.Session;

/**
 * {@link MailPartDataSource} - A generic {@link DataSource} for mail parts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailPartDataSource implements DataSource {

    /**
     * Common required arguments for uniquely determining a mail part:
     * <ul>
     * <li>com.openexchange.mail.conversion.fullname</li>
     * <li>com.openexchange.mail.conversion.mailid</li>
     * <li>com.openexchange.mail.conversion.sequenceid</li>
     * </ul>
     */
    protected static final String[] ARGS = {
        "com.openexchange.mail.conversion.fullname", "com.openexchange.mail.conversion.mailid",
        "com.openexchange.mail.conversion.sequenceid" };

    /**
     * Initializes a new {@link MailPartDataSource}
     */
    protected MailPartDataSource() {
        super();
    }

    protected final MailPart getMailPart(int accountId, String fullname, String mailId, String sequenceId, Session session) throws OXException {
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, accountId);
            CryptographicAwareMailAccessFactory cryptoMailAccessFactory = Services.getServiceLookup().getOptionalService(CryptographicAwareMailAccessFactory.class);
            if (cryptoMailAccessFactory != null) {
                mailAccess = cryptoMailAccessFactory.createAccess(
                    (MailAccess<IMailFolderStorage, IMailMessageStorage>) mailAccess,
                    session,
                    null);
            }
            mailAccess.connect();
            return loadPart(fullname, mailId, sequenceId, mailAccess);
        } catch (OXException e) {
            if ((null != mailAccess) && shouldRetry(e)) {
                // Re-connect
                mailAccess = MailAccess.reconnect(mailAccess);
                return loadPart(fullname, mailId, sequenceId, mailAccess);
            }
            throw e;
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

    private MailPart loadPart(String fullname, String mailId, String sequenceId, MailAccess<?, ?> mailAccess) throws OXException {
        final MailPart mailPart = mailAccess.getMessageStorage().getAttachment(fullname, mailId, sequenceId);
        mailPart.loadContent();
        return mailPart;
    }

    @Override
    public String[] getRequiredArguments() {
        return new String[] { ARGS[0], ARGS[1], ARGS[2] };
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[] { InputStream.class };
    }
}
