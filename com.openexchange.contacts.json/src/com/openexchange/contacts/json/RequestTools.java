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

package com.openexchange.contacts.json;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.activation.MimetypesFileTypeMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.java.Streams;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;


/**
 * {@link RequestTools}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class RequestTools {

    public static int getNullableIntParameter(final AJAXRequestData request, final String parameter) throws OXException {
        Integer intParam = null;
        try {
            intParam = request.getParameter(parameter, int.class, true);
            if (intParam == null) {
                return 0;
            }
            return intParam.intValue();
        } catch (final NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, parameter, intParam);
        }
    }

    public static int[][] buildObjectIdAndFolderId(final JSONArray json) throws OXException {
        final int[][] objectIdAndFolderId = new int[json.length()][];
        for (int i = 0; i < json.length(); i++) {
            try {
                final JSONObject object = json.getJSONObject(i);
                final int folder = object.getInt("folder");
                final int id = object.getInt("id");
                objectIdAndFolderId[i] = new int[] { id, folder };
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            }
        }

        return objectIdAndFolderId;
    }

    /**
     * Applies image data from request to given contact.
     *
     * @param request The request providing image data
     * @param contact The contact
     * @throws OXException If applying image data to contact fails
     */
	public static void setImageData(final ContactRequest request, final Contact contact) throws OXException {
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

    /**
     * Applies image data from file to given contact.
     *
     * @param contact The contact
     * @param file The (uploaded) file providing image data
     * @throws OXException If applying image data to contact fails
     */
    public static void setImageData(final Contact contact, final UploadFile file) throws OXException {
        FileInputStream fis = null;
        try {
            // First check MIME type
            final String checkedMimeType = checkIsImageFile(file);
            // Read image data
            fis = new FileInputStream(file.getTmpFile());
            final ByteArrayOutputStream outputStream = Streams.newByteArrayOutputStream((int) file.getSize());
            String mimeType = null;
            {
                final int buflen = 2048;
                final byte[] buf = new byte[buflen];
                int read;
                // Examine first chunk
                if ((read = fis.read(buf, 0, buflen)) > 0) {
                    mimeType = com.openexchange.java.ImageTypeDetector.getMimeType(buf, 0, read);
                    if (!com.openexchange.java.Strings.toLowerCase(mimeType).startsWith("image/") || com.openexchange.java.HTMLDetector.containsHTMLTags(buf, 0, read)) {
                        throw AjaxExceptionCodes.NO_IMAGE_FILE.create(file.getPreparedFileName(), mimeType);
                    }
                    outputStream.write(buf, 0, read);
                }
                // Read subsequent chunks
                while ((read = fis.read(buf, 0, buflen)) > 0) {
                    if (com.openexchange.java.HTMLDetector.containsHTMLTags(buf, 0, read)) {
                        throw AjaxExceptionCodes.NO_IMAGE_FILE.create(file.getPreparedFileName(), mimeType);
                    }
                    outputStream.write(buf, 0, read);
                }
            }
            // Final check for image's width & height using javax.imageio.*
            if (!isValidImage(Streams.asInputStream(outputStream))) {
                throw AjaxExceptionCodes.NO_IMAGE_FILE.create(file.getPreparedFileName(), mimeType);
            }
            contact.setImage1(outputStream.toByteArray());
            contact.setImageContentType(null == mimeType ? checkedMimeType : mimeType);
        } catch (final FileNotFoundException e) {
            throw AjaxExceptionCodes.NO_UPLOAD_IMAGE.create(e);
        } catch (final IOException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, "I/O error while reading uploaded contact image.");
        } finally {
            Streams.close(fis);
        }
    }

    /**
     * Applies image data from file to given contact.
     *
     * @param contact The contact
     * @param bytes The image data
     * @param mimeType The image MIME type
     * @throws OXException If applying image data to contact fails
     */
    public static void setImageData(final Contact contact, final byte[] bytes, final String mimeType) throws OXException {
        try {
            // First check MIME type
            String contentType = null == mimeType ? "image/jpeg" : com.openexchange.java.Strings.toLowerCase(mimeType);
            if (!contentType.startsWith("image/")) {
                final String readableType = null == mimeType ? "application/unknown" : mimeType;
                throw AjaxExceptionCodes.NO_IMAGE_FILE.create("file", readableType);
            }
            // Check image data
            contentType = com.openexchange.java.ImageTypeDetector.getMimeType(bytes);
            if (!com.openexchange.java.Strings.toLowerCase(contentType).startsWith("image/") || com.openexchange.java.HTMLDetector.containsHTMLTags(bytes)) {
                throw AjaxExceptionCodes.NO_IMAGE_FILE.create("file", contentType);
            }
            // Final check for image's width & height using javax.imageio.*
            if (!isValidImage(Streams.newByteArrayInputStream(bytes))) {
                throw AjaxExceptionCodes.NO_IMAGE_FILE.create("file", contentType);
            }
            contact.setImage1(bytes);
            contact.setImageContentType(contentType);
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, "Error while applying contact image.");
        }
    }

    private static String checkIsImageFile(final UploadFile file) throws OXException {
        if (null == file) {
            throw AjaxExceptionCodes.NO_UPLOAD_IMAGE.create();
        }
        final String contentType = file.getContentType();
        if (isImageContentType(contentType)) {
            return contentType;
        }
        String mimeType = null;
        if (null != file.getPreparedFileName()) {
            mimeType = new MimetypesFileTypeMap().getContentType(file.getPreparedFileName());
            if (isImageContentType(mimeType)) {
                return mimeType;
            }
        }
        // Throw an exception
        final String readableType = null == contentType ? (null == mimeType ? "application/unknown" : mimeType) : contentType;
        throw AjaxExceptionCodes.NO_IMAGE_FILE.create(file.getPreparedFileName(), readableType);
    }

    private static boolean isImageContentType(final String contentType) {
        return null != contentType && com.openexchange.java.Strings.toLowerCase(contentType).startsWith("image/");
    }

    private static boolean isValidImage(final InputStream data) {
        try {
            final java.awt.image.BufferedImage bimg = javax.imageio.ImageIO.read(data);
            return (bimg != null && bimg.getHeight() > 0 && bimg.getWidth() > 0);
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Gets a comparator to sort contacts by an upcoming annual date, relative to the supplied reference date. Only available for
     * {@link ContactField#BIRTHDAY} and {@link ContactField#ANNIVERSARY}.
     *
     * @param dateField The annual date field to compare
     * @param reference The reference date
     * @return
     */
    public static Comparator<Contact> getAnnualDateComparator(final ContactField dateField, final Date reference) {
        return new Comparator<Contact>() {

            @Override
            public int compare(final Contact o1, final Contact o2) {
                Date date1, date2;
                if (ContactField.BIRTHDAY.equals(dateField)) {
                    date1 = o1.getBirthday();
                    date2 = o2.getBirthday();
                } else if (ContactField.ANNIVERSARY.equals(dateField)) {
                    date1 = o1.getAnniversary();
                    date2 = o2.getAnniversary();
                } else {
                    throw new UnsupportedOperationException("Unsupported field: " + dateField);
                }
                if (null == date1) {
                    return null == date2 ? 0 : 1;
                } else if (null == date2) {
                    return -1;
                } else {
                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    calendar.setTime(date1);
                    int month1 = calendar.get(Calendar.MONTH);
                    int dayOfMonth1 = calendar.get(Calendar.DAY_OF_MONTH);
                    int year1 = calendar.get(Calendar.YEAR);
                    calendar.setTime(date2);
                    int month2 = calendar.get(Calendar.MONTH);
                    int dayOfMonth2 = calendar.get(Calendar.DAY_OF_MONTH);
                    int year2 = calendar.get(Calendar.YEAR);
                    calendar.setTime(reference);
                    int monthReference = calendar.get(Calendar.MONTH);
                    int dayOfMonthReference = calendar.get(Calendar.DAY_OF_MONTH);
                    if (month1 == month2 && dayOfMonth1 == dayOfMonth2) {
                        // same month/date, compare years
                        return Integer.valueOf(year1).compareTo(Integer.valueOf(year2));
                    } else if ((month1 >= monthReference || month1 == monthReference && dayOfMonth1 >= dayOfMonthReference) &&
                        (month2 >= monthReference || month2 == monthReference && dayOfMonth2 >= dayOfMonthReference)) {
                        // both after reference date, use default comparison
                        int monthResult = Integer.valueOf(month1).compareTo(Integer.valueOf(month2));
                        return 0 != monthResult ? monthResult : Integer.valueOf(dayOfMonth1).compareTo(Integer.valueOf(dayOfMonth2));
                    } else if ((month1 < monthReference || month1 == monthReference && dayOfMonth1 < dayOfMonthReference) &&
                        (month2 < monthReference || month2 == monthReference && dayOfMonth2 < dayOfMonthReference)) {
                        // both before reference date, use default comparison
                        int monthResult = Integer.valueOf(month1).compareTo(Integer.valueOf(month2));
                        return 0 != monthResult ? monthResult : Integer.valueOf(dayOfMonth1).compareTo(Integer.valueOf(dayOfMonth2));
                    } else if ((month1 >= monthReference || month1 == monthReference && dayOfMonth1 >= dayOfMonthReference) &&
                        (month2 < monthReference || month2 == monthReference && dayOfMonth2 < dayOfMonthReference)) {
                        // first is next
                        return -1;
                    } else {
                     // second is next
                        return 1;
                    }
                }
            }
        };
    }
}
