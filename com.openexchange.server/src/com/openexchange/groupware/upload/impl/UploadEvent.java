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

package com.openexchange.groupware.upload.impl;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.groupware.upload.UploadFile;

/**
 * Just a plain class that wraps information about an upload e.g. files, form fields, content type, size, etc.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UploadEvent {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UploadEvent.class);

    /*-
     * ------------ Constants ------------
     */

    /**
     * Affiliation ID for an upload dedicated to mail module.
     */
    public static final int MAIL_UPLOAD = 1;

    /**
     * Affiliation ID for an upload dedicated to calendar module.
     */
    public static final int APPOINTMENT_UPLOAD = 2;

    /**
     * Affiliation ID for an upload dedicated to task module.
     */
    public static final int TASK_UPLOAD = 3;

    /**
     * Affiliation ID for an upload dedicated to contact module.
     */
    public static final int CONTACT_UPLOAD = 4;

    /**
     * Affiliation ID for an upload dedicated to infostore module.
     */
    public static final int DOCUMENT_UPLOAD = 5;

    /*-
     * ------------ Members ------------
     */

    private int affiliationId = -1;

    private final Map<String, List<UploadFile>> uploadFilesByFieldName;

    private final Map<String, String> formFields;

    private String action;

    private final Map<String, Object> parameters;

    /**
     * Initializes a new {@link UploadEvent}.
     */
    public UploadEvent() {
        super();
        uploadFilesByFieldName = new LinkedHashMap<String, List<UploadFile>>();
        formFields = new HashMap<String, String>();
        parameters = new HashMap<String, Object>();
    }

    /**
     * Gets the affiliation ID.
     *
     * @return The affiliation ID.
     */
    public final int getAffiliationId() {
        return affiliationId;
    }

    /**
     * Sets the affiliation ID.
     *
     * @param affiliationId The affiliation ID.
     */
    public final void setAffiliationId(int affiliationId) {
        this.affiliationId = affiliationId;
    }

    /**
     * Adds given upload file.
     *
     * @param uploadFile The upload file to add.
     */
    public final void addUploadFile(UploadFile uploadFile) {
        if (null != uploadFile) {
            String fieldName = uploadFile.getFieldName();
            List<UploadFile> list = uploadFilesByFieldName.get(fieldName);
            if (null == list) {
                list = new LinkedList<UploadFile>();
                uploadFilesByFieldName.put(fieldName, list);
            }
            list.add(uploadFile);
        }
    }

    /**
     * Gets the (first) upload file associated with specified field name.
     *
     * @param fieldName The field name.
     * @return The upload file associated with specified field name or <code>null</code>
     */
    public final UploadFile getUploadFileByFieldName(String fieldName) {
        List<UploadFile> list = uploadFilesByFieldName.get(fieldName);
        return null == list || list.isEmpty() ? null : list.get(0);
    }

    /**
     * Gets the upload files associated with specified field name.
     *
     * @param fieldName The field name.
     * @return The upload files associated with specified field name or <code>null</code>
     */
    public final List<UploadFile> getUploadFilesByFieldName(String fieldName) {
        List<UploadFile> list = uploadFilesByFieldName.get(fieldName);
        return null == list ? null : Collections.unmodifiableList(list);
    }

    /**
     * Gets the upload files associated with specified file name.
     *
     * @param fileName The file name.
     * @return The upload files associated with specified file name.
     */
    public final List<UploadFile> getUploadFileByFileName(String fileName) {
        if (null == fileName) {
            return Collections.emptyList();
        }
        List<UploadFile> ret = new LinkedList<UploadFile>();
        for (List<UploadFile> ufs : uploadFilesByFieldName.values()) {
            for (UploadFile uf : ufs) {
                if (fileName.equals(uf.getFileName())) {
                    ret.add(uf);
                }
            }
        }
        return ret;
    }

    /**
     * Clears all upload files.
     */
    public final void clearUploadFiles() {
        cleanUp();
    }

    /**
     * Gets the number of upload files.
     *
     * @return The number of upload files.
     */
    public final int getNumberOfUploadFiles() {
        return createList().size();
    }

    /**
     * Gets an iterator for upload files.
     *
     * @return An iterator for upload files.
     */
    public final Iterator<UploadFile> getUploadFilesIterator() {
        return createList().iterator();
    }

    /**
     * Gets a list containing the upload files.
     *
     * @return A list containing the upload files.
     */
    public final List<UploadFile> getUploadFiles() {
        return createList();
    }

    private final List<UploadFile> createList() {
        if (uploadFilesByFieldName.isEmpty()) {
            return Collections.emptyList();
        }
        List<UploadFile> ret = new LinkedList<UploadFile>();
        for (List<UploadFile> ufs : uploadFilesByFieldName.values()) {
            ret.addAll(ufs);
        }
        return ret;
    }

    /**
     * Adds a name-value-pair of a form field.
     *
     * @param fieldName The field's name.
     * @param fieldValue The field's value.
     */
    public final void addFormField(String fieldName, String fieldValue) {
        formFields.put(fieldName, fieldValue);
    }

    /**
     * Gets the number of form fields.
     *
     * @return The number of form fields
     */
    public int getNumberOfFormFields() {
        return formFields.size();
    }

    /**
     * Removes the form field whose name equals specified field name.
     *
     * @param fieldName The field name.
     * @return The removed form field's value or <code>null</code>.
     */
    public final String removeFormField(String fieldName) {
        return formFields.remove(fieldName);
    }

    /**
     * Gets the form field whose name equals specified field name.
     *
     * @param fieldName The field name.
     * @return The value of associated form field or <code>null</code>.
     */
    public final String getFormField(String fieldName) {
        return formFields.get(fieldName);
    }

    /**
     * Clears all form fields.
     */
    public final void clearFormFields() {
        formFields.clear();
    }

    /**
     * Gets an iterator for form fields.
     *
     * @return An iterator for form fields.
     */
    public final Iterator<String> getFormFieldNames() {
        return formFields.keySet().iterator();
    }

    /**
     * Gets this upload event's action string.
     *
     * @return The action string.
     */
    public final String getAction() {
        return action;
    }

    /**
     * Sets this upload event's action string.
     *
     * @param action The action string.
     */
    public final void setAction(String action) {
        this.action = action;
    }

    /**
     * Gets the parameter associated with specified name.
     *
     * @param name The parameter's name.
     * @return The parameter associated with specified name or <code>null</code> .
     */
    public final Object getParameter(String name) {
        return name == null ? null : parameters.get(name);
    }

    /**
     * Associates specified parameter name with given parameter value.
     *
     * @param name The parameter name.
     * @param value The parameter value.
     */
    public final void setParameter(String name, Object value) {
        if (name != null && value != null) {
            parameters.put(name, value);
        }
    }

    /**
     * Removes the parameter associated with specified name.
     *
     * @param name The parameter's name.
     */
    public final void removeParameter(String name) {
        if (name != null) {
            parameters.remove(name);
        }
    }

    /**
     * Deletes all created temporary files created through this <code>UploadEvent</code> instance and clears upload files.
     */
    public final void cleanUp() {
        for (List<UploadFile> uploadFiles : uploadFilesByFieldName.values()) {
            for (UploadFile uploadFile : uploadFiles) {
                File tmpFile = uploadFile.getTmpFile();
                if (null != tmpFile && tmpFile.exists()) {
                    try {
                        if (!tmpFile.delete()) {
                            LOG.error("Temporary upload file could not be deleted: {}", tmpFile.getName());
                        }
                    } catch (Exception e) {
                        LOG.error("Temporary upload file could not be deleted: {}", tmpFile.getName(), e);
                    }
                }
            }
        }
        uploadFilesByFieldName.clear();
        LOG.debug("Upload event cleaned-up. All temporary stored files deleted.");
    }

    /**
     * Strips off heading path information from specified file path by looking for last occurrence of a common file separator character like
     * <code>'/'</code> or <code>'\'</code> to only return sole file name.
     *
     * @param filePath The file path
     * @return The sole file name.
     */
    public static final String getFileName(String filePath) {
        String retval = filePath;
        int pos;
        if ((pos = retval.lastIndexOf('\\')) > -1) {
            retval = retval.substring(pos + 1);
        } else if ((pos = retval.lastIndexOf('/')) > -1) {
            retval = retval.substring(pos + 1);
        }
        return retval;
    }

}
