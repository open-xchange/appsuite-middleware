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

package com.openexchange.groupware.upload;

import java.util.Iterator;

/**
 * {@link StreamedUpload} - A streamed upload.
 * <p>
 * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
 * Please note that a streamed upload comes with various limitations as the uploaded files are supposed to be streamed-through to the
 * destination. Therefore {@link #getUploadFiles()} may only be called once and the upload files returned by {@link StreamedUploadFileIterator}
 * are supposed to be handled/consumed directly. Continuing to the next upload file renders the previously returned one unusable.
 * </div>
 * <p>
 * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
 * Requires that a multipart/form-data POST request first provides the named form-data items then followed by uploaded files.<br>
 * Example:
 * <pre>
 * ------SomeBoundary
 * Content-Disposition: form-data; name="json"
 *
 * {"folder_id":"1234","description":"Some descriptive text"}
 * ------SomeBoundary
 * Content-Disposition: form-data; name="file"; filename="image.png"
 * Content-Type: image/png
 *
 * [file-data]
 * ------SomeBoundary--
 * </pre>
 * </div>
 * <p>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public interface StreamedUpload {

    /**
     * Gets the form field whose name equals specified field name.
     *
     * @param fieldName The field name.
     * @return The value of associated form field or <code>null</code>.
     */
    String getFormField(String fieldName);

    /**
     * Gets an iterator for form fields.
     *
     * @return An iterator for form fields.
     */
    Iterator<String> getFormFieldNames();

    /**
     * Whether this stream upload provides any upload file
     *
     * @return <code>true</code> if any upload file is provided; otherwise <code>false</code>
     */
    boolean hasAny();

    /**
     * Gets an iterator for upload files.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>:
     * <ul>
     * <li>
     * This method can only be called once. Calling this method multiple times will yield <code>IllegalStateException</code>
     * </li>
     * <li>
     * Each retrieved <code>StreamedUploadFile</code> is supposed to be handled directly.<br>
     * Continuing to the (possibly) next <code>StreamedUploadFile</code> instance renders the previously obtained one useless.
     * </li>
     * </ul>
     * </div>
     *
     * @return An iterator for form fields.
     * @throws IllegalStateException If this method is called more than once
     */
    StreamedUploadFileIterator getUploadFiles();

}
