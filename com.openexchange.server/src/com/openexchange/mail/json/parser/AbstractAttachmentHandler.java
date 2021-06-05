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

package com.openexchange.mail.json.parser;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.quotachecker.MailUploadQuotaChecker;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractAttachmentHandler} - An abstract {@link IAttachmentHandler attachment handler}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractAttachmentHandler implements IAttachmentHandler {

    protected final List<MailPart> attachments;
    protected final boolean doAction;
    protected final long uploadQuota;
    protected final long uploadQuotaPerFile;

    /**
     * Initializes a new {@link AbstractAttachmentHandler}.
     *
     * @param session The session providing needed user information
     * @throws OXException If initialization fails
     */
    public AbstractAttachmentHandler(Session session) throws OXException {
        super();
        attachments = new ArrayList<MailPart>(4);

        final UserSettingMail usm;
        if (session instanceof ServerSession) {
            usm = ((ServerSession) session).getUserSettingMail();
        } else {
            usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), session.getContextId());
        }

        final MailUploadQuotaChecker checker = new MailUploadQuotaChecker(usm);
        this.uploadQuota = checker.getQuotaMax();
        this.uploadQuotaPerFile = checker.getFileQuotaMax();

        doAction = ((uploadQuotaPerFile > 0) || (uploadQuota > 0));
    }
}
