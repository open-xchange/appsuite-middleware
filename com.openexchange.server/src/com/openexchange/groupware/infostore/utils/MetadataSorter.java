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
			if (v2 == null && v1 == null) {
				return 0;
			}
			if (v2 == null) {
				return 1;
			}
			if (v1 == null) {
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
			if (metadataField != null) {
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
		if (null == metadata && metadataList != null){
			final List<DocumentMetadata> sortedMetadata = getSortedAsList();
			return sortedMetadata.toArray(new DocumentMetadata[sortedMetadata.size()]);
		}
		if (metadata != null) {
			Arrays.sort(metadata, new MetadataComparator(field, direction));
		}
		return metadata;
	}

	public List<DocumentMetadata> getSortedAsList(){
		if (null == metadataList && null != metadata){
			return Arrays.asList(getSorted());
		}
		if (null != metadataList) {
			final List<DocumentMetadata> copy = new ArrayList<DocumentMetadata>(metadataList);
			Collections.sort(copy,new MetadataComparator(field,direction));
			return copy;
		}
		return Collections.emptyList();
	}
}
