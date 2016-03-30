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

package com.openexchange.groupware.attach;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AttachmentField {
	public static final int CREATED_BY = 2;
	public static final int CREATION_DATE = 4;
	public static final int FILE_MIMETYPE = 805;
	public static final int FILE_SIZE = 804;
	public static final int FILENAME = 803;
	public static final int ATTACHED_ID = 801;
	public static final int MODULE_ID = 802;
	public static final int RTF_FLAG = 806;
	public static final int ID = 1;
	public static final int FOLDER_ID = 800;
	public static final int COMMENT = 807;
	public static final int FILE_ID = 880;

	public static final AttachmentField CREATED_BY_LITERAL = new AttachmentField(CREATED_BY,"created_by");
	public static final AttachmentField CREATION_DATE_LITERAL = new AttachmentField(CREATION_DATE, "creation_date");
	public static final AttachmentField FILE_MIMETYPE_LITERAL = new AttachmentField(FILE_MIMETYPE,"file_mimetype");
	public static final AttachmentField FILE_SIZE_LITERAL = new AttachmentField(FILE_SIZE,"file_size");
	public static final AttachmentField FILENAME_LITERAL = new AttachmentField(FILENAME,"filename");
	public static final AttachmentField ATTACHED_ID_LITERAL = new AttachmentField(ATTACHED_ID,"attached");
	public static final AttachmentField MODULE_ID_LITERAL = new AttachmentField(MODULE_ID,"module");
	public static final AttachmentField RTF_FLAG_LITERAL = new AttachmentField(RTF_FLAG,"rtf_flag");
	public static final AttachmentField ID_LITERAL = new AttachmentField(ID,"id");
	public static final AttachmentField FOLDER_ID_LITERAL = new AttachmentField(FOLDER_ID,"folder");
	public static final AttachmentField COMMENT_LITERAL = new AttachmentField(COMMENT,"comment");
	public static final AttachmentField FILE_ID_LITERAL = new AttachmentField(FILE_ID,"file_id");

	public static final AttachmentField[] VALUES_ARRAY = new AttachmentField[]{
		CREATED_BY_LITERAL,
		CREATION_DATE_LITERAL,
		FILE_MIMETYPE_LITERAL,
		FILE_SIZE_LITERAL,
		FILENAME_LITERAL,
		ATTACHED_ID_LITERAL,
		MODULE_ID_LITERAL,
		RTF_FLAG_LITERAL,
		ID_LITERAL,
		FOLDER_ID_LITERAL,
		COMMENT_LITERAL,
		FILE_ID_LITERAL
	};

	private static final AttachmentField[] HTTPAPI_VALUES_ARRAY = new AttachmentField[]{
		CREATED_BY_LITERAL,
		CREATION_DATE_LITERAL,
		FILE_MIMETYPE_LITERAL,
		FILE_SIZE_LITERAL,
		FILENAME_LITERAL,
		ATTACHED_ID_LITERAL,
		MODULE_ID_LITERAL,
		ID_LITERAL,
		COMMENT_LITERAL,
		RTF_FLAG_LITERAL
	};

	public static final List<AttachmentField> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));
	public static final List<AttachmentField> HTTPAPI_VALUES = Collections.unmodifiableList(Arrays.asList(HTTPAPI_VALUES_ARRAY));



	public static AttachmentField get(final int i){
		switch(i){
		case CREATED_BY : return CREATED_BY_LITERAL;
		case CREATION_DATE : return CREATION_DATE_LITERAL;
		case FILE_MIMETYPE : return FILE_MIMETYPE_LITERAL;
		case FILE_SIZE : return FILE_SIZE_LITERAL;
		case FILENAME : return FILENAME_LITERAL;
		case ATTACHED_ID : return ATTACHED_ID_LITERAL;
		case MODULE_ID : return MODULE_ID_LITERAL;
		case ID : return ID_LITERAL;
		case FOLDER_ID : return FOLDER_ID_LITERAL;
		case RTF_FLAG : return RTF_FLAG_LITERAL;
		case COMMENT : return COMMENT_LITERAL;
		case FILE_ID : return FILE_ID_LITERAL;
		default : return null;
		}
	}

	public static AttachmentField get(final String s) {
		for(final AttachmentField field : VALUES) {
			if(field.name.equals(s)) {
				return field;
			}
		}
		return null;
	}

	public Object doSwitch(final AttachmentSwitch sw){
		switch(id){
		case CREATED_BY : return sw.createdBy();
		case CREATION_DATE : return sw.creationDate();
		case FILE_MIMETYPE : return sw.fileMIMEType();
		case FILE_SIZE : return sw.fileSize();
		case FILENAME : return sw.fileName();
		case ATTACHED_ID : return sw.attachedId();
		case MODULE_ID : return sw.moduleId();
		case ID : return sw.id();
		case FOLDER_ID : return sw.folderId();
		case RTF_FLAG : return sw.rtfFlag();
		case COMMENT : return sw.comment();
		case FILE_ID : return sw.fileId();
		default : return null;
		}
	}


	private final int id;
	private String name = "";


	private AttachmentField(final int id, final String name){
		this.id=id;
		this.name=name;
	}

	public String getName(){
		return name;
	}

	public int getId(){
		return id;
	}

	@Override
	public String toString(){
		return name;
	}

	public static interface AttachmentSwitch {

		public Object createdBy();

		public Object fileId();

		public Object comment();

		public Object rtfFlag();

		public Object fileName();

		public Object folderId();

		public Object id();

		public Object moduleId();

		public Object attachedId();

		public Object fileSize();

		public Object fileMIMEType();

		public Object creationDate();

	}

}
