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

package com.openexchange.mail;

import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.FolderObject;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * {@link MailListField} - An enumeration of mail list fields as defined in <a href=
 * "http://www.open-xchange.com/wiki/index.php?title=HTTP_API#Module_.22mail.22" >HTTP API's mail section</a>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public enum MailListField {

    /**
     * The mail ID (600)
     */
    ID(600, DataFields.ID),
    /**
     * The folder ID (601)
     */
    FOLDER_ID(601, FolderChildFields.FOLDER_ID),
    /**
     * Whether message contains attachments (602)
     */
    ATTACHMENT(602, MailJSONField.HAS_ATTACHMENTS.getKey()), //FIXME use new approach
    /**
     * From (603)
     */
    FROM(603, MailJSONField.FROM.getKey()),
    /**
     * To (604)
     */
    TO(604, MailJSONField.RECIPIENT_TO.getKey()),
    /**
     * Cc (605)
     */
    CC(605, MailJSONField.RECIPIENT_CC.getKey()),
    /**
     * Bcc (606)
     */
    BCC(606, MailJSONField.RECIPIENT_BCC.getKey()),
    /**
     * Subject (607)
     */
    SUBJECT(607, MailJSONField.SUBJECT.getKey()),
    /**
     * Size (608)
     */
    SIZE(608, MailJSONField.SIZE.getKey()),
    /**
     * Sent date (609)
     */
    SENT_DATE(609, MailJSONField.SENT_DATE.getKey()),
    /**
     * Received date (610)
     */
    RECEIVED_DATE(610, MailJSONField.RECEIVED_DATE.getKey()),
    /**
     * Flags (611)
     */
    FLAGS(611, MailJSONField.FLAGS.getKey()),
    /**
     * Thread level (612)
     */
    THREAD_LEVEL(612, MailJSONField.THREAD_LEVEL.getKey()),
    /**
     * <code>Disposition-Notification-To</code> (613)
     */
    DISPOSITION_NOTIFICATION_TO(613, MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey()),
    /**
     * Priority (614)
     */
    PRIORITY(614, MailJSONField.PRIORITY.getKey()),
    /**
     * Message reference (615)
     */
    MSG_REF(615, MailJSONField.MSGREF.getKey()),
    /**
     * Color Label (102)
     */
    COLOR_LABEL(CommonObject.COLOR_LABEL, CommonFields.COLORLABEL),
    /**
     * Folder (650)
     */
    FOLDER(650, MailJSONField.FOLDER.getKey()),
    /**
     * Flag \SEEN (651).
     * <p>
     * Acts as a special sort field for the purpose of having retrieved mails sorted by seen/unseen status.
     */
    FLAG_SEEN(651, MailJSONField.SEEN.getKey()),
    /**
     * Total count (309)
     */
    TOTAL(FolderObject.TOTAL, MailJSONField.TOTAL.getKey()),
    /**
     * New count (310)
     */
    NEW(FolderObject.NEW, MailJSONField.NEW.getKey()),
    /**
     * Unread count (311)
     */
    UNREAD(FolderObject.UNREAD, MailJSONField.UNREAD.getKey()),
    /**
     * Deleted count (312)
     */
    DELETED(FolderObject.DELETED, MailJSONField.DELETED.getKey()),
    /**
     * Account name (652)
     */
    ACCOUNT_NAME(652, MailJSONField.ACCOUNT_NAME.getKey()),
    /**
     * Account identifier (653)
     */
    ACCOUNT_ID(653, MailJSONField.ACCOUNT_ID.getKey()),
    /**
     * The original mail ID. (654)
     *
     * @since v7.8.0
     */
    ORIGINAL_ID(654, MailJSONField.ORIGINAL_ID.getKey()),
    /**
     * The original folder ID (655)
     *
     * @since v7.8.0
     */
    ORIGINAL_FOLDER_ID(655, MailJSONField.ORIGINAL_FOLDER_ID.getKey()),
    /**
     * The MIME type information (656)
     *
     * @since v7.8.0
     */
    MIME_TYPE(656, MailJSONField.CONTENT_TYPE.getKey()),
    /**
     * Flag \ANSWERED (657).
     * <p>
     * Acts as a special sort field for the purpose of having retrieved mails sorted by answered/unanswered status.
     */
    FLAG_ANSWERED(657, MailJSONField.ANSWERED.getKey()),
    /**
     * Flag \FORWARDED (658).
     * <p>
     * Acts as a special sort field for the purpose of having retrieved mails sorted by forwarded/not forwarded status.
     * <p>
     * <b>Note</b>:<br>
     * To serve that sort field, the backing mail service is required to either support a <code>\Forwarded</code> system flag or
     * a <code>$Forwarded</code> user flag. For the latter, the user flags capability is needed; otherwise an error is returned.
     */
    FLAG_FORWARDED(658, MailJSONField.FORWARDED.getKey()),
    /**
     * Flag \DRAFT (659).
     * <p>
     * Acts as a special sort field for the purpose of having retrieved mails sorted by draft flag.
     */
    FLAG_DRAFT(659, MailJSONField.DRAFT.getKey()),
    /**
     * Flag \FLAGGED (660).
     * <p>
     * Acts as a special sort field for the purpose of having retrieved mails sorted by flagged/unflagged status.
     */
    FLAG_FLAGGED(660, MailJSONField.FLAGGED.getKey()),
    /**
     * The date of a mail message. As configured (see <code>"com.openexchange.mail.preferSentDate"</code>), either the internal received date or mail's sent date (as given by <code>"Date"</code> header).
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * This field is only considered in JSON layer and is not supposed to be treated in actual MAL implementations.
     * </div>
     * <p>
     */
    DATE(661, MailJSONField.DATE.getKey()),
    /**
     * A message's text preview only if immediately available
     *
     * @since v7.10.0
     */
    TEXT_PREVIEW_IF_AVAILABLE(662, MailJSONField.TEXT_PREVIEW.getKey()),
    /**
     * A message's text preview; generate it if absent (which may be slow)
     *
     * @since v7.10.0
     */
    TEXT_PREVIEW(663, MailJSONField.TEXT_PREVIEW.getKey()),
    /**
     * The message's authentication overall result (light version); maps to <code>"Authentication-Results"</code> header
     *
     * @since v7.10.0
     */
    AUTHENTICATION_OVERALL_RESULT(664, MailJSONField.AUTHENTICITY_PREVIEW.getKey()),
    /**
     * The message's authentication mechanism results (heavy version); maps to <code>"Authentication-Results"</code> header
     *
     * @since v7.10.0
     */
    AUTHENTICATION_MECHANISM_RESULTS(665, MailJSONField.AUTHENTICITY.getKey()),

    ;

    private final int field;
    private final String key;

    private MailListField(int field, String jsonKey) {
        this.field = field;
        key = jsonKey;
    }

    /**
     * @return The <code>int</code> field value
     */
    public int getField() {
        return field;
    }

    /**
     * @return The JSON key
     */
    public String getKey() {
        return key;
    }

    private static final MailListField[] EMPTY_FIELDS = new MailListField[0];

    private static final TIntObjectMap<MailListField> FIELDS_MAP = new TIntObjectHashMap<MailListField>(25);

    static {
        final MailListField[] fields = MailListField.values();
        for (MailListField listField : fields) {
            FIELDS_MAP.put(listField.field, listField);
        }
    }

    /**
     * Creates an array of {@link MailListField} corresponding to given <code>int</code> values
     *
     * @param fields The <code>int</code> values
     * @return The array of {@link MailListField} corresponding to given <code>int</code> values
     */
    public static final MailListField[] getFields(int[] fields) {
        if ((fields == null) || (fields.length == 0)) {
            return EMPTY_FIELDS;
        }
        final MailListField[] retval = new MailListField[fields.length];
        for (int i = 0; i < fields.length; i++) {
            retval[i] = getField(fields[i]);
        }
        return retval;
    }

    /**
     * Determines the corresponding {@link MailListField} constant to given <code>int</code> value
     *
     * @param field The <code>int</code> value
     * @return The corresponding {@link MailListField} constant
     */
    public static final MailListField getField(int field) {
        return FIELDS_MAP.get(field);
    }

    /**
     * Returns all field values as an array of integers.
     *
     * @return
     */
    public static final int[] getAllFields() {
        final MailListField[] values = values();
        final int[] all = new int[values.length];
        for (int i = 0; i < all.length; i++) {
            all[i] = values[i].getField();
        }
        return all;
    }

    /**
     * Gets a field by the JSON name
     *
     * @param jsonName identifier
     * @return MailListField identified by jsonName, null if not found
     */
    public static final MailListField getBy(String jsonName) {
        final MailListField[] values = values();
        for (MailListField field : values) {
            if (jsonName.equals(field.getKey())) {
                return field;
            }
        }
        return null;
    }
}
