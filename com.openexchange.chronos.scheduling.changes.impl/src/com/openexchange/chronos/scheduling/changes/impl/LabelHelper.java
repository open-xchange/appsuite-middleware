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

import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.ArgumentType;
import com.openexchange.chronos.itip.generators.DateHelper;
import com.openexchange.chronos.scheduling.RecipientSettings;
import com.openexchange.chronos.scheduling.common.Utils;
import com.openexchange.html.HtmlService;
import com.openexchange.html.tools.HTMLUtils;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;

/**
 * {@link LabelHelper} - For external recipients
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - Adjusted to new stack
 */
public class LabelHelper {

    final Event update;
    final CalendarUser originator;
    final CalendarUser recipient;

    final MessageContext messageContext;

    private final String comment;
    private final DelegationState delegationState;
    private final DateHelper dateHelper;
    private final ServiceLookup serviceLookup;
    private final Event seriesMaster;
    private final RecipientSettings recipientSettings;

    /**
     * Initializes a new {@link LabelHelper}.
     *
     * @param serviceLookup
     * @param update The {@link Event} to generate the mail for
     * @param seriesMaster The series master event if changes affect a recurrence instance, <code>null</code>, otherwise
     * @param originator The originator
     * @param recipientSettings The regional settings
     * @param comment The comment to set
     * @param messageContext The message context to use
     */
    public LabelHelper(ServiceLookup serviceLookup, Event update, Event seriesMaster, CalendarUser originator, RecipientSettings recipientSettings, String comment, MessageContext messageContext) {
        super();
        this.messageContext = messageContext;
        this.update = update;
        this.recipientSettings = recipientSettings;
        this.originator = originator;
        this.recipient = recipientSettings.getRecipient();
        this.comment = comment;
        this.seriesMaster = seriesMaster;
        this.delegationState = getDelegationState(originator, recipient);
        this.dateHelper = new DateHelper(update, recipientSettings.getLocale(), recipientSettings.getTimeZone(), recipientSettings.getRegionalSettings());
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

    private boolean useInstanceIntroduction() {
        return null != update.getRecurrenceId() && null != seriesMaster && Strings.isNotEmpty(seriesMaster.getSummary());
    }

    private String getStatusChangeIntroduction(ParticipationStatus status) {
        if (useInstanceIntroduction()) {
            return delegationState.statusChangeInstance(originator, status, seriesMaster.getSummary());
        }
        return delegationState.statusChange(originator, status);
    }

    public String getShowAs() {
        if (update.getTransp() != null && Transp.TRANSPARENT.equals(update.getTransp().getValue())) {
            return new SentenceImpl(Messages.FREE).getMessage(messageContext);
        }
        return new SentenceImpl(Messages.RESERVERD).getMessage(messageContext);
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
    public String getAcceptIntroduction() {
        return getStatusChangeIntroduction(ParticipationStatus.ACCEPTED);
    }

    public String getDeclineIntroduction() {
        return getStatusChangeIntroduction(ParticipationStatus.DECLINED);
    }

    public String getTentativeIntroduction() {
        return getStatusChangeIntroduction(ParticipationStatus.TENTATIVE);
    }

    public String getNoneIntroduction() {
        return getStatusChangeIntroduction(ParticipationStatus.NEEDS_ACTION);
    }

    public String getCounterOrganizerIntroduction() {
        return new SentenceImpl(Messages.COUNTER_ORGANIZER_INTRO).add(originator.getCn(), ArgumentType.PARTICIPANT).getMessage(messageContext);
    }

    public String getCounterParticipantIntroduction() {
        return new SentenceImpl(Messages.COUNTER_PARTICIPANT_INTRO).add(originator.getCn(), ArgumentType.PARTICIPANT).add(update.getOrganizer().getCn(), ArgumentType.PARTICIPANT).getMessage(messageContext);
    }

    public String getCreateIntroduction() {
        return delegationState.getCreateIntroduction();
    }

    public String getCreateExceptionIntroduction() {
        return new SentenceImpl(Messages.CREATE_EXCEPTION_INTRO).add(originator.getCn(), ArgumentType.PARTICIPANT).add(dateHelper.getRecurrenceDatePosition(), ArgumentType.UPDATED).getMessage(messageContext);
    }

    public String getRefreshIntroduction() {
        return new SentenceImpl(Messages.REFRESH_INTRO).add(originator.getCn(), ArgumentType.PARTICIPANT).add(update.getSummary(), ArgumentType.UPDATED).getMessage(messageContext);
    }

    public String getDeclineCounterIntroduction() {
        return delegationState.getDeclineCounterIntroduction();
    }

    public String getUpdateIntroduction() {
        if (useInstanceIntroduction()) {
            return delegationState.getUpdateInstanceIntroduction(seriesMaster.getSummary());
        }
        return delegationState.getUpdateIntroduction();
    }

    public String getComment() {
        if (Strings.isEmpty(comment)) {
            return null;
        }
        return new SentenceImpl(Messages.COMMENT_INTRO).add(comment, ArgumentType.ITALIC).getMessage(messageContext);
    }

    public String getDeleteIntroduction() {
        if (useInstanceIntroduction()) {
            return delegationState.getDeleteInstanceIntroduction(seriesMaster.getSummary());
        }
        return delegationState.getDeleteIntroduction();
    }

    public String getDirectLink() {
        return recipientSettings.getDirectLink(update);
    }

    public String getAttachmentNote() {
        if (update.getAttachments() == null || update.getAttachments().isEmpty() || false == Utils.isInternalCalendarUser(recipient)) {
            return "";
        }
        return new SentenceImpl(Messages.HAS_ATTACHMENTS).add(getDirectLink(), ArgumentType.REFERENCE).getMessage(messageContext);
    }

    public String getWhenLabel() {
        return new SentenceImpl(Messages.LABEL_WHEN).getMessage(messageContext);
    }

    public String getWhereLabel() {
        return new SentenceImpl(Messages.LABEL_WHERE).getMessage(messageContext);
    }

    public String getConferencesLabel() {
        return new SentenceImpl(Messages.LABEL_CONFERENCES).getMessage(messageContext);
    }

    public String getParticipantsLabel() {
        return new SentenceImpl(Messages.LABEL_PARTICIPANTS).getMessage(messageContext);
    }

    public String getResourcesLabel() {
        return new SentenceImpl(Messages.LABEL_RESOURCES).getMessage(messageContext);
    }

    public String getDetailsLabel() {
        return new SentenceImpl(Messages.LABEL_DETAILS).getMessage(messageContext);
    }

    public String getShowAsLabel() {
        return new SentenceImpl(Messages.LABEL_SHOW_AS).getMessage(messageContext);
    }

    public String getCreatedLabel() {
        return new SentenceImpl(Messages.LABEL_CREATED).getMessage(messageContext);
    }

    public String getDirectLinkLabel() {
        return new SentenceImpl(Messages.LINK_LABEL).getMessage(messageContext);
    }

    public String getModifiedLabel() {
        return new SentenceImpl(Messages.LABEL_MODIFIED).getMessage(messageContext);
    }

    public String getCreator() {
        return Utils.getDisplayName(update.getOrganizer());
    }

    public String getModifier() {
        if (update.getModifiedBy() == null) {
            return "Unknown";
        }
        return Utils.getDisplayName(update.getModifiedBy());
    }

    public String getTimezoneInfo() {
        String displayName = messageContext.getTimeZone().getDisplayName(messageContext.getLocale());
        return new SentenceImpl(Messages.TIMEZONE).add(displayName, ArgumentType.EMPHASIZED).getMessage(messageContext);
    }

    public String getJustification() {
        //        if (recipient.hasRole(ITipRole.PRINCIPAL)) {
        //            return new Sentence(Messages.PRINCIPAL_JUSTIFICATION).getMessage(messageContext);
        //        } else
        if (CalendarUtils.matches(recipient, update.getOrganizer())) {
            return new SentenceImpl(Messages.ORGANIZER_JUSTIFICATION).getMessage(messageContext);
        } else if (Attendee.class.isAssignableFrom(recipient.getClass()) && CalendarUserType.RESOURCE.matches(((Attendee) recipient).getCuType())) {
            return new SentenceImpl(Messages.RESOURCE_MANAGER_JUSTIFICATION).add(recipient.getCn(), ArgumentType.PARTICIPANT).getMessage(messageContext);
        }
        return null;
    }

    interface DelegationState {

        String statusChange(CalendarUser originator, ParticipationStatus none);

        String statusChangeInstance(CalendarUser originator, ParticipationStatus none, String ofSeries);

        String getDeleteIntroduction();

        String getDeleteInstanceIntroduction(String ofSeries);

        String getUpdateIntroduction();

        String getUpdateInstanceIntroduction(String ofSeries);

        String getDeclineCounterIntroduction();

        String getCreateIntroduction();

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
            return new SentenceImpl(msg).add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT).add(statusString, ArgumentType.STATUS, status).getMessage(messageContext);
        }

        @Override
        public String statusChangeInstance(CalendarUser originator, ParticipationStatus status, String ofSeries) {
            String msg;
            String statusString;
            if (ParticipationStatus.ACCEPTED.matches(status)) {
                msg = Messages.ACCEPT_INSTANCE_ON_YOUR_BEHALF_INTRO;
                statusString = "";
            } else if (ParticipationStatus.DECLINED.matches(status)) {
                msg = Messages.DECLINE_INSTANCE_ON_YOUR_BEHALF_INTRO;
                statusString = "";
            } else if (ParticipationStatus.TENTATIVE.matches(status)) {
                msg = Messages.TENTATIVE_INSTANCE_ON_YOUR_BEHALF_INTRO;
                statusString = "";
            } else {
                msg = Messages.NONE_INSTANCE_ON_YOUR_BEHALF_INTRO;
                statusString = Messages.NONE;
            }
            return new SentenceImpl(msg)
                .add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT)
                .add(statusString, ArgumentType.STATUS, status)
                .add(ofSeries, ArgumentType.ITALIC)
                .getMessage(messageContext)
            ;
        }

        @Override
        public String getDeleteIntroduction() {
            return new SentenceImpl(Messages.DELETE_ON_YOUR_BEHALF_INTRO).add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT).getMessage(messageContext);
        }

        @Override
        public String getDeleteInstanceIntroduction(String ofSeries) {
            return new SentenceImpl(Messages.DELETE_INSTANCE_ON_YOUR_BEHALF_INTRO)
                .add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT)
                .add(ofSeries, ArgumentType.ITALIC)
                .getMessage(messageContext)
            ;
        }

        @Override
        public String getUpdateIntroduction() {
            return new SentenceImpl(Messages.UPDATE_ON_YOUR_BEHALF_INTRO).add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT).getMessage(messageContext);
        }

        @Override
        public String getUpdateInstanceIntroduction(String ofSeries) {
            return new SentenceImpl(Messages.UPDATE_INSTANCE_ON_YOUR_BEHALF_INTRO)
                .add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT)
                .add(ofSeries, ArgumentType.ITALIC)
                .getMessage(messageContext)
            ;
        }

        @Override
        public String getDeclineCounterIntroduction() {
            return "FIXME"; // This makes little sense
        }

        @Override
        public String getCreateIntroduction() {
            return new SentenceImpl(Messages.CREATE_ON_YOUR_BEHALF_INTRO).add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT).getMessage(messageContext);
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
            return new SentenceImpl(msg)
                .add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT)
                .add(statusString, ArgumentType.STATUS, status)
                .add(originator.getCn(), ArgumentType.PARTICIPANT)
                .getMessage(messageContext);
        }

        @Override
        public String statusChangeInstance(CalendarUser originator, ParticipationStatus status, String ofSeries) {
            String msg;
            String statusString;
            if (ParticipationStatus.ACCEPTED.matches(status)) {
                msg = Messages.ACCEPT_INSTANCE_ON_BEHALF_INTRO;
                statusString = "";
            } else if (ParticipationStatus.DECLINED.matches(status)) {
                msg = Messages.DECLINE_INSTANCE_ON_BEHALF_INTRO;
                statusString = "";
            } else if (ParticipationStatus.TENTATIVE.matches(status)) {
                msg = Messages.TENTATIVE_INSTANCE_ON_BEHALF_INTRO;
                statusString = "";
            } else {
                msg = Messages.NONE_INSTANCE_ON_BEHALF_INTRO;
                statusString = Messages.NONE;
            }
            return new SentenceImpl(msg)
                .add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT)
                .add(statusString, ArgumentType.STATUS, status)
                .add(ofSeries, ArgumentType.ITALIC)
                .add(originator.getCn(), ArgumentType.PARTICIPANT)
                .getMessage(messageContext)
            ;
        }

        @Override
        public String getDeleteIntroduction() {
            return new SentenceImpl(Messages.DELETE_ON_BEHALF_INTRO).add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT).add(originator.getCn(), ArgumentType.PARTICIPANT).getMessage(messageContext);
        }

        @Override
        public String getDeleteInstanceIntroduction(String ofSeries) {
            return new SentenceImpl(Messages.DELETE_INSTANCE_ON_BEHALF_INTRO)
                .add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT)
                .add(ofSeries, ArgumentType.ITALIC)
                .add(originator.getCn(), ArgumentType.PARTICIPANT)
                .getMessage(messageContext)
            ;
        }

        @Override
        public String getUpdateIntroduction() {
            return new SentenceImpl(Messages.UPDATE_ON_BEHALF_INTRO).add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT).add(originator.getCn(), ArgumentType.PARTICIPANT).getMessage(messageContext);
        }

        @Override
        public String getUpdateInstanceIntroduction(String ofSeries) {
            return new SentenceImpl(Messages.UPDATE_INSTANCE_ON_BEHALF_INTRO)
                .add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT)
                .add(ofSeries, ArgumentType.ITALIC)
                .add(originator.getCn(), ArgumentType.PARTICIPANT)
                .getMessage(messageContext);
        }

        @Override
        public String getDeclineCounterIntroduction() {
            return new SentenceImpl(Messages.DECLINECOUNTER_ON_BEHALF_INTRO).add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT).add(originator.getCn(), ArgumentType.PARTICIPANT).add(update.getSummary(), ArgumentType.UPDATED).getMessage(messageContext);
        }

        @Override
        public String getCreateIntroduction() {
            return new SentenceImpl(Messages.CREATE_ON_BEHALF_INTRO).add(originator.getSentBy().getCn(), ArgumentType.PARTICIPANT).add(originator.getCn()).getMessage(messageContext);
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
            return new SentenceImpl(msg).add(originator.getCn(), ArgumentType.PARTICIPANT).add(statusString, ArgumentType.STATUS, status).getMessage(messageContext);
        }

        @Override
        public String statusChangeInstance(CalendarUser originator, ParticipationStatus status, String ofSeries) {
            String msg;
            String statusString;
            if (ParticipationStatus.ACCEPTED.matches(status) || ParticipationStatus.DECLINED.matches(status) || ParticipationStatus.TENTATIVE.matches(status)) {
                msg = Messages.STATUS_CHANGED_INSTANCE_INTRO;
                statusString = "";
            } else {
                msg = Messages.NONE_INSTANCE_INTRO;
                statusString = Messages.NONE;
            }
            return new SentenceImpl(msg)
                .add(originator.getCn(), ArgumentType.PARTICIPANT)
                .add(statusString, ArgumentType.STATUS, status)
                .add(ofSeries, ArgumentType.ITALIC)
                .getMessage(messageContext)
            ;
        }
        @Override
        public String getDeleteIntroduction() {
            return new SentenceImpl(Messages.DELETE_INTRO).add(originator.getCn(), ArgumentType.PARTICIPANT).getMessage(messageContext);
        }

        @Override
        public String getDeleteInstanceIntroduction(String ofSeries) {
            return new SentenceImpl(Messages.DELETE_INSTANCE_INTRO)
                .add(originator.getCn(), ArgumentType.PARTICIPANT)
                .add(ofSeries, ArgumentType.ITALIC)
                .getMessage(messageContext)
            ;
        }

        @Override
        public String getUpdateIntroduction() {
            return new SentenceImpl(Messages.UPDATE_INTRO).add(originator.getCn(), ArgumentType.PARTICIPANT).getMessage(messageContext);
        }

        @Override
        public String getUpdateInstanceIntroduction(String ofSeries) {
            return new SentenceImpl(Messages.UPDATE_INSTANCE_INTRO)
                .add(originator.getCn(), ArgumentType.PARTICIPANT)
                .add(ofSeries, ArgumentType.ITALIC)
                .getMessage(messageContext);
        }

        @Override
        public String getDeclineCounterIntroduction() {
            return new SentenceImpl(Messages.DECLINECOUNTER_INTRO).add(originator.getCn(), ArgumentType.PARTICIPANT).add(update.getSummary(), ArgumentType.UPDATED).getMessage(messageContext);
        }

        @Override
        public String getCreateIntroduction() {
            return new SentenceImpl(Messages.CREATE_INTRO).add(originator.getCn(), ArgumentType.PARTICIPANT).getMessage(messageContext);
        }
    }

}
