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
     * Gets the first form field.
     *
     * @return The value of first form field or <code>null</code>.
     */
    String getFirstFormField();

    /**
     * Gets an iterator for form fields.
     *
     * @return An iterator for form fields.
     */
    Iterator<String> getFormFieldNames();

    /**
     * Whether this stream upload provides any upload file
     * <p>
     * In contrast to {@link #getUploadFiles()} this method is allowed to be called multiple times.
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
     * This method can only be called once. Calling this method multiple times will yield <code>IllegalStateException</code>. For a quick
     * check whether this streamed upload provides any upload files, please use {@link #hasAny()}.
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
