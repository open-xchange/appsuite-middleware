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

package com.openexchange.caldav;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link Patches}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Patches {

    private Patches() {
    	// prevent instantiation
    }

    /**
     * {@link Incoming}
     *
     * Patches for incoming iCal files.
     *
     * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
     */
    public static final class Incoming {

        private Incoming() {
        	// prevent instantiation
        }

        /**
         * Adds the user to the list of participants if needed, i.e. the
         * appointment not yet has any internal user participants.
         *
         * @param appointment
         */
        public static void addUserParticipantIfEmpty(int userID, Appointment appointment) {
            if (null == appointment.getParticipants() || 0 == appointment.getParticipants().length) {
                UserParticipant user = new UserParticipant(userID);
                user.setConfirm(Appointment.ACCEPT);
                user.setAlarmMinutes(appointment.containsAlarm() ? appointment.getAlarm() : -1);
                appointment.setParticipants(new UserParticipant[] { user });
            } else {
                boolean hasSomethingInternal = false;
                for (Participant participant : appointment.getParticipants()) {
                    if (Participant.GROUP == participant.getType() || Participant.RESOURCE == participant.getType() ||
                        Participant.USER == participant.getType() || Participant.RESOURCEGROUP == participant.getType()) {
                        hasSomethingInternal = true;
                        break;
                    }
                }
                if (false == hasSomethingInternal) {
                    Participant[] participants = Arrays.copyOf(appointment.getParticipants(), 1 + appointment.getParticipants().length);
                    UserParticipant user = new UserParticipant(userID);
                    user.setConfirm(Appointment.ACCEPT);
                    user.setAlarmMinutes(appointment.containsAlarm() ? appointment.getAlarm() : -1);
                    participants[participants.length - 1] = user;
                    appointment.setParticipants(participants);
                }
            }
        }

        /**
         * Tries to restore the participant- and user-arrays in the updated
         * appointment from the original information found in the original
         * appointment, preserving any updated participant states. This way,
         * group- and resource-information that has been excluded for CalDAV-
         * synchronization is restored implicitly.
         *
         * Note: As this only works when there are no changes to the individual
         * participants in the update at the moment, there might be other other
         * steps necessary when restoring fails.
         *
         * @param original the original appointment
         * @param update the updated appointment
         * @return <code>true</code>, if restoring participants was successful,
         * <code>false</code>, otherwise
         */
        public static boolean tryRestoreParticipants(Appointment original, Appointment update) {
        	/*
        	 * extract individual participants
        	 */
        	Set<Participant> originalIndividualParticipants = ParticipantTools.getIndividualParticipants(original);
        	Set<Participant> updateIndividualParticipants = ParticipantTools.getIndividualParticipants(update);
        	//if (originalIndividualParticipants.equals(updateIndividualParticipants)) {
        	if (ParticipantTools.equals(originalIndividualParticipants, updateIndividualParticipants, false)) {
        		/*
        		 * no changes in individual participants, restore participants from original
        		 */
        		Participant[] restoredParticipants = Arrays.copyOf(original.getParticipants(), original.getParticipants().length);
        		UserParticipant[] restoredUsers = Arrays.copyOf(original.getUsers(), original.getUsers().length);
        		for (Participant updatedParticipant : updateIndividualParticipants) {
        			/*
        			 * update matching participants
        			 */
        			for (int i = 0; i < restoredUsers.length; i++) {
        				// we have adequate equals overrides here
        				if (updatedParticipant.equals(restoredUsers[i])) {
        					restoredUsers[i] = (UserParticipant)updatedParticipant;
        				}
    				}
        			for (int i = 0; i < restoredParticipants.length; i++) {
        				if (ParticipantTools.equals(updatedParticipant, restoredParticipants[i])) {
        					restoredParticipants[i] = updatedParticipant;
        				}
    				}
    			}
        		/*
        		 * restore participants in updated appointment
        		 */
        		update.setParticipants(restoredParticipants);
        		update.setUsers(restoredUsers);
        		return true;
        	} else {
        		/*
        		 * changes detected, give up here for now //TODO
        		 */
        		return false;
        	}
        }

        /**
         * Adds all ResourceParticipants from the oldAppointment to the update,
         * effectively disallowing modification of resources
         *
         * @param original
         * @param update
         */
        public static void patchResources(Appointment original, Appointment update) {
            Set<Integer> guardian = new HashSet<Integer>();
            List<Participant> newParticipants = new ArrayList<Participant>();

            Participant[] participants = update.getParticipants();
            if (participants == null) {
                return;
            }
            for (Participant participant : participants) {
                if (ResourceParticipant.class.isInstance(participant)) {
                    guardian.add(new Integer(participant.getIdentifier()));
                }
                newParticipants.add(participant);
            }

            participants = original.getParticipants();
            if (null != participants) {
                for (Participant participant : participants) {
                    if (ResourceParticipant.class.isInstance(participant) && !guardian.contains(new Integer(participant.getIdentifier()))) {
                        newParticipants.add(participant);
                    }
                }
            }

            update.setParticipants(newParticipants);
        }

        /**
         * Removes duplicate users from the appointment's participant list,
         * based on the user identifiers.
         *
         * @param appointmnet
         */
        public static void patchParticipantListRemovingDoubleUsers(Appointment appointmnet) {
            Set<Integer> users = new HashSet<Integer>();
            Participant[] participants = appointmnet.getParticipants();
            List<Participant> uniqueParticipants = new ArrayList<Participant>();
            if (participants == null) {
                return;
            }
            for (Participant participant : participants) {
                if (UserParticipant.class.isInstance(participant)) {
                    UserParticipant up = (UserParticipant) participant;
                    if (users.add(new Integer(up.getIdentifier()))) {
                        uniqueParticipants.add(participant);
                    }
                } else {
                    uniqueParticipants.add(participant);
                }
            }
            appointmnet.setParticipants(uniqueParticipants);
        }

        /**
         * Removes duplicate users from the appointment's participant list,
         * based on the known user aliases.
         *
         * @param factory
         * @param update
         * @throws WebdavProtocolException
         */
        public static void patchParticipantListRemovingAliases(GroupwareCaldavFactory factory, Appointment update) throws OXException {
            // Firstly, let's build a Set of all aliases that are already taking part in this appointment
            Set<String> knownInternalMailAddresses = new HashSet<String>();
            Participant[] participants = update.getParticipants();
            if (participants == null) {
                return;
            }
            for (Participant participant : participants) {
                if (UserParticipant.class.isInstance(participant)) {
                    UserParticipant up = (UserParticipant) participant;
                    int userId = up.getIdentifier();
                    User user = factory.resolveUser(userId);
                    if (user.getAliases() != null) {
                        knownInternalMailAddresses.addAll(Arrays.asList(user.getAliases()));
                    }
                    knownInternalMailAddresses.add(user.getMail());
                }
            }
            List<Participant> prunedParticipants = new ArrayList<Participant>(participants.length);
            for (Participant participant : participants) {
                if (com.openexchange.groupware.container.ExternalUserParticipant.class.isInstance(participant)) {
                    ExternalUserParticipant external = (ExternalUserParticipant) participant;
                    String emailAddress = external.getEmailAddress();
                    if (!knownInternalMailAddresses.contains(emailAddress)) {
                        prunedParticipants.add(participant);
                    }
                } else {
                    prunedParticipants.add(participant);
                }
            }
        }

        /**
         * Tries to restore the original task status and percent-completed in case the updated task does not look like a 'done' / 'undone'
         * operation by the client (relevant for bugs #23058, #25240, 24812)
         *
         * @param originalTask
         * @param updatedTask
         */
        public static void adjustTaskStatus(Task originalTask, Task updatedTask) {
            if (false == originalTask.containsStatus()) {
                /*
                 * Nothing to restore
                 */
                return;
            } else if (Task.DONE == updatedTask.getStatus() && Task.DONE != originalTask.getStatus()) {
                /*
                 * 'Done' in Mac OS client: STATUS:COMPLETED / PERCENT-COMPLETE:100
                 */
                updatedTask.setPercentComplete(100);
                updatedTask.setStatus(Task.DONE);
            } else if (Task.NOT_STARTED == updatedTask.getStatus() && Task.DONE == originalTask.getStatus()) {
                /*
                 * 'Undone' in Mac OS client: STATUS:NEEDS-ACTION
                 */
                updatedTask.setPercentComplete(0);
            } else if (Task.NOT_STARTED == updatedTask.getStatus() && Task.NOT_STARTED != originalTask.getStatus()) {
                /*
                 * neither done/undone transition, restore from original task
                 */
                updatedTask.setPercentComplete(originalTask.getPercentComplete());
                updatedTask.setStatus(originalTask.getStatus());
            }
        }

        /**
         * Removes the start date of a task in case the updated task's end date is set to a time before the set start date.
         *
         * @param originalTask The original task
         * @param updatedTask The updated task
         */
        public static void adjustTaskStart(Task originalTask, Task updatedTask) {
            Date startDate = updatedTask.containsStartDate() ? updatedTask.getStartDate() : originalTask.getStartDate();
            if (null != startDate && updatedTask.containsEndDate() && updatedTask.getEndDate().before(startDate)) {
                /*
                 * remove currently set start date
                 */
                updatedTask.setStartDate(null);
            }
        }

        /**
         * Removes the appointment's participants other than the user with the given user ID in case the supplied folder is a 'public' one.
         *
         * @param userID The user ID
         * @param folder The folder where the appointment is going to be saved
         * @param appointment The appointment to patch
         */
        public static void removeParticipantsForPrivateAppointmentInPublicfolder(int userID, UserizedFolder folder, Appointment appointment) {
            if (appointment.getPrivateFlag() && PublicType.getInstance().equals(folder.getType())) {
                Participant[] participants = appointment.getParticipants();
                if (null != participants && 0 < participants.length) {
                    List<Participant> filteredParticipants = new ArrayList<Participant>(1);
                    for (Participant participant : participants) {
                        if (participant.getIdentifier() == userID) {
                            filteredParticipants.add(participant);
                            break;
                        }
                    }
                    appointment.setParticipants(filteredParticipants);
                }
            }
        }

    }

    /**
     * {@link Outgoing}
     *
     * Patches for outgoing iCal files.
     *
     * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
     */
    public static final class Outgoing {

        private static final Pattern EMPTY_RDATE = Pattern.compile("^RDATE:\\r\\n", Pattern.MULTILINE);

        private Outgoing() {
        	// prevent instantiation
        }

        /**
         * Removes empty RDATE components without information in iCal strings
         * (those are generated by the {@link VTimeZone} onsets)
         *
         * @param iCal
         * @return
         */
        public static String removeEmptyRDates(final String iCal) {
        	return EMPTY_RDATE.matcher(iCal).replaceAll("");
        }

        /**
         * Sets the correct start- and enddate in a recurring appointment.
         *
         * @param factory
         * @param appointment
         */
        public static void setSeriesStartAndEnd(GroupwareCaldavFactory factory, CalendarDataObject appointment) {
            if (CalendarObject.NO_RECURRENCE != appointment.getRecurrenceType()) {
                CalendarCollectionService calUtils = factory.getCalendarUtilities();
                calUtils.safelySetStartAndEndDateForRecurringAppointment(appointment);
            }
        }

        /**
         * Transforms change- to delete-exceptions where user is removed from participants if needed (bug #26293).
         *
         * @param factory
         * @param appointment
         * @param changeExceptions
         * @return
         */
        public static CalendarDataObject[] setDeleteExceptionForRemovedParticipant(GroupwareCaldavFactory factory, Appointment appointment, CalendarDataObject[] changeExceptions) {
            if (0 < appointment.getRecurrenceID() && null != changeExceptions && 0 < changeExceptions.length) {
                int userID = factory.getUser().getId();
                boolean isParticipantInMaster = false;
                if (null != appointment.getUsers() && 0 < appointment.getUsers().length) {
                    for (UserParticipant userParticipant : appointment.getUsers()) {
                        if (userID == userParticipant.getIdentifier()) {
                            isParticipantInMaster = true;
                            break;
                        }
                    }
                }
                if (isParticipantInMaster) {
                    List<CalendarDataObject> patchedChangeExceptions = new ArrayList<CalendarDataObject>();
                    for (CalendarDataObject changeException : changeExceptions) {
                        boolean isParticipantInException = false;
                        if (null != changeException.getUsers() && 0 < changeException.getUsers().length) {
                            for (UserParticipant userParticipant : changeException.getUsers()) {
                                if (userID == userParticipant.getIdentifier()) {
                                    isParticipantInException = true;
                                    break;
                                }
                            }
                        }
                        if (false == isParticipantInException && null != changeException.getChangeException()
                            && 1 == changeException.getChangeException().length) {
                            /*
                             * transfer to delete exceptions
                             */
                            Date recurrenceDatePosition = changeException.getChangeException()[0];
                            HashSet<Date> patchedChangeExceptionDates = new HashSet<Date>(Arrays.asList(appointment.getChangeException()));
                            patchedChangeExceptionDates.remove(recurrenceDatePosition);
                            appointment.setChangeExceptions(patchedChangeExceptionDates.toArray(new Date[patchedChangeExceptionDates.size()]));
                            appointment.addDeleteException(recurrenceDatePosition);
                        } else {
                            /*
                             * keep in change exceptions
                             */
                            patchedChangeExceptions.add(changeException);
                        }
                    }
                    changeExceptions = patchedChangeExceptions.toArray(new CalendarDataObject[patchedChangeExceptions.size()]);
                }
            }
            return changeExceptions;
        }

        /**
         * If not yet set, sets the organizer's participant status to "accepted".
         *
         * @param appointment
         */
        public static void setOrganizersParticipantStatus(Appointment appointment) {
            UserParticipant[] users = appointment.getUsers();
            if (null != users) {
                int createdBy = appointment.getCreatedBy();
                TIntObjectMap<UserParticipant> userMap = new TIntObjectHashMap<UserParticipant>();
                for (UserParticipant userParticipant : users) {
                    int identifier = userParticipant.getIdentifier();
                    if (createdBy == identifier && userParticipant.getConfirm() == CalendarObject.NONE) {
                        userParticipant.setConfirm(CalendarObject.ACCEPT);
                    }
                    userMap.put(identifier, userParticipant);
                }

                Participant[] participants = appointment.getParticipants();
                if (null != participants) {
                    for (Participant participant : participants) {
                        if (UserParticipant.class.isInstance(participant)) {
                            UserParticipant userParticipant = (UserParticipant) participant;
                            int identifier = userParticipant.getIdentifier();
                            UserParticipant up = userMap.get(identifier);
                            if (up != null && CalendarObject.NONE != up.getConfirm()) {
                                // prefer confirmation status from users when set
                                userParticipant.setConfirm(up.getConfirm());
                                userParticipant.setConfirmMessage(up.getConfirmMessage());
                            } else if (createdBy == identifier && CalendarObject.NONE == userParticipant.getConfirm()) {
                                // assume 'accepted' when no confirmation set
                                userParticipant.setConfirm(CalendarObject.ACCEPT);
                            }
                        }
                    }
                }
            }
        }

        /**
         * Adds all user participants to the participants list and removes all
         * group participants.
         *
         * @param appointment
         */
        public static void resolveGroupParticipants(Appointment appointment) {
            Participant[] participants = appointment.getParticipants();
            if (null != participants) {
                Set<Integer> guardian = new HashSet<Integer>();
                List<Participant> newParticipants = new ArrayList<Participant>();
                for (Participant participant : participants) {
                    if (UserParticipant.class.isInstance(participant)) {
                        UserParticipant userParticipant = (UserParticipant) participant;
                        guardian.add(new Integer(userParticipant.getIdentifier()));
                        newParticipants.add(userParticipant);
                    } else if (false == GroupParticipant.class.isInstance(participant)) {
                        newParticipants.add(participant);
                    }
                }
                UserParticipant[] users = appointment.getUsers();
                if (null != users) {
                    for (UserParticipant userParticipant : users) {
                        if (false == guardian.contains(new Integer(userParticipant.getIdentifier()))) {
                            newParticipants.add(userParticipant);
                        }
                    }
                }
                appointment.setParticipants(newParticipants);
            }
        }

        /**
         * Adjusts the appointment's alarm property. Based on the folder type, the alarm is overridden with the configured alarm of the
         * user in case it is a public folder and the user participates, otherwise the alarm is removed. No changes are done if the
         * folder is a private one.
         *
         * @param folder The parent folder of the appointment
         * @param appointment The appointment
         */
        public static void adjustAlarm(UserizedFolder folder, Appointment appointment) {
            if (SharedType.getInstance().equals(folder.getType())) {
                /*
                 * remove alarm, since user has the appointment in his personal folder, too
                 */
                appointment.removeAlarm();
            } else if (PublicType.getInstance().equals(folder.getType())) {
                /*
                 * remove alarm by default
                 */
                appointment.removeAlarm();
                if (null != folder.getUser() && null != appointment.getUsers()) {
                    int userID = folder.getUser().getId();
                    for (UserParticipant user : appointment.getUsers()) {
                        if (userID == user.getIdentifier()) {
                            /*
                             * take over alarm of current user
                             */
                            appointment.setAlarmFlag(user.containsAlarm());
                            appointment.setAlarm(user.getAlarmMinutes());
                            break;
                        }
                    }
                }
            }
        }

        /**
         * Sets the appointment's organizer using the appointment's
         * 'created-by' information if not yet set.
         *
         * @param factory
         * @param appointment
         * @throws WebdavProtocolException
         */
        public static void setOrganizerInformation(GroupwareCaldavFactory factory, Appointment appointment) throws OXException {
            String organizer = appointment.getOrganizer();
            if (null == organizer) {
                int createdBy = appointment.getCreatedBy();
                if (0 < createdBy) {
                    User user = factory.resolveUser(createdBy);
                    appointment.setOrganizer(user.getMail());
                }
            }
        }

        /**
         * Removes the implicitly added folder owner participant for appointments in private/shared folders in case no further
         * participants were added, along with any organizer information.
         * <p/>
         * This effectively makes the appointment to not appear as group appointment in the Mac OS client, as well as allowing
         * modifications on it.
         *
         * @param folder The parent folder of the appointment
         * @param appointment The appointment
         */
        public static void removeImplicitParticipant(UserizedFolder folder, Appointment appointment) {
            int folderOwnerID = -1;
            if (PrivateType.getInstance().equals(folder.getType())) {
                folderOwnerID = folder.getUser().getId();
            } else if (SharedType.getInstance().equals(folder.getType())) {
                Permission[] permissions = folder.getPermissions();
                if (null != permissions && 0 < permissions.length) {
                    for (Permission permission : permissions) {
                        if (permission.isAdmin() && false == permission.isGroup()) {
                            folderOwnerID = permission.getEntity();
                            break;
                        }
                    }
                }
            }
            if (-1 != folderOwnerID) {
                Participant[] participants = appointment.getParticipants();
                if (null != participants && 1 == participants.length && UserParticipant.class.isInstance(participants[0]) &&
                    ((UserParticipant)participants[0]).getIdentifier() == folderOwnerID) {
                    appointment.removeParticipants();
                    appointment.removeUsers();
                    appointment.removeCreatedBy();
                    appointment.removeOrganizer();
                    appointment.removeOrganizerId();
                }
            }
        }

    }
}
