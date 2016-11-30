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

package com.openexchange.data.conversion.ical.ical4j.internal.calendar;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.idn.IDNA;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ConversionWarning.Code;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.OXResourceResolver;
import com.openexchange.data.conversion.ical.ical4j.internal.ResourceResolver;
import com.openexchange.data.conversion.ical.ical4j.internal.UserResolver;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.NotificationConfig;
import com.openexchange.groupware.notify.NotificationConfig.NotificationProperty;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.resource.Resource;
import com.openexchange.server.ServiceExceptionCode;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TextList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Resources;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.util.Uris;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Participants<T extends CalendarComponent, U extends CalendarObject> extends AbstractVerifyingAttributeConverter<T,U> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Participants.class);
    private static final AtomicReference<GroupService> GROUP_SERVICE_REFERENCE = new AtomicReference<GroupService>();

    public static UserResolver userResolver = UserResolver.EMPTY;

    public static ResourceResolver resourceResolver = new OXResourceResolver();

    /**
     * Sets the group service reference.
     *
     * @param groupService The group service
     */
    public static void setGroupService(GroupService groupService) {
        GROUP_SERVICE_REFERENCE.set(groupService);
    }

    /**
     * Initializes a new {@link Participants}.
     */
    public Participants() {
        super();
    }

    @Override
    public boolean isSet(final U cObj) {
        return cObj.containsParticipants();
    }

    @Override
    public void emit(final Mode mode, final int index, final U cObj, final T component, final List<ConversionWarning> warnings, final Context ctx, final Object... args) throws ConversionError {
        final List<ResourceParticipant> resources = new LinkedList<ResourceParticipant>();
        if (cObj.getUsers() != null) {
            for (UserParticipant up : cObj.getUsers()) {
                addUserAttendee(index, up, ctx, component, cObj);
            }
        }
        for(final Participant p : cObj.getParticipants()) {
            switch(p.getType()) {
                case Participant.EXTERNAL_USER:
                    addExternalAttendee(index, (ExternalUserParticipant)p, component, warnings);
                    break;
                case Participant.RESOURCE:
                    resources.add((ResourceParticipant) p);
                    addResourceAttendee((ResourceParticipant) p, cObj, component, ctx);
                    break;
                default:
            }
        }
        /*
         * add private comment if set
         */
        String privateComment = cObj.getProperty("com.openexchange.data.conversion.ical.participants.privateComment");
        if (null != privateComment) {
            component.getProperties().add(new XProperty("X-CALENDARSERVER-PRIVATE-COMMENT", privateComment));
        }
        if(resources.isEmpty()) { return; }
        setResources(index, component, resources, ctx);
    }

    protected void addResourceAttendee(ResourceParticipant resourceParticipant, U calendarObject, T component, Context ctx) {
        Attendee attendee = new Attendee();
        try {
            attendee.setValue("urn:uuid:" + encode(ctx.getContextId(), Participant.RESOURCE, resourceParticipant.getIdentifier()));
            ParameterList parameters = attendee.getParameters();
            parameters.add(CuType.RESOURCE);
            parameters.add(PartStat.ACCEPTED);
            parameters.add(Role.REQ_PARTICIPANT);
            String displayName = resourceParticipant.getDisplayName();
            try {
                Resource resource = resourceResolver.load(resourceParticipant.getIdentifier(), ctx);
                displayName = resource.getDisplayName();
                /*
                 * add resource description as attendee's comment for organizer if available
                 */
                if (Strings.isNotEmpty(resource.getDescription()) &&
                    Boolean.TRUE.equals(calendarObject.getProperty("com.openexchange.data.conversion.ical.participants.attendeeComments"))) {
                    XProperty attendeeCommentProperty = new XProperty("X-CALENDARSERVER-ATTENDEE-COMMENT", resource.getDescription());
                    attendeeCommentProperty.getParameters().add(new XParameter("X-CALENDARSERVER-ATTENDEE-REF", attendee.getValue()));
                    component.getProperties().add(attendeeCommentProperty);
                }
            } catch (OXException e) {
                LOG.warn("Error resolving resource participant", e);
            }
            parameters.add(new Cn(displayName));
            component.getProperties().add(attendee);
        } catch (URISyntaxException e) {
            LOG.error("", e); // Shouldn't happen
        }
    }

    private void setResources(final int index, final T component,
        final List<ResourceParticipant> resources, final Context ctx) throws ConversionError {
        final TextList list = new TextList();
        for (final ResourceParticipant res : resources) {
            String displayName = res.getDisplayName();
            if (null == displayName) {
                try {
                    final Resource resource = resourceResolver.load(res.getIdentifier(), ctx);
                    displayName = resource.getDisplayName();
                } catch (final OXException e) {
                    throw new ConversionError(index, e);
                }
            }
            list.add(displayName);
        }
        final Resources property = new Resources(list);
        component.getProperties().add(property);
    }

    protected void addExternalAttendee(int index, final ExternalUserParticipant externalUserParticipant, final T component, List<ConversionWarning> warnings) {
        final Attendee attendee = new Attendee();
        try {
            try {
                new InternetAddress(externalUserParticipant.getEmailAddress()).validate();
            } catch (AddressException e) {
                warnings.add(new ConversionWarning(index, Code.INVALID_MAIL_ADDRESS, externalUserParticipant.getEmailAddress()));
                // Discard
                return;
            }
            attendee.setValue("mailto:" + IDNA.toACE(externalUserParticipant.getEmailAddress()));
            final ParameterList parameters = attendee.getParameters();
            parameters.add(CuType.INDIVIDUAL);
            parameters.add(getPartStat(externalUserParticipant));
            parameters.add(Role.REQ_PARTICIPANT);
            parameters.add(Rsvp.TRUE);
            component.getProperties().add(attendee);
        } catch (final URISyntaxException e) {
            LOG.error("", e); // Shouldn't happen
        } catch (AddressException e) {
            LOG.error("", e);
        }
    }

    private PartStat getPartStat(ExternalUserParticipant external) {
        switch (external.getStatus()) {
        case ACCEPT:
            return PartStat.ACCEPTED;
        case DECLINE:
            return PartStat.DECLINED;
        case TENTATIVE:
            return PartStat.TENTATIVE;
        case NONE:
        default:
            return PartStat.NEEDS_ACTION;
        }
    }

    protected void addUserAttendee(final int index, final UserParticipant userParticipant, final Context ctx, final T component, final U obj) throws ConversionError {
        final Attendee attendee = new Attendee();
        try {
            String address;
            //This sets the attendees email-addresses to their DefaultSenderAddress if configured via com.openexchange.notification.fromSource in notification.properties
            final String senderSource = NotificationConfig.getProperty(NotificationProperty.FROM_SOURCE, "primaryMail");
            if ("defaultSenderAddress".equals(senderSource)) {
                try {
                    address = UserSettingMailStorage.getInstance().loadUserSettingMail(userParticipant.getIdentifier(), ctx).getSendAddr();
                } catch (final OXException e) {
                    LOG.error("", e);
                    address = resolveUserMail(index, userParticipant, ctx);
                }
            } else {
                address = resolveUserMail(index, userParticipant, ctx);
            }
            address = IDNA.toACE(address);
            PropertyList properties = component.getProperties(Property.ATTENDEE);
            for (Object p : properties) {
                if (((Property)p).getValue().equals("mailto:" + address)) {
                    return; // skip
                }
            }
            attendee.setValue("mailto:" + address);
            final ParameterList parameters = attendee.getParameters();
            parameters.add(Role.REQ_PARTICIPANT);
            parameters.add(CuType.INDIVIDUAL);
            final String displayName = this.resolveUserDisplayName(index, userParticipant, ctx);
            parameters.add(new Cn(null != displayName && 0 < displayName.length() ? displayName : address));
            switch (getConfirm(userParticipant, obj)) {
                case CalendarObject.ACCEPT:
                    parameters.add(PartStat.ACCEPTED);
                    break;
                case CalendarObject.DECLINE:
                    parameters.add(PartStat.DECLINED);
                    break;
                case CalendarObject.TENTATIVE:
                    parameters.add(PartStat.TENTATIVE);
                    break;
                case CalendarObject.NONE:
                    parameters.add(PartStat.NEEDS_ACTION);
                    break;
                default:
                    break;
            }
            component.getProperties().add(attendee);
            /*
             * add each attendee's comment for organizer if available
             */
            if (Strings.isNotEmpty(userParticipant.getConfirmMessage()) &&
                Boolean.TRUE.equals(obj.getProperty("com.openexchange.data.conversion.ical.participants.attendeeComments"))) {
                XProperty attendeeCommentProperty = new XProperty("X-CALENDARSERVER-ATTENDEE-COMMENT", userParticipant.getConfirmMessage());
                attendeeCommentProperty.getParameters().add(new XParameter("X-CALENDARSERVER-ATTENDEE-REF", attendee.getValue()));
                component.getProperties().add(attendeeCommentProperty);
            }
        } catch (final URISyntaxException e) {
            LOG.error("", e); // Shouldn't happen
        } catch (AddressException e) {
            LOG.error("", e);
        }
    }

    private int getConfirm(UserParticipant p, U obj) {
        if (p.containsConfirm()) {
            return p.getConfirm();
        }

        for (UserParticipant u : obj.getUsers()) {
            if (u.getIdentifier() == p.getIdentifier()) {
                return u.getConfirm();
            }
        }
        return 0;
    }

    protected String resolveUserDisplayName(int index, UserParticipant userParticipant, Context ctx) throws ConversionError {
        String displayName = userParticipant.getDisplayName();
        if (null == displayName || 0 == displayName.length()) {
            try {
                displayName = userResolver.loadUser(userParticipant.getIdentifier(), ctx).getDisplayName();
            } catch (OXException e) {
                LOG.warn("Error resolving displayname for user participant", e);
            }
        }
        return displayName;
    }

    protected String resolveUserMail(final int index, final UserParticipant userParticipant, final Context ctx) throws ConversionError {
        String address = userParticipant.getEmailAddress();
        if (address == null) {
            try {
                final User user = userResolver.loadUser(userParticipant.getIdentifier(), ctx);
                address = user.getMail();
            } catch (final OXException e) {
                throw new ConversionError(index, e);
            }
        }
        return address;
    }

    @Override
    public boolean hasProperty(final T component) {
        final PropertyList properties = component.getProperties(Property.ATTENDEE);
        final PropertyList resourcesList = component.getProperties(Property.RESOURCES);
        return properties.size() > 0 || resourcesList.size() > 0;
    }

    @Override
    public void parse(final int index, final T component, final U cObj, final TimeZone timeZone, final Context ctx, final List<ConversionWarning> warnings) throws ConversionError {
        Map<String, ICalParticipant> mails = new HashMap<String, ICalParticipant>();
        String comment = component.getProperty(Property.COMMENT) == null ? null : component.getProperty(Property.COMMENT).getValue();
        List<Resource> resources = new ArrayList<Resource>();
        List<Group> groups = new ArrayList<Group>();

        PropertyList properties = component.getProperties(Property.ATTENDEE);
        for (int i = 0, size = properties.size(); i < size; i++) {
            Attendee attendee = (Attendee) properties.get(i);
            /*
             * try to decode unique urn:uuid first
             */
            URI uri = attendee.getCalAddress();
            if (null != uri && "urn".equals(uri.getScheme())) {
                String specificPart = uri.getSchemeSpecificPart();
                if (Strings.isNotEmpty(specificPart) && specificPart.startsWith("uuid:")) {
                    try {
                        UUID uuid = UUID.fromString(specificPart.substring(5));
                        if (ctx.getContextId() == decodeContextID(uuid)) {
                            int entity = decodeEntity(uuid);
                            switch (decodeType(uuid)) {
                                case Participant.GROUP:
                                    GroupService groupService = GROUP_SERVICE_REFERENCE.get();
                                    if (null == groupService) {
                                        throw new ConversionWarning(index, Code.CANT_RESOLVE_GROUP, ServiceExceptionCode.absentService(GroupService.class), attendee.toString());
                                    }
                                    groups.add(groupService.getGroup(ctx, decodeEntity(uuid)));
                                    continue;
                                case Participant.RESOURCE:
                                    resources.add(resourceResolver.load(decodeEntity(uuid), ctx));
                                    continue;
                                case Participant.USER:
                                    User user = userResolver.loadUser(entity, ctx);
                                    addMail(index, user.getMail(), mails, attendee, comment, warnings);
                                    continue;
                                default:
                                    break;
                            }
                        }
                    } catch (IllegalArgumentException | OXException e) {
                        warnings.add(new ConversionWarning(index, Code.CANT_RESOLVE_USER, e, specificPart));
                    }
                }
            }
            /*
             * decode based on CUTYPE
             */
            if (CuType.RESOURCE.equals(attendee.getParameter(Parameter.CUTYPE))) {
                try {
                    resources.add(parseResourceAttendee(index, ctx, attendee));
                } catch (ConversionWarning e) {
                    warnings.add(e);
                }
            } else if (CuType.GROUP.equals(attendee.getParameter(Parameter.CUTYPE))) {
                try {
                    groups.add(parseGroupAttendee(index, ctx, attendee));
                } catch (ConversionWarning e) {
                    warnings.add(e);
                }
            } else if (null != uri) {
                String specificPart = uri.getSchemeSpecificPart();
                if (false == Strings.isEmpty(specificPart)) {
                    String mail = null;
                    if ("mailto".equalsIgnoreCase(uri.getScheme())) {
                        /*
                         * mailto-scheme -> e-mail address
                         */
                        mail = uri.getSchemeSpecificPart();
                    } else if (Uris.INVALID_SCHEME.equals(uri.getScheme())) {
                        /*
                         * try and parse value as quoted internet address (best effort)
                         */
                        try {
                            mail = new QuotedInternetAddress(specificPart).getAddress();
                        } catch (AddressException e) {
                            // ignore
                        }
                    }
                    /*
                     * add iCal participant if parsed successfully
                     */
                    if (false == Strings.isEmpty(mail)) {
                        addMail(index, mail, mails, attendee, comment, warnings);
                    }
                }
            }
        }

        List<User> users;
        try {
            users = userResolver.findUsers(new ArrayList<String>(mails.keySet()), ctx);
        } catch (final OXException e) {
            throw new ConversionError(index, e);
        }

        for(final User user : users) {
            final UserParticipant up = new UserParticipant(user.getId());
            ICalParticipant icalP = null;
            for (String alias: getPossibleAddresses(user)) {
            	String toRemove = null;
            	for ( Entry<String, Participants<T, U>.ICalParticipant> mailEntry : mails.entrySet()){
            	    String mail = mailEntry.getKey();
            		if (mail.equalsIgnoreCase(alias)){
            			icalP = mailEntry.getValue();
            			toRemove = mail;
            			break;
            		}
            	}
                if (toRemove != null) {
                    mails.remove(toRemove);
                    break;
                }
            }
            if (icalP == null) {
                LOG.warn("Should not be possible to find a user ({}) by their alias and then be unable to remove that alias  from list", user.getMail());
            } else {
                if (icalP.message != null) {
                    up.setConfirmMessage(icalP.message);
                }
                if (icalP.status != -1) {
                    up.setConfirm(icalP.status);
                }
            }

            cObj.addParticipant(up);
        }

        final List<ConfirmableParticipant> confirmableParticipants = new ArrayList<ConfirmableParticipant>();
        for(final String mail : mails.keySet()) {
            final ExternalUserParticipant external = new ExternalUserParticipant(mail);
            external.setDisplayName(null);

            final ICalParticipant icalP = mails.get(mail);

            if (icalP.message != null) {
                external.setMessage(icalP.message);
            }
            if (icalP.status != -1) {
                external.setConfirm(icalP.status);
            }

            if (comment != null) {
                external.setMessage(comment);
            }

            cObj.addParticipant(external);
            confirmableParticipants.add(external);
        }

        if (confirmableParticipants.size() > 0) {
            cObj.setConfirmations(confirmableParticipants.toArray(new ConfirmableParticipant[confirmableParticipants.size()]));
        }

        for (Group group : groups) {
            GroupParticipant groupParticipant = new GroupParticipant(group.getIdentifier());
            groupParticipant.setDisplayName(group.getDisplayName());
            cObj.addParticipant(groupParticipant);
        }

        final PropertyList resourcesList = component.getProperties(Property.RESOURCES);
        final List<String> resourceNames = new LinkedList<String>();
        for (int i = 0, size = resourcesList.size(); i < size; i++) {
            final Resources resourcesProperty = (Resources) resourcesList.get(i);
            final Iterator<?> resObjects = resourcesProperty.getResources().iterator();
            while (resObjects.hasNext()) {
                resourceNames.add(resObjects.next().toString());
            }
        }
        try {
            resources.addAll(resourceResolver.find(resourceNames, ctx));
        } catch (final OXException e) {
            throw new ConversionError(index, e);
        }

        for (final Resource resource : resources) {
            cObj.addParticipant(new ResourceParticipant(resource));
            resourceNames.remove(resource.getDisplayName());
        }
        for (final String resourceName : resourceNames) {
            warnings.add(new ConversionWarning(index, Code.CANT_RESOLVE_RESOURCE, resourceName));
        }
        /*
         * make private comment available as property if set
         */
        Property privateCommentProperty = component.getProperty("X-CALENDARSERVER-PRIVATE-COMMENT");
        if (null != privateCommentProperty) {
            cObj.setProperty("com.openexchange.data.conversion.ical.participants.privateComment", privateCommentProperty.getValue());
        }
    }

    private void addMail(int index, String mail, Map<String, ICalParticipant> mails, Attendee attendee, String comment, List<ConversionWarning> warnings) {
        try {
            new InternetAddress(mail).validate();
        } catch (AddressException e) {
            warnings.add(new ConversionWarning(index, Code.INVALID_MAIL_ADDRESS, mail));
            return;
        }
        mails.put(mail, createIcalParticipant(attendee, IDNA.toIDN(mail), comment));
    }

    private static Set<String> getPossibleAddresses(User user) {
        Set<String> possibleAddresses = new HashSet<String>();
        if (null != user.getMail()) {
            possibleAddresses.add((user.getMail()));
        }
        if (null != user.getAliases()) {
            for (String alias : user.getAliases()) {
                possibleAddresses.add(alias);
            }
        }
        return possibleAddresses;
    }

    /**
     * @param attendee
     * @return
     */
    private ICalParticipant createIcalParticipant(final Attendee attendee, final String mail, final String message) {
        final ICalParticipant retval = new ICalParticipant(mail, -1, message);

        final Parameter parameter = attendee.getParameter(Parameter.PARTSTAT);
        if (parameter != null) {
            if (parameter.equals(PartStat.ACCEPTED)) {
                retval.status = CalendarObject.ACCEPT;
            } else if (parameter.equals(PartStat.DECLINED)) {
                retval.status = CalendarObject.DECLINE;
            } else if (parameter.equals(PartStat.TENTATIVE)) {
                retval.status = CalendarObject.TENTATIVE;
            } else {
                retval.status = CalendarObject.NONE;
            }
        }
        return retval;
    }

    private class ICalParticipant {
        public String mail;
        public int status;
        public String message;

        public ICalParticipant(final String mail, final int status, final String message) {
            this.mail = mail;
            this.status = status;
            this.message = message;
        }
    }

    /**
     * Tries to resolve a resource from an <code>ATTENDEE</code> component.
     *
     * @param index The current index in the parsed iCal file
     * @param context The context
     * @param attendee The attendee to parse
     * @return The parsed resource
     */
    private static Resource parseResourceAttendee(int index, Context context, Attendee attendee) throws ConversionWarning {
        /*
         * try to decode urn:uuid
         */
        URI uri = attendee.getCalAddress();
        if (null != uri && "urn".equals(uri.getScheme())) {
            String specificPart = uri.getSchemeSpecificPart();
            if (Strings.isNotEmpty(specificPart) && specificPart.startsWith("uuid:")) {
                try {
                    UUID uuid = UUID.fromString(specificPart.substring(5));
                    if (context.getContextId() == decodeContextID(uuid) && Participant.RESOURCE == decodeType(uuid)) {
                        return resourceResolver.load(decodeEntity(uuid), context);
                    }
                } catch (IllegalArgumentException | OXException e) {
                    throw new ConversionWarning(index, Code.CANT_RESOLVE_RESOURCE, e, specificPart);
                }
            }
        }
        /*
         * lookup by common name as fallback
         */
        Parameter cnParameter = attendee.getParameter(Parameter.CN);
        if (null != cnParameter && Strings.isNotEmpty(cnParameter.getValue())) {
            List<Resource> foundResources = null;
            try {
                foundResources = resourceResolver.find(Collections.singletonList(cnParameter.getValue()), context);
            } catch (OXException e) {
                throw new ConversionWarning(index, Code.CANT_RESOLVE_RESOURCE, e, cnParameter.getValue());
            }
            if (null != foundResources && 1 == foundResources.size()) {
                return foundResources.get(0);
            }
        }
        throw new ConversionWarning(index, Code.CANT_RESOLVE_RESOURCE, attendee.toString());
    }

    /**
     * Tries to resolve a group from an <code>ATTENDEE</code> component.
     *
     * @param index The current index in the parsed iCal file
     * @param context The context
     * @param attendee The attendee to parse
     * @return The parsed group
     */
    private static Group parseGroupAttendee(int index, Context context, Attendee attendee) throws ConversionWarning {
        GroupService groupService = GROUP_SERVICE_REFERENCE.get();
        if (null == groupService) {
            throw new ConversionWarning(index, Code.CANT_RESOLVE_GROUP, ServiceExceptionCode.absentService(GroupService.class), attendee.toString());
        }
        /*
         * try to decode urn:uuid
         */
        URI uri = attendee.getCalAddress();
        if (null != uri && "urn".equals(uri.getScheme())) {
            String specificPart = uri.getSchemeSpecificPart();
            if (Strings.isNotEmpty(specificPart) && specificPart.startsWith("uuid:")) {
                try {
                    UUID uuid = UUID.fromString(specificPart.substring(5));
                    if (context.getContextId() == decodeContextID(uuid) && Participant.GROUP == decodeType(uuid)) {
                        return groupService.getGroup(context, decodeEntity(uuid));
                    }
                } catch (IllegalArgumentException | OXException e) {
                    throw new ConversionWarning(index, Code.CANT_RESOLVE_GROUP, e, specificPart);
                }
            }
        }
        /*
         * lookup by common name as fallback
         */
        Parameter cnParameter = attendee.getParameter(Parameter.CN);
        if (null != cnParameter && Strings.isNotEmpty(cnParameter.getValue())) {
            Group[] foundGroups = null;
            try {
                foundGroups = groupService.search(context, cnParameter.getValue(), false);
            } catch (OXException e) {
                throw new ConversionWarning(index, Code.CANT_RESOLVE_GROUP, e, cnParameter.getValue());
            }
            if (null != foundGroups && 1 == foundGroups.length) {
                return foundGroups[0];
            }
        }
        throw new ConversionWarning(index, Code.CANT_RESOLVE_GROUP, attendee.toString());
    }

    private static UUID encode(int contextID, int type, int entity) {
        long lsb = entity;
        long cid = contextID & 0xffffffffL;
        long msb = cid << 16;
        msb += type;
        return new UUID(msb, lsb);
    }

    private static int decodeContextID(UUID encoded) {
        long msb = encoded.getMostSignificantBits();
        return (int) msb >> 16;
    }

    private static int decodeType(UUID encoded) {
        long msb = encoded.getMostSignificantBits();
        long cid = msb >> 16;
        return (int) (msb - (cid << 16));
    }

    private static int decodeEntity(UUID encoded) {
        return (int) encoded.getLeastSignificantBits();
    }

}
