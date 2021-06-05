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
                mailAccess.getFolderStorage().getSubfolders(MailFolder.ROOT_FOLDER_ID, true),
                mailAccess,
                mailAccess.getMailConfig(),
                session);
            return root;
        } catch (OXException e) {
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
