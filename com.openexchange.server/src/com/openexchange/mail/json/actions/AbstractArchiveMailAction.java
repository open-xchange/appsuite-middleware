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

package com.openexchange.mail.json.actions;

import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link AbstractArchiveMailAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public abstract class AbstractArchiveMailAction extends AbstractMailAction {

    private static final String CAPABILITY_ARCHIVE_EMAILS = "archive_emails";

    /**
     * Initializes a new {@link AbstractArchiveMailAction}.
     */
    protected AbstractArchiveMailAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected final AJAXRequestResult perform(MailRequest req) throws OXException, JSONException {
        // Check required "archive_emails" capability
        {
            CapabilityService capabilityService = ServerServiceRegistry.getInstance().getService(CapabilityService.class);
            if (null != capabilityService && !capabilityService.getCapabilities(req.getSession()).contains(CAPABILITY_ARCHIVE_EMAILS)) {
                throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create("mail-archive");
            }
        }

        // Continue...
        return performArchive(req);
    }

    /**
     * Performs specified mail archive request.
     *
     * @param req The mail archive request
     * @return The result
     * @throws OXException If an error occurs
     * @throws JSONException If a JSON error occurs
     */
    protected abstract AJAXRequestResult performArchive(MailRequest req) throws OXException, JSONException;

}
