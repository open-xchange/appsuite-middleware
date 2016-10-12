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

package com.openexchange.mail.mime;

import javax.mail.Folder;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.session.Session;
import com.sun.mail.iap.ProtocolException;


/**
 * {@link AbstractImapProtocolExceptionHandler} - The abstract handler for {@link com.sun.mail.iap.ProtocolException IMAP protocol exceptions}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public abstract class AbstractImapProtocolExceptionHandler implements MimeMailExceptionHandler {

    /**
     * Initializes a new {@link AbstractImapProtocolExceptionHandler}.
     */
    protected AbstractImapProtocolExceptionHandler() {
        super();
    }

    @Override
    public OXException handle(MessagingException me, MailConfig mailConfig, Session session, Folder folder) {
        if (null == me) {
            return null;
        }

        Exception nextException = me.getNextException();
        if (!(nextException instanceof com.sun.mail.iap.ProtocolException)) {
            return null;
        }

        return handleProtocolException((com.sun.mail.iap.ProtocolException) nextException, mailConfig, session, folder);
    }

    /**
     * Handles given IMAP protocol exception.
     *
     * @param protocolException The IMAP protocol exception to handle
     * @param mailConfig The optional mail config
     * @param session The optional session
     * @param folder The optional folder
     * @return The handled exception or <code>null</code>
     */
    protected abstract OXException handleProtocolException(ProtocolException protocolException, MailConfig mailConfig, Session session, Folder folder);

}
