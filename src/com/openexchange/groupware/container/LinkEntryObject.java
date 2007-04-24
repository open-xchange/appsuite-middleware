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
 * DistributionListObject
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class LinkEntryObject {
	
	private int contact_id;
	private String contact_displayname;
	
	private int link_id;
	private String link_displayname;
	
	private boolean b_contact_id;
	private boolean b_contact_displayname;
	
	private boolean b_link_id;
	private boolean b_link_displayname;
	
	public LinkEntryObject()
	{
		
	}
		
	// GET METHODS
	public int getContactID( ) {
		return contact_id;
	}
	
	public String getContactDisplayname( ) {
		return contact_displayname;
	}

	public int getLinkID( ) {
		return link_id;
	}
	
	public String getLinkDisplayname( ) {
		return link_displayname;
	}
		
	// SET METHODS
	public void setContactID( final int contact_id ) {
		this.contact_id = contact_id;
		b_contact_id = true;
	}
	
	public void setContactDisplayname( final String contact_displayname ) {
		this.contact_displayname = contact_displayname;
		b_contact_displayname = true;
	}

	public void setLinkID( final int link_id ) {
		this.link_id = link_id;
		b_link_id = true;
	}
	
	public void setLinkDisplayname( final String link_displayname ) {
		this.link_displayname = link_displayname;
		b_link_displayname = true;
	}

	
	// REMOVE METHODS
	public void removeContactID( ) {
		this.contact_id = 0;
		b_contact_id = false;
	}
	
	public void removeContactDisplayname( ) {
		this.contact_displayname = null;
		b_contact_displayname = false;
	}

	public void removeLinkID( ) {
		this.link_id = 0;
		b_link_id = false;
	}
	
	public void removeLinkDisplayname( ) {
		this.link_displayname = null;
		b_link_displayname = false;
	}
	
	// CONTAINS METHODS
	public boolean containsContactID( ) {
		return b_contact_id;
	}
	
	public boolean containsContactDisplayname( ) {
		return b_contact_displayname;
	}

	public boolean containsLinkID( ) {
		return b_link_id;
	}
	
	public boolean containsLinkDisplayname( ) {
		return b_link_displayname;
	}
	
	public boolean compare( final LinkEntryObject leo){
		if (getContactID() == leo.getContactID()){
			if (getLinkID() == leo.getLinkID()){
				return true;
			}
			return false;
		}
		return false;
	}
	
	public void reset() {
		contact_id = 0;
		contact_displayname = null;

		link_id = 0;
		link_displayname = null;
		
		b_contact_id = false;
		b_contact_displayname = false;

		b_link_id = false;
		b_link_displayname = false;
	}
}
