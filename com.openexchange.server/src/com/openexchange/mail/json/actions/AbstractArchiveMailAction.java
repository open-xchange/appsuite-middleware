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
    protected final AJAXRequestResult perform(final MailRequest req) throws OXException, JSONException {
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

    /**
     * A simple wrapper class for archive actions.
     */
    public static class ArchiveDataWrapper {

        private final String id;
        private final boolean created;

        /**
         * Initializes a new {@link ArchiveDataWrapper}.
         */
        public ArchiveDataWrapper(String id, boolean created) {
            this.id = id;
            this.created = created;
        }

        /**
         * Gets the identifier of the (sub-)archive folder
         *
         * @return The identifier
         */
        public String getId() {
            return id;
        }

        /**
         * Signals whether that folder has been created or not
         *
         * @return <code>true</code> if created; otherwise <code>false</code>
         */
        public boolean isCreated() {
            return created;
        }
    }

}
