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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.eav.json.multiple;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVNodeTypeCoercionVisitor;
import com.openexchange.eav.EAVPath;
import com.openexchange.eav.EAVSetTransformation;
import com.openexchange.eav.EAVSetTransformationTypeCoercionVisitor;
import com.openexchange.eav.EAVStorage;
import com.openexchange.eav.EAVType;
import com.openexchange.eav.EAVTypeCoercion;
import com.openexchange.eav.EAVTypeMetadataNode;
import com.openexchange.eav.TreeTools;
import com.openexchange.eav.json.exception.EAVJsonException;
import com.openexchange.eav.json.exception.EAVJsonExceptionMessage;
import com.openexchange.eav.json.parse.JSONParser;
import com.openexchange.eav.json.parse.arrayupdate.JSONArrayUpdateParser;
import com.openexchange.eav.json.parse.metadata.type.JSONTypeMetadataParser;
import com.openexchange.eav.json.write.JSONWriter;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.tools.session.ServerSession;

// TODO: Refactor me! I'm a bit ugly.

/**
 * {@link EAVMultipleHandler}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class EAVMultipleHandler implements MultipleHandler {

    private EAVStorage storage;

    private EAVPath path = null;

    private EAVNode parsedNodes;
    
    private EAVTypeMetadataNode types = null;

    private boolean allBinaries;
    
    private Set<EAVPath> loadBinaries = null;
    
    private boolean raw = true;

    private String action;

    private EAVSetTransformation setTransformation;

    private TimeZone timezone;

    private boolean overwrite;
    
    public void close() {
    
    }

    public Date getTimestamp() {
        return null;
    }

    public Object performRequest(final String action, final JSONObject jsonObject, final ServerSession session, final boolean secure) throws AbstractOXException, JSONException {
        this.action = action;
        parse(jsonObject, session.getUser());
        
        final Context ctx = session.getContext();
        
        if(action.equals("new")) {
            if(parsedNodes == null) {
                throw EAVJsonExceptionMessage.MissingParameter.create("body");
            }
            if(types != null) {
                parsedNodes.visit(new EAVNodeTypeCoercionVisitor(types, timezone, EAVTypeCoercion.Mode.INCOMING));            
            }
            storage.insert(ctx, path.parent(), parsedNodes);
            return 1;
        } else if (action.equals("update")){
            if(parsedNodes == null) {
                throw EAVJsonExceptionMessage.MissingParameter.create("body");
            }
            final EAVTypeMetadataNode savedTypes = storage.getTypes(ctx, path.parent(), parsedNodes);
            if(types != null) {
                types = savedTypes.mergeWith(types);
            } else {
                types = savedTypes;
            }
            parsedNodes.visit(new EAVNodeTypeCoercionVisitor(types, timezone, EAVTypeCoercion.Mode.INCOMING));            
            if(overwrite) {
                storage.replace(ctx, path.parent(), parsedNodes);
            } else {
                storage.update(ctx, path.parent(), parsedNodes);
            }
            return 1;
        } else if (action.equals("updateSets")) {
            if(setTransformation == null) {
                throw EAVJsonExceptionMessage.MissingParameter.create("body");
            }
            final EAVNode structure = TreeTools.copyStructure(new EAVNode(), setTransformation);
            
            final EAVTypeMetadataNode savedTypes = storage.getTypes(ctx, path.parent(), structure);
            if(types != null) {
                types = savedTypes.mergeWith(types);
            } else {
                types = savedTypes;
            }
            
            setTransformation.visit(new EAVSetTransformationTypeCoercionVisitor(types, timezone, EAVTypeCoercion.Mode.INCOMING));
                       
            storage.updateSets(ctx, path.parent(), setTransformation);
            
            return 1;
            
        } else if (action.equals("delete")) {
            storage.delete(ctx, path);
            return 1;
        } else if (action.equals("get")) {
            final EAVNode loaded = (loadBinaries == null) ? storage.get(ctx, path, allBinaries) : storage.get(ctx, path, loadBinaries);
            if(jsonObject.has("binaryEncoding") && raw && !loaded.isLeaf()) {
                throw EAVJsonExceptionMessage.BinariesInTreesMustBeBase64Encoded.create();
            }
            if(loaded.getType() == EAVType.BINARY && !loaded.isMultiple() && raw) {
                return loaded.getPayload();
            }
            
            if(types == null) {
                types = loaded.extractTypeData();
            }
            
            
            loaded.visit(new EAVNodeTypeCoercionVisitor(types, timezone, EAVTypeCoercion.Mode.OUTGOING));            
            
            return new JSONWriter(loaded).getJson().get(path.last());
        }
        
        
        throw EAVJsonExceptionMessage.UnknownAction.create(action);
        
    }

    private void parse(final JSONObject jsonObject, final User user) throws JSONException, EAVJsonException {
        if(!jsonObject.has("path")) {
            throw EAVJsonExceptionMessage.MissingParameter.create("path");
        }
        path = EAVPath.parse(jsonObject.getString("path"));
        parsedNodes = null;
        if(jsonObject.has(ResponseFields.DATA)) {
            final Object payload = jsonObject.get(ResponseFields.DATA);
            
            if(JSONObject.class.isInstance(payload)) {
                final JSONObject data = (JSONObject) payload;
                if(data.has("data") || data.has("types")) {
                    types = new JSONTypeMetadataParser(data.getJSONObject("types")).getNode();
                    
                    if(data.has("data")) {
                        final Object definitiveData = data.get("data");
                        if(JSONObject.class.isInstance(definitiveData)) {
                            if(!action.equals("updateSets")) {
                                parsedNodes = new JSONParser(path.last(), (JSONObject) definitiveData).getNode();
                            } else {
                                setTransformation = new JSONArrayUpdateParser(path.last(), (JSONObject) definitiveData).getNode();
                            }
                            parsedNodes = new JSONParser(path.last(), (JSONObject) definitiveData).getNode();
                        } else {
                            final JSONObject toParse = new JSONObject();
                            toParse.put(path.last(), definitiveData);
                            parsedNodes = new JSONParser(path.last(), toParse).getNode().getChildByName(path.last());
                            parsedNodes.setParent(null);
                        }
                    }
                    
                } else {
                    if(!action.equals("updateSets")) {
                        parsedNodes = new JSONParser(path.last(), data).getNode();
                    } else {
                        setTransformation = new JSONArrayUpdateParser(path.last(), data).getNode();
                    }
                    parsedNodes = new JSONParser(path.last(), data).getNode();
                }
            } else {
                final JSONObject toParse = new JSONObject();
                toParse.put(path.last(), payload);
                if(!action.equals("updateSets")) {
                    parsedNodes = new JSONParser(path.last(), toParse).getNode().getChildByName(path.last());
                    parsedNodes.setParent(null);
                } else {
                    setTransformation = new JSONArrayUpdateParser(path.last(), toParse).getNode().getChildByName(path.last());
                    setTransformation.setParent(null);
                }
                parsedNodes = new JSONParser(path.last(), toParse).getNode().getChildByName(path.last());
                parsedNodes.setParent(null);
            }
        }
        if(jsonObject.has("allBinaries")) {
            allBinaries = jsonObject.getBoolean("allBinaries");
        }
        
        if(action.equals("get") && jsonObject.has(ResponseFields.DATA)) {
            final JSONObject metadata = jsonObject.getJSONObject(ResponseFields.DATA);
            if(metadata.has("loadBinaries")) {
                final Object loadBin = metadata.get("loadBinaries");
                if(!JSONArray.class.isInstance(loadBin)) {
                    throw EAVJsonExceptionMessage.InvalidLoadBinaries.create();
                }
                final JSONArray namedBinaries = (JSONArray) loadBin;
                loadBinaries = new HashSet<EAVPath>();
                for(int i = 0, size = namedBinaries.length(); i < size; i++) {
                    loadBinaries.add(EAVPath.parse(namedBinaries.getString(i)));
                }
            }
        }
        if(jsonObject.has("binaryEncoding")) {
            final String binEncoding = jsonObject.getString("binaryEncoding");
            if("base64".equalsIgnoreCase(binEncoding)) {
                raw = false;
            } else if ("raw".equalsIgnoreCase(binEncoding)) {
                raw = true;
            } else {
                throw EAVJsonExceptionMessage.UnknownBinaryEncoding.create(binEncoding);
            }
        }
        
        if(loadBinaries != null && jsonObject.has("allBinaries")) {
            throw EAVJsonExceptionMessage.ConflictingParameters.create("allBinaries", "loadBinaries");
        }
        
        if(jsonObject.has("timezone")) {
            timezone = TimeZone.getTimeZone(jsonObject.getString("timezone"));
        } else {
            timezone = TimeZone.getTimeZone(user.getTimeZone());
        }
        
        if(jsonObject.has("overwrite")) {
            overwrite = jsonObject.getBoolean("overwrite");
        }
    }

    public void setStorage(final EAVStorage storage) {
        this.storage = storage;
    }

    public Collection<AbstractOXException> getWarnings() {
        return Collections.<AbstractOXException> emptySet();
    }

}
