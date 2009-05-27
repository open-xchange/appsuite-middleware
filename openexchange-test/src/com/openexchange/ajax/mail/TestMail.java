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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.util.Arrays;
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
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;

/**
 * {@link TestMail} - simulates a mail object, but without the necessary session and whatnot needed that makes for a complicated setup.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestMail implements IdentitySource<TestMail> {

    public enum TestMailField {
        MESSAGE("message");

        private String fitnesse;

        private TestMailField(String fitnesse) {
            this.fitnesse = fitnesse;
        }

        public String toString() {
            return fitnesse;
        }

        public static TestMailField getBy(String fitnesse) {
            for (TestMailField field : values()) {
                if (fitnesse.equals(field.fitnesse))
                    return field;
            }
            return null;
        }
    }

    private List<String> from, to, cc, bcc;

    private List<JSONObject> attachment;

    private String subject, body, contentType, folder, id;

    private int priority, flags, color;

    private List<MailTypeStrategy> strategies = Arrays.asList(new MailTypeStrategy[] {
        new PlainTextStrategy(), new AlternativeStrategy(), new FallbackStrategy() });

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<String> getFrom() {
        return from;
    }

    public void setFrom(List<String> from) {
        this.from = from;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public List<String> getCc() {
        return cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public List<String> getBcc() {
        return bcc;
    }

    public void setBcc(List<String> bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String[] getFolderAndId() {
        return new String[] { getFolder(), getId() };
    }

    public void setFolderAndID(String[] folderAndID) {
        setFolder(folderAndID[0]);
        setId(folderAndID[1]);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getFolder() {
        return folder;
    }

    public List<JSONObject> getAttachment() {
        return attachment;
    }

    public void setAttachment(List<JSONObject> attachment) {
        this.attachment = attachment;
    }

    public TestMail() {
    }

    public TestMail(JSONObject obj) throws JSONException {
        read(obj);
    }

    public TestMail(int[] columns, JSONArray values) throws JSONException {
        read(columns, values);
    }

    public static TestMail create(JSONObject obj) throws JSONException {
        return new TestMail(obj);
    }

    public TestMail(String sender, String recipient, String subject, String contentType, String text) throws JSONException {
        setFrom( Arrays.asList( new String[]{sender}));
        setTo( Arrays.asList( new String[]{recipient}));
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
    public void read(Map<String, String> map) throws JSONException {
        Set<String> keys = map.keySet();
        for (String key : keys) {
            MailListField field = MailListField.getBy(key);
            if (key == null)
                continue;

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
    public void read(JSONObject json) throws JSONException {
        // lists
        MailJSONField[] values = MailJSONField.values();
        List<Integer> columns = new LinkedList<Integer>();
        JSONArray jsonArray = new JSONArray();

        for (MailJSONField jsonField : values) {
            MailListField listField = MailListField.getBy(jsonField.getKey());
            if (listField == null || !json.has(jsonField.getKey()))
                continue;
            columns.add(I(listField.getField()));
            jsonArray.put(json.get(jsonField.getKey()));
        }

        read(I2i(columns), jsonArray);

        // handle fields that the other method cannot handle, because MailListField does not contain them.
        String field = "id";
        if (json.has(field)) {
            setId(json.getString(field));
        }
        field = "folder_id"; // yes, not MailJSONField.FOLDER, don't get any ideas...
        if (json.has(field)) {
            setFolder(json.getString(field));
        }
        field = MailJSONField.ATTACHMENTS.getKey();
        if (json.has(field)) {
            JSONArray array = json.getJSONArray(field);
            attachment = new LinkedList<JSONObject>();
            for (int i = 0, size = array.length(); i < size; i++) {
                attachment.add(array.getJSONObject(i));
            }
        }
        sanitize();
    }

    /**
     * Used for reading in fields returned by requests such as SEARCH
     * 
     * @param columns Columns (as in #MailListField) that were requested
     * @param values A JSONArray carrying values for a TestMail
     * @throws JSONException
     */
    public void read(int[] columns, JSONArray values) throws JSONException {
        for (int index = 0; index < columns.length; index++) {
            MailListField field = MailListField.getField(columns[index]);
            // lists
            if (field == MailListField.FROM) {
                setFrom(j2l(values.getJSONArray(index)));
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
                setFlags(values.getInt(index));
            }
            if (field == MailListField.PRIORITY) {
                setPriority(values.getInt(index));
            }
            // attachments: requests using columns to select fields cannot require the attachment field.
        }
        sanitize();
    }

    public Object getBy(TestMailField field) {
        if (field == TestMailField.MESSAGE)
            return getBody();

        return null;
    }

    public Object getBy(MailListField field) {
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

    public void setBy(TestMailField field, Object value) {
        if (field == TestMailField.MESSAGE) {
            setBody((String) value);
        }
    }

    public void setBy(MailListField field, Object value) {
        if (field == MailListField.FROM) {
            setFrom(addresses2list((String) value));
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
            setColor(((Integer) value).intValue());
        }
        if (field == MailListField.FLAGS) {
            setFlags(((Integer) value).intValue());
        }
        if (field == MailListField.PRIORITY) {
            setPriority(((Integer) value).intValue());
        }
    }

    /**
     * Converts a JSON array into a string list.
     */
    protected List<String> j2l(JSONArray array) throws JSONException {
        return JSON.jsonArray2list(array);
    }

    protected List<String> addresses2list(String mailAddresses) {
        return Arrays.asList(mailAddresses.split(","));
    }

    /**
     * Makes this mail look properly (e.g. attaching the content as attachment if it is of content_type &quot;alternative&quot;
     * 
     * @return
     * @throws JSONException
     */
    public void sanitize() throws JSONException {
        for (MailTypeStrategy strategy : strategies) {
            if (strategy.isResponsibleFor(this)) {
                strategy.sanitize(this);
            }
        }
    }

    /**
     * Transforms this mail into a JSONObject like used by the HTTP API.
     * 
     * @return
     * @throws JSONException
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject result = new JSONObject();
        result.put(MailJSONField.FROM.getKey(), correctMailAddresses(getFrom()));
        result.put(MailJSONField.RECIPIENT_TO.getKey(), getTo() != null ? correctMailAddresses(getTo()) : "");
        result.put(MailJSONField.RECIPIENT_BCC.getKey(), getBcc() != null ? correctMailAddresses(getBcc()) : "");
        result.put(MailJSONField.RECIPIENT_CC.getKey(), getCc() != null ? correctMailAddresses(getCc()) : "");
        if (getSubject() != null)
            result.put(MailJSONField.SUBJECT.getKey(), getSubject());

        if (getBody() != null) {
            JSONArray attachments = new JSONArray();
            JSONObject jsonbody = new JSONObject();
            jsonbody.put(MailJSONField.CONTENT.getKey(), getBody());
            jsonbody.put(MailJSONField.CONTENT_TYPE.getKey(), MailContentType.ALTERNATIVE.toString());
            attachments.put(jsonbody);
            result.put(MailJSONField.ATTACHMENTS.getKey(), attachments);
        }
        result.put(MailJSONField.PRIORITY.getKey(), getPriority());
        result.put(MailJSONField.CONTENT.getKey(), getBody() != null ? getBody() : "");
        return result;
    }

    public JSONArray correctMailAddresses(List<String> list) {
        JSONArray corrected = new JSONArray();
        for (String recipient : list) {
            JSONArray adress = correctMailAdress(recipient);
            corrected.put(adress);
        }
        return corrected;
    }

    public JSONArray correctMailAdress(String recipient) {
        JSONArray corrected = new JSONArray();
        corrected.put(recipient);
        corrected.put(recipient);
        return corrected;
    }

    public String toString() {
        StringBuilder bob = new StringBuilder();
        bob.append("Folder = " + getFolder() + ", ID = " + getId() + "\n");
        if (from != null) {
            bob.append("From: ");
            bob.append(Strings.join(from, ", "));
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

    public void assumeIdentity(TestMail entry) {
        entry.setId(getId());
        entry.setFolder(getFolder());
    }

    public void forgetIdentity(TestMail entry) {

    }

    public Class<TestMail> getType() {
        return TestMail.class;
    }

    public void rememberIdentityValues(TestMail entry) {
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TestMail other = (TestMail) obj;
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
