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
                if (tmp.startsWith(MailFolder.DEFAULT_FOLDER_ID)) {
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
                if (tmp.startsWith(MailFolder.DEFAULT_FOLDER_ID)) {
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
