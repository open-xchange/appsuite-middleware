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
 * LinkObject
 * @author <a href="mailto:ben.pahne@comfire.de">Benjamin Frederic Pahne</a>
 */

public class LinkObject {
	
	private int first_id;
	private int second_id;
	
	private int first_type;
	private int second_type;
	
	private int first_folder;
	private int second_folder;
	
	private int cid;
	
	public LinkObject()
	{
		
	}
	
	public LinkObject(final int first_id, final int first_type, final int first_folder, final int second_id, final int second_type, final int second_folder, final int cid){
		this.first_id = first_id;
		this.first_type = first_type;
		this.first_folder = first_folder;
		this.second_id = second_id;
		this.second_type = second_type;
		this.second_folder = second_folder;
		this.cid = cid;
	}
	
	public void setLink(final int first_id, final int first_type, final int first_folder, final int second_id, final int second_type, final int second_folder, final int cid){
		this.first_id = first_id;
		this.first_type = first_type;
		this.first_folder = first_folder;
		this.second_id = second_id;
		this.second_type = second_type;
		this.second_folder = second_folder;
		this.cid = cid;
	}
	
	public void setFirstId(final int id){
		this.first_id = id;
	}
	public void setFirstType(final int id){
		this.first_type = id;
	}
	public void setFirstFolder(final int id){
		this.first_folder = id;
	}
	public void setSecondId(final int id){
		this.second_id = id;
	}
	public void setSecondType(final int id){
		this.second_type = id;
	}
	public void setSecondFolder(final int id){
		this.second_folder = id;
	}	
	public void setContext(final int id){
		this.cid = id;
	}
	
	public int getFirstId(){
		return first_id;
	}
	
	public int getSecondId(){
		return second_id;
	}
	
	public int getFirstType(){
		return first_type;
	}
	
	public int getSecondType(){
		return second_type;
	}
	
	public int getFirstFolder(){
		return first_folder;
	}
	
	public int getSecondFolder(){
		return second_folder;
	}
	
	public int getContectId(){
		return cid;
	}
	
	public void reset() {
		first_id = 0;
		second_id = 0;
		
		first_type = 0;
		second_type = 0;
		
		first_folder = 0;
		second_folder = 0;
	}
}
