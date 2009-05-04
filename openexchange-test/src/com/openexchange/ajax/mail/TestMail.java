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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.kata.IdentitySource;
import com.openexchange.java.JSON;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailJSONField;

/**
 * {@link TestMail} - simulates a mail object, but without the necessary session and whatnot needed that makes for a complicated setup.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestMail implements IdentitySource<TestMail> {

    private List<String> from, to, cc, bcc;

    private String subject, body, contentType, folder, id;

    private int priority, flags, color;

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

    public String[] getFolderAndId(){
        return new String[]{ getFolder(), getId() };
    }
    
    public TestMail() {
    }
    
    

    public TestMail(JSONObject obj) throws JSONException {
        read(obj);
    }

    public static TestMail create(JSONObject obj) throws JSONException {
        return new TestMail(obj);
    }

    public void read(Map<String,String> map){
        //TODO find some proper names
        setFrom( Arrays.asList( map.get( "From" ).split("[,;]") ) );
        setTo( Arrays.asList( map.get( "To" ).split("[,;]") ) );
        setSubject(map.get( "Subject") );
        setBody( map.get("Content") );
        
    }

    public void read(JSONObject json) throws JSONException {
        // lists
        String field = MailJSONField.FROM.getKey();
        if (json.has(field)) {
            setFrom(j2l(json.getJSONArray(field)));
        }
        field = MailJSONField.RECIPIENT_TO.getKey();
        if (json.has(field)) {
            setTo(j2l(json.getJSONArray(field)));
        }
        field = MailJSONField.RECIPIENT_CC.getKey();
        if (json.has(field)) {
            setCc(j2l(json.getJSONArray(field)));
        }
        field = MailJSONField.RECIPIENT_BCC.getKey();
        if (json.has(field)) {
            bcc = new LinkedList<String>();
            setBcc(j2l(json.getJSONArray(field)));
        }
        // strings
        field = MailJSONField.SUBJECT.getKey();
        if (json.has(field)) {
            setSubject(json.getString(field));
        }
        field = MailJSONField.CONTENT_TYPE.getKey();
        if (json.has(field)) {
            setContentType(json.getString(field));
        }
        field = MailJSONField.CONTENT.getKey();
        if (json.has(field)) {
            setBody(json.getString(field));
        }
        field = "id";
        if (json.has(field)) {
            setId(json.getString(field));
        }
        field = "folder_id";
        if (json.has(field)) {
            setFolder(json.getString(field));
        }
        // ints
        field = "color_label";
        if (json.has(field)) {
            setColor(json.getInt(field));
        }
        field = "flags";
        if (json.has(field)) {
            setFlags(json.getInt(field));
        }
        field = "priority";
        if (json.has(field)) {
            setPriority(json.getInt(field));
        }
    }

    public void read(int[] columns, JSONArray values) {
        //TODO
    }

    /**
     * Converts a JSON array into a string list.
     */
    protected List<String> j2l(JSONArray array) throws JSONException {
        return JSON.jsonArray2list(array);
    }

    /**
     * Makes this mail look properly (e.g. attaching the content as attachment if it is of content_type &quot;alternative&quot;
     * 
     * @return
     */
    public TestMail sanitize() {
        return null;
    }

    /**
     * Transforms this mail into a JSONObject like used by the HTTP API.
     * 
     * @return
     */
    public JSONObject toJSON() {
        return null;
    }

    public String toString() {
        StringBuilder bob = new StringBuilder();
        bob.append("From: ");
        bob.append(Strings.join(from, ", "));
        bob.append("\nTo: ");
        bob.append(Strings.join(to, ", "));
        bob.append("\nCC: ");
        bob.append(Strings.join(cc, ", "));
        bob.append("\nBCC: ");
        bob.append(Strings.join(bcc, ", "));
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

}
