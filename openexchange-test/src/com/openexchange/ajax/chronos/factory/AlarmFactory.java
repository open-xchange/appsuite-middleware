/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.chronos.factory;

import java.util.ArrayList;
import java.util.List;
import javax.activation.MimetypesFileTypeMap;
import com.openexchange.testing.httpclient.models.Alarm;
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
     * @param description The description of the alarm (used as the body of the mail notification)
     * @param summary The summary of the alarm (used as the subject in the email notification)
     * @return the new {@link Alarm}
     */
    public static Alarm createMailAlarm(String duration, String description, String summary) {
        Alarm mailAlarm = createAlarm(duration, AlarmAction.EMAIL);
        mailAlarm.setDescription(description);
        mailAlarm.setSummary(summary);
        mailAlarm.setAttendees(null);
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
