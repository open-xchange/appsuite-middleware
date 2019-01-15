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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.scheduling.changes.impl;

import static com.openexchange.java.Autoboxing.I;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.scheduling.common.Messages;
import com.openexchange.chronos.scheduling.common.Utils;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.html.HtmlService;
import com.openexchange.html.tools.HTMLUtils;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;

/**
 * {@link LabelHelper} - For external recipients
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - Adjusted to new stack
 */
public class LabelHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(LabelHelper.class);

    private static final String fallbackHostname;
    static {
        String fbHostname;
        try {
            fbHostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException e) {
            fbHostname = "localhost";
        }
        fallbackHostname = fbHostname;
    }

    private static final String CALENDAR = "calendar";

    private final Pattern patternSlashFixer = Pattern.compile("^//+|[^:]//+");

    final Event update;
    final CalendarUser originator;
    final CalendarUser recipient;

    final Locale locale;
    final TypeWrapper wrapper;

    private final String comment;
    private final int contextId;

    private final TimeZone timeZone;
    private final DelegationState delegationState;

    private final DateHelper dateHelper;
    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link LabelHelper}.
     * 
     * @param wrapper The {@link TypeWrapper}
     * @param serviceLookup
     * @param update The {@link Event} to generate the mail for
     * @param contextId The context identifer
     * @param originator The originator
     * @param recipient The recipient
     * @param comment The comment to set
     * @param locale The {@link Locale} of the recipient
     * @param timeZone The {@link TimeZone} of the recipient
     */
    public LabelHelper(TypeWrapper wrapper, ServiceLookup serviceLookup, Event update, int contextId, CalendarUser originator, CalendarUser recipient, String comment, Locale locale, TimeZone timeZone) {
        super();
        this.update = update;
        this.locale = locale;
        this.contextId = contextId;
        this.originator = originator;
        this.recipient = recipient;
        this.comment = comment;
        this.wrapper = wrapper;
        this.timeZone = timeZone;
        this.delegationState = getDelegationState(originator, recipient);
        this.dateHelper = new DateHelper(update, locale, timeZone);
        this.serviceLookup = serviceLookup;
    }

    private DelegationState getDelegationState(CalendarUser originator, CalendarUser recipient) {
        if (null != originator.getSentBy()) {
            if (CalendarUtils.matches(originator, recipient)) {
                return new OnMyBehalf();
            }
            return new OnBehalfOfAnother();
        }
        return new OnNoOnesBehalf();
    }

    public String getShowAs() {
        if (update.getTransp() != null && Transp.TRANSPARENT.equals(update.getTransp().getValue())) {
            return new Sentence(Messages.FREE).getMessage(locale);
        }
        return new Sentence(Messages.RESERVERD).getMessage(locale);
    }

    public String getShowAsClass() {
        if (update.getTransp() != null && update.getTransp().getValue() != null && Transp.TRANSPARENT.equals(update.getTransp().getValue())) {
            return "free";
        }
        return "reserved";
    }

    public String getNoteAsHTML() {
        final String note = update.getDescription();
        if (note == null) {
            return "";
        }
        HtmlService htmlService = serviceLookup.getOptionalService(HtmlService.class);
        if (null == htmlService) {
            return "";
        }
        return new HTMLUtils(htmlService).htmlFormat(note);
    }

    // Sentences
    public String getAcceptIntroduction() throws OXException {
        return delegationState.statusChange(originator, ParticipationStatus.ACCEPTED);
    }

    public String getDeclineIntroduction() throws OXException {
        return delegationState.statusChange(originator, ParticipationStatus.DECLINED);
    }

    public String getTentativeIntroduction() throws OXException {
        return delegationState.statusChange(originator, ParticipationStatus.TENTATIVE);
    }

    public String getNoneIntroduction() throws OXException {
        return delegationState.statusChange(originator, ParticipationStatus.NEEDS_ACTION);
    }

    public String getCounterOrganizerIntroduction() {
        return new Sentence(Messages.COUNTER_ORGANIZER_INTRO).add(originator.getCn(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
    }

    public String getCounterParticipantIntroduction() {
        return new Sentence(Messages.COUNTER_PARTICIPANT_INTRO).add(originator.getCn(), ArgumentType.PARTICIPANT).add(update.getOrganizer().getCn(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
    }

    public String getCreateIntroduction() throws OXException {
        return delegationState.getCreateIntroduction();
    }

    public String getCreateExceptionIntroduction() {
        return new Sentence(Messages.CREATE_EXCEPTION_INTRO).add(originator.getCn(), ArgumentType.PARTICIPANT).add(dateHelper.getRecurrenceDatePosition(), ArgumentType.UPDATED).getMessage(wrapper, locale);
    }

    public String getRefreshIntroduction() {
        return new Sentence(Messages.REFRESH_INTRO).add(originator.getCn(), ArgumentType.PARTICIPANT).add(update.getSummary(), ArgumentType.UPDATED).getMessage(wrapper, locale);
    }

    public String getDeclineCounterIntroduction() throws OXException {
        return delegationState.getDeclineCounterIntroduction();
    }

    public String getUpdateIntroduction() throws OXException {
        return delegationState.getUpdateIntroduction();
    }

    public String getComment() {
        if (Strings.isNotEmpty(comment)) {
            return comment;
        }
        ExtendedProperties extendedProperties = update.getExtendedProperties();
        if (null == extendedProperties || null != extendedProperties.get(CalendarParameters.PARAMETER_COMMENT).getValue()) {
            return "";
        }
        return new Sentence(Messages.COMMENT_INTRO).add(extendedProperties.get(CalendarParameters.PARAMETER_COMMENT).getValue(), ArgumentType.ITALIC).getMessage(wrapper, locale);
    }

    public String getDeleteIntroduction() throws OXException {
        return delegationState.getDeleteIntroduction();
    }

    public String getDirectLink() {
        if (recipient == null || false == Utils.isInternalCalendarUser(recipient)) {
            return null;
        }

        ConfigurationService config = serviceLookup.getOptionalService(ConfigurationService.class);
        if (null == config) {
            return "";
        }
        String template = patternSlashFixer.matcher(config.getProperty("object_link", "https://[hostname]/[uiwebpath]#m=[module]&i=[object]&f=[folder]")).replaceAll("/");
        String webpath = config.getProperty("com.openexchange.UIWebPath", "/appsuite/");
        if (webpath.startsWith("/")) {
            webpath = webpath.substring(1, webpath.length());
        }

        String objectId = update.getId();
        int recipientId = recipient.getEntity();
        String folder = null;
        try {
            folder = CalendarUtils.getFolderView(update, recipientId);
            folder = CalendarUtils.prependDefaultAccount(folder);
        } catch (OXException e) {
            LOGGER.error("Unable to generate Link. Folder Id for user {} can't be found.", Integer.valueOf(recipientId), e);
            return "";
        }

        String hostname = null;
        HostnameService hostnameService = serviceLookup.getOptionalService(HostnameService.class);
        UserService userService = serviceLookup.getOptionalService(UserService.class);
        if (hostnameService != null && null != userService) {
            try {
                User user = userService.getUser(recipientId, contextId);
                if (user.isGuest()) {
                    hostname = hostnameService.getGuestHostname(recipientId, contextId);
                } else {
                    hostname = hostnameService.getHostname(recipientId, contextId);
                }
            } catch (OXException e) {
                LOGGER.warn("Unable to retrive user with identifier {} from context {}. Using fallback hostname.", I(recipientId), I(contextId), e);
            }
        }

        if (hostname == null) {
            hostname = fallbackHostname;
        }

        if (objectId == null || folder == null) {
            LOGGER.error("Unable to generate Link. Either Object Id ({}) or Folder Id ({}) is null.", objectId, folder, new Throwable());
            return "";
        }

        return template.replaceAll("\\[hostname\\]", hostname).replaceAll("\\[uiwebpath\\]", webpath).replaceAll("\\[module\\]", CALENDAR).replaceAll("\\[object\\]", objectId).replaceAll("\\[folder\\]", folder);
    }

    public String getAttachmentNote() {
        if (update.getAttachments() == null || update.getAttachments().isEmpty() || false == Utils.isInternalCalendarUser(recipient)) {
            return "";
        }
        return new Sentence(Messages.HAS_ATTACHMENTS).add(getDirectLink(), ArgumentType.REFERENCE).getMessage(wrapper, locale);
    }

    public String getWhenLabel() {
        return new Sentence(Messages.LABEL_WHEN).getMessage(wrapper, locale);
    }

    public String getWhereLabel() {
        return new Sentence(Messages.LABEL_WHERE).getMessage(wrapper, locale);
    }

    public String getAttendeesLabel() {
        return new Sentence(Messages.LABEL_ATTENDEES).getMessage(wrapper, locale);
    }

    public String getResourcesLabel() {
        return new Sentence(Messages.LABEL_RESOURCES).getMessage(wrapper, locale);
    }

    public String getDetailsLabel() {
        return new Sentence(Messages.LABEL_DETAILS).getMessage(wrapper, locale);
    }

    public String getShowAsLabel() {
        return new Sentence(Messages.LABEL_SHOW_AS).getMessage(wrapper, locale);
    }

    public String getCreatedLabel() {
        return new Sentence(Messages.LABEL_CREATED).getMessage(wrapper, locale);
    }

    public String getDirectLinkLabel() {
        return new Sentence(Messages.LINK_LABEL).getMessage(wrapper, locale);
    }

    public String getModifiedLabel() {
        return new Sentence(Messages.LABEL_MODIFIED).getMessage(wrapper, locale);
    }

    public String getCreator() {
        return update.getOrganizer().getCn();
    }

    public String getModifier() {
        if (update.getModifiedBy() == null) {
            return "Unknown";
        }
        return update.getModifiedBy().getCn();
    }

    public String getTimezoneInfo() {
        return new Sentence(Messages.TIMEZONE).add(timeZone.getDisplayName(locale), ArgumentType.EMPHASIZED).getMessage(wrapper, locale);
    }

    public String getJustification() {
        //        if (recipient.hasRole(ITipRole.PRINCIPAL)) {
        //            return new Sentence(Messages.PRINCIPAL_JUSTIFICATION).getMessage(wrapper, locale);
        //        } else 
        if (CalendarUtils.matches(recipient, update.getOrganizer())) {
            return new Sentence(Messages.ORGANIZER_JUSTIFICATION).getMessage(wrapper, locale);
        } else if (Attendee.class.isAssignableFrom(recipient.getClass()) && CalendarUserType.RESOURCE.matches(((Attendee) recipient).getCuType())) {
            return new Sentence(Messages.RESOURCE_MANAGER_JUSTIFICATION).add(recipient.getCn(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }
        return null;
    }

    interface DelegationState {

        String statusChange(CalendarUser originator, ParticipationStatus none) throws OXException;

        String getDeleteIntroduction() throws OXException;

        String getUpdateIntroduction() throws OXException;

        String getDeclineCounterIntroduction() throws OXException;

        String getCreateIntroduction() throws OXException;

    }

    protected class OnMyBehalf implements DelegationState {

        @Override
        public String statusChange(CalendarUser originator, ParticipationStatus status) {

            String msg = null;
            String statusString = null;

            if (status.equals(ParticipationStatus.ACCEPTED)) {
                msg = Messages.ACCEPT_ON_YOUR_BEHALF_INTRO;
                statusString = "";
            } else if (status.equals(ParticipationStatus.DECLINED)) {
                msg = Messages.DECLINE_ON_YOUR_BEHALF_INTRO;
                statusString = "";
            } else if (status.equals(ParticipationStatus.TENTATIVE)) {
                msg = Messages.TENTATIVE_ON_YOUR_BEHALF_INTRO;
                statusString = "";
            } else {
                msg = Messages.NONE_ON_YOUR_BEHALF_INTRO;
                statusString = Messages.NONE;
            }
            return new Sentence(msg).add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT).add(statusString, ArgumentType.STATUS, status).getMessage(wrapper, locale);
        }

        @Override
        public String getDeleteIntroduction() {
            return new Sentence(Messages.DELETE_ON_YOUR_BEHALF_INTRO).add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }

        @Override
        public String getUpdateIntroduction() {
            return new Sentence(Messages.UPDATE_ON_YOUR_BEHALF_INTRO).add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }

        @Override
        public String getDeclineCounterIntroduction() {
            return "FIXME"; // This makes little sense
        }

        @Override
        public String getCreateIntroduction() {
            return new Sentence(Messages.CREATE_ON_YOUR_BEHALF_INTRO).add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }

    }

    protected class OnBehalfOfAnother implements DelegationState {

        @Override
        public String statusChange(CalendarUser originator, ParticipationStatus status) {
            String msg = null;
            String statusString = "";

            if (status.equals(ParticipationStatus.ACCEPTED)) {
                msg = Messages.ACCEPT_ON_BEHALF_INTRO;
            } else if (status.equals(ParticipationStatus.DECLINED)) {
                msg = Messages.DECLINE_ON_BEHALF_INTRO;
            } else if (status.equals(ParticipationStatus.TENTATIVE)) {
                msg = Messages.TENTATIVE_ON_BEHALF_INTRO;
            } else {
                msg = Messages.NONE_ON_BEHALF_INTRO;
                statusString = Messages.NONE;
            }
            return new Sentence(msg).add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT).add(statusString, ArgumentType.STATUS, status).add(originator.getCn(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }

        @Override
        public String getDeleteIntroduction() throws OXException {
            return new Sentence(Messages.DELETE_ON_BEHALF_INTRO).add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT).add(originator.getCn(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }

        @Override
        public String getUpdateIntroduction() throws OXException {
            return new Sentence(Messages.UPDATE_ON_BEHALF_INTRO).add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT).add(originator.getCn(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }

        @Override
        public String getDeclineCounterIntroduction() throws OXException {
            return new Sentence(Messages.DECLINECOUNTER_ON_BEHALF_INTRO).add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT).add(originator.getCn(), ArgumentType.PARTICIPANT).add(update.getSummary(), ArgumentType.UPDATED).getMessage(wrapper, locale);
        }

        @Override
        public String getCreateIntroduction() throws OXException {
            return new Sentence(Messages.CREATE_ON_BEHALF_INTRO).add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT).add(originator.getCn()).getMessage(wrapper, locale);
        }

    }

    protected class OnNoOnesBehalf implements DelegationState {

        @Override
        public String statusChange(CalendarUser originator, ParticipationStatus status) {
            String msg = null;
            String statusString = "";
            if (ParticipationStatus.ACCEPTED.equals(status) || ParticipationStatus.TENTATIVE.equals(status) || ParticipationStatus.DECLINED.equals(status)) {
                msg = Messages.STATUS_CHANGED_INTRO;
            } else {
                msg = Messages.NONE_INTRO;
                statusString = Messages.NONE;
            }
            return new Sentence(msg).add(originator.getCn(), ArgumentType.PARTICIPANT).add(statusString, ArgumentType.STATUS, status).getMessage(wrapper, locale);
        }

        @Override
        public String getDeleteIntroduction() {
            return new Sentence(Messages.DELETE_INTRO).add(originator.getCn(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }

        @Override
        public String getUpdateIntroduction() {
            return new Sentence(Messages.UPDATE_INTRO).add(originator.getCn(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }

        @Override
        public String getDeclineCounterIntroduction() {
            return new Sentence(Messages.DECLINECOUNTER_INTRO).add(originator.getCn(), ArgumentType.PARTICIPANT).add(update.getSummary(), ArgumentType.UPDATED).getMessage(wrapper, locale);
        }

        @Override
        public String getCreateIntroduction() {
            return new Sentence(Messages.CREATE_INTRO).add(originator.getCn(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }
    }

}
