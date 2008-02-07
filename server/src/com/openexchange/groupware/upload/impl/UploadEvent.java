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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Just a plain class that wraps informations about the upload e.g. file name,
 * content type, size, etc.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class UploadEvent {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(UploadEvent.class);

	public static final int MAIL_UPLOAD = 1;

	public static final int APPOINTMENT_UPLOAD = 2;

	public static final int TASK_UPLOAD = 3;

	public static final int CONTACT_UPLOAD = 4;

	public static final int DOCUMENT_UPLOAD = 5;

	private int affiliationId = -1;

	private final Map<String, UploadFile> uploadFiles;

	private final Map<String, String> formFields;

	private String action;

	private final Map<String, Object> parameters;

	private List<UploadFile> sequentialList;

	public UploadEvent() {
		super();
		this.uploadFiles = new HashMap<String, UploadFile>();
		this.formFields = new HashMap<String, String>();
		this.parameters = new HashMap<String, Object>();
	}

	public final int getAffiliationId() {
		return affiliationId;
	}

	public final void setAffiliationId(final int affiliationId) {
		this.affiliationId = affiliationId;
	}

	public final void addUploadFile(final UploadFile uploadFile) {
		if (sequentialList != null) {
			/*
			 * Discard cached sequential list
			 */
			sequentialList = null;
		}
		if (uploadFiles.containsKey(uploadFile.getFileName())) {
			UploadFile current = uploadFiles.get(uploadFile.getFileName());
			while (current.getHomonymous() != null) {
				current = current.getHomonymous();
			}
			current.setHomonymous(uploadFile);
		} else {
			uploadFiles.put(uploadFile.getFileName(), uploadFile);
		}
	}

	public final void removeUploadFile(final String fileName) {
		if (sequentialList != null) {
			/*
			 * Discard cached sequential list
			 */
			sequentialList = null;
		}
		UploadFile uploadFile = uploadFiles.remove(fileName);
		while (uploadFile != null) {
			if (uploadFile.getTmpFile().exists()) {
				try {
					if (!uploadFile.getTmpFile().delete()) {
						LOG.error(new StringBuilder(ERR_PREFIX).append(uploadFile.getTmpFile().getName()));
					}
				} catch (SecurityException e) {
					LOG.error(new StringBuilder(ERR_PREFIX).append(uploadFile.getTmpFile().getName()), e);
				}
			}
			uploadFile = uploadFile.getHomonymous();
		}
	}

	public final UploadFile getUploadFile(final String fileName) {
		return uploadFiles.get(fileName);
	}

	public final UploadFile getUploadFileByFieldName(final String fieldName) {
		final int size = uploadFiles.size();
		final Iterator<Map.Entry<String, UploadFile>> iter = uploadFiles.entrySet().iterator();
		for (int i = 0; i < size; i++) {
			UploadFile uf = iter.next().getValue();
			while (uf != null) {
				if (uf.getFieldName().equalsIgnoreCase(fieldName)) {
					return uf;
				}
				uf = uf.getHomonymous();
			}
		}
		return null;
	}

	public final void clearUploadFiles() {
		cleanUp();
	}

	public final int getNumberOfUploadFiles() {
		return createList().size();
	}

	public final Iterator<UploadFile> getUploadFilesIterator() {
		return createList().iterator();
	}

	public final List<UploadFile> getUploadFiles() {
		return createList();
	}

	private final List<UploadFile> createList() {
		if (sequentialList != null) {
			/*
			 * Return cached sequential list
			 */
			return sequentialList;
		}
		final int size = uploadFiles.size();
		if (size == 0) {
			return new ArrayList<UploadFile>(0);
		}
		sequentialList = new ArrayList<UploadFile>(size);
		final Iterator<Map.Entry<String, UploadFile>> iter = uploadFiles.entrySet().iterator();
		for (int i = 0; i < size; i++) {
			UploadFile uf = iter.next().getValue();
			while (uf != null) {
				sequentialList.add(uf);
				uf = uf.getHomonymous();
			}
		}
		return sequentialList;
	}

	public final void addFormField(final String fieldName, final String fieldValue) {
		formFields.put(fieldName, fieldValue);
	}

	public final String removeFormField(final String fieldName) {
		return formFields.remove(fieldName);
	}

	public final String getFormField(final String fieldName) {
		return formFields.get(fieldName);
	}

	public final void clearFormFields() {
		formFields.clear();
	}

	public final Iterator getFormFieldNames() {
		return formFields.keySet().iterator();
	}

	public final String getAction() {
		return action;
	}

	public final void setAction(final String action) {
		this.action = action;
	}

	public final Object getParameter(final String name) {
		return name == null ? null : parameters.get(name);
	}

	public final void setParameter(final String name, final Object value) {
		if (name != null && value != null) {
			parameters.put(name, value);
		}
	}

	public final void removeParameter(final String name) {
		if (name != null) {
			parameters.remove(name);
		}
	}

	private static final String ERR_PREFIX = "Temporary upload file could not be deleted: ";

	/**
	 * Deletes all created temporary files created through this
	 * <code>DeleteEvent</code> instance and clears upload files.
	 */
	public final void cleanUp() {
		final List<UploadFile> l = createList();
		final int size = l.size();
		final Iterator<UploadFile> iter = l.iterator();
		for (int i = 0; i < size; i++) {
			final UploadFile uploadFile = iter.next();
			final File tmpFile = uploadFile.getTmpFile();
			if (tmpFile.exists()) {
				try {
					if (!tmpFile.delete()) {
						LOG.error(new StringBuilder(ERR_PREFIX).append(tmpFile.getName()));
					}
				} catch (SecurityException e) {
					LOG.error(new StringBuilder(ERR_PREFIX).append(tmpFile.getName()), e);
				}
			}
		}
		uploadFiles.clear();
	}

	/**
	 * Strips off heading path informations from specified file path by looking
	 * for last occurrence of a common file separator character like
	 * <code>'/'</code> or <code>'\'</code> to only return sole file name.
	 * 
	 * @param filePath
	 *            The file path
	 * @return The sole file name.
	 */
	public static final String getFileName(final String filePath) {
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
