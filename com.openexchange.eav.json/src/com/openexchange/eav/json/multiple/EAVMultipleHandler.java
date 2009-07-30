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

package com.openexchange.eav.json.multiple;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVNodeTypeCoercionVisitor;
import com.openexchange.eav.EAVPath;
import com.openexchange.eav.EAVStorage;
import com.openexchange.eav.EAVType;
import com.openexchange.eav.EAVTypeCoercion;
import com.openexchange.eav.EAVTypeMetadataNode;
import com.openexchange.eav.json.exception.EAVJsonException;
import com.openexchange.eav.json.exception.EAVJsonExceptionMessage;
import com.openexchange.eav.json.parse.JSONParser;
import com.openexchange.eav.json.parse.metadata.type.JSONTypeMetadataParser;
import com.openexchange.eav.json.write.JSONWriter;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.tools.session.ServerSession;


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
    
    public void close() {
    
    }

    public Date getTimestamp() {
        return null;
    }

    public Object performRequest(String action, JSONObject jsonObject, ServerSession session) throws AbstractOXException, JSONException {
        this.action = action;
        parse(jsonObject);
        
        Context ctx = session.getContext();
        
        if(action.equals("new")) {
            if(types != null) {
                parsedNodes.visit(new EAVNodeTypeCoercionVisitor(types, null, EAVTypeCoercion.Mode.INCOMING));            
            }
            if(parsedNodes == null) {
                throw EAVJsonExceptionMessage.MISSING_PARAMETER.create("body");
            }
            storage.insert(ctx, path.parent(), parsedNodes);
        } else if (action.equals("update")){
            if(parsedNodes == null) {
                throw EAVJsonExceptionMessage.MISSING_PARAMETER.create("body");
            }
            EAVTypeMetadataNode savedTypes = storage.getTypes(ctx, path.parent(), parsedNodes);
            if(types != null) {
                types = types.mergeWith(savedTypes);
            } else {
                types = savedTypes;
            }
            parsedNodes.visit(new EAVNodeTypeCoercionVisitor(types, null, EAVTypeCoercion.Mode.INCOMING));            
            storage.update(ctx, path.parent(), parsedNodes);
            return 1;
        } else if (action.equals("delete")) {
            storage.delete(ctx, path);
            return 1;
        } else if (action.equals("get")) {
            EAVNode loaded = (loadBinaries == null) ? storage.get(ctx, path, allBinaries) : storage.get(ctx, path, loadBinaries);
            if(loaded.getType() == EAVType.BINARY && !loaded.isMultiple() && raw) {
                return loaded.getPayload();
            }
            if(types != null) {
                loaded.visit(new EAVNodeTypeCoercionVisitor(types, null, EAVTypeCoercion.Mode.OUTGOING));            
            }
            return new JSONWriter(loaded).getJson().get(path.last());
        }
        
        
        return 1;
        
    }

    private void parse(JSONObject jsonObject) throws JSONException, EAVJsonException {
        if(!jsonObject.has("path")) {
            throw EAVJsonExceptionMessage.MISSING_PARAMETER.create("path");
        }
        path = EAVPath.parse(jsonObject.getString("path"));
        parsedNodes = null;
        if(jsonObject.has(ResponseFields.DATA)) {
            Object payload = jsonObject.get(ResponseFields.DATA);
            
            if(JSONObject.class.isInstance(payload)) {
                JSONObject data = (JSONObject) payload;
                if(data.has("data") || data.has("types")) {
                    types = new JSONTypeMetadataParser(data.getJSONObject("types")).getEAVNode();
                    
                    if(data.has("data")) {
                        Object definitiveData = data.get("data");
                        if(JSONObject.class.isInstance(definitiveData)) {
                            parsedNodes = new JSONParser(path.last(), (JSONObject) definitiveData).getEAVNode();
                        } else {
                            JSONObject toParse = new JSONObject();
                            toParse.put(path.last(), definitiveData);
                            parsedNodes = new JSONParser(path.last(), toParse).getEAVNode().getChildByName(path.last());
                            parsedNodes.setParent(null);
                        }
                    }
                    
                } else {
                    parsedNodes = new JSONParser(path.last(), data).getEAVNode();
                }
            } else {
                JSONObject toParse = new JSONObject();
                toParse.put(path.last(), payload);
                parsedNodes = new JSONParser(path.last(), toParse).getEAVNode().getChildByName(path.last());
                parsedNodes.setParent(null);
            }
        }
        if(jsonObject.has("allBinaries")) {
            allBinaries = jsonObject.getBoolean("allBinaries");
        }
        
        if(action.equals("get") && jsonObject.has(ResponseFields.DATA)) {
            JSONObject metadata = jsonObject.getJSONObject(ResponseFields.DATA);
            if(metadata.has("loadBinaries")) {
                JSONArray namedBinaries = metadata.getJSONArray("loadBinaries");
                loadBinaries = new HashSet<EAVPath>();
                for(int i = 0, size = namedBinaries.length(); i < size; i++) {
                    loadBinaries.add(EAVPath.parse(namedBinaries.getString(i)));
                }
            }
        }
        if(jsonObject.has("binaryEncoding") && "base64".equalsIgnoreCase(jsonObject.getString("binaryEncoding"))) {
            raw = false;
        }
    }

    public void setStorage(EAVStorage storage) {
        this.storage = storage;
    }

}
