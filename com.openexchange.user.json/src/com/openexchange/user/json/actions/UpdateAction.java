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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.activation.MimetypesFileTypeMap;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.ContactService;
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
import com.openexchange.java.Streams;
import com.openexchange.tools.oxfolder.OXFolderAdminHelper;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.user.UserService;
import com.openexchange.user.json.Constants;
import com.openexchange.user.json.field.UserField;
import com.openexchange.user.json.mapping.UserMapper;
import com.openexchange.user.json.services.ServiceRegistry;

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
    public UpdateAction() {
        super();
    }

    private static ContactField[] CONTACT_FIELDS;
    static {
        final Set<ContactField> set = EnumSet.noneOf(ContactField.class);
        set.add(ContactField.DISPLAY_NAME);
        set.add(ContactField.SUR_NAME);
        set.add(ContactField.GIVEN_NAME);
        set.add(ContactField.MIDDLE_NAME);
        set.add(ContactField.SUFFIX);
        set.add(ContactField.TITLE);
        set.add(ContactField.STREET_HOME);
        set.add(ContactField.POSTAL_CODE_HOME);
        set.add(ContactField.CITY_HOME);
        set.add(ContactField.STATE_HOME);
        set.add(ContactField.COUNTRY_HOME);
        set.add(ContactField.MARITAL_STATUS);
        set.add(ContactField.NUMBER_OF_CHILDREN);
        set.add(ContactField.PROFESSION);
        set.add(ContactField.NICKNAME);
        set.add(ContactField.SPOUSE_NAME);
        set.add(ContactField.NOTE);
        set.add(ContactField.COMPANY);
        set.add(ContactField.DEPARTMENT);
        set.add(ContactField.POSITION);
        set.add(ContactField.EMPLOYEE_TYPE);
        set.add(ContactField.ROOM_NUMBER);
        set.add(ContactField.STREET_BUSINESS);
        set.add(ContactField.POSTAL_CODE_BUSINESS);
        set.add(ContactField.CITY_BUSINESS);
        set.add(ContactField.STATE_BUSINESS);
        set.add(ContactField.COUNTRY_BUSINESS);
        set.add(ContactField.NUMBER_OF_EMPLOYEE);
        set.add(ContactField.SALES_VOLUME);
        set.add(ContactField.TAX_ID);
        set.add(ContactField.COMMERCIAL_REGISTER);
        set.add(ContactField.BRANCHES);
        set.add(ContactField.BUSINESS_CATEGORY);
        set.add(ContactField.INFO);
        set.add(ContactField.MANAGER_NAME);
        set.add(ContactField.ASSISTANT_NAME);
        set.add(ContactField.STREET_OTHER);
        set.add(ContactField.POSTAL_CODE_OTHER);
        set.add(ContactField.CITY_OTHER);
        set.add(ContactField.STATE_OTHER);
        set.add(ContactField.COUNTRY_OTHER);
        set.add(ContactField.TELEPHONE_ASSISTANT);
        set.add(ContactField.TELEPHONE_BUSINESS1);
        set.add(ContactField.TELEPHONE_BUSINESS2);
        set.add(ContactField.FAX_BUSINESS);
        set.add(ContactField.TELEPHONE_CALLBACK);
        set.add(ContactField.TELEPHONE_CAR);
        set.add(ContactField.TELEPHONE_COMPANY);
        set.add(ContactField.TELEPHONE_HOME1);
        set.add(ContactField.TELEPHONE_HOME2);
        set.add(ContactField.FAX_HOME);
        set.add(ContactField.TELEPHONE_ISDN);
        set.add(ContactField.CELLULAR_TELEPHONE1);
        set.add(ContactField.CELLULAR_TELEPHONE2);
        set.add(ContactField.TELEPHONE_OTHER);
        set.add(ContactField.FAX_OTHER);
        set.add(ContactField.TELEPHONE_PAGER);
        set.add(ContactField.TELEPHONE_PRIMARY);
        set.add(ContactField.TELEPHONE_RADIO);
        set.add(ContactField.TELEPHONE_TELEX);
        set.add(ContactField.TELEPHONE_TTYTDD);
        set.add(ContactField.INSTANT_MESSENGER1);
        set.add(ContactField.INSTANT_MESSENGER2);
        set.add(ContactField.TELEPHONE_IP);
        set.add(ContactField.EMAIL1);
        set.add(ContactField.EMAIL2);
        set.add(ContactField.EMAIL3);
        set.add(ContactField.URL);
        set.add(ContactField.CATEGORIES);
        set.add(ContactField.USERFIELD01);
        set.add(ContactField.USERFIELD02);
        set.add(ContactField.USERFIELD03);
        set.add(ContactField.USERFIELD04);
        set.add(ContactField.USERFIELD05);
        set.add(ContactField.USERFIELD06);
        set.add(ContactField.USERFIELD07);
        set.add(ContactField.USERFIELD08);
        set.add(ContactField.USERFIELD09);
        set.add(ContactField.USERFIELD10);
        set.add(ContactField.USERFIELD11);
        set.add(ContactField.USERFIELD12);
        set.add(ContactField.USERFIELD13);
        set.add(ContactField.USERFIELD14);
        set.add(ContactField.USERFIELD15);
        set.add(ContactField.USERFIELD16);
        set.add(ContactField.USERFIELD17);
        set.add(ContactField.USERFIELD18);
        set.add(ContactField.USERFIELD19);
        set.add(ContactField.USERFIELD20);
        set.add(ContactField.BIRTHDAY);
        set.add(ContactField.ANNIVERSARY);
        set.add(ContactField.COLOR_LABEL);
        set.add(ContactField.DEFAULT_ADDRESS);
        set.add(ContactField.YOMI_FIRST_NAME);
        set.add(ContactField.YOMI_LAST_NAME);
        set.add(ContactField.YOMI_COMPANY);
        set.add(ContactField.HOME_ADDRESS);
        set.add(ContactField.BUSINESS_ADDRESS);
        set.add(ContactField.OTHER_ADDRESS);
        CONTACT_FIELDS = set.toArray(new ContactField[0]);
    }

    //    {
    //        ContactField.DISTRIBUTIONLIST, ContactField.LINKS, ContactField.CATEGORIES, ContactField.COLOR_LABEL, ContactField.PRIVATE_FLAG,
    //        ContactField.NUMBER_OF_ATTACHMENTS, ContactField.FOLDER_ID, ContactField.OBJECT_ID, ContactField.INTERNAL_USERID,
    //        ContactField.CREATED_BY, ContactField.CREATION_DATE, ContactField.MODIFIED_BY, ContactField.LAST_MODIFIED, ContactField.STATE_HOME,
    //        ContactField.COMPANY, ContactField.CELLULAR_TELEPHONE1, ContactField.STREET_HOME, ContactField.STREET_BUSINESS, ContactField.TELEPHONE_HOME1,
    //        ContactField.STATE_BUSINESS, ContactField.DISPLAY_NAME, ContactField.SUR_NAME, ContactField.CITY_HOME, ContactField.MIDDLE_NAME,
    //        ContactField.BIRTHDAY, ContactField.FAX_BUSINESS, ContactField.GIVEN_NAME, ContactField.POSTAL_CODE_HOME, ContactField.POSTAL_CODE_BUSINESS,
    //        ContactField.TELEPHONE_BUSINESS1, ContactField.CITY_BUSINESS, ContactField.IMAGE1, ContactField.IMAGE1_CONTENT_TYPE
    //    }

    private static UserField[] USER_FIELDS = { UserField.ID, UserField.LOCALE, UserField.TIME_ZONE };

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
		try {
	        /*
	         * Parse parameters
	         */
	        boolean containsImage = request.hasUploads();

	        final int id = checkIntParameter(AJAXServlet.PARAMETER_ID, request);
	        final Date clientLastModified = new Date(checkLongParameter(AJAXServlet.PARAMETER_TIMESTAMP, request));
	        /*
	         * Get user service to get contact ID
	         */
	        final UserService userService = ServiceRegistry.getInstance().getService(UserService.class, true);
	        final User storageUser = userService.getUser(id, session.getContext());
	        final int contactId = storageUser.getContactId();
	        /*
	         * Parse user & contact data
	         */
	        String timeZoneID = request.getParameter("timezone");
	        if (null == timeZoneID) {
	            timeZoneID = session.getUser().getTimeZone();
	        }
	        final JSONObject jData = containsImage ? new JSONObject(request.getUploadEvent().getFormField("json")) : (JSONObject) request.getData();
	        Contact parsedUserContact;
	        User parsedUser;
			parsedUserContact = ContactMapper.getInstance().deserialize(jData, CONTACT_FIELDS, timeZoneID);
	        parsedUserContact.setObjectID(contactId);
			jData.put(UserField.ID.getName(), id);
			parsedUser = UserMapper.getInstance().deserialize(jData, USER_FIELDS, timeZoneID);

	        if (containsImage) {
	        	setImageData(request, parsedUserContact);
	        }


	        /*
	         * Update contact
	         */
	        final ContactService contactService = ServiceRegistry.getInstance().getService(ContactService.class, true);
	        if (parsedUserContact.containsDisplayName()) {
	            final String displayName = parsedUserContact.getDisplayName();
	            if (null != displayName) {
	                if (isEmpty(displayName)) {
	                    parsedUserContact.removeDisplayName();
	                } else {
	                    // Remove display name if equal to storage version to avoid update conflict
	                    final Contact storageContact = contactService.getUser(session, id);
	                    if (displayName.equals(storageContact.getDisplayName())) {
	                        parsedUserContact.removeDisplayName();
	                    }
	                }
	            }
	        }
	        contactService.updateUser(session, Integer.toString(Constants.USER_ADDRESS_BOOK_FOLDER_ID), Integer.toString(contactId), parsedUserContact, clientLastModified);
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
	        }
	        /*
	         * Check what has been updated
	         */
	        if (parsedUserContact.containsDisplayName() && null != parsedUserContact.getDisplayName()) {
	            // Update folder name if display-name was changed
                final DatabaseService service = com.openexchange.user.json.services.ServiceRegistry.getInstance().getService(DatabaseService.class);
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
	        return new AJAXRequestResult(new JSONObject(), parsedUserContact.getLastModified());
		} catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
		}

    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
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

}
