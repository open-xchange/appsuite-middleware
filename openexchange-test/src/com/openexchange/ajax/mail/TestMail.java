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

package com.openexchange.ajax.mail;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.kata.IdentitySource;
import com.openexchange.ajax.mail.contenttypes.AlternativeStrategy;
import com.openexchange.ajax.mail.contenttypes.FallbackStrategy;
import com.openexchange.ajax.mail.contenttypes.MailContentType;
import com.openexchange.ajax.mail.contenttypes.MailTypeStrategy;
import com.openexchange.ajax.mail.contenttypes.PlainTextStrategy;
import com.openexchange.java.JSON;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.utils.DateUtils;

/**
 * {@link TestMail} - simulates a mail object, but without the necessary session and whatnot needed that makes for a complicated setup.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestMail implements IdentitySource<TestMail> {

    private String from;

    private List<String> to, cc, bcc;

    private List<JSONObject> attachment;

    private JSONObject headers;

    private String subject, body, contentType, folder, id;

    private int priority, flags, color;

    private List<String> userFlags;

    private final List<MailTypeStrategy> strategies = Arrays.asList(new MailTypeStrategy[] {
        new PlainTextStrategy(), new AlternativeStrategy(), new FallbackStrategy() });

    public int getFlags() {
        return flags;
    }

    /**
     * Gets specified header
     *
     * @param name The header name
     * @return Either a <code>String</code> or a <code>JSONArray</code> instance if header has multiple values in mail headers
     */
    public Object getHeader(final String name) {
        if (null == headers) {
            return null;
        }
        if (!headers.hasAndNotNull(name)) {
            return null;
        }
        return headers.opt(name);
    }

    public Set<MailFlag> getFlagsAsSet() {
        return MailFlag.transform(flags);
    }

    public void setFlags(final int flags) {
        this.flags = flags;
    }

    public int getColor() {
        return color;
    }

    public void setColor(final int color) {
        this.color = color;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(final int priority) {
        this.priority = priority;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(final String from) {
        this.from = from;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(final List<String> to) {
        this.to = to;
    }

    public List<String> getCc() {
        return cc;
    }

    public void setCc(final List<String> cc) {
        this.cc = cc;
    }

    public List<String> getBcc() {
        return bcc;
    }

    public void setBcc(final List<String> bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public String[] getFolderAndId() {
        return new String[] { getFolder(), getId() };
    }

    public void setFolderAndID(final String[] folderAndID) {
        setFolder(folderAndID[0]);
        setId(folderAndID[1]);
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setFolder(final String folder) {
        this.folder = folder;
    }

    public String getFolder() {
        return folder;
    }

    public List<JSONObject> getAttachment() {
        return attachment;
    }

    public List<String> getUserFlags() {
        return userFlags;
    }

    public void setAttachment(final List<JSONObject> attachment) {
        this.attachment = attachment;
    }

    public TestMail() {
    }

    public TestMail(final JSONObject obj) throws JSONException {
        this();
        read(obj);
    }

    public TestMail(final int[] columns, final JSONArray values) throws JSONException {
        this();
        read(columns, values);
    }

    public TestMail(final Map<String, String> map) throws JSONException {
        this();
        read(map);
    }

    public TestMail(final String sender, final String recipient, final String subject, final String contentType, final String text) throws JSONException {
        setFrom(sender);
        setTo(Arrays.asList(new String[] { recipient }));
        setSubject(subject);
        setContentType(contentType);
        setBody(text);
        sanitize();
    }

    /**
     * Used for reading from FitNesse tables
     *
     * @param map A map, where the keys are taken from MailListField or TestMailField
     * @throws JSONException
     */
    public void read(final Map<String, String> map) throws JSONException {
        final Set<String> keys = map.keySet();
        for (final String key : keys) {
            final MailListField field = MailListField.getBy(key);
            if (key == null) {
                continue;
            }

            setBy(field, map.get(key));
        }
        setBody(map.get(TestMailField.MESSAGE.toString()));
        sanitize();
    }

    /**
     * Used for reading from a JSONObject like it is returned from a GET request
     *
     * @param json
     * @throws JSONException
     */
    public void read(final JSONObject json) throws JSONException {
        // lists
        final MailJSONField[] values = MailJSONField.values();
        final List<Integer> columns = new LinkedList<Integer>();
        final JSONArray jsonArray = new JSONArray();

        for (final MailJSONField jsonField : values) {
            final MailListField listField = MailListField.getBy(jsonField.getKey());
            if (listField == null || !json.has(jsonField.getKey())) {
                continue;
            }
            columns.add(I(listField.getField()));
            jsonArray.put(json.get(jsonField.getKey()));
        }

        read(I2i(columns), jsonArray);

        // handle fields that the other method cannot handle, because MailListField does not contain them.

        String field = "user";
        if (json.has(field)) {
            List<String> stringList = new ArrayList<>();
            for (Object o : ((JSONArray) json.get(field)).asList()) {
                if (o instanceof String) {
                    stringList.add((String) o);
                }
            }
            setUserFlags(stringList);
        }
        field = "id";
        if (json.has(field)) {
            setId(json.getString(field));
        }
        field = "folder_id"; // yes, not MailJSONField.FOLDER, don't get any ideas...
        if (json.has(field)) {
            setFolder(json.getString(field));
        }
        field = MailJSONField.ATTACHMENTS.getKey();
        if (json.has(field)) {
            final JSONArray array = json.getJSONArray(field);
            attachment = new LinkedList<JSONObject>();
            for (int i = 0, size = array.length(); i < size; i++) {
                attachment.add(array.getJSONObject(i));
            }
        }
        field = MailJSONField.HEADERS.getKey();
        if (json.has(field)) {
            final JSONObject hdrObject = json.getJSONObject(field);
            this.headers = hdrObject;
        }
        sanitize();
    }

    /**
     * Sets the user flags
     * 
     * @param flags A list of user flags
     */
    private void setUserFlags(List<String> flags) {
        this.userFlags = flags;
    }

    /**
     * Used for reading in fields returned by requests such as SEARCH
     *
     * @param columns Columns (as in #MailListField) that were requested
     * @param values A JSONArray carrying values for a TestMail
     * @throws JSONException
     */
    public void read(final int[] columns, final JSONArray values) throws JSONException {
        for (int index = 0; index < columns.length; index++) {
            final MailListField field = MailListField.getField(columns[index]);
            // lists
            if (field == MailListField.FROM) {
                setFrom(values.getString(index));
            }
            if (field == MailListField.TO) {
                setTo(j2l(values.getJSONArray(index)));
            }
            if (field == MailListField.CC) {
                setCc(j2l(values.getJSONArray(index)));
            }
            if (field == MailListField.BCC) {
                setBcc(j2l(values.getJSONArray(index)));
            }
            // strings
            if (field == MailListField.SUBJECT) {
                setSubject(values.getString(index));
            }
            // no content_type
            // no content
            if (field == MailListField.ID) {
                setId(values.getString(index));
            }
            // difference between folder and folder_id?
            if (field == MailListField.FOLDER_ID) {
                setFolder(values.getString(index));
            }
            // ints
            if (field == MailListField.COLOR_LABEL) {
                setColor(values.getInt(index));
            }
            if (field == MailListField.FLAGS) {
                try{
                    final int flags = values.getInt(index);
                    setFlags(flags);
                } catch (final JSONException e){
                    final String flagString = values.getString(index);
                    final String[] flags = flagString.split(",");
                    int bitmask = 0;
                    for(final String flagName : flags){
                        final MailFlag flag = MailFlag.getByName(flagName);
                        if(flag != null) {
                            bitmask += flag.getValue();
                        }
                    }
                    setFlags(bitmask);
                }

            }
            if (field == MailListField.PRIORITY) {
                setPriority(values.getInt(index));
            }
            // attachments: requests using columns to select fields cannot require the attachment field.
        }
        sanitize();
    }

    public Object getBy(final TestMailField field) {
        if (field == TestMailField.MESSAGE) {
            return getBody();
        }

        return null;
    }

    public Object getBy(final MailListField field) {
        if (field == MailListField.FROM) {
            return getFrom();
        }
        if (field == MailListField.TO) {
            return getTo();
        }
        if (field == MailListField.CC) {
            return getCc();
        }
        if (field == MailListField.BCC) {
            return getBcc();
        }
        // strings
        if (field == MailListField.SUBJECT) {
            return getSubject();
        }
        // no content_type
        // no content
        if (field == MailListField.ID) {
            return getId();
        }
        // difference between folder and folder_id?
        if (field == MailListField.FOLDER) {
            return getFolder();
        }
        // ints
        if (field == MailListField.COLOR_LABEL) {
            return I(getColor());
        }
        if (field == MailListField.FLAGS) {
            return I(getFlags());
        }
        if (field == MailListField.PRIORITY) {
            return I(getPriority());
        }
        return null;
    }

    public void setBy(final TestMailField field, final Object value) {
        if (field == TestMailField.MESSAGE) {
            setBody((String) value);
        }
    }

    public void setBy(final MailListField field, final Object value) {
        if (field == MailListField.FROM) {
            setFrom((String) value);
        }
        if (field == MailListField.TO) {
            setTo(addresses2list((String) value));
        }
        if (field == MailListField.CC) {
            setCc(addresses2list((String) value));
        }
        if (field == MailListField.BCC) {
            setBcc(addresses2list((String) value));
        }
        // strings
        if (field == MailListField.SUBJECT) {
            setSubject((String) value);
        }
        // no content_type
        // no content
        if (field == MailListField.ID) {
            setId((String) value);
        }
        // difference between folder and folder_id?
        if (field == MailListField.FOLDER) {
            setFolder((String) value);
        }
        // ints
        if (field == MailListField.COLOR_LABEL) {
            setColor(Integer.valueOf((String) value).intValue());
        }
        if (field == MailListField.FLAGS) {
            final String myValue = (String) value;
            try {
                setFlags(Integer.valueOf(myValue).intValue());
            } catch (final NumberFormatException e) {
                final String[] flags = myValue.split(",");
                int bitmask = 0;
                for(final String flagName : flags){
                    final MailFlag flag = MailFlag.getByName(flagName);
                    if(flag != null) {
                        bitmask += flag.getValue();
                    }
                }
                setFlags(bitmask);
            }

        }
        if (field == MailListField.PRIORITY) {
            setPriority(Integer.valueOf((String) value).intValue());
        }
    }

    /**
     * Converts a JSON array into a string list.
     */
    protected List<String> j2l(final JSONArray array) throws JSONException {
        return JSON.jsonArray2list(array);
    }

    protected List<String> addresses2list(final String mailAddresses) {
        final LinkedList<String> addresses = new LinkedList<String>();
        final String[] strings = mailAddresses.split(",");
        for (final String address : strings) {
            addresses.add(address.trim());
        }
        return addresses;
    }

    /**
     * Makes this mail look properly (e.g. attaching the content as attachment if it is of content_type &quot;alternative&quot;
     *
     * @return
     * @throws JSONException
     */
    public void sanitize() throws JSONException {
        for (final MailTypeStrategy strategy : strategies) {
            if (strategy.isResponsibleFor(this)) {
                strategy.sanitize(this);
            }
        }
    }

    /**
     * Transforms this mail into a JSONObject like used by the HTTP EnumAPI.
     *
     * @return
     * @throws JSONException
     */
    public JSONObject toJSON() throws JSONException {
        final JSONObject result = new JSONObject();
        result.put(MailJSONField.FROM.getKey(), getFrom());
        result.put(MailJSONField.RECIPIENT_TO.getKey(), getTo() != null ? correctMailAddresses(getTo()) : "");
        result.put(MailJSONField.RECIPIENT_BCC.getKey(), getBcc() != null ? correctMailAddresses(getBcc()) : "");
        result.put(MailJSONField.RECIPIENT_CC.getKey(), getCc() != null ? correctMailAddresses(getCc()) : "");
        if (getSubject() != null) {
            result.put(MailJSONField.SUBJECT.getKey(), getSubject());
        }

        if (getBody() != null) {
            final JSONArray attachments = new JSONArray();
            final JSONObject jsonbody = new JSONObject();
            jsonbody.put(MailJSONField.CONTENT.getKey(), getBody());
            jsonbody.put(MailJSONField.CONTENT_TYPE.getKey(), MailContentType.ALTERNATIVE.toString());
            attachments.put(jsonbody);
            result.put(MailJSONField.ATTACHMENTS.getKey(), attachments);
        }
        result.put(MailJSONField.PRIORITY.getKey(), getPriority());
        result.put(MailJSONField.CONTENT.getKey(), getBody() != null ? getBody() : "");
        return result;
    }

    public String toRFC822String() {
        StringBuilder sb = new StringBuilder();
        putHeader(sb, "From", getFrom());
        putHeader(sb, "To", getTo());
        putHeader(sb, "CC", getCc());
        putHeader(sb, "BCC", getBcc());
        putHeader(sb, "Received", "from ox.open-xchange.com;" + DateUtils.toStringRFC822(new Date(), TimeZones.UTC));
        putHeader(sb, "Date", DateUtils.toStringRFC822(new Date(), TimeZones.UTC));
        putHeader(sb, "Subject", getSubject());
        String ct = getContentType();
        if (ct == null) {
            ct = "text/plain; charset=\"UTF-8\"";
        }
        putHeader(sb, "Content-Type", ct);
        putHeader(sb, "Content-Transfer-Encoding", "8bit");
        sb.append("\n");
        sb.append(getBody());

        return sb.toString();
    }

    private void putHeader(StringBuilder sb, String header, Object value) {
        if (value != null) {
            sb.append(header).append(": ");
            if (value instanceof String) {
                sb.append(value);
            } else if (value instanceof Iterable<?>) {
                boolean first = true;
                for (Object o : (Iterable<?>) value) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(o.toString());
                }
            }

            sb.append("\n");
        }
    }

    public JSONArray correctMailAddresses(final List<String> list) {
        final JSONArray corrected = new JSONArray();
        for (final String recipient : list) {
            final JSONArray adress = correctMailAdress(recipient);
            corrected.put(adress);
        }
        return corrected;
    }

    public JSONArray correctMailAdress(final String recipient) {
        final JSONArray corrected = new JSONArray();
        corrected.put(recipient);
        corrected.put(recipient);
        return corrected;
    }

    @Override
    public String toString() {
        final StringBuilder bob = new StringBuilder();
        bob.append("Folder = " + getFolder() + ", ID = " + getId() + "\n");
        if (from != null) {
            bob.append("From: ");
            bob.append(from);
        }
        if (from != null) {
            bob.append("\nTo: ");
            bob.append(Strings.join(to, ", "));
        }
        if (cc != null) {
            bob.append("\nCC: ");
            bob.append(Strings.join(cc, ", "));
        }
        if (bcc != null) {
            bob.append("\nBCC: ");
            bob.append(Strings.join(bcc, ", "));
        }
        bob.append("\nPriority: ");
        bob.append(getPriority());
        bob.append("\nContent-Type: ");
        bob.append(getContentType());
        bob.append("\nSubject: ");
        bob.append(getSubject());
        bob.append("\nContent:\n");
        bob.append(getBody());
        return bob.toString();
    }

    @Override
    public void assumeIdentity(final TestMail entry) {
        entry.setId(getId());
        entry.setFolder(getFolder());
    }

    @Override
    public void forgetIdentity(final TestMail entry) {

    }

    @Override
    public Class<TestMail> getType() {
        return TestMail.class;
    }

    @Override
    public void rememberIdentityValues(final TestMail entry) {
        setId(entry.getId());
        setFolder(entry.getFolder());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((folder == null) ? 0 : folder.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestMail other = (TestMail) obj;
        if (folder == null) {
            if (other.folder != null) {
                return false;
            }
        } else if (!folder.equals(other.folder)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}
