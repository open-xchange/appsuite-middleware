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

package com.openexchange.mail.json.compose;

import java.util.List;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ComposeRequest} - Represents a compose request initiated by a client.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ComposeRequest {

    private final int accountId;
    private final JSONObject jMail;
    private final ComposeType sendType;
    private final UploadEvent uploadEvent;
    private final AJAXRequestData request;
    private final List<OXException> warnings;

    /**
     * Initializes a new {@link ComposeRequest}.
     *
     * @throws OXException If initialization fails
     */
    public ComposeRequest(int accountId, JSONObject jMail, ComposeType sendType, UploadEvent uploadEvent, AJAXRequestData request, List<OXException> warnings) throws OXException {
        super();
        this.accountId = accountId;
        this.jMail = jMail;
        this.sendType = sendType;
        this.uploadEvent = uploadEvent;
        this.request = request;
        this.warnings = warnings;
    }

    /**
     * Gets the associated context
     *
     * @return The context
     */
    public Context getContext() {
        return getSession().getContext();
    }

    /**
     * Gets the associated user, which executed this request
     *
     * @return The user
     */
    public User getUser() {
        return getSession().getUser();
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public ServerSession getSession() {
        return request.getSession();
    }

    /**
     * Gets the account identifier
     *
     * @return The account identifier
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the JSON representation of the message to process.
     *
     * @return The message's JSON representation
     */
    public JSONObject getJsonMail() {
        return jMail;
    }

    /**
     * Gets the send type
     *
     * @return The send type
     */
    public ComposeType getSendType() {
        return sendType;
    }

    /**
     * Gets the upload event providing uploaded files.
     *
     * @return The upload event
     */
    public UploadEvent getUploadEvent() {
        return uploadEvent;
    }

    /**
     * Gets the associated request
     *
     * @return The request
     */
    public AJAXRequestData getRequest() {
        return request;
    }

    /**
     * Gets an optional list of warnings to which to add possible warnings to.
     *
     * @return The warnings or <code>null</code>
     */
    public List<OXException> getWarnings() {
        return warnings;
    }

}
