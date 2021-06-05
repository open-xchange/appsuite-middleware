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

package com.openexchange.mail.json.compose;

import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import com.google.common.collect.ImmutableList;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link ComposeRequest} - Represents a compose request initiated by a client.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ComposeRequest {

    private final int accountId;
    private final AJAXRequestData request;
    private final List<OXException> warnings;

    private final JSONObject jMail;
    private final ComposeType sendType;
    private final UploadEvent uploadEvent;

    private final ComposedMailMessage sourceMessage;
    private final TextBodyMailPart textPart;
    private final List<MailPart> parts;
    private final Map<String, Object> parameters;

    /**
     * Initializes a new {@link ComposeRequest}.
     */
    public ComposeRequest(int accountId, JSONObject jMail, ComposeType sendType, UploadEvent uploadEvent, AJAXRequestData request, List<OXException> warnings) {
        super();
        this.accountId = accountId;
        this.request = request;
        this.warnings = warnings;

        this.jMail = jMail;
        this.sendType = sendType;
        this.uploadEvent = uploadEvent;

        this.sourceMessage = null;
        this.textPart = null;
        this.parts = null;
        this.parameters = null;
    }

    /**
     * Initializes a new {@link ComposeRequest}.
     */
    public ComposeRequest(int accountId, ComposedMailMessage sourceMessage, TextBodyMailPart textPart, List<MailPart> parts, Map<String, Object> parameters, AJAXRequestData request, List<OXException> warnings) {
        super();
        this.accountId = accountId;
        this.request = request;
        this.warnings = warnings;

        this.jMail = null;
        this.sendType = null;
        this.uploadEvent = null;

        this.sourceMessage = sourceMessage;
        this.textPart = textPart;
        this.parts = ImmutableList.copyOf(parts);
        this.parameters = parameters;
    }

    /**
     * Gets the parameters
     *
     * @return The parameters
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Gets the optional source message
     *
     * @return The source message or <code>null</code>
     */
    public ComposedMailMessage getSourceMessage() {
        return sourceMessage;
    }

    /**
     * Gets the parts
     *
     * @return The parts
     */
    public List<MailPart> getParts() {
        return parts;
    }

    /**
     * Gets the text part
     *
     * @return The text part
     */
    public TextBodyMailPart getTextPart() {
        return textPart;
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
