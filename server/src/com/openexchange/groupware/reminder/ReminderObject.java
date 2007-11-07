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



package com.openexchange.groupware.reminder;

import com.openexchange.groupware.container.SystemObject;
import java.util.Date;

/**
 * ReminderObject
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */

public class ReminderObject extends SystemObject {
	
	private Date lastModified = null;
	
	private int userId = 0;
	
	private Date date = null;
	
	private int objectId = 0;
    
	private String targetId = null;
	
	private int module = 0;
	
	private String description = null;
	
	private String folder = null;
	
	private boolean isRecurrenceAppointment = false;
    
    private int recurrencePosition = 0;
	
	public ReminderObject() {
		
	}
	
	public void setUser( final int userId )
	{
		this.userId = userId;
	}
	
	public int getUser( )
	{
		return userId;
	}
	
	public void setRecurrenceAppointment( final boolean isRecurrenceAppointment) {
		this.isRecurrenceAppointment = isRecurrenceAppointment;
	}
	
	public boolean isRecurrenceAppointment() {
		return isRecurrenceAppointment;
	}
	
	public void setDate( final Date date )
	{
		this.date = date;
	}
	
	public Date getDate( )
	{
		return date;
	}
	
	public void setTargetId( final String targetId )
	{
		this.targetId = targetId;
	}
	
	public void setTargetId( final int targetId )
	{
		this.targetId = String.valueOf(targetId);
	}
	
	public String getTargetId( )
	{
		return targetId;
	}
	
	public void setObjectId( final int objectId )
	{
		this.objectId = objectId;
	}

	public int getObjectId( )
	{
		return objectId;
	}
	
	public void setModule( final int module )
	{
		this.module = module;
	}
	
	public int getModule( )
	{
		return module;
	}
	
	public void setDescription( final String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	public void setFolder( final String folder) {
		this.folder = folder;
	}

	public String getFolder() {
		return folder;
	}
	
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	public Date getLastModified() {
		return lastModified;
	}
    
    public void setRecurrencePosition(final int recurrencePosition) {
        this.recurrencePosition = recurrencePosition;
    }
    
    public int getRecurrencePosition() {
        return recurrencePosition;
    }
}
