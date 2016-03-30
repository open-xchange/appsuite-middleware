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

package com.openexchange.ajax.user.actions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;

/**
 * {@link UpdateRequest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class UpdateRequest extends AbstractUserRequest<UpdateResponse> {

    private Contact contactData;

    private User userData;

    private String stringifiedJSON;

    // Does the contactData contain an image?
    private boolean hasImage = false;

    // Should test execution fail on errors or return a result with the error?
    private boolean failOnError = false;

    /**
     * Initializes a new {@link UpdateRequest} that will fail on errors that happen during the request processing
     *
     * @param contactData contains the contact attributes that should be updated. This contact must contain the attributes parent folder
     *            identifier, object identifier and last modification timestamp.
     * @param userData the user attributes that should be updated: "timezone" and "locale" are the only fields from Detailed user data which
     *            are allowed to be updated.
     */
    public UpdateRequest(final Contact contactData, User userData) {
        this(contactData, userData, true);
    }

    /**
     * Initializes a new {@link UpdateRequest}.
     *
     * @param contactData contains the contact attributes that should be updated. This contact must contain the attributes parent folder
     *            identifier, object identifier and last modification timestamp.
     * @param userData the user attributes that should be updated: "timezone" and "locale" are the only fields from Detailed user data which
     *            are allowed to be updated.
     * @param failOnError should test execution fail on errors or return a result with the error
     */
    public UpdateRequest(Contact contactData, User userData, boolean failOnError) {
        super();
        this.contactData = contactData;
        this.userData = userData;
        this.failOnError = failOnError;
        this.hasImage = contactData.containsImage1() && (null != contactData.getImage1());

        if (hasImage) {
            try {
                stringifiedJSON = convert(contactData, userData).toString();
            } catch (final JSONException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        if (hasImage) {
            return Method.POST;
        }
        return Method.PUT;
    }

    @Override
    public Header[] getHeaders() {
        if (hasImage) {
            return new Header[] { new Header.SimpleHeader("Content-Type", "multipart/form-data") };
        }

        return NO_HEADER;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        if (hasImage) {
            String ct = "image/jpg";
            String ext = "jpg";
            if (contactData.getImageContentType() != null) {
                ct = contactData.getImageContentType();
                if (ct.startsWith("image/png")) {
                    ext = "png";
                }
            }
            return new Parameter[] {
                new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE),
                new URLParameter(AJAXServlet.PARAMETER_ID, Integer.toString(contactData.getInternalUserId())),
                new URLParameter(AJAXServlet.PARAMETER_TIMESTAMP, Long.toString(contactData.getLastModified().getTime())),
                new FieldParameter("json", stringifiedJSON),
                new FileParameter("file", "open-xchange_image." + ext, new ByteArrayInputStream(contactData.getImage1()), ct) };
        }

        return new Parameter[] {
            new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE),
            new Parameter(AJAXServlet.PARAMETER_ID, Integer.toString(contactData.getInternalUserId())),
            new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, Long.toString(contactData.getLastModified().getTime())) };
    }

    @Override
    public AbstractAJAXParser<? extends UpdateResponse> getParser() {
        return new UpdateParser(failOnError, hasImage);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return convert(contactData, userData);
    }

    private JSONObject convert(Contact contactData, User userData) throws JSONException {
        final JSONObject jsonObj = new JSONObject();
        final ContactWriter contactWriter = new ContactWriter(TimeZone.getTimeZone("UTC"));
        contactWriter.writeContact(contactData, jsonObj, null);

        // "timezone" and "locale" are the only fields from Detailed user data which are allowed to be updated.
        if (userData != null) {
            String timezone = userData.getTimeZone();
            if (timezone != null && !timezone.isEmpty()) {
                jsonObj.put("timezone", timezone);
            }
            Locale locale = userData.getLocale();
            if (locale != null) {
                jsonObj.put("locale", locale.toString());
            }
        }

        return jsonObj;
    }

}
