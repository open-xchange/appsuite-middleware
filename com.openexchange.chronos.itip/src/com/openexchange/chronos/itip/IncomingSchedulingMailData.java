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

package com.openexchange.chronos.itip;

import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link IncomingSchedulingMailData} - Object containing necessary information about an incoming
 * iMIP mail
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.6
 */
public class IncomingSchedulingMailData {

    private static final String CONVERSION_MAIL_ID = "com.openexchange.mail.conversion.mailid";
    private static final String CONVERSION_MAIL_FOLDER = "com.openexchange.mail.conversion.fullname";
    private static final String CONVERSION_SEQUENCE_ID = "com.openexchange.mail.conversion.sequenceid";

    private final FullnameArgument fullnameArgument;
    private final String mailId;
    private final String sequenceId;

    /**
     * Initializes a new {@link IncomingSchedulingMailData}.
     * 
     * @param fullnameArgument The full name argument of the mail folder
     * @param mailId The mail identifier
     * @param sequenceId The sequence identifier, can be <code>null</code>
     */
    public IncomingSchedulingMailData(FullnameArgument fullnameArgument, String mailId, String sequenceId) {
        super();
        this.fullnameArgument = fullnameArgument;
        this.mailId = mailId;
        this.sequenceId = sequenceId;
    }

    /**
     * Gets the mail folder data
     *
     * @return The mail folder data as {@link FullnameArgument}
     */
    public FullnameArgument getFullnameArgument() {
        return fullnameArgument;
    }

    /**
     * Gets the mail identifier
     *
     * @return The mail identifier
     */
    public String getMailId() {
        return mailId;
    }

    /**
     * Gets the sequence identifier of the iCAL attachment
     *
     * @return The sequence identifier
     */
    public String getSequenceId() {
        return sequenceId;
    }

    @Override
    public String toString() {
        return "IncomingSchedulingMailData [fullnameArgument=" + fullnameArgument + ", mailId=" + mailId + ", sequenceId=" + sequenceId + "]";
    }

    /*
     * ============================== HELPERS ==============================
     */

    /**
     * Builds a {@link IncomingSchedulingMailData} from a {@link AJAXRequestData}
     *
     * @param request The data to parse
     * @return The object containing the data
     * @throws OXException In case the request can't be parsed
     */
    public static IncomingSchedulingMailData fromRequest(AJAXRequestData request) throws OXException {
        /*
         * Get request payload
         */
        Object data = request.getData();
        if (data == null) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        if (false == data instanceof JSONObject) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }
        /*
         * Parse
         */
        try {
            JSONObject body = (JSONObject) data;
            FullnameArgument fullnameArgument = prepareMailFolderParam(body.getString(CONVERSION_MAIL_FOLDER));
            return new IncomingSchedulingMailData(fullnameArgument, body.getString(CONVERSION_MAIL_ID), body.getString(CONVERSION_SEQUENCE_ID));
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

}
