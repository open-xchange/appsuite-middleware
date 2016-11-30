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

package com.openexchange.mailaccount.json.actions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.json.writer.FolderWriter;
import com.openexchange.mailaccount.json.ActiveProviderDetector;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractMailAccountTreeAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractMailAccountTreeAction extends AbstractValidateMailAccountAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractMailAccountTreeAction.class);

    /**
     * Initializes a new {@link AbstractMailAccountTreeAction}.
     */
    protected AbstractMailAccountTreeAction(ActiveProviderDetector activeProviderDetector) {
        super(activeProviderDetector);
    }

    protected static JSONObject actionValidateTree0(final MailAccess<?, ?> mailAccess, final ServerSession session) throws JSONException {
        // Now try to connect
        boolean close = false;
        try {
            mailAccess.connect();
            close = true;
            // Compose folder tree
            final JSONObject root = FolderWriter.writeMailFolder(-1, mailAccess.getRootFolder(), mailAccess.getMailConfig(), session);
            // Recursive call
            addSubfolders(
                root,
                mailAccess.getFolderStorage().getSubfolders(MailFolder.DEFAULT_FOLDER_ID, true),
                mailAccess,
                mailAccess.getMailConfig(),
                session);
            return root;
        } catch (final OXException e) {
            LOG.debug("Composing mail account's folder tree failed.", e);
            // TODO: How to indicate error if folder tree requested?
            return null;
        } finally {
            if (close) {
                mailAccess.close(false);
            }
        }
    }

    protected static void addSubfolders(final JSONObject parent, final MailFolder[] subfolders, final MailAccess<?, ?> mailAccess, final MailConfig mailConfig, final ServerSession session) throws JSONException, OXException {
        if (subfolders.length == 0) {
            return;
        }

        final JSONArray subfolderArray = new JSONArray();
        parent.put("subfolder_array", subfolderArray);

        for (final MailFolder subfolder : subfolders) {
            final JSONObject subfolderObject = FolderWriter.writeMailFolder(-1, subfolder, mailConfig, session);
            subfolderArray.put(subfolderObject);
            // Recursive call
            addSubfolders(
                subfolderObject,
                mailAccess.getFolderStorage().getSubfolders(subfolder.getFullname(), true),
                mailAccess,
                mailConfig,
                session);
        }
    }

}
