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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.calendar.itip.generators.changes.generators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.calendar.AppointmentDiff.FieldUpdate;
import com.openexchange.calendar.itip.Messages;
import com.openexchange.calendar.itip.generators.ArgumentType;
import com.openexchange.calendar.itip.generators.Sentence;
import com.openexchange.calendar.itip.generators.changes.ChangeDescriptionGenerator;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Change;
import com.openexchange.groupware.container.ConfirmationChange;
import com.openexchange.groupware.container.Difference;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Autoboxing;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.user.UserService;
import static com.openexchange.ajax.fields.AppointmentFields.*;

/**
 * {@link Participants}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Participants implements ChangeDescriptionGenerator{

    private static final EnumSet<ChangeType> STATE_CHANGES = EnumSet.of(ChangeType.ACCEPT, ChangeType.DECLINE, ChangeType.TENTATIVE);

	private static final String[] FIELDS = new String[]{USERS, PARTICIPANTS, CONFIRMATIONS};

    protected static enum ChangeType {
        ADD, REMOVE, ACCEPT, DECLINE, TENTATIVE
    }

    private static final Map<ChangeType, String> PARTICIPANT_MESSAGE_MAP = new HashMap<ChangeType, String>(){{
        put(ChangeType.ADD, Messages.HAS_ADDED_PARTICIPANT);
        
        put(ChangeType.REMOVE,  Messages.HAS_REMOVED_PARTICIPANT);

        put(ChangeType.ACCEPT, Messages.HAS_CHANGED_STATE);
        put(ChangeType.DECLINE, Messages.HAS_CHANGED_STATE);
        put(ChangeType.TENTATIVE, Messages.HAS_CHANGED_STATE);

    }};
    
    private static final Map<ChangeType, String> STATE_MESSAGE_MAP = new HashMap<ChangeType, String>(){{
      
        put(ChangeType.ACCEPT,  Messages.ACCEPTED);
        put(ChangeType.DECLINE, Messages.DECLINED);
        put(ChangeType.TENTATIVE, Messages.TENTATIVELY_ACCEPTED);
     }};
    

    private static final Map<ChangeType,String> GROUP_MESSAGE_MAP = new HashMap<ChangeType, String>(){{
        put(ChangeType.ADD, Messages.HAS_INVITED_GROUP);
        put(ChangeType.REMOVE, Messages.HAS_REMOVED_GROUP);
    }};
    
    private static final Map<ChangeType, String> RESOURCE_MESSAGE_MAP = new HashMap<ChangeType, String>(){{
        put(ChangeType.ADD,  Messages.HAS_ADDED_RESOURCE);
        put(ChangeType.REMOVE, Messages.HAS_REMOVED_RESOURCE);
    }};
    
    
    
    private final UserService users;
    private final GroupService groups;
    private final ResourceService resources;

	private final boolean stateChanges;
    
    
    
    public Participants(UserService users, GroupService groups, ResourceService resources, boolean stateChanges) {
        super();
        this.users = users;
        this.groups = groups;
        this.resources = resources;
        this.stateChanges = stateChanges;
    }

    @Override
    public List<Sentence> getDescriptions(Context ctx, Appointment original, Appointment updated, AppointmentDiff diff, Locale locale, TimeZone timezone) throws OXException {
        
        List<Integer> userIds = new ArrayList<Integer>();
        List<Integer> groupIds = new ArrayList<Integer>();
        List<Integer> resourceIds = new ArrayList<Integer>();
        
        Map<Integer,ChangeType> userChange = new HashMap<Integer, ChangeType>();
        Map<Integer,ChangeType> resourceChange = new HashMap<Integer, ChangeType>();
        Map<Integer,ChangeType> groupChange = new HashMap<Integer, ChangeType>();
        Map<String,ChangeType> externalChange = new HashMap<String, ChangeType>();
        
        Set<String> external = new HashSet<String>();
        Participant[] participants = updated.getParticipants();
        if (participants != null) {
            for(Participant p : participants) {
                if (p instanceof ExternalUserParticipant) {
                    ExternalUserParticipant ep = (ExternalUserParticipant) p;
                    external.add(ep.getEmailAddress());
                }
            }
        }
        
        participants = original.getParticipants();
        if (participants != null) {
            for(Participant p : participants) {
                if (p instanceof ExternalUserParticipant) {
                    ExternalUserParticipant ep = (ExternalUserParticipant) p;
                    external.add(ep.getEmailAddress());
                }
            }
        }
        

        FieldUpdate update = diff.getUpdateFor("participants");
        if (update != null) {
            Difference difference = (Difference) update.getExtraInfo();
            investigateSetOperation(difference, userIds, groupIds, resourceIds, userChange, resourceChange, groupChange, externalChange, ChangeType.ADD, difference.getAdded());
            investigateSetOperation(difference, userIds, groupIds, resourceIds, userChange, resourceChange, groupChange, externalChange, ChangeType.REMOVE, difference.getRemoved());
            investigateChanges(difference, userIds, userChange, externalChange, external);
        }
        
        
        update = diff.getUpdateFor("confirmations");
        if (update != null) {
            Difference difference = (Difference) update.getExtraInfo();
            
            investigateSetOperation(difference, userIds, groupIds, resourceIds, userChange, resourceChange, groupChange, externalChange, ChangeType.ADD, difference.getAdded());
            investigateSetOperation(difference, userIds, groupIds, resourceIds, userChange, resourceChange, groupChange, externalChange, ChangeType.REMOVE, difference.getRemoved());
            investigateChanges( difference, userIds, userChange, externalChange, external);
        }
               
        
        update = diff.getUpdateFor("users");
        if (update != null) {
            Difference difference = (Difference) update.getExtraInfo();

            investigateChanges(difference, userIds, userChange, externalChange, external);
            
        }

        List<Sentence> changes = new ArrayList<Sentence>();
        
        User[] user = users.getUser(ctx, Autoboxing.Coll2i(userIds));
        for (User u : user) {
            ChangeType changeType = userChange.get(u.getId());
            if (changeType == null) {
                continue;
            }
            if (STATE_CHANGES.contains(changeType) && !stateChanges) {
            	continue;
            }
            switch (changeType) {
            case ADD: case REMOVE: changes.add(new Sentence(PARTICIPANT_MESSAGE_MAP.get(changeType)).add(u.getDisplayName(), ArgumentType.PARTICIPANT)); break;
            case ACCEPT: changes.add(new Sentence(PARTICIPANT_MESSAGE_MAP.get(changeType)).add(u.getDisplayName(), ArgumentType.PARTICIPANT).add(STATE_MESSAGE_MAP.get(changeType), ArgumentType.STATUS, ConfirmStatus.ACCEPT)); break;
            case DECLINE: changes.add(new Sentence(PARTICIPANT_MESSAGE_MAP.get(changeType)).add(u.getDisplayName(), ArgumentType.PARTICIPANT).add(STATE_MESSAGE_MAP.get(changeType), ArgumentType.STATUS, ConfirmStatus.DECLINE)); break;
            case TENTATIVE: changes.add(new Sentence(PARTICIPANT_MESSAGE_MAP.get(changeType)).add(u.getDisplayName(), ArgumentType.PARTICIPANT).add(STATE_MESSAGE_MAP.get(changeType), ArgumentType.STATUS, ConfirmStatus.TENTATIVE)); break;
            }
        }
        
        List<String> externalMails = new ArrayList<String>(externalChange.keySet());
        Collections.sort(externalMails);
        for (String mail : externalMails) {
            ChangeType changeType = externalChange.get(mail);
            if (changeType == null) {
                continue;
            }
            if (STATE_CHANGES.contains(changeType) && !stateChanges) {
            	continue;
            }
            switch (changeType) {
            case ADD: case REMOVE: changes.add(new Sentence(PARTICIPANT_MESSAGE_MAP.get(changeType)).add(mail, ArgumentType.PARTICIPANT)); break;
            case ACCEPT: changes.add(new Sentence(PARTICIPANT_MESSAGE_MAP.get(changeType)).add(mail, ArgumentType.PARTICIPANT).add(STATE_MESSAGE_MAP.get(changeType), ArgumentType.STATUS, ConfirmStatus.ACCEPT)); break;
            case DECLINE: changes.add(new Sentence(PARTICIPANT_MESSAGE_MAP.get(changeType)).add(mail, ArgumentType.PARTICIPANT).add(STATE_MESSAGE_MAP.get(changeType), ArgumentType.STATUS, ConfirmStatus.DECLINE)); break;
            case TENTATIVE: changes.add(new Sentence(PARTICIPANT_MESSAGE_MAP.get(changeType)).add(mail, ArgumentType.PARTICIPANT).add(STATE_MESSAGE_MAP.get(changeType), ArgumentType.STATUS, ConfirmStatus.TENTATIVE)); break;
            }
        }
        
        for(Integer id : groupChange.keySet()) {
            Group group = groups.getGroup(ctx, id);
            ChangeType changeType = externalChange.get(id);
            if (changeType == null) {
                continue;
            }
            switch (changeType) {
            case ADD: case REMOVE: changes.add(new Sentence(GROUP_MESSAGE_MAP.get(changeType)).add(group.getDisplayName(), ArgumentType.PARTICIPANT)); break;
            default: // Skip
            }
        }

        for(Integer id : resourceChange.keySet()) {
            Resource resource = resources.getResource(id, ctx);
            ChangeType changeType = externalChange.get(id);
            if (changeType == null) {
                continue;
            }
            switch (changeType) {
            case ADD: case REMOVE: changes.add(new Sentence(RESOURCE_MESSAGE_MAP.get(changeType)).add(resource.getDisplayName(), ArgumentType.PARTICIPANT)); break;
            default: // Skip
            }
        }

        
        return changes;
    }

    private void investigateChanges(Difference difference, List<Integer> userIds, Map<Integer, ChangeType> userChange, Map<String, ChangeType> externalChange, Set<String> external) {
        
        for(Change change : difference.getChanged()) {
            if (change instanceof ConfirmationChange) {
                ConfirmationChange cchange = (ConfirmationChange) change;
                String identifier = cchange.getIdentifier();

                int newStatus = cchange.getNewStatus();
                ChangeType changeType = null;
                switch (newStatus) {
                case CalendarObject.ACCEPT: 
                    changeType = ChangeType.ACCEPT; 
                    break;
                case CalendarObject.DECLINE: 
                    changeType = ChangeType.DECLINE; 
                    break;
                case CalendarObject.TENTATIVE: 
                    changeType = ChangeType.TENTATIVE; 
                    break;
                }

                
                if (external.contains(identifier)) {
                    externalChange.put(identifier, changeType);
                } else {
                    int id = Integer.parseInt(identifier);
                    userIds.add(id);
                    userChange.put(id, changeType);
                }
            }
        }
    }

    private void investigateSetOperation(Difference difference, List<Integer> userIds, List<Integer> groupIds, List<Integer> resourceIds, Map<Integer, ChangeType> userChange, Map<Integer, ChangeType> resourceChange, Map<Integer, ChangeType> groupChange, Map<String, ChangeType> externalChange, ChangeType changeType, List<Object> list) {
        for(Object added : list) {
            if (added instanceof UserParticipant) {
                UserParticipant up = (UserParticipant) added;
                userIds.add(up.getIdentifier());
                userChange.put(up.getIdentifier(), changeType);
            }
            
            if (added instanceof ExternalUserParticipant) {
                ExternalUserParticipant ep = (ExternalUserParticipant) added;
                externalChange.put(ep.getEmailAddress(), changeType);
            }
            
            if (added instanceof ResourceParticipant) {
                ResourceParticipant rp = (ResourceParticipant) added;
                resourceIds.add(rp.getIdentifier());
                resourceChange.put(rp.getIdentifier(), changeType);
                
            }
            
            if (added instanceof GroupParticipant) {
                GroupParticipant gp = (GroupParticipant) added;
                groupIds.add(gp.getIdentifier());
                groupChange.put(gp.getIdentifier(), changeType);
            }
        }
    }

    @Override
    public String[] getFields() {
        return FIELDS;
    }
    
    

}
