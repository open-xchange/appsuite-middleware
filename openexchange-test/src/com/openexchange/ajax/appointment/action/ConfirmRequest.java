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

package com.openexchange.ajax.appointment.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.groupware.container.Participant;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class ConfirmRequest extends AbstractAppointmentRequest<ConfirmResponse> {

    private final int folderId;

    private final int objectId;

    private final int confirmStatus;

    private final String confirmMessage;

    private int user;

    private final boolean failOnError;

    private String mail;

    private final int type;

    private int occurrence;

    private Date lastModified;

    /**
     * For external users
     *
     * Initializes a new {@link ConfirmRequest}.
     * 
     * @param folderId
     * @param objectId
     * @param occurrence
     * @param confirmStatus
     * @param confirmMessage
     * @param mail
     * @param failOnError
     */
    public ConfirmRequest(int folderId, int objectId, int occurrence, int confirmStatus, String confirmMessage, String mail, Date lastModified, boolean failOnError) {
        super();
        this.folderId = folderId;
        this.objectId = objectId;
        this.occurrence = occurrence;
        this.confirmStatus = confirmStatus;
        this.confirmMessage = confirmMessage;
        this.mail = mail;
        this.lastModified = lastModified;
        this.failOnError = failOnError;
        this.type = Participant.EXTERNAL_USER;
    }

    public ConfirmRequest(int folderId, int objectId, int confirmStatus, String confirmMessage, String mail, Date lastModified, boolean failOnError) {
        this(folderId, objectId, 0, confirmStatus, confirmMessage, mail, lastModified, failOnError);
    }

    /**
     * For internal users
     *
     * Initializes a new {@link ConfirmRequest}.
     * 
     * @param folderId
     * @param objectId
     * @param occurrence
     * @param confirmStatus
     * @param confirmMessage
     * @param user
     * @param failOnError
     */
    public ConfirmRequest(int folderId, int objectId, int occurrence, int confirmStatus, String confirmMessage, int user, Date lastModified, boolean failOnError) {
        super();
        this.folderId = folderId;
        this.objectId = objectId;
        this.occurrence = occurrence;
        this.confirmStatus = confirmStatus;
        this.confirmMessage = confirmMessage;
        this.user = user;
        this.lastModified = lastModified;
        this.failOnError = failOnError;
        this.type = Participant.USER;

    }

    public ConfirmRequest(int folderId, int objectId, int confirmStatus, String confirmMessage, int user, Date lastModified, boolean failOnError) {
        this(folderId, objectId, 0, confirmStatus, confirmMessage, user, lastModified, failOnError);
    }

    public ConfirmRequest(int folderId, int objectId, int confirmStatus, String confirmMessage, Date lastModified, boolean failOnError) {
        this(folderId, objectId, confirmStatus, confirmMessage, 0, lastModified, failOnError);
    }

    @Override
    public Object getBody() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(ParticipantsFields.CONFIRM_MESSAGE, confirmMessage);
        json.put(ParticipantsFields.CONFIRMATION, confirmStatus);
        json.put(ParticipantsFields.TYPE, type);
        if (user != 0) {
            json.put(AJAXServlet.PARAMETER_ID, user);
        }
        if (mail != null) {
            json.put(AJAXServlet.PARAMETER_MAIL, mail);
        }
        return json;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        final List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_CONFIRM));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_ID, objectId));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderId));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, lastModified));
        if (occurrence > 0) {
            parameterList.add(new Parameter(AJAXServlet.PARAMETER_OCCURRENCE, occurrence));
        }
        return parameterList.toArray(new Parameter[parameterList.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends ConfirmResponse> getParser() {
        return new ConfirmParser(failOnError);
    }

}
