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


package com.openexchange.groupware.container;

/**
 * CommonObject
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public abstract class CommonObject extends FolderChildObject {
	
	public static final int LABEL_NONE = 0;
	
	public static final int LABEL_1 = 1;
	
	public static final int LABEL_2 = 2;
	
	public static final int LABEL_3 = 3;
	
	public static final int LABEL_4 = 4;
	
	public static final int LABEL_5 = 5;
	
	public static final int LABEL_6 = 6;
	
	public static final int LABEL_7 = 7;
	
	public static final int LABEL_8 = 8;
	
	public static final int LABEL_9 = 9;
	
	public static final int LABEL_10 = 10;
	
	public static final int CATEGORIES = 100;

	public static final int PRIVATE_FLAG = 101;
	
	public static final int COLOR_LABEL = 102;
	
	public static final int NUMBER_OF_LINKS = 103;
	
	public static final int NUMBER_OF_ATTACHMENTS = 104;

	protected int personal_folder_id = 0;
	protected int number_of_attachments = 0;
	protected int number_of_links = 0;
	protected String categories = null;
	protected boolean privateFlag = false;
	protected int label = 0;

	protected boolean b_personal_folder_id = false;
	protected boolean b_number_of_attachments = false;
	protected boolean b_number_of_links = false;
	protected boolean b_categories = false;
	protected boolean b_private_flag = false;
	protected boolean bLabel = false;
	
	// GET METHODS
	public String getCategories( ) {
		return categories;
	}
	
	public int getPersonalFolderID( ) {
		return personal_folder_id;
	}
	
	public int getNumberOfAttachments( ) {
		return number_of_attachments;
	}
	
	public int getNumberOfLinks( ) {
		return number_of_links;
	}
	
	public boolean getPrivateFlag() {
		return privateFlag;
	}
	
	public int getLabel( )
	{
		return label;
	}
	
	// SET METHODS
	public void setCategories( final String categories ) {
		this.categories = categories;
		b_categories = true;
	}
	
	public void setPersonalFolderID( final int personal_folder_id ) {
		this.personal_folder_id = personal_folder_id;
		b_personal_folder_id = true;
	}
	
	public void setNumberOfAttachments( final int number_of_attachments ) {
		this.number_of_attachments = number_of_attachments;
		b_number_of_attachments = true;
	}
	
	public void setNumberOfLinks( final int number_of_links ) {
		this.number_of_links = number_of_links;
		b_number_of_links = true;
	}
	
	public void setPrivateFlag( final boolean privateFlag) {
		this.privateFlag = privateFlag;
		b_private_flag = true;
	}  
	
	public void setLabel( final int label) {
		this.label = label;
		bLabel = true;
	}

	// REMOVE METHODS
	public void removeCategories( ) {
		categories = null;
		b_categories = false;
	}

	public void removePersonalFolderID( ) {
		personal_folder_id = 0;
		b_personal_folder_id = false;
	}
	
	public void removeNumberOfAttachments( ) {
		number_of_attachments = 0;
		b_number_of_attachments = false;
	}
	
	public void removeNumberOfLinks( ) {
		number_of_links = 0;
		b_number_of_links = false;
	}
	
	public void removePrivateFlag() {
		privateFlag = false;
		b_private_flag = false;
	}
	
	public void removeLabel() {
		label = 0;
		bLabel = false;
	}
	
	// CONTAINS METHODS
	public boolean containsCategories( ) {
		return b_categories;
	}
	
	public boolean containsPersonalFolderID( ) {
		return b_personal_folder_id;
	}
	
	public boolean containsNumberOfAttachments( ) {
		return b_number_of_attachments;
	}
	
	public boolean containsNumberOfLinks( ) {
		return b_number_of_links;
	}
	
	public boolean containsPrivateFlag() {
		return b_private_flag;
	}
	
	public boolean containsLabel()
	{
		return bLabel;
	}
	
	@Override
	public void reset() {
		super.reset();
		
		personal_folder_id = 0;
		number_of_attachments = 0;
		number_of_links = 0;
		categories = null;
		label = 0;
		
		b_personal_folder_id = false;
		b_number_of_attachments = false;
		b_number_of_links = false;
		b_categories = false;
		bLabel = false;
	}
}
