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

package com.openexchange.api2;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;

/**
 * AppointmentSQLInterface
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public interface AppointmentSQLInterface {
	
	/**
	 * Lists all appointment that match the given search
	 * @param folderId
	 * The folder ID
	 * @param cols
	 * fields that will be added to the data object
	 * @param start
	 * The given start date 
	 * @param end
	 * The given end date 
	 * @return A SearchIterator contains AppointmentObjects
	 * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
	 */	
	public SearchIterator getAppointmentsBetweenInFolder(int folderId, int cols[], Date start, Date end, int orderBy, String orderDir) throws OXException, SQLException;
	
	/**
	 * Lists all appointment that match the given search
	 * @param folderId
	 * The folder ID
	 * @param cols
	 * fields that will be added to the data object
	 * @param start
	 * The given start date 
	 * @param end
	 * The given end date 
	 * @param from
	 * from
	 * @param to
	 * to
	 * @return A SearchIterator contains AppointmentObjects
	 * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
	 */
	public SearchIterator getAppointmentsBetweenInFolder(int folderId, int cols[], Date start, Date end, int from, int to, int orderBy, String orderDir) throws OXException, SQLException;
	
	/**
	 * returns the days where the user has appointments
	 * @param start
	 * The start Date
	 * @param end
	 * The end Date
	 * @return a boolean[] that contains true if the user has an appointment on this day or false if not
	 */
	public boolean[] hasAppointmentsBetween(Date start, Date end) throws OXException;
		
	/**
	 * Lists all modified objects in a folder
	 * @param folderID
	 * The Folder ID
	 * @param cols
	 * fields that will be added to the data object
	 * @param since
	 * all modification >= since
	 * @return A SearchIterator contains AppointmentObject
	 * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
	 */	 
	public SearchIterator getModifiedAppointmentsInFolder(int fid, int[] cols, Date since) throws OXException, SQLException;
	
	/**
	 * Lists all modified objects where the user is participant 
	 * @param userId
	 * The user ID
	 * @param start
	 * The start date
	 * @param end
	 * The end date
	 * @param cols
	 * fields that will be added to the data object
	 * @param since
	 * all modification >= since
	 * @return A SearchIterator contains AppointmentObject
	 * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
	 */	 
	public SearchIterator getModifiedAppointmentsBetween(int userId, Date start, Date end, int[] cols, Date since, int orderBy, String orderDir) throws OXException, SQLException;
	
	/**
	 * Lists all modified objects in a folder
	 * @param folderID
	 * The Folder ID
	 * @param start
	 * The start date
	 * @param end
	 * The end date
	 * @param cols
	 * fields that will be added to the data object
	 * @param since
	 * all modification >= since
	 * @return A SearchIterator contains AppointmentObject
	 * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
	 */	 
	public SearchIterator getModifiedAppointmentsInFolder(int fid, Date start, Date end, int[] cols, Date since) throws OXException, SQLException;
	
	/**
	 * Lists all deleted objects in a folder
	 * @param folderID
	 * The Folder ID
	 * @param cols
	 * fields that will be added to the data object
	 * @param since
	 * all modification >= since
	 * @return A SearchIterator contains AppointmentObject
	 * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
	 */	
	public SearchIterator getDeletedAppointmentsInFolder(int folderId, int cols[], Date since) throws OXException, SQLException;
	
	/**
	 * Lists all appointment that match the given search
	 * @param searchObject
	 * The SearchObject
	 * @param cols
	 * fields that will be added to the data object
	 * @return A SearchIterator contains AppointmentObjects
	 * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
	 */	
	public SearchIterator getAppointmentsByExtendedSearch(AppointmentSearchObject searchObject, int orderBy, String orderDir, int cols[]) throws OXException, SQLException;
	
	/**
	 * Lists all apointments where the titlematch the given searchpattern
	 * @param searchpattern
	 * The searchpattern
	 * @param folderId
	 * folder id where to search	 
	 * @param cols
	 * fields that will be added to the data object
	 * @return A SearchIterator contains AppointmentObjects
	 * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
	 */	
	public SearchIterator searchAppointments(String searchpattern, int folderId, int orderBy, String orderDir, int[] cols) throws OXException;
	
	/**
	 * Loads one appointment by the given ID
	 * @param objectId
	 * The Object ID
	 * @param inFolder
	 * Object in folder
	 * @return 
	 * return the AppointmentObject
	 * @throws OXException, OXPermissionException
	 */
	public CalendarDataObject getObjectById(int objectId, int inFolder) throws OXException, SQLException, OXObjectNotFoundException;
	
	/**
	 * Loads a range of appointments by the given IDs
	 * @param objectIdAndInFolder[]
	 * array with two dimensions. First dimension contains a seond array with two values.
	 * 1. value is object_id
	 * 2. value if folder_id
	 * @param cols
	 * The columns filled to the dataobject
	 * @return A SearchIterator contains AppointmentObjects
	 * @throws OXException
	 */	
	public SearchIterator getObjectsById(int[][] objectIdAndInFolder, int cols[]) throws OXException;
	
	/**
	 * insert the AppointmentObject
	 * By the insert the folderId is a mandatory field.
	 * @param AppointmentObject
	 * @throws OXException, OXPermissionException, OXFolderNotFoundException, OXConflictException, OXMandatoryFieldException, OXObjectNotFoundException
	 */
	public CalendarDataObject[] insertAppointmentObject(CalendarDataObject cdao) throws OXException;

	/**
	 * update the AppointmentObject
	 * @param AppointmentObject
	 * @throws OXException, OXPermissionException, OXFolderNotFoundException, OXConflictException, OXMandatoryFieldException, OXObjectNotFoundException
	 */
	public CalendarDataObject[] updateAppointmentObject(CalendarDataObject cdao, int inFolder, Date clientLastModified) throws OXException;
	
	/**
	 * deletes the AppointmentObject
	 * The objectId is a mandatory field in the AppointmentObject
	 * @param appointmentObject
	 * @throws OXException, OXPermissionException, OXFolderNotFoundException, OXConflictException, OXMandatoryFieldException, OXObjectNotFoundException
	 */
	public void deleteAppointmentObject(CalendarDataObject appointmentObject, int inFolder, Date clientLastModified) throws OXException, SQLException;

	/**
	 * deletes all Appointments in given folder
	 * @param folderid
	 * @throws OXException, OXPermissionException, OXFolderNotFoundException, OXConflictException, OXMandatoryFieldException, OXObjectNotFoundException
	 */
	public void deleteAppointmentsInFolder(int inFolder) throws OXException, SQLException;

	/**
	 * deletes all Appointments in given folder using specified connection.
	 * <p>
	 * connection is left untouched; meaning no commit/rollback actions take place
	 * @param folderid
	 * @param writeCon
	 * @throws OXException, OXPermissionException, OXFolderNotFoundException, OXConflictException, OXMandatoryFieldException, OXObjectNotFoundException
	 */
	public void deleteAppointmentsInFolder(int inFolder, Connection writeCon) throws OXException, SQLException;
        
	/**
	 * checks if the given folder contains any foreign objects
         * @param user_id
	 * @param folderid
	 * @throws OXException, SQLException
	 */
	public boolean checkIfFolderContainsForeignObjects(int user_id, int inFolder) throws OXException, SQLException;
	
	/**
	 * checks if the given folder contains any foreign objects using specified connection
     * @param user_id
	 * @param folderid
	 * @param readCon
	 * @throws OXException, SQLException
	 */
	public boolean checkIfFolderContainsForeignObjects(int user_id, int inFolder, Connection readCon) throws OXException, SQLException;
        
        
	/**
	 * returns true if the given folder is empty
     * @param user_id
	 * @param folderid
	 * @throws OXException, SQLException
	 */        
     public boolean isFolderEmpty(int uid, int fid) throws OXException, SQLException;
         
    /**
 	 * returns true if the given folder is empty using specified connection
     * @param user_id
 	 * @param folderid
 	 * @param readCon
 	 * @throws OXException, SQLException
 	 */        
     public boolean isFolderEmpty(int uid, int fid, Connection readCon) throws OXException, SQLException;
        
	/**
	 * set the confirmation of the user
	 * @param objectId
	 * The object ID
	 * @param userId
	 * The user ID
	 * @param confirm
	 * The confirm status
	 * @param confirmMessage
	 * The confirm message
	 * @throws OXException, OXPermissionException, OXFolderNotFoundException, OXConflictException, OXMandatoryFieldException, OXObjectNotFoundException
	 */
	public void setUserConfirmation(int object_id, int user_id, int confirm, String confirm_message) throws OXException;
	
        
	/**
	 * Method to attach or detach attachments
	 * @param objectId
	 * The object ID
	 * @param userId
	 * The user ID
	 * @param Context
	 * The context
	 * @param action
	 * true = attach, false = detach
	 * @throws OXException
	 */        
        public long attachmentAction(int objectId, int uid, Context c, boolean action) throws OXException;
        
        
	/**
	 * Lists of FreeBusy Information
	 * @param id
	 * The id
	 * @param tyoe
	 * The type of the id
	 * @param start
	 * The given start date 
	 * @param end
	 * The given end date 
	 * @return A SearchIterator contains AppointmentObjects
	 * @throws SearchIteratorException, OXException
	 */	
	public SearchIterator getFreeBusyInformation(int id, int type, Date start, Date end) throws OXException;

	/**
	 * Lists of all appointments where the user will participate between start and end
	 * @param user_id
	 * The user_id
	 * @param start
	 * The given start date 
	 * @param end
	 * The given end date 
	 * @return A SearchIterator contains AppointmentObjects
	 * @throws SearchIteratorException, OXException
	 */	        
        public SearchIterator getActiveAppointments(int user_uid, Date start, Date end, int cols[]) throws OXException;
        
	/**
	 * Lists of all appointments in all folders where the user will participate between start and end
	 * @param user_id
	 * The user_id
	 * @param start
	 * The given start date 
	 * @param end
	 * The given end date 
	 * @return A SearchIterator contains AppointmentObjects
	 * @throws SearchIteratorException, OXException
	 */	        
        public SearchIterator getAppointmentsBetween(int user_uid, Date start, Date end, int cols[], int orderBy, String orderDir) throws OXException, SQLException;
        
}

