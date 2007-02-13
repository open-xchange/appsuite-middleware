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

package com.openexchange.groupware.infostore.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.SearchEngine;
import com.openexchange.groupware.infostore.database.impl.GetSwitch;

public class MetadataSorter {

	private String field = Metadata.TITLE_LITERAL.getName();
	private int direction = SearchEngine.ASC;
	
	private DocumentMetadata[] metadata;
	private List metadataList;
	
	private static final class MetadataComparator implements Comparator{

		private String field;
		private int direction;
		private Metadata metadataField;
		
		public MetadataComparator(String field, int direction){
			this.field = field;
			metadataField = Metadata.get(field);
			this.direction = direction;
			
		}
		
		public int compare(Object arg0, Object arg1) {
			Comparable v1 = getValue(arg0);
			Comparable v2 = getValue(arg1);
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
			case SearchEngine.DESC : return v2.compareTo(v1); 
			default : return v1.compareTo(v2);
			}
		}

		private Comparable getValue(Object arg0) {
			DocumentMetadata metadata = (DocumentMetadata)arg0;
			GetSwitch get = new GetSwitch(metadata);
			if(metadataField != null)
				return (Comparable) metadataField.doSwitch(get);
			return metadata.getProperty(field);
		}
		
	}
	
	public MetadataSorter(DocumentMetadata[] metadata){
		this.metadata = metadata;
	}
	
	public MetadataSorter(DocumentMetadata[] metadata, String field){
		this(metadata);
		this.field = field;
	}
	
	public MetadataSorter(DocumentMetadata[] metadata,String field, int direction){
		this(metadata, field);
		this.direction = direction;
	}
	
	public MetadataSorter(List metadata){
		this.metadataList = metadata;
	}
	
	public MetadataSorter(List metadata, String field){
		this(metadata);
		this.field = field;
	}
	
	public MetadataSorter(List metadata,String field, int direction){
		this(metadata, field);
		this.direction = direction;
	}
	
	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}
	
	public DocumentMetadata[] getSorted(){
		if(null == metadata && metadataList != null){
			List sortedMetadata = getSortedAsList();
			return (DocumentMetadata[]) sortedMetadata.toArray(new DocumentMetadata[sortedMetadata.size()]);
		}
		if(metadata != null) 
			Arrays.sort(metadata,new MetadataComparator(field,direction));
		return metadata;
	}
	
	public List getSortedAsList(){
		if(null == metadataList && null != metadata){
			return Arrays.asList(getSorted());
		}
		if(null != metadataList) {
			List copy = new ArrayList(metadataList);
			Collections.sort(copy,new MetadataComparator(field,direction));
			return copy;
		}
		return Collections.EMPTY_LIST;
	}
}
