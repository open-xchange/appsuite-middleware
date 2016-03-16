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

package com.openexchange.user.json.actions;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.activation.MimetypesFileTypeMap;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.Type;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.guest.GuestService;
import com.openexchange.java.Streams;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.oxfolder.OXFolderAdminHelper;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.user.UserService;
import com.openexchange.user.json.Constants;
import com.openexchange.user.json.field.UserField;
import com.openexchange.user.json.mapping.UserMapper;

/**
 * {@link UpdateAction} - Maps the action to an <tt>update</tt> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Action(method = RequestMethod.PUT, name = "update", description = "Update a user.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "id", description = "Object ID of the updated user."),
    @Parameter(name = "timestamp", type = Type.NUMBER, description = "Timestamp of the updated user. If the user was modified after the specified timestamp, then the update must fail.")
}, requestBody = "User object as described in Common object data, Detailed contact data and Detailed user data. Only modified fields are present. Note: \"timezone\" and \"locale\" are the only fields from Detailed user data which are allowed to be updated.",
    responseDescription = "Response with timestamp: An empty object.")
public final class UpdateAction extends AbstractUserAction {

    /**
     * The <tt>update</tt> action string.
     */
    public static final String ACTION = AJAXServlet.ACTION_UPDATE;

    /**
     * Initializes a new {@link UpdateAction}.
     */
    public UpdateAction(ServiceLookup services) {
        super(services);
    }

    private static UserField[] USER_FIELDS = { UserField.ID, UserField.LOCALE, UserField.TIME_ZONE };

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        try {
            /*
             * Parse parameters
             */
            long maxSize = sysconfMaxUpload();
            boolean containsImage = request.hasUploads(-1, maxSize > 0 ? maxSize : -1L);

            final int id = checkIntParameter(AJAXServlet.PARAMETER_ID, request);
            final Date clientLastModified = new Date(checkLongParameter(AJAXServlet.PARAMETER_TIMESTAMP, request));
            /*
             * Get user service to get contact ID
             */
            final UserService userService = services.getService(UserService.class);
            final User storageUser = userService.getUser(id, session.getContext());
            final int contactId = storageUser.getContactId();
            /*
             * Parse user & contact data
             */
            String timeZoneID = request.getParameter("timezone");
            if (null == timeZoneID) {
                timeZoneID = session.getUser().getTimeZone();
            }
            final JSONObject jData = containsImage ? new JSONObject(request.getUploadEvent().getFormField("json")) : (JSONObject) request.requireData();
            Contact parsedUserContact;
            User parsedUser;
            parsedUserContact = ContactMapper.getInstance().deserialize(jData, ContactMapper.getInstance().getAllFields(), timeZoneID);
            parsedUserContact.setObjectID(contactId);
            jData.put(UserField.ID.getName(), id);
            parsedUser = UserMapper.getInstance().deserialize(jData, USER_FIELDS, timeZoneID);

            if (containsImage) {
                setImageData(request, parsedUserContact);
            }

            /*
             * Update contact
             */
            final ContactService contactService;
            if (!storageUser.isGuest()) {
                contactService = services.getService(ContactService.class);
                if (parsedUserContact.containsDisplayName()) {
                    final String displayName = parsedUserContact.getDisplayName();
                    if (null != displayName) {
                        if (com.openexchange.java.Strings.isEmpty(displayName)) {
                            parsedUserContact.removeDisplayName();
                        } else {
                            // Remove display name if equal to storage version to avoid update conflict
                            Contact storageContact = contactService.getUser(session, id, new ContactField[] { ContactField.DISPLAY_NAME });
                            if (displayName.equals(storageContact.getDisplayName())) {
                                parsedUserContact.removeDisplayName();
                            }
                        }
                    }
                }
                contactService.updateUser(session, Integer.toString(Constants.USER_ADDRESS_BOOK_FOLDER_ID), Integer.toString(contactId), parsedUserContact, clientLastModified);
            } else {
                ContactUserStorage contactUserStorage = services.getService(ContactUserStorage.class);
                contactUserStorage.updateGuestContact(session, contactId, parsedUserContact, parsedUserContact.getLastModified());

                GuestService guestService = services.getService(GuestService.class);
                if ((guestService != null) && (storageUser.isGuest())) {
                    Contact updatedGuestContact = contactUserStorage.getGuestContact(session.getContextId(), id, ContactField.values());
                    guestService.updateGuestContact(updatedGuestContact, session.getContextId());
                }
            }
            /*
             * Update user, too, if necessary
             */
            final String parsedTimeZone = parsedUser.getTimeZone();
            final Locale parsedLocale = parsedUser.getLocale();
            if ((null != parsedTimeZone) || (null != parsedLocale)) {
                if (null == parsedTimeZone) {
                    UserMapper.getInstance().get(UserField.TIME_ZONE).copy(storageUser, parsedUser);
                }
                if (null == parsedLocale) {
                    UserMapper.getInstance().get(UserField.LOCALE).copy(storageUser, parsedUser);
                }

                userService.updateUser(parsedUser, session.getContext());

                GuestService guestService = services.getService(GuestService.class);
                if ((guestService != null) && (storageUser.isGuest())) {
                    User updatedUser = userService.getUser(id, session.getContextId());
                    guestService.updateGuestUser(updatedUser, session.getContextId());
                }
            }
            /*
             * Check what has been updated
             */
            if (parsedUserContact.containsDisplayName() && null != parsedUserContact.getDisplayName()) {
                // Update folder name if display-name was changed
                final DatabaseService service = services.getService(DatabaseService.class);
                if (null != service) {
                    final int contextId = session.getContextId();
                    Connection con = null;
                    boolean rollback = false;
                    try {
                        con = service.getWritable(contextId);
                        con.setAutoCommit(false);
                        rollback = true;
                        final int[] changedfields = new int[] { Contact.DISPLAY_NAME };
                        OXFolderAdminHelper.propagateUserModification(id, changedfields, System.currentTimeMillis(), con, con, contextId);
                        con.commit();
                        rollback = false;
                    } catch (final Exception ignore) {
                        // Ignore
                    } finally {
                        if (null != con) {
                            if (rollback) {
                                Databases.rollback(con);
                            }
                            Databases.autocommit(con);
                            service.backWritable(contextId, con);
                        }
                    }
                }
            }
            /*
             * Return contact last-modified from server
             */
            return new AJAXRequestResult(new JSONObject(0), parsedUserContact.getLastModified());
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }

    }

    // Copied from RequestTools in contact module

    public static void setImageData(final AJAXRequestData request, final Contact contact) throws OXException {
        UploadEvent uploadEvent = null;
        try {
            uploadEvent = request.getUploadEvent();
            final UploadFile uploadFile;
            {
                final List<UploadFile> list = uploadEvent.getUploadFilesByFieldName("file");
                uploadFile = null == list || list.isEmpty() ? null : list.get(0);
            }
            if (null == uploadFile) {
                throw AjaxExceptionCodes.NO_UPLOAD_IMAGE.create();
            }
            setImageData(contact, uploadFile);
        } finally {
            if (null != uploadEvent) {
                uploadEvent.cleanUp();
            }
        }
    }

    public static void setImageData(final Contact contact, final UploadFile file) throws OXException {
        checkIsImageFile(file);
        FileInputStream fis = null;
        ByteArrayOutputStream outputStream = null;
        try {
            fis = new FileInputStream(file.getTmpFile());
            outputStream = new UnsynchronizedByteArrayOutputStream((int) file.getSize());
            final byte[] buf = new byte[2048];
            int len = -1;
            while ((len = fis.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            contact.setImage1(outputStream.toByteArray());
            contact.setImageContentType(file.getContentType());
        } catch (final FileNotFoundException e) {
            throw AjaxExceptionCodes.NO_UPLOAD_IMAGE.create(e);
        } catch (final IOException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, "I/O error while reading uploaded contact image.");
        } finally {
            Streams.close(outputStream);
            Streams.close(fis);
        }
    }

    private static void checkIsImageFile(UploadFile file) throws OXException {
        if (null == file) {
            throw AjaxExceptionCodes.NO_UPLOAD_IMAGE.create();
        }
        String contentType = file.getContentType();
        if (isImageContentType(contentType)) {
            return;
        }
        String mimeType = null;
        if (null != file.getPreparedFileName()) {
            mimeType = new MimetypesFileTypeMap().getContentType(file.getPreparedFileName());
            if (isImageContentType(mimeType)) {
                return;
            }
        }
        String readableType = (null == contentType ? (null == mimeType ? "application/unknown" : mimeType) : contentType);
        //        int idx = readableType.indexOf('/');
        //        if (-1 < idx && idx < readableType.length()) {
        //            readableType = readableType.substring(idx + 1);
        //        }
        throw AjaxExceptionCodes.NO_IMAGE_FILE.create(file.getPreparedFileName(), readableType);
    }

    private static boolean isImageContentType(String contentType) {
        return null != contentType && contentType.toLowerCase().startsWith("image");
    }

    private static long sysconfMaxUpload() {
        final String sizeS = ServerConfig.getProperty(com.openexchange.configuration.ServerConfig.Property.MAX_UPLOAD_SIZE);
        if (null == sizeS) {
            return 0;
        }
        return Long.parseLong(sizeS);
    }

}
