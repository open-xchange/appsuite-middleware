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



package com.openexchange.groupware.container.mail;

import java.io.File;
import java.io.InputStream;
import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.mail.parser.MessageUtils;
import com.openexchange.groupware.imap.OXMailException;
import com.openexchange.groupware.imap.OXMailException.MailCode;
import com.openexchange.groupware.upload.UploadEvent;

/**
 * JSONMessageAttachmentObject
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class JSONMessageAttachmentObject implements Cloneable {

	public static final int CONTENT_NONE = 0;

	public static final int CONTENT_STRING = 1;

	public static final int CONTENT_INPUT_STREAM = 2;

	public static final int CONTENT_BYTE_ARRAY = 3;

	public static final String DISPOSITION_ALTERNATIVE = "alternative";

	private String fileName;

	private Object content;

	private String contentType;

	private long size = -1;

	private String positionInMail;

	private String disposition;

	private File uniqueDiskFileName;

	private InputStream infostoreDocumentInputStream;

	private int contentID = CONTENT_NONE;

	public JSONMessageAttachmentObject() {
		super();
	}

	public JSONMessageAttachmentObject(String positionInMail) {
		this();
		this.positionInMail = positionInMail;
	}

	public void reset() {
		fileName = null;
		content = null;
		contentType = null;
		size = -1;
		positionInMail = null;
		disposition = null;
		uniqueDiskFileName = null;
		infostoreDocumentInputStream = null;
		contentID = CONTENT_NONE;
	}

	public Object getContent() {
		return content;
	}

	/**
	 * Sets the content of this message attachment instance which can either be
	 * instance of <code>java.lang.String</code>,
	 * <code>java.io.InputStream</code> or can be left <code>null</code>
	 * 
	 * @param content
	 */
	public void setContent(final Object content) {
		this.content = content;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(final String contentType) {
		this.contentType = contentType;
	}

	public String getDisposition() {
		return disposition;
	}

	public void setDisposition(final String disposition) {
		this.disposition = disposition;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName == null ? null : UploadEvent.getFileName(fileName);
	}

	public String getPositionInMail() {
		return positionInMail;
	}

	public void setPositionInMail(final String id) {
		this.positionInMail = id;
	}

	public long getSize() {
		return size;
	}

	public void setSize(final long size) {
		this.size = size;
	}

	public File getUniqueDiskFileName() {
		return uniqueDiskFileName;
	}

	public void setUniqueDiskFileName(final File diskFileName) {
		this.uniqueDiskFileName = diskFileName;
	}

	public InputStream getInfostoreDocumentInputStream() {
		return infostoreDocumentInputStream;
	}

	public void setInfostoreDocumentInputStream(final InputStream infostoreDocumentInputStream) {
		this.infostoreDocumentInputStream = infostoreDocumentInputStream;
	}

	public int getContentID() {
		return contentID;
	}

	public void setContentID(final int contentID) {
		this.contentID = contentID;
	}

	/**
	 * @return an attachment comparator which uses attachment's position in mail
	 */
	public static Comparator<JSONMessageAttachmentObject> getAttachmentComparator() {
		return new Comparator<JSONMessageAttachmentObject>() {
			public int compare(JSONMessageAttachmentObject o1, JSONMessageAttachmentObject o2) {
				final int[] levelArr1 = MessageUtils.parseIdentifier(o1.getPositionInMail());
				final int[] levelArr2 = MessageUtils.parseIdentifier(o2.getPositionInMail());
				final int decreasedLength = levelArr1.length - 1;
				for (int i = 0; i < decreasedLength; i++) {
					if (levelArr1[i] < levelArr2[i]) {
						return -1;
					} else if (levelArr1[i] > levelArr2[i]) {
						return 1;
					}
				}
				if (levelArr1[decreasedLength] < levelArr2[decreasedLength]) {
					return -1;
				} else if (levelArr1[decreasedLength] > levelArr2[decreasedLength]) {
					return 1;
				} else {
					return 0;
				}
			}
		};
	}

	public String toString() {
		try {
			return getJSONObject().toString();
		} catch (JSONException e) {
			return "toString() failed";
		}
	}

	public JSONObject getJSONObject() throws JSONException {
		final JSONObject jsonObj = new JSONObject();
		jsonObj.put(JSONMessageObject.JSON_ID, positionInMail);
		jsonObj.put(JSONMessageObject.JSON_ATTACHMENT_FILE_NAME, fileName);
		jsonObj.put(JSONMessageObject.JSON_SIZE, size);
		jsonObj.put(JSONMessageObject.JSON_CONTENT_TYPE, contentType);
		jsonObj.put(JSONMessageObject.JSON_CONTENT, content == null ? JSONObject.NULL : content);
		jsonObj.put(JSONMessageObject.JSON_ATTACHMENT_UNIQUE_DISK_FILE_NAME,
				uniqueDiskFileName == null ? JSONObject.NULL : uniqueDiskFileName.getPath());
		jsonObj.put(JSONMessageObject.JSON_DISPOSITION, disposition);
		return jsonObj;
	}

	public JSONMessageAttachmentObject parseJSONObject(final JSONObject jo, final int partLevel,
			final int partCount) throws OXException {
		try {
			/*
			 * Set attachment's position in mail
			 */
			if (jo.has(JSONMessageObject.JSON_ID) && !jo.isNull(JSONMessageObject.JSON_ID)) {
				/*
				 * Id given by JSON object. Propably a reference id.
				 */
				setPositionInMail(jo.getString(JSONMessageObject.JSON_ID));
			} else {
				/*
				 * None found in JSON object. Create one according to given message level, multipart level & part count
				 */
				setPositionInMail(MessageUtils.getIdentifier(new int[] { partLevel, partCount}));
			}

			if (jo.has(JSONMessageObject.JSON_CONTENT_TYPE) && !jo.isNull(JSONMessageObject.JSON_CONTENT_TYPE)) {
				setContentType(jo.getString(JSONMessageObject.JSON_CONTENT_TYPE));
			}

			if ((!jo.has(JSONMessageObject.JSON_CONTENT) || jo.isNull(JSONMessageObject.JSON_CONTENT))
					&& (!jo.has(JSONMessageObject.JSON_ATTACHMENT_FILE_NAME) || jo
							.isNull(JSONMessageObject.JSON_ATTACHMENT_FILE_NAME))) {
				throw new OXMailException(MailCode.MISSING_JSON_KEY_XOR, JSONMessageObject.JSON_CONTENT,
						JSONMessageObject.JSON_ATTACHMENT_FILE_NAME);
			} else if ((jo.has(JSONMessageObject.JSON_CONTENT) && !jo.isNull(JSONMessageObject.JSON_CONTENT))
					&& (jo.has(JSONMessageObject.JSON_ATTACHMENT_FILE_NAME) && !jo
							.isNull(JSONMessageObject.JSON_ATTACHMENT_FILE_NAME))) {
				throw new OXMailException(MailCode.INVALID_KEY_COMBINATION, JSONMessageObject.JSON_CONTENT,
						JSONMessageObject.JSON_ATTACHMENT_FILE_NAME);
			}

			if (jo.has(JSONMessageObject.JSON_ATTACHMENT_FILE_NAME)
					&& !jo.isNull(JSONMessageObject.JSON_ATTACHMENT_FILE_NAME)) {
				setFileName(jo.getString(JSONMessageObject.JSON_ATTACHMENT_FILE_NAME));
			}

			if (jo.has(JSONMessageObject.JSON_SIZE) && !jo.isNull(JSONMessageObject.JSON_SIZE)) {
				setSize(jo.getInt(JSONMessageObject.JSON_SIZE));
			}

			if (jo.has(JSONMessageObject.JSON_DISPOSITION) && !jo.isNull(JSONMessageObject.JSON_DISPOSITION)) {
				setDisposition(jo.getString(JSONMessageObject.JSON_DISPOSITION));
			}

			if (jo.has(JSONMessageObject.JSON_CONTENT) && !jo.isNull(JSONMessageObject.JSON_CONTENT)) {
				setContent(jo.getString(JSONMessageObject.JSON_CONTENT));
			}

			if (jo.has(JSONMessageObject.JSON_ATTACHMENT_UNIQUE_DISK_FILE_NAME)
					&& !jo.isNull(JSONMessageObject.JSON_ATTACHMENT_UNIQUE_DISK_FILE_NAME)) {
				setUniqueDiskFileName(new File(jo.getString(JSONMessageObject.JSON_ATTACHMENT_UNIQUE_DISK_FILE_NAME)));
			}

			return this;
		} catch (JSONException e) {
			throw new OXMailException(MailCode.INTERNAL_ERROR, e, e.getMessage());
		}

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		JSONMessageAttachmentObject copy;
		try {
			copy = (JSONMessageAttachmentObject) super.clone();
		} catch (CloneNotSupportedException e) {
			// This shouldn't happen, since we are Cloneable
			return null;
		}
		// Return bitwise copy
		return copy;
	}

}
