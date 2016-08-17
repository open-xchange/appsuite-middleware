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

package com.openexchange.group.json.resultconverter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.anonymizer.AnonymizerService;
import com.openexchange.ajax.anonymizer.Anonymizers;
import com.openexchange.ajax.anonymizer.Module;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.writer.GroupWriter;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.Group.Field;
import com.openexchange.groupware.results.CollectionDelta;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link GroupJsonResultConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class GroupJsonResultConverter implements ResultConverter {

    /**
     * Initializes a new {@link GroupJsonResultConverter}.
     */
    public GroupJsonResultConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "group";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        try {
            convert(requestData, result, session);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session) throws OXException, JSONException {
        Object resultObject = result.getResultObject();
        if (resultObject instanceof Group) {
            Group group = (Group) resultObject;

            // Optional anonymization
            Anonymizers.optAnonymizeIfGuest(group, Module.GROUP, session);

            JSONObject jGroup = new JSONObject(6);
            new GroupWriter().writeGroup(group, jGroup);
            result.setResultObject(jGroup, "json");
        } else if (resultObject instanceof CollectionDelta) {
            CollectionDelta<Group> collectionDelta = (CollectionDelta<Group>) resultObject;

            List<Group> groups = collectionDelta.getNewAndModified();
            JSONArray jGroups = new JSONArray(groups.size());
            GroupWriter groupWriter = new GroupWriter();

            if (Anonymizers.isGuest(session)) {
                AnonymizerService<Group> anonymizer = Anonymizers.optAnonymizerFor(Module.GROUP);
                for (Group group : groups) {
                    group = anonymizer.anonymize(group, session);

                    JSONObject jGroup = new JSONObject(6);
                    groupWriter.writeGroup(group, jGroup);
                    jGroups.put(jGroup);
                }
            } else {
                for (Group group : groups) {
                    JSONObject jGroup = new JSONObject(6);
                    groupWriter.writeGroup(group, jGroup);
                    jGroups.put(jGroup);
                }
            }

            List<Group> deleted = collectionDelta.getDeleted();
            JSONArray jDeletedGroups;

            if (null == deleted) {
                jDeletedGroups = new JSONArray(0);
            } else {
                jDeletedGroups = new JSONArray(deleted.size());

                if (Anonymizers.isGuest(session)) {
                    AnonymizerService<Group> anonymizer = Anonymizers.optAnonymizerFor(Module.GROUP);
                    for (Group group : deleted) {
                        group = anonymizer.anonymize(group, session);

                        JSONObject jGroup = new JSONObject(6);
                        groupWriter.writeGroup(group, jGroup);
                        jDeletedGroups.put(jGroup);
                    }
                } else {
                    for (Group group : deleted) {
                        JSONObject jGroup = new JSONObject(6);
                        groupWriter.writeGroup(group, jGroup);
                        jDeletedGroups.put(jGroup);
                    }
                }
            }

            JSONObject jResult = new JSONObject(3);
            jResult.put("new", jGroups).put("modified", jGroups);
            jResult.put("deleted", jDeletedGroups);

            result.setResultObject(jResult, "json");
        } else if (AJAXServlet.ACTION_ALL.equalsIgnoreCase(requestData.getAction())) {
            Collection<Group> groups = (Collection<Group>) resultObject;

            int[] columns = requestData.checkIntArray(AJAXServlet.PARAMETER_COLUMNS);
            boolean loadMembers = false;
            List<Field> fields = new LinkedList<Field>();
            for (int column : columns) {
                Field field = Group.Field.getByColumnNumber(column);
                if (field == Group.Field.MEMBERS) {
                    loadMembers = true;
                }
                fields.add(field);
            }

            JSONArray jGroupRows = new JSONArray(groups.size());
            GroupWriter groupWriter = new GroupWriter();

            if (Anonymizers.isGuest(session)) {
                AnonymizerService<Group> anonymizer = Anonymizers.optAnonymizerFor(Module.GROUP);
                for (Group group : groups) {
                    group = anonymizer.anonymize(group, session);

                    JSONArray row = new JSONArray();
                    groupWriter.writeArray(group, row, fields);
                    jGroupRows.put(row);
                }
            } else {
                for (Group group : groups) {
                    JSONArray row = new JSONArray();
                    groupWriter.writeArray(group, row, fields);
                    jGroupRows.put(row);
                }
            }

            result.setResultObject(jGroupRows, "json");
        } else {
            Collection<Group> groups = (Collection<Group>) resultObject;
            JSONArray jGroups = new JSONArray(groups.size());
            GroupWriter groupWriter = new GroupWriter();

            if (Anonymizers.isGuest(session)) {
                AnonymizerService<Group> anonymizer = Anonymizers.optAnonymizerFor(Module.GROUP);
                for (Group group : groups) {
                    group = anonymizer.anonymize(group, session);

                    JSONObject jGroup = new JSONObject(6);
                    groupWriter.writeGroup(group, jGroup);
                    jGroups.put(jGroup);
                }
            } else {
                for (Group group : groups) {
                    JSONObject jGroup = new JSONObject(6);
                    groupWriter.writeGroup(group, jGroup);
                    jGroups.put(jGroup);
                }
            }

            result.setResultObject(jGroups, "json");
        }
    }

}
