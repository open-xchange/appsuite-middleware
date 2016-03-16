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
