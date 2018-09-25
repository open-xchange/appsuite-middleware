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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.ajax.chronos.factory;

import java.util.ArrayList;
import java.util.List;
import javax.activation.MimetypesFileTypeMap;
import com.openexchange.testing.httpclient.models.Alarm;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.ChronosAttachment;
import com.openexchange.testing.httpclient.models.Trigger;
import com.openexchange.testing.httpclient.models.Trigger.RelatedEnum;

/**
 * {@link AlarmFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class AlarmFactory {

    public enum AlarmAction {
        DISPLAY, AUDIO, EMAIL;
    }

    /**
     * Creates a new single display {@link Alarm} with the specified duration and {@link RelatedEnum#START} trigger
     *
     * @param duration The duration of the {@link Alarm}
     * @return The new {@link Alarm}
     */
    public static Alarm createDisplayAlarm(String duration) {
        return createAlarm(duration, RelatedEnum.START, AlarmAction.DISPLAY);
    }

    /**
     * Creates an audio alarm with the specified duration and {@link RelatedEnum}
     *
     * @param duration The duration of the {@link Alarm}
     * @param audioFileUri The URI for the audio file to be played when the alarm is triggered
     * @return The new {@link Alarm}
     */
    public static Alarm createAudioAlarm(String duration, String audioFileUri) {
        List<ChronosAttachment> attachments = new ArrayList<>(1);
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        String mimeType = mimeTypesMap.getContentType(audioFileUri);

        ChronosAttachment attachment = new ChronosAttachment();
        attachment.setUri(audioFileUri);
        attachment.setFmtType(mimeType);
        attachments.add(attachment);

        Alarm audioAlarm = createAlarm(duration, AlarmAction.AUDIO);
        audioAlarm.setAttachments(attachments);

        return audioAlarm;
    }

    /**
     * Creates an e-mail alarm with the specified duration, description and summary
     *
     * @param duration The duration of the {@link Alarm}
     * @param mailAddress The e-mail address to send the notification
     * @param description The description of the alarm (used as the body of the mail notification)
     * @param summary The summary of the alarm (used as the subject in the email notification)
     * @return the new {@link Alarm}
     */
    public static Alarm createMailAlarm(String duration, String mailAddress, String description, String summary) {


        Alarm mailAlarm = createAlarm(duration, AlarmAction.EMAIL);
        mailAlarm.setDescription(description);
        mailAlarm.setSummary(summary);

        if (mailAddress != null) {
            List<Attendee> attendees = new ArrayList<>(1);
            Attendee attendee = new Attendee();
            attendee.setUri("mailto:" + mailAddress);
            attendee.setEmail(mailAddress);
            attendees.add(attendee);
            mailAlarm.setAttendees(attendees);
        } else {
            mailAlarm.setAttendees(null);
        }

        return mailAlarm;
    }

    /**
     * Creates a new single display {@link Alarm} with the specified duration and {@link RelatedEnum} trigger
     *
     * @param duration The duration of the {@link Alarm}
     * @param related The {@link RelatedEnum} trigger
     * @return The new {@link Alarm}
     */
    public static Alarm createAlarm(String duration, RelatedEnum related) {
        return createAlarm(duration, related, AlarmAction.DISPLAY);
    }

    /**
     * Creates a new single {@link Alarm} with the specified {@link AlarmAction}, duration and {@link RelatedEnum#START} trigger
     *
     * @param duration The duration of the {@link Alarm}
     * @param alarmAction The {@link AlarmAction}
     * @return The new {@link Alarm}
     */
    public static Alarm createAlarm(String duration, AlarmAction alarmAction) {
        return createAlarm(duration, RelatedEnum.START, alarmAction);
    }

    /**
     * Creates a new single display {@link Alarm} with the specified duration and {@link RelatedEnum} trigger
     *
     * @param duration The duration of the {@link Alarm}
     * @param related The {@link RelatedEnum} trigger
     * @param alarmAction The {@link AlarmAction}
     * @return The new {@link Alarm}
     */
    public static Alarm createAlarm(String duration, RelatedEnum related, AlarmAction alarmAction) {
        Trigger trigger = new Trigger();
        trigger.setRelated(related == null ? RelatedEnum.START : related);
        trigger.setDuration(duration);

        Alarm alarm = new Alarm();
        alarm.setAction(alarmAction.name());
        alarm.setTrigger(trigger);
        alarm.setDescription("This is the display message!");
        return alarm;
    }
}
