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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.json.compose.share;

import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailPart;
import com.openexchange.mail.json.compose.AbstractQuotaAwareComposeContext;
import com.openexchange.mail.json.compose.ComposeRequest;

/**
 * {@link ShareComposeContext}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ShareComposeContext extends AbstractQuotaAwareComposeContext {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ShareComposeContext.class);

    private boolean createShares;
    private long consumed;

    /**
     * Initializes a new {@link ShareComposeContext}.
     *
     * @param request The compose request
     * @throws OXException If initialization fails
     */
    public ShareComposeContext(ComposeRequest request) throws OXException {
        super(request);
        createShares = AJAXRequestDataTools.parseBoolParameter("share_attachments", request.getRequest(), false);
    }

    /**
     * Checks whether shares are supposed to be created instead of sending a regular message
     *
     * @return <code>true</code> for creating shares; otherwise <code>false</code> for regular message
     */
    public boolean isCreateShares() {
        return createShares;
    }

    @Override
    protected void onPartAdd(MailPart part, ComposedMailPart info) throws OXException {
        if (createShares) {
            return;
        }

        if (doAction) {
            long size = part.getSize();
            if (size <= 0) {
                LOG.debug("Missing size: {}", Long.valueOf(size), new Throwable());
                return;
            }
            if (uploadQuotaPerFile > 0 && size > uploadQuotaPerFile) {
                createShares = true;
            }
            /*
             * Add current file size
             */
            consumed += size;
            if (uploadQuota > 0 && consumed > uploadQuota) {
                createShares = true;
            }
        }
    }

}
