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

package com.openexchange.groupware.infostore.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;

public class MetadataSorter {

	private String field = Metadata.TITLE_LITERAL.getName();
	private int direction = InfostoreSearchEngine.ASC;

	private DocumentMetadata[] metadata;
	private List<DocumentMetadata> metadataList;

	private static final class MetadataComparator implements Comparator<DocumentMetadata>{

		private final String field;
		private final int direction;
		private final Metadata metadataField;

		public MetadataComparator(final String field, final int direction){
			this.field = field;
			metadataField = Metadata.get(field);
			this.direction = direction;

		}

		@Override
        public int compare(final DocumentMetadata arg0, final DocumentMetadata arg1) {
			final Comparable<Object> v1 = getValue(arg0);
			final Comparable<Object> v2 = getValue(arg1);
			if(v2 == null && v1 == null) {
				return 0;
			}
			if(v2 == null) {
				return 1;
			}
			if(v1 == null) {
				return -1;
			}
			switch(direction){
			case InfostoreSearchEngine.DESC : return v2.compareTo(v1);
			default : return v1.compareTo(v2);
			}
		}

		private Comparable getValue(final DocumentMetadata arg0) {
			final DocumentMetadata metadata = arg0;
			final GetSwitch get = new GetSwitch(metadata);
			if(metadataField != null) {
				return (Comparable) metadataField.doSwitch(get);
			}
			return metadata.getProperty(field);
		}

	}

	public MetadataSorter(final DocumentMetadata[] metadata){
		this.metadata = metadata;
	}

	public MetadataSorter(final DocumentMetadata[] metadata, final String field){
		this(metadata);
		this.field = field;
	}

	public MetadataSorter(final DocumentMetadata[] metadata,final String field, final int direction){
		this(metadata, field);
		this.direction = direction;
	}

	public MetadataSorter(final List<DocumentMetadata> metadata){
		this.metadataList = metadata;
	}

	public MetadataSorter(final List<DocumentMetadata> metadata, final String field){
		this(metadata);
		this.field = field;
	}

	public MetadataSorter(final List<DocumentMetadata> metadata,final String field, final int direction){
		this(metadata, field);
		this.direction = direction;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(final int direction) {
		this.direction = direction;
	}

	public String getField() {
		return field;
	}

	public void setField(final String field) {
		this.field = field;
	}

	public DocumentMetadata[] getSorted(){
		if(null == metadata && metadataList != null){
			final List<DocumentMetadata> sortedMetadata = getSortedAsList();
			return sortedMetadata.toArray(new DocumentMetadata[sortedMetadata.size()]);
		}
		if (metadata != null) {
			Arrays.sort(metadata, new MetadataComparator(field, direction));
		}
		return metadata;
	}

	public List<DocumentMetadata> getSortedAsList(){
		if(null == metadataList && null != metadata){
			return Arrays.asList(getSorted());
		}
		if(null != metadataList) {
			final List<DocumentMetadata> copy = new ArrayList<DocumentMetadata>(metadataList);
			Collections.sort(copy,new MetadataComparator(field,direction));
			return copy;
		}
		return Collections.emptyList();
	}
}
