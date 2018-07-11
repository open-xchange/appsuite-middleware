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

package com.openexchange.chronos.itip.generators;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.itip.ITipRole;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.html.HtmlService;
import com.openexchange.html.tools.HTMLUtils;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;

/**
 * {@link LabelHelper}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class LabelHelper {

    private final Context ctx;
    private final DateHelper dateHelper;
    private final UserService users;
    private final HTMLUtils html;
    private TimeZone timezone;
    private DelegationState delegationState;
    private final ServiceLookup services;
    private final Pattern patternSlashFixer;
    private static final String fallbackHostname;
    
    private static final Logger LOG = LoggerFactory.getLogger(LabelHelper.class);

    protected final NotificationMail mail;
    protected final TypeWrapper wrapper;
    protected final Locale locale;
    
    static {
        String fbHostname;
        try {
            fbHostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException e) {
            fbHostname = "localhost";
        }
        fallbackHostname = fbHostname;
    }

    public LabelHelper(final DateHelper dateHelper, final TimeZone timezone, final NotificationMail mail, final Locale locale, final Context ctx, final TypeWrapper wrapper, final ServiceLookup services) throws OXException {
        super();
        this.services = services;
        this.mail = mail;
        this.locale = locale;
        this.ctx = ctx;
        this.wrapper = wrapper;
        this.dateHelper = dateHelper;
        this.users = services.getService(UserService.class);
        this.html = new HTMLUtils(services.getService(HtmlService.class));
        this.timezone = timezone;
        if (timezone == null) {
            this.timezone = TimeZone.getDefault(); // Fallback
        }

        if (mail.actionIsDoneOnMyBehalf()) {
            delegationState = new OnMyBehalf();
        } else if (mail.actionIsDoneOnBehalfOfAnother()) {
            delegationState = new OnBehalfOfAnother();
        } else {
            delegationState = new OnNoOnesBehalf();
        }

        patternSlashFixer = Pattern.compile("^//+|[^:]//+");
    }

    public String getShowAs() {
        final Event event = mail.getEvent();

        if (event.getTransp() != null && Transp.TRANSPARENT.equals(event.getTransp().getValue())) {
            return new Sentence(Messages.FREE).getMessage(locale);
        } else {
            return new Sentence(Messages.RESERVERD).getMessage(locale);
        }
    }

    public String getShowAsClass() {
        final Event event = mail.getEvent();
        if (event.getTransp() != null && event.getTransp().getValue() != null && Transp.TRANSPARENT.equals(event.getTransp().getValue())) {
            return "free";
        } else {
            return "reserved";
        }
    }

    public String getNoteAsHTML() {
        final String note = mail.getEvent().getDescription();
        if (note == null) {
            return "";
        }
        return html.htmlFormat(note);
    }

    // Sentences
    public String getAcceptIntroduction() throws OXException {
        if (mail.getActor().isVirtual()) {
            return "";
        }

        return delegationState.statusChange(mail.getActor(), ParticipationStatus.ACCEPTED);
    }

    public String getDeclineIntroduction() throws OXException {
        if (mail.getActor().isVirtual()) {
            return "";
        }
        return delegationState.statusChange(mail.getActor(), ParticipationStatus.DECLINED);
    }

    public String getTentativeIntroduction() throws OXException {
        if (mail.getActor().isVirtual()) {
            return "";
        }
        return delegationState.statusChange(mail.getActor(), ParticipationStatus.TENTATIVE);
    }

    public String getNoneIntroduction() throws OXException {
        if (mail.getActor().isVirtual()) {
            return "";
        }
        return delegationState.statusChange(mail.getActor(), ParticipationStatus.NEEDS_ACTION);
    }

    public String getCounterOrganizerIntroduction() {
        if (mail.getActor().isVirtual()) {
            return "";
        }
        return new Sentence(Messages.COUNTER_ORGANIZER_INTRO).add(mail.getSender().getDisplayName(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
    }

    public String getCounterParticipantIntroduction() {
        if (mail.getActor().isVirtual()) {
            return "";
        }
        return new Sentence(Messages.COUNTER_PARTICIPANT_INTRO).add(mail.getSender().getDisplayName(), ArgumentType.PARTICIPANT).add(mail.getOrganizer().getDisplayName(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
    }

    public String getCreateIntroduction() throws OXException {
        if (mail.getActor().isVirtual()) {
            return "";
        }
        return delegationState.getCreateIntroduction();
    }

    public String getCreateExceptionIntroduction() {
        if (mail.getActor().isVirtual()) {
            return "";
        }
        return new Sentence(Messages.CREATE_EXCEPTION_INTRO).add(mail.getSender().getDisplayName(), ArgumentType.PARTICIPANT).add(dateHelper.getRecurrenceDatePosition(), ArgumentType.UPDATED).getMessage(wrapper, locale);
    }

    public String getRefreshIntroduction() {
        if (mail.getActor().isVirtual()) {
            return "";
        }
        return new Sentence(Messages.REFRESH_INTRO).add(mail.getSender().getDisplayName(), ArgumentType.PARTICIPANT).add(mail.getEvent().getSummary(), ArgumentType.UPDATED).getMessage(wrapper, locale);
    }

    public String getDeclineCounterIntroduction() throws OXException {
        if (mail.getActor().isVirtual()) {
            return "";
        }
        return delegationState.getDeclineCounterIntroduction();
    }

    public String getUpdateIntroduction() throws OXException {
        if (mail.getActor().isVirtual()) {
            return "";
        }
        return delegationState.getUpdateIntroduction();
    }
    
    public String getComment() {
        if (mail.getActor().isVirtual()) {
            return "";
        }
        if (mail.getMessage() == null || Strings.isEmpty(mail.getMessage().getComment())) {
            return "";
        }
        return new Sentence(Messages.COMMENT_INTRO).add(mail.getMessage().getComment(), ArgumentType.ITALIC).getMessage(wrapper, locale);
    }

    public String getDirectLink() {
        if (mail.getRecipient().isExternal() || mail.getRecipient().isResource()) {
            return null;
        }
        final ConfigurationService config = services.getService(ConfigurationService.class);
        final String template = patternSlashFixer.matcher(config.getProperty("object_link", "https://[hostname]/[uiwebpath]#m=[module]&i=[object]&f=[folder]")).replaceAll("/");
        String webpath = config.getProperty("com.openexchange.UIWebPath", "/appsuite/");
        if (webpath.startsWith("/")) {
            webpath = webpath.substring(1, webpath.length());
        }
        final String objectId = (mail.getEvent() != null) ? mail.getEvent().getId() : mail.getOriginal().getId();
        final String module = "calendar";
        String folder = mail.getRecipient().getFolderId();
        int recipientId = mail.getRecipient().getIdentifier();
        if (folder == null) {
            try {
                folder = CalendarUtils.getFolderView(mail.getEvent(), recipientId);
            } catch (OXException e) {
                LOG.error("Unable to generate Link. Folder Id for user {} can't be found.", Integer.valueOf(recipientId), e);
                return "";
            }
        }

        String hostname = null;

        final HostnameService hostnameService = services.getOptionalService(HostnameService.class);
        if (hostnameService != null) {
            if (null != mail.getRecipient().getUser() && mail.getRecipient().getUser().isGuest()) {
                hostname = hostnameService.getGuestHostname(recipientId, ctx.getContextId());
            } else {
                hostname = hostnameService.getHostname(recipientId, ctx.getContextId());
            }
        }

        if (hostname == null) {
            hostname = fallbackHostname;
        }

        if (objectId == null || folder == null) {
            LOG.error("Unable to generate Link. Either Object Id ({}) or Folder Id ({}) is null.", objectId, folder, new Throwable());
            return "";
        }

        return template.replaceAll("\\[hostname\\]", hostname).replaceAll("\\[uiwebpath\\]", webpath).replaceAll("\\[module\\]", module).replaceAll("\\[object\\]", objectId).replaceAll("\\[folder\\]", folder);
    }

    public String getAttachmentNote() {
        if (mail.getAttachments().isEmpty() || mail.getRecipient().isExternal()) {
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

    public String getParticipantsLabel() {
        return new Sentence(Messages.LABEL_PARTICIPANTS).getMessage(wrapper, locale);
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

    public String getDeleteIntroduction() throws OXException {
        return delegationState.getDeleteIntroduction();
    }

    public String getCreator() {
        return mail.getOrganizer().getDisplayName();
    }

    public String getModifier() throws OXException {
        if (mail.getEvent().getModifiedBy() == null) {
            return "Unknown";
        }
        return users.getUser(mail.getEvent().getModifiedBy().getEntity(), ctx).getDisplayName();
    }

    public String getTimezoneInfo() {
        return new Sentence(Messages.TIMEZONE).add(timezone.getDisplayName(locale), ArgumentType.EMPHASIZED).getMessage(wrapper, locale);
    }

    public String getJustification() {
        final NotificationParticipant recipient = mail.getRecipient();
        if (recipient.hasRole(ITipRole.PRINCIPAL)) {
            return new Sentence(Messages.PRINCIPAL_JUSTIFICATION).getMessage(wrapper, locale);
        } else if (recipient.hasRole(ITipRole.ORGANIZER)) {
            return new Sentence(Messages.ORGANIZER_JUSTIFICATION).getMessage(wrapper, locale);
        } else if (recipient.isResource()) {
            return new Sentence(Messages.RESOURCE_MANAGER_JUSTIFICATION).add(recipient.getDisplayName(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }
        return null;
    }

    // Utilities

    protected interface DelegationState {

        String statusChange(NotificationParticipant actor, ParticipationStatus none) throws OXException;

        String getDeleteIntroduction() throws OXException;

        String getUpdateIntroduction() throws OXException;

        String getDeclineCounterIntroduction() throws OXException;

        String getCreateIntroduction() throws OXException;

    }

    protected class OnMyBehalf implements DelegationState {

        @Override
        public String statusChange(final NotificationParticipant participant, final ParticipationStatus status) {

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
            return new Sentence(msg).add(participant.getDisplayName(), ArgumentType.PARTICIPANT).add(statusString, ArgumentType.STATUS, status).getMessage(wrapper, locale);
        }

        @Override
        public String getDeleteIntroduction() {
            return new Sentence(Messages.DELETE_ON_YOUR_BEHALF_INTRO).add(mail.getActor().getDisplayName(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }

        @Override
        public String getUpdateIntroduction() {
            return new Sentence(Messages.UPDATE_ON_YOUR_BEHALF_INTRO).add(mail.getActor().getDisplayName(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }

        @Override
        public String getDeclineCounterIntroduction() {
            return "FIXME"; // This makes little sense
        }

        @Override
        public String getCreateIntroduction() {
            return new Sentence(Messages.CREATE_ON_YOUR_BEHALF_INTRO).add(mail.getActor().getDisplayName(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }

    }

    protected class OnBehalfOfAnother implements DelegationState {

        @Override
        public String statusChange(final NotificationParticipant participant, final ParticipationStatus status) throws OXException {
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
            return new Sentence(msg).add(participant.getDisplayName(), ArgumentType.PARTICIPANT).add(statusString, ArgumentType.STATUS, status).add(mail.getOnBehalfOf().getDisplayName(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }

        @Override
        public String getDeleteIntroduction() throws OXException {
            return new Sentence(Messages.DELETE_ON_BEHALF_INTRO).add(mail.getActor().getDisplayName(), ArgumentType.PARTICIPANT).add(mail.getOnBehalfOf().getDisplayName(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }

        @Override
        public String getUpdateIntroduction() throws OXException {
            return new Sentence(Messages.UPDATE_ON_BEHALF_INTRO).add(mail.getActor().getDisplayName(), ArgumentType.PARTICIPANT).add(mail.getOnBehalfOf().getDisplayName(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }

        @Override
        public String getDeclineCounterIntroduction() throws OXException {
            return new Sentence(Messages.DECLINECOUNTER_ON_BEHALF_INTRO).add(mail.getActor().getDisplayName(), ArgumentType.PARTICIPANT).add(mail.getOnBehalfOf().getDisplayName(), ArgumentType.PARTICIPANT).add(mail.getEvent().getSummary(), ArgumentType.UPDATED).getMessage(wrapper, locale);
        }

        @Override
        public String getCreateIntroduction() throws OXException {
            return new Sentence(Messages.CREATE_ON_BEHALF_INTRO).add(mail.getActor().getDisplayName(), ArgumentType.PARTICIPANT).add(mail.getOnBehalfOf().getDisplayName()).getMessage(wrapper, locale);
        }

    }

    protected class OnNoOnesBehalf implements DelegationState {

        @Override
        public String statusChange(final NotificationParticipant participant, final ParticipationStatus status) {
            String msg = null;
            String statusString = "";
            if (ParticipationStatus.ACCEPTED.equals(status) || ParticipationStatus.TENTATIVE.equals(status) || ParticipationStatus.DECLINED.equals(status)) {
                msg = Messages.STATUS_CHANGED_INTRO;
            } else {
                msg = Messages.NONE_INTRO;
                statusString = Messages.NONE;
            }
            return new Sentence(msg).add(participant.getDisplayName(), ArgumentType.PARTICIPANT).add(statusString, ArgumentType.STATUS, status).getMessage(wrapper, locale);
        }

        @Override
        public String getDeleteIntroduction() {
            return new Sentence(Messages.DELETE_INTRO).add(mail.getActor().getDisplayName(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }

        @Override
        public String getUpdateIntroduction() {
            return new Sentence(Messages.UPDATE_INTRO).add(mail.getActor().getDisplayName(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }

        @Override
        public String getDeclineCounterIntroduction() {
            return new Sentence(Messages.DECLINECOUNTER_INTRO).add(mail.getActor().getDisplayName(), ArgumentType.PARTICIPANT).add(mail.getEvent().getSummary(), ArgumentType.UPDATED).getMessage(wrapper, locale);
        }

        @Override
        public String getCreateIntroduction() {
            return new Sentence(Messages.CREATE_INTRO).add(mail.getActor().getDisplayName(), ArgumentType.PARTICIPANT).getMessage(wrapper, locale);
        }

    }

}
