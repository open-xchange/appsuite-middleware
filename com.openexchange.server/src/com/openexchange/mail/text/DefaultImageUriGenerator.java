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

package com.openexchange.mail.text;

import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.exception.OXException;
import com.openexchange.image.ImageLocation;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.conversion.InlineImageDataSource;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link DefaultImageUriGenerator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DefaultImageUriGenerator implements ImageUriGenerator {

    private static final String EVENT_RESTRICTIONS = "onmousedown=\"return false;\" oncontextmenu=\"return false;\"";

    private static final DefaultImageUriGenerator INSTANCE = new DefaultImageUriGenerator();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static DefaultImageUriGenerator getInstance() {
        return INSTANCE;
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link DefaultImageUriGenerator}.
     */
    private DefaultImageUriGenerator() {
        super();
    }

    @Override
    public void generateImageUri(StringBuilder linkBuilder, String prefix, String optAppendix, String imageIdentifier, MailPath mailPath, Session session) throws OXException {
        // Check mail identifier
        String mailId = mailPath.getMailID();
        if (mailId.indexOf('%') >= 0) {
            int unifiedINBOXAccountID = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class).getUnifiedINBOXAccountID(session);
            if (unifiedINBOXAccountID < 0 || mailPath.getAccountId() != unifiedINBOXAccountID) {
                String tmp = AJAXUtility.decodeUrl(mailId, null);
                if (tmp.startsWith(MailFolder.MAIL_PREFIX)) {
                    // Expect mail path; e.g. "default0/INBOX/123"
                    try {
                        mailId = new MailPath(tmp).getMailID();
                    } catch (OXException e) {
                        // Ignore
                    }
                }
            }
        }

        // Build image location
        ImageLocation imageLocation = new ImageLocation.Builder(imageIdentifier).folder(prepareFullname(mailPath.getAccountId(), mailPath.getFolder())).id(mailId).optImageHost(HtmlProcessing.imageHost()).build();
        InlineImageDataSource imgSource = InlineImageDataSource.getInstance();
        String imageURL = imgSource.generateUrl(imageLocation, session);
        linkBuilder.append(prefix).append('"').append(imageURL).append('"').append(" id=\"").append(imageIdentifier).append("\" ").append(EVENT_RESTRICTIONS);
        if (null != optAppendix) {
            linkBuilder.append(optAppendix);
        }
    }

    @Override
    public String getPlainImageUri(String imageIdentifier, MailPath mailPath, Session session) throws OXException {
        // Check mail identifier
        String mailId = mailPath.getMailID();
        if (mailId.indexOf('%') >= 0) {
            int unifiedINBOXAccountID = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class).getUnifiedINBOXAccountID(session);
            if (unifiedINBOXAccountID < 0 || mailPath.getAccountId() != unifiedINBOXAccountID) {
                String tmp = AJAXUtility.decodeUrl(mailId, null);
                if (tmp.startsWith(MailFolder.MAIL_PREFIX)) {
                    // Expect mail path; e.g. "default0/INBOX/123"
                    try {
                        mailId = new MailPath(tmp).getMailID();
                    } catch (OXException e) {
                        // Ignore
                    }
                }
            }
        }

        ImageLocation imageLocation = new ImageLocation.Builder(imageIdentifier).folder(prepareFullname(mailPath.getAccountId(), mailPath.getFolder())).id(mailId).optImageHost(HtmlProcessing.imageHost()).build();
        InlineImageDataSource imgSource = InlineImageDataSource.getInstance();
        return imgSource.generateUrl(imageLocation, session);
    }

}
