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

package com.openexchange.calendar.printing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringEscapeUtils;
import com.openexchange.calendar.printing.days.CalendarTools;
import com.openexchange.exception.OXException;
import com.openexchange.folder.FolderService;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.user.UserService;

/**
 * The view of an appointment. A dumbed-down version without recurrence pattern, day spanning and so on.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CPAppointment {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CPAppointment.class);

    private String title, description, location;

    private Date startDate, endDate;

    private Appointment original;

    private final CPCalendar cal;

    private final Context context;

    private int colorLabel;

    public CPAppointment() {
        super();
        this.cal = null;
        this.context = null;
    }

    public CPAppointment(final Appointment mother) {
        this(mother, null, null);
    }

    public CPAppointment(final Appointment mother, final CPCalendar cal, final Context context) {
        super();
        this.cal = cal;
        this.context = context;
        setTitle(mother.getTitle());
        setDescription(mother.getNote());
        setLocation(mother.getLocation());
        setStartDate(mother.getStartDate());
        setEndDate(mother.getEndDate());
        setOriginal(mother);
        setColorLabel(loadColorLabel(mother, context));
    }

    private int loadColorLabel(Appointment mother, Context context) {
        int appointmentColor = mother.getLabel();
        if (appointmentColor == 0) {
            if (null != context) {
                appointmentColor = getColorFromFolder(mother.getParentFolderID(), context.getContextId());
            } else if (CalendarDataObject.class.isInstance(mother)) {
                appointmentColor = getColorFromFolder(mother.getParentFolderID(), ((CalendarDataObject) mother).getContextID());
            }
        } 
        return appointmentColor;
    }

    private int getColorFromFolder(int parentFolderID, int contextId) {
        int resultColor = 1;
        try {
            final FolderService folderService = CPServiceRegistry.getInstance().getService(FolderService.class, true);
            FolderObject folderObject = folderService.getFolderObject(parentFolderID, contextId);
            Map<String, Object> meta = folderObject.getMeta();
            if (null != meta) {
                Object metaColor = meta.get("color_label");
                if (null != metaColor && Integer.class.isInstance(metaColor)) {
                    resultColor = ((Integer) metaColor).intValue();
                }
            }
        } catch (OXException e) {
            if (e.getCode() == ServiceExceptionCode.SERVICE_UNAVAILABLE.getNumber()) {
                LOG.error("", e);
            } else {
                LOG.error(String.format("Failed to load FolderObject for folderId: %1$s an contextId: %2$s", parentFolderID, contextId), e);
            }
            
        }
        return resultColor;
    }

    private void setColorLabel(int label) {
        this.colorLabel = label;
    }

    public String getColorLabel() {
        return StringEscapeUtils.escapeHtml(Integer.toString(colorLabel));
    }

    public String getTitle() {
        final String retval;
        if (null == title) {
            retval = "";
        } else {
            retval = StringEscapeUtils.escapeHtml(title);
        }
        return retval;
    }

    public String getDescription() {
        return StringEscapeUtils.escapeHtml(description);
    }

    public String getLocation() {
        return StringEscapeUtils.escapeHtml(location);
    }

    public Date getStartDate() {
        return startDate;
    }

    public String format(final String pattern, final Date date) {
        return cal.format(pattern, date);
    }

    public long getStartMinutes() {
        return (startDate.getTime() - CalendarTools.getDayStart(cal, startDate).getTime()) / Constants.MILLI_MINUTE;
    }

    public Date getEndDate() {
        return endDate;
    }

    public long getDurationInMinutes() {
        return (endDate.getTime() - startDate.getTime()) / Constants.MILLI_MINUTE;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public void setStartDate(final Date start) {
        this.startDate = start;
    }

    public void setEndDate(final Date end) {
        this.endDate = end;
    }

    public List<String> getParticipants() {
        final List<String> retval = new ArrayList<String>();
        for (final Participant participant : original.getParticipants()) {
            switch (participant.getType()) {
                case Participant.USER:
                    try {
                        final UserService userService = CPServiceRegistry.getInstance().getService(UserService.class, true);
                        retval.add(userService.getUser(participant.getIdentifier(), context).getDisplayName());
                    } catch (final OXException e) {
                        LOG.error("", e);
                    }
                    break;
                case Participant.GROUP:
                    try {
                        final GroupService service = CPServiceRegistry.getInstance().getService(GroupService.class, true);
                        retval.add(service.getGroup(context, participant.getIdentifier()).getDisplayName());
                    } catch (final OXException e) {
                        LOG.error("", e);
                    }
                    break;
                case Participant.EXTERNAL_USER:
                    if (null != participant.getDisplayName()) {
                        retval.add(participant.getDisplayName());
                    } else {
                        retval.add(participant.getEmailAddress());
                    }
                    break;
                default:
                    break;
            }
        }
        return retval;
    }

    public void setOriginal(final Appointment original) {
        this.original = original;
    }

    public Appointment getOriginal() {
        return original;
    }

    @Override
    public String toString() {
        return getStartDate() + " - " + getEndDate() + ": " + getTitle();
    }

}
