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

package com.openexchange.api2;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * AppointmentSQLInterface
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public interface AppointmentSQLInterface {

    /**
     * @param include TRUE if you want to include other people's private appointments when returning data, FALSE otherwise (default case)
     * @return
     */
    void setIncludePrivateAppointments(boolean include);

    /**
     * @return TRUE if relevant methods will include other people's private appointments when returning data, FALSE otherwise (default case)
     */
    boolean getIncludePrivateAppointments();

    /**
     * Lists all appointment that match the given search.
     *
     * @param folderId The folder ID
     * @param cols fields that will be added to the data object
     * @param start The given start date
     * @param end The given end date
     * @return A SearchIterator contains AppointmentObjects
     * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
     */
    SearchIterator<Appointment> getAppointmentsBetweenInFolder(int folderId, int cols[], Date start, Date end, int orderBy, Order order) throws OXException, SQLException;

    /**
     * Lists all appointment that match the given search.
     *
     * @param folderId The folder ID
     * @param cols fields that will be added to the data object
     * @param start The given start date
     * @param end The given end date
     * @param from from
     * @param to to
     * @return A SearchIterator contains AppointmentObjects
     * @throws OXException , OXPermissionException, OXFolderObjectNotFoundException
     */
    SearchIterator<Appointment> getAppointmentsBetweenInFolder(int folderId, int cols[], Date start, Date end, int from, int to, int orderBy, Order orderDir) throws OXException, SQLException;

    /**
     * returns the days where the user has appointments
     * @param start
     * The start Date
     * @param end
     * The end Date
     * @return a boolean[] that contains true if the user has an appointment on this day or false if not
     */
    public boolean[] hasAppointmentsBetween(Date start, Date end) throws OXException;

    public List<Appointment> getAppointmentsWithExternalParticipantBetween(String email, int[] cols, Date start, Date end, int orderBy, Order order) throws OXException;

    public List<Appointment> getAppointmentsWithUserBetween(User user, int[] cols, Date start, Date end, int orderBy, Order order) throws OXException;

    /**
     * Lists all modified objects in a folder.
     *
     * @param folderID The Folder ID
     * @param cols fields that will be added to the data object
     * @param since all modification >= since
     * @return A SearchIterator contains AppointmentObject
     * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
     */
    SearchIterator<Appointment> getModifiedAppointmentsInFolder(int fid, int[] cols, Date since) throws OXException;

    /**
     * Lists all modified objects where the user is participant.
     *
     * @param userId The user ID
     * @param start The start date
     * @param end The end date
     * @param cols fields that will be added to the data object
     * @param since all modification >= since
     * @return A SearchIterator contains AppointmentObject
     * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
     */
    SearchIterator<Appointment> getModifiedAppointmentsBetween(int userId, Date start, Date end, int[] cols, Date since, int orderBy, Order orderDir) throws OXException, SQLException;

    /**
     * Lists all modified objects in a folder.
     *
     * @param folderID The Folder ID
     * @param start The start date
     * @param end The end date
     * @param cols fields that will be added to the data object
     * @param since all modification >= since
     * @param includePrivateFlag <code>true</code> to include private-flag information, meaning to exclude private appointments when
     *            querying s shared folder; otherwise <code>false</code>
     * @return A SearchIterator contains AppointmentObject
     * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
     */
    SearchIterator<Appointment> getModifiedAppointmentsInFolder(int fid, Date start, Date end, int[] cols, Date since) throws OXException, SQLException;

    /**
     * Lists all deleted objects in a folder.
     *
     * @param folderID The Folder ID
     * @param cols fields that will be added to the data object
     * @param since all modification >= since
     * @return A SearchIterator contains AppointmentObject
     * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
     */
    SearchIterator<Appointment> getDeletedAppointmentsInFolder(int folderId, int cols[], Date since) throws OXException, SQLException;

    /**
     * Gets the sequence number for the contents of a specific folder, which is evaluated by determining the biggest changing date of
     * all contained appointments, considering both the "working" as well as the "backup" tables.
     *
     * @param folderId The identifier of the folder to get the sequence number for
     * @return The sequence number, or <code>0</code> if there is none
     */
    long getSequenceNumber(int folderId) throws OXException;

    /**
     * Lists all appointment that match the given search
     * @param searchObject
     * The SearchObject
     * @param cols
     * fields that will be added to the data object
     * @return A SearchIterator contains AppointmentObjects
     * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
     */
    //    public SearchIterator<Appointment> getAppointmentsByExtendedSearch(AppointmentSearchObject searchObject, int orderBy, Order orderDir, int cols[]) throws OXException, SQLException;

    /**
     * Lists all appointments where the title or description matches the given pattern in the {@link AppointmentSearchObject}.
     *
     * @param searchObj The {@link AppointmentSearchObject}.
     * @param cols Fields that will be added to the data object.
     * @param orderBy The field for ordering the results, or <code>-1</code> for no order
     * @param orderDir The direction for ordering the results, or {@link Order#NO_ORDER} for no specific direction
     * @return A SearchIterator containing AppointmentObjects
     */
    SearchIterator<Appointment> searchAppointments(AppointmentSearchObject searchObj, int orderBy, Order orderDir, int[] cols) throws OXException;

    /**
     * Lists all appointments where the title or description matches the given pattern in the {@link AppointmentSearchObject}.
     *
     * @param searchObj The {@link AppointmentSearchObject}.
     * @param cols Fields that will be added to the data object.
     * @param orderBy The field for ordering the results, or <code>-1</code> for no order
     * @param limit The maximum number of results to return, or <code>-1</code> for no limit
     * @param orderDir The direction for ordering the results, or {@link Order#NO_ORDER} for no specific direction
     * @return A SearchIterator containing AppointmentObjects
     */
    SearchIterator<Appointment> searchAppointments(AppointmentSearchObject searchObj, int orderBy, Order orderDir, int limit, int[] cols) throws OXException;

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
    public CalendarDataObject getObjectById(int objectId) throws OXException, SQLException;

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
    public CalendarDataObject getObjectById(int objectId, int inFolder) throws OXException, SQLException;

    /**
     * Loads one appointment by the given ID
     * <p>
     * Note that this method is less isolated as one passes a {@link Connection},
     * but it is required in some cases where this needs to be called as part of
     * a batch of operations, in order to use the same {@link Connection} and
     * avoid potential deadlocks.
     * <p>
     * Futhermore, this method does <em>not</em> perform any transactional control
     * and does not commit nor rollback the specified {@link Connection}, which is
     * left to the caller.
     * 
     * @param objectId
     * The Object ID
     * @param inFolder
     * Object in folder
     * @param readCon
     * the {@link Connection} to use to retrieve the {@link CalendarDataObject}
     * @return
     * return the AppointmentObject
     * @throws OXException, OXPermissionException
     */
    public CalendarDataObject getObjectById(int objectId, int inFolder, Connection readConnection) throws OXException, SQLException;
    
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
    public SearchIterator<Appointment> getObjectsById(int[][] objectIdAndInFolder, int cols[]) throws OXException;

    /**
     * insert the AppointmentObject
     * By the insert the folderId is a mandatory field.
     * @param Appointment
     * @throws OXException, OXPermissionException, OXFolderNotFoundException, OXConflictException, OXMandatoryFieldException, OXObjectNotFoundException
     */
    public Appointment[] insertAppointmentObject(CalendarDataObject cdao) throws OXException;

    /**
     * update the AppointmentObject
     * @param checkPermissions
     * @param Appointment
     * @throws OXException, OXPermissionException, OXFolderNotFoundException, OXConflictException, OXMandatoryFieldException, OXObjectNotFoundException
     */
    public Appointment[] updateAppointmentObject(CalendarDataObject cdao, int inFolder, Date clientLastModified) throws OXException;

    /**
     * update the AppointmentObject
     * @param checkPermissions
     * @param Appointment
     * @throws OXException, OXPermissionException, OXFolderNotFoundException, OXConflictException, OXMandatoryFieldException, OXObjectNotFoundException
     */
    public Appointment[] updateAppointmentObject(CalendarDataObject cdao, int inFolder, Date clientLastModified, boolean checkPermissions) throws OXException;

    /**
     * deletes the AppointmentObject
     * The objectId is a mandatory field in the AppointmentObject
     * @param appointmentObject
     * @throws OXException, OXPermissionException, OXFolderNotFoundException, OXConflictException, OXMandatoryFieldException, OXObjectNotFoundException
     */
    public void deleteAppointmentObject(CalendarDataObject appointmentObject, int inFolder, Date clientLastModified) throws OXException, SQLException;

    public void deleteAppointmentObject(CalendarDataObject appointmentObject, int inFolder, Date clientLastModified, boolean checkPermissions) throws OXException, SQLException;
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
    public boolean deleteAppointmentsInFolder(int inFolder, Connection writeCon) throws OXException, SQLException;

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

    // ---------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Sets the confirmation of an appointment for a user.
     *
     * @param objectId unique identifier of the appointment.
     * @param folderId folder of the appointment
     * @param userId unique identifier of the user.
     * @param confirm The confirm status
     * @param confirmMessage The confirm message
     * @return The last-modified time stamp of associated appointment
     * @throws OXException If setting the confirmation fails
     */
    Date setUserConfirmation(int objectId, int folderId, int userId, int confirm, String confirmMessage) throws OXException;

    /**
     * Sets the confirmation of an appointment for an external user, identified with his mail address.
     *
     * @param objectId unique identifier of the appointment.
     * @param folderId folder of the appointment
     * @param mail The E-Mail address of the associated external participant
     * @param confirm The confirm status
     * @param message The confirm message
     * @return The last-modified time stamp of associated appointment
     * @throws OXException If setting the confirmation fails
     */
    Date setExternalConfirmation(int objectId, int folderId, String mail, int confirm, String message) throws OXException;

    /**
     * Sets the confirmation of an appointment for a user.
     *
     * @param objectId unique identifier of the appointment.
     * @param folderId folder of the appointment
     * @param optOccurrenceId The numeric identifier of the occurrence to which the confirmation applies in case <code>objectId</code>
     *            denotes a series appointment; otherwise <code>0</code> (zero)
     * @param userId unique identifier of the user.
     * @param confirm The confirm status
     * @param confirmMessage The confirm message
     * @return A change exception object, if created, otherwise an almost empty object with just the timestamp.
     * @throws OXException If setting the confirmation fails
     */
    CalendarDataObject setUserConfirmation(int objectId, int folderId, int optOccurrenceId, int userId, int confirm, String confirmMessage) throws OXException;

    /**
     * Sets the confirmation of an appointment for an external user, identified with his mail address.
     *
     * @param objectId unique identifier of the appointment.
     * @param folderId folder of the appointment
     * @param optOccurrenceId The numeric identifier of the occurrence to which the confirmation applies in case <code>objectId</code>
     *            denotes a series appointment; otherwise <code>0</code> (zero)
     * @param mail The E-Mail address of the associated external participant
     * @param confirm The confirm status
     * @param message The confirm message
     * @return A change exception object, if created, otherwise an almost empty object with just the timestamp.
     * @throws OXException If setting the confirmation fails
     */
    CalendarDataObject setExternalConfirmation(int objectId, int folderId, int optOccurrenceId, String mail, int confirm, String message) throws OXException;

    /**
     * Method to attach or detach attachments
     * @param folderId
     * The folder Id
     * @param objectId
     * The object ID
     * @param userId
     * The user ID
     * @param session
     * The session
     * @param Context
     * The context
     * @param numberOfAttachments
     * Amount of attached attachments.
     * @throws OXException
     */
    long attachmentAction(int folderId, int objectId, int userId, Session session, Context c, int numberOfAttachments) throws OXException;


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
     * @throws OXException
     */
    public SearchIterator<Appointment> getFreeBusyInformation(int id, int type, Date start, Date end) throws OXException;

    /**
     * Lists of all appointments where the user will participate between start and end.
     *
     * @param user_id The user_id
     * @param start The given start date
     * @param end The given end date
     * @return A SearchIterator contains AppointmentObjects
     * @throws OXException
     */
    SearchIterator<Appointment> getActiveAppointments(int user_uid, Date start, Date end, int cols[]) throws OXException;

    /**
     * Lists of all appointments in all folders where the user will participate between start and end.
     *
     * @param user_id The user_id
     * @param start The given start date
     * @param end The given end date
     * @return A SearchIterator contains AppointmentObjects
     * @throws OXException
     * @throws SQLException
     */
    SearchIterator<Appointment> getAppointmentsBetween(int user_uid, Date start, Date end, int cols[], int orderBy, Order order) throws OXException, SQLException;

    /**
     * Lists all appointments in all folders for the whole context between start and end.
     *
     * @param start The given start date
     * @param end The given end date
     * @return A SearchIterator contains AppointmentObjects
     * @throws OXException
     * @throws SQLException
     */
    SearchIterator<Appointment> getAppointmentsBetween(Date start, Date end, int cols[], int orderBy, Order order) throws OXException, SQLException;

    /**
     * Resolves the given uid.
     *
     * @param uid
     * @return the object id of the corresponding object, if it exists, 0 otherwise.
     * @throws OXException
     */
    public int resolveUid(String uid) throws OXException;

    /**
     * Resolves the given filename.
     *
     * @param filename
     * @return the object id of the corresponding object, if it exists, 0 otherwise.
     * @throws OXException
     */
    int resolveFilename(String filename) throws OXException;

    /**
     * Returns the folder in which this appointment is located for the current user.
     *
     * @param objectId
     * @return
     * @throws OXException
     */
    public int getFolder(int objectId) throws OXException;

    /**
     * Counts the visible calendar objects in the given folder.
     *
     * @param folderId
     * @return
     * @throws OXException
     */
    public int countObjectsInFolder(int folderId) throws OXException;
}
