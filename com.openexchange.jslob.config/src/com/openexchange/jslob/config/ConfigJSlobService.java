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

package com.openexchange.jslob.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONValue;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.openexchange.ajax.tools.JSONUtil;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.impl.ConfigTree;
import com.openexchange.groupware.settings.impl.SettingStorage;
import com.openexchange.jslob.JSONPathElement;
import com.openexchange.jslob.JSONUpdate;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobExceptionCodes;
import com.openexchange.jslob.JSlobId;
import com.openexchange.jslob.JSlobService;
import com.openexchange.jslob.storage.JSlobStorage;
import com.openexchange.jslob.storage.registry.JSlobStorageRegistry;
import com.openexchange.log.LogFactory;
import com.openexchange.server.ServiceLookup;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link ConfigJSlobService}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConfigJSlobService implements JSlobService {

    private static final List<String> ALIASES = Arrays.asList("config");

    /**
     * <code>"preferencePath"</code>
     */
    private static final String PREFERENCE_PATH = "preferencePath".intern();

    /**
     * <code>"core"</code>
     */
    private static final String CORE = "core".intern();

    /**
     * <code>"meta"</code>
     */
    private static final String METADATA_PREFIX = "meta".intern();
    private static final String DATA_PREFIX = "data".intern();

    private static final String SERVICE_ID = "com.openexchange.jslob.config";

    /*-
     * ------------------------- Member stuff -----------------------------
     */

    private final ServiceLookup services;
    private final Map<String, Map<String, AttributedProperty>> preferenceItems;
    private final Map<String, String> core2AttributeMap;
    private final Map<String, String> attribute2CoreMap;

    /**
     * Initializes a new {@link ConfigJSlobService}.
     * 
     * @throws OXException If initialization fails
     */
    public ConfigJSlobService(final ServiceLookup services) throws OXException {
        super();
        this.services = services;
        this.preferenceItems = initPreferenceItems();
        // TODO: Initialize core name mapping
        final BiMap<String, String> biMap = HashBiMap.create(128);
        core2AttributeMap = biMap;
        attribute2CoreMap = biMap.inverse();
    }

    private Map<String, Map<String, AttributedProperty>> initPreferenceItems() throws OXException {
        // Read from config cascade
        final ConfigView view = getConfigViewFactory().getView();
        final Map<String, ComposedConfigProperty<String>> all = view.all();
        // Logger
        final Log logger = com.openexchange.log.Log.valueOf(LogFactory.getLog(ConfigJSlobService.class));
        // Initialize resulting map
        final int initialCapacity = all.size() >> 1;
        final Map<String, Map<String, AttributedProperty>> preferenceItems =
            new HashMap<String, Map<String, AttributedProperty>>(initialCapacity);
        for (final Map.Entry<String, ComposedConfigProperty<String>> entry : all.entrySet()) {
            // Check for existence of "preferencePath"
            final ComposedConfigProperty<String> property = entry.getValue();
            String preferencePath = property.get(PREFERENCE_PATH);
            if (null != preferencePath) {
                // e.g. ui/halo/configuration/property01
                final int firstSlashPos = preferencePath.indexOf('/');
                final String key = preferencePath.substring(0, firstSlashPos);
                preferencePath = preferencePath.substring(firstSlashPos + 1);
                Map<String, AttributedProperty> attributes = preferenceItems.get(key);
                if (null == attributes) {
                    attributes = new HashMap<String, AttributedProperty>(initialCapacity);
                    preferenceItems.put(key, attributes);
                }
                try {
                    attributes.put(preferencePath, new AttributedProperty(preferencePath, entry.getKey(), property));
                } catch (final Exception e) {
                    logger.warn("Couldn't initialize preference path: " + preferencePath, e);
                }
            }
        }
        return preferenceItems;
    }

    /**
     * Gets the <code>SessiondService</code>.
     *
     * @return The <code>SessiondService</code>
     */
    private SessiondService getSessiondService() {
        return services.getService(SessiondService.class);
    }

    @Override
    public Collection<JSlob> get(final int userId, final int contextId) throws OXException {
        final Collection<JSlob> list = getStorage().list(new JSlobId(SERVICE_ID, null, userId, contextId));
        final List<JSlob> ret = new ArrayList<JSlob>(list.size() << 1);
        for (final JSlob jSlob : list) {
            ret.add(get(jSlob.getId().getId(), userId, contextId));
        }
        final ConfigView view = getConfigViewFactory().getView(userId, contextId);
        for (final Entry<String, Map<String, AttributedProperty>> entry : preferenceItems.entrySet()) {
            final JSlob jSlob = new JSlob(new JSONObject());
            jSlob.setId(new JSlobId(SERVICE_ID, entry.getKey(), userId, contextId));
            for (final Entry<String, AttributedProperty> entry2 : entry.getValue().entrySet()) {
                add2JSlob(entry2.getValue(), jSlob, view);
            }
            ret.add(jSlob);
        }
        ret.add(getCoreJslob(userId, contextId));
        return ret;
    }

    @Override
    public JSlob get(final String id, final int userId, final int contextId) throws OXException {
        if (CORE.equals(id)) {
            return getCoreJslob(userId, contextId);
        }
        /*
         * Get from storage
         */
        JSlob jsonJSlob = getStorage().opt(new JSlobId(SERVICE_ID, id, userId, contextId));
        if (null == jsonJSlob) {
            jsonJSlob = new JSlob(new JSONObject());
            jsonJSlob.setId(new JSlobId(SERVICE_ID, id, userId, contextId));
        }
        /*
         * Fill with config cascade settings
         */
        final Map<String, AttributedProperty> attributes = preferenceItems.get(id);
        if (null != attributes) {
            final ConfigView view = getConfigViewFactory().getView(userId, contextId);
            for (final AttributedProperty attributedProperty : attributes.values()) {
                add2JSlob(attributedProperty, jsonJSlob, view);
            }
        }
        return jsonJSlob;
    }

    /**
     * Gets the special <code>"core"</code> {@link JSlob} instance.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The special <code>"core"</code> {@link JSlob} instance.
     * @throws OXException If operation fails
     */
    private JSlob getCoreJslob(final int userId, final int contextId) throws OXException {
        try {
            // Special handling then
            final SettingStorage stor = SettingStorage.getInstance(getSessiondService().getAnyActiveSessionForUser(userId, contextId));
            final ConfigTree configTree = ConfigTree.getInstance();
            final JSONObject jObject = new JSONObject();
            for (final Setting setting : configTree.getSettings()) {
                String attributeName = core2AttributeMap.get(setting.getPath());
                if (null == attributeName) {
                    attributeName = setting.getPath();
                }
                stor.readValues(setting);
                jObject.put(attributeName, convert2JS(setting));
            }
            return new JSlob(jObject);
        } catch (final JSONException e) {
            throw JSlobExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Converts a tree of settings into the according java script objects.
     * 
     * @param setting Tree of settings.
     * @return java script object representing the setting tree.
     * @throws JSONException if the conversion to java script objects fails.
     */
    private static Object convert2JS(final Setting setting) throws JSONException {
        Object retval = null;
        if (setting.isLeaf()) {
            final Object[] multiValue = setting.getMultiValue();
            if (null == multiValue) {
                final Object singleValue = setting.getSingleValue();
                if (null == singleValue) {
                    retval = JSONObject.NULL;
                } else if (singleValue instanceof JSONObject) {
                    retval = singleValue;
                } else {
                    try {
                        retval = new JSONObject(singleValue.toString());
                    } catch (final JSONException e) {
                        retval = singleValue;
                    }
                }
            } else {
                final JSONArray array = new JSONArray();
                for (final Object value : multiValue) {
                    array.put(value);
                }
                retval = array;
            }
        } else {
            final JSONObject json = new JSONObject();
            for (final Setting subSetting : setting.getElements()) {
                json.put(subSetting.getName(), convert2JS(subSetting));
            }
            retval = json;
        }
        return retval;
    }
    
	@Override
    public String getIdentifier() {
        return SERVICE_ID;
    }

    @Override
    public List<String> getAliases() {
        return ALIASES;
    }

    @Override
    public void set(final String id, final JSlob jsonJSlob, final int userId, final int contextId) throws OXException {
        if (CORE.equals(id)) {
            // Special handling then
            if (null == jsonJSlob) {
                throw JSlobExceptionCodes.SET_NOT_SUPPORTED.create(id, SERVICE_ID);
            }
            final JSONObject jObject = jsonJSlob.getJsonObject();
            if (null == jObject) {
                throw JSlobExceptionCodes.SET_NOT_SUPPORTED.create(id, SERVICE_ID);
            }
            // Iterate JSON object
            final SettingStorage stor = SettingStorage.getInstance(getSessiondService().getAnyActiveSessionForUser(userId, contextId));
            final ConfigTree configTree = ConfigTree.getInstance();
            for (final Entry<String, Object> entry : jObject.entrySet()) {
                String path = attribute2CoreMap.get(entry.getKey());
                if (null == path) {
                    path = entry.getKey();
                }
                if (path.length() > 0 && path.charAt(0) == '/') {
                    path = path.substring(1);
                }
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }
                final Setting setting = configTree.getSettingByPath(path);
                setting.setSingleValue(entry.getValue());
                saveSettingWithSubs(stor, setting);
            }
            return;
        }
        /*
         * Set in storage (other than "core")
         */
        if (null == jsonJSlob) {
            getStorage().remove(new JSlobId(SERVICE_ID, id, userId, contextId));
        } else {
            final JSONObject jObject = jsonJSlob.getJsonObject();
            if (null == jObject) {
                getStorage().remove(new JSlobId(SERVICE_ID, id, userId, contextId));
                return;
            }
            // Remember the paths to purge
            final List<List<JSONPathElement>> pathsToPurge = new LinkedList<List<JSONPathElement>>();
            // Set (or replace) JSlob
            final Map<String, AttributedProperty> attributes = preferenceItems.get(id);
            if (null == attributes) {
                // Store JSlob in common storage
                getStorage().store(new JSlobId(SERVICE_ID, id, userId, contextId), jsonJSlob);
            } else {
                // A config cascade change because identifier refers to a preference item
                final ConfigView view = getConfigViewFactory().getView(userId, contextId);
                for (final AttributedProperty attributedProperty : attributes.values()) {
                    final Object value = JSONPathElement.getPathFrom(attributedProperty.path, jObject);
                    if (null != value) {
                    	if (view.property(attributedProperty.propertyName, String.class).isDefined()) {
                            pathsToPurge.add(attributedProperty.path);
                    		final Object oldValue = asJSObject(view.get(attributedProperty.propertyName, String.class));
                            // Clients have a habit of dumping the config back at us, so we only save differing values.
                            if (!value.equals(oldValue)) {
                                view.set("user", attributedProperty.propertyName, value);
                            }
                    	}
                    }
                }
            }
            for (final List<JSONPathElement> path: pathsToPurge) {
            	JSONPathElement.remove(path, jObject);
            }
            jsonJSlob.setJsonObject(jObject);
            // Finally store JSlob
            getStorage().store(new JSlobId(SERVICE_ID, id, userId, contextId), jsonJSlob);
        }
    }

    /**
     * Splits a value for a not leaf setting into its sub-settings and stores them.
     * 
     * @param storage setting storage.
     * @param setting actual setting.
     * @throws OXException If an error occurs.
     */
    private void saveSettingWithSubs(final SettingStorage storage, final Setting setting) throws OXException {
        try {
            if (setting.isLeaf()) {
                final String value = (String) setting.getSingleValue();
                if (null != value && value.length() > 0 && '[' == value.charAt(0)) {
                    final JSONArray array = new JSONArray(value);
                    if(array.length() == 0) {
                        setting.setEmptyMultiValue();
                    } else {
                        for (int i = 0; i < array.length(); i++) {
                            setting.addMultiValue(array.getString(i));
                        }
                    }
                    setting.setSingleValue(null);
                }
                storage.save(setting);
            } else {
                final JSONObject json = new JSONObject(setting.getSingleValue().toString());
                final Iterator<String> iter = json.keys();
                OXException exc = null;
                while (iter.hasNext()) {
                    final String key = iter.next();
                    final Setting sub = ConfigTree.getSettingByPath(setting, new String[] { key });
                    sub.setSingleValue(json.getString(key));
                    try {
                        // Catch single exceptions if GUI writes not writable fields.
                        saveSettingWithSubs(storage, sub);
                    } catch (final OXException e) {
                        exc = e;
                    }
                }
                if (null != exc) {
                    throw exc;
                }
            }
        } catch (final JSONException e) {
            throw JSlobExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void update(final String id, final JSONUpdate jsonUpdate, final int userId, final int contextId) throws OXException {
        try {
            if (CORE.equals(id)) {
                // Special handling then
                final List<JSONPathElement> path = jsonUpdate.getPath();
                if (path.isEmpty()) {
                    /*
                     * Merge whole object
                     */
                    final JSONObject merged = JSONUtil.merge(getCoreJslob(userId, contextId).getJsonObject(), (JSONObject) jsonUpdate.getValue());
                    set(id, new JSlob(merged), userId, contextId);
                    return;
                }
                /*
                 * Update by path
                 */
                final StringBuilder pathBuilder = new StringBuilder(16);
                pathBuilder.append(path.get(0).toString());
                for (int i = 1, size = path.size(); i < size; i++) {
                    pathBuilder.append('/').append(path.get(i).toString());
                }
                final Object value = jsonUpdate.getValue();
                if (null != value) {
                    final SettingStorage stor = SettingStorage.getInstance(getSessiondService().getAnyActiveSessionForUser(userId, contextId));
                    final ConfigTree configTree = ConfigTree.getInstance();
                    final Setting setting = configTree.getSettingByPath(pathBuilder.toString());
                    setting.setSingleValue(value);
                    saveSettingWithSubs(stor, setting);
                }
                return;
            }
            /*
             * Look-up appropriate storage
             */
            final JSlobStorage storage = getStorage();
            final JSlobId jslobId = new JSlobId(SERVICE_ID, id, userId, contextId);
            /*
             * Get JSlob
             */
            final JSONObject storageObject;
            JSlob jsonJSlob = storage.opt(jslobId);
            if (null == jsonJSlob) {
                jsonJSlob = new JSlob();
                storageObject = new JSONObject();
            } else {
                storageObject = jsonJSlob.getJsonObject();
            }
            /*
             * Examine path
             */
            final List<JSONPathElement> path = jsonUpdate.getPath();
            if (path.isEmpty()) {
                /*
                 * Merge whole object
                 */
                final JSONObject merged = JSONUtil.merge(storageObject, (JSONObject) jsonUpdate.getValue());
                set(id, jsonJSlob.setJsonObject(merged), userId, contextId);
                return;
            }
            /*
             * Update in config cascade
             */
            final int size = path.size();
            final Map<String, AttributedProperty> attributes = preferenceItems.get(id);
            if (null == attributes) {
                /*-
                 * Update in store
                 * 
                 * Iterate path except last element
                 */
                final int msize = size - 1;
                JSONObject current = storageObject;
                for (int i = 0; i < msize; i++) {
                    final JSONPathElement jsonPathElem = path.get(i);
                    final int index = jsonPathElem.getIndex();
                    final String name = jsonPathElem.getName();
                    if (index >= 0) {
                        /*
                         * Denotes an index within a JSON array
                         */
                        if (isInstance(name, JSONArray.class, current)) {
                            final JSONArray jsonArray = current.getJSONArray(name);
                            if (index >= jsonArray.length()) {
                                current = putNewJSONObject(jsonArray);
                            } else {
                                if (isInstance(index, JSONObject.class, jsonArray)) {
                                    current = jsonArray.getJSONObject(index);
                                } else {
                                    current = putNewJSONObject(index, jsonArray);
                                }
                            }
                        } else {
                            final JSONArray newArray = new JSONArray();
                            current.put(name, newArray);
                            current = putNewJSONObject(newArray);
                        }
                    } else {
                        /*
                         * Denotes an element within a JSON object
                         */
                        if (isInstance(name, JSONObject.class, current)) {
                            current = current.getJSONObject(name);
                        } else {
                            final JSONObject newObject = new JSONObject();
                            current.put(name, newObject);
                            current = newObject;
                        }
                    }
                }
                /*
                 * Handle last path element
                 */
                final JSONPathElement lastPathElem = path.get(msize);
                final int index = lastPathElem.getIndex();
                final String name = lastPathElem.getName();
                if (index >= 0) {
                    if (isInstance(name, JSONArray.class, current)) {
                        current.getJSONArray(name).put(index, jsonUpdate.getValue());
                    } else {
                        final JSONArray newArray = new JSONArray();
                        current.put(name, newArray);
                        newArray.put(jsonUpdate.getValue());
                    }
                } else {
                    current.put(name, jsonUpdate.getValue());
                }
                /*
                 * Write to store
                 */
                storage.store(jslobId, jsonJSlob.setJsonObject(storageObject));
            } else {
                /*
                 * A config cascade change because identifier refers to a preference item
                 */
                final StringBuilder pathBuilder = new StringBuilder(16);
                pathBuilder.append(path.get(0).toString());
                for (int i = 1; i < size; i++) {
                    pathBuilder.append('/').append(path.get(i).toString());
                }
                final AttributedProperty attributedProperty = attributes.get(pathBuilder.toString());
                if (null == attributedProperty) {
                    /*
                     * No such property
                     */
                    throw JSlobExceptionCodes.PATH_NOT_FOUND.create(pathBuilder.toString());
                }
                final Object value = jsonUpdate.getValue();
                if (null != value) {
                    try {
                        final ConfigView view = getConfigViewFactory().getView(userId, contextId);
                        final String oldValue = view.get(attributedProperty.propertyName, String.class);
                        // Clients have a habit of dumping the config back at us, so we only save differing values.
                        if (!value.equals(oldValue)) {
                            view.set("user", attributedProperty.propertyName, value);
                        }
                    } catch (final OXException e) {
                        throw new OXException(e);
                    }
                }
            }
        } catch (final JSONException e) {
            throw JSlobExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static JSONObject putNewJSONObject(final JSONArray jsonArray) throws JSONException {
        return putNewJSONObject(-1, jsonArray);
    }

    private static JSONObject putNewJSONObject(final int index, final JSONArray jsonArray) throws JSONException {
        final JSONObject newObject = new JSONObject();
        if (index >= 0) {
            jsonArray.put(index, newObject);
        } else {
            jsonArray.put(newObject);
        }
        return newObject;
    }

    private static boolean isInstance(final String name, final Class<? extends JSONValue> clazz, final JSONObject jsonObject) {
        if (!jsonObject.hasAndNotNull(name)) {
            return false;
        }
        return clazz.isInstance(jsonObject.opt(name));
    }

    private static boolean isInstance(final int index, final Class<? extends JSONValue> clazz, final JSONArray jsonArray) {
        return clazz.isInstance(jsonArray.opt(index));
    }

    private JSlobStorage getStorage() throws OXException {
        final JSlobStorageRegistry storageRegistry = services.getService(JSlobStorageRegistry.class);
        // TODO: Make configurable
        final String storageId = "io.ox.wd.jslob.storage.db";
        final JSlobStorage storage = storageRegistry.getJSlobStorage(storageId);
        if (null == storage) {
            throw JSlobExceptionCodes.NOT_FOUND.create(storageId);
        }
        return storage;
    }

    private ConfigViewFactory getConfigViewFactory() {
        return services.getService(ConfigViewFactory.class);
    }
    
    private static final Set<String> SKIP_META = new HashSet<String>(Arrays.asList("final", "protected", "preferencePath"));
    
    private static void add2JSlob(final AttributedProperty attributedProperty, final JSlob jsonJSlob, final ConfigView view) throws OXException {
        if (null == attributedProperty) {
            return;
        }
        try {
            // Add property's value
            final List<JSONPathElement> path = attributedProperty.path;
            Object value = asJSObject(view.get(attributedProperty.propertyName, String.class));

            addValueByPath(path, value, jsonJSlob.getJsonObject());
            
            // Add the metadata as well as a separate JSON object
            final JSONObject jMetaData = new JSONObject();
            final ComposedConfigProperty<String> preferenceItem = attributedProperty.property;
            final List<String> metadataNames = preferenceItem.getMetadataNames();
            if (null != metadataNames && !metadataNames.isEmpty()) {
                for (final String metadataName : metadataNames) {
                    if (SKIP_META.contains(metadataName)) {
                    	continue;
                    }
                	// Metadata value
                    final ComposedConfigProperty<String> prop = view.property(attributedProperty.propertyName, String.class);
                    value = asJSObject(prop.get(metadataName));
                    jMetaData.put(metadataName, value);
                }
            }
            // Lastly, let's add configurability.
            final String finalScope = preferenceItem.get("final");
            final String isProtected = preferenceItem.get("protected");
            final boolean writable =
                (finalScope == null || finalScope.equals("user")) && (isProtected == null || !preferenceItem.get("protected", boolean.class).booleanValue());
            value = Boolean.valueOf(writable);
            if (!(Boolean)value) {
                jMetaData.put("configurable", value);
            }
            
            
            if (jMetaData.length() > 0) {
                addValueByPath(path, jMetaData, jsonJSlob.getMetaObject());
            }
        } catch (final JSONException e) {
            throw JSlobExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static void addValueByPath(final List<JSONPathElement> path, final Object value, final JSONObject object) throws JSONException {
        final int msize = path.size() - 1;
        JSONObject current = object;
        for (int i = 0; i < msize; i++) {
            final JSONPathElement jsonPathElem = path.get(i);
            final int index = jsonPathElem.getIndex();
            final String name = jsonPathElem.getName();
            if (index >= 0) {
                /*
                 * Denotes an index within a JSON array
                 */
                if (isInstance(name, JSONArray.class, current)) {
                    final JSONArray jsonArray = current.getJSONArray(name);
                    if (index >= jsonArray.length()) {
                        current = putNewJSONObject(jsonArray);
                    } else {
                        if (isInstance(index, JSONObject.class, jsonArray)) {
                            current = jsonArray.getJSONObject(index);
                        } else {
                            current = putNewJSONObject(index, jsonArray);
                        }
                    }
                } else {
                    final JSONArray newArray = new JSONArray();
                    current.put(name, newArray);
                    current = putNewJSONObject(newArray);
                }
            } else {
                /*
                 * Denotes an element within a JSON object
                 */
                if (isInstance(name, JSONObject.class, current)) {
                    current = current.getJSONObject(name);
                } else {
                    final JSONObject newObject = new JSONObject();
                    current.put(name, newObject);
                    current = newObject;
                }
            }
        }
        /*
         * Handle last path element
         */
        final JSONPathElement lastPathElem = path.get(msize);
        final int index = lastPathElem.getIndex();
        final String name = lastPathElem.getName();
        if (index >= 0) {
            if (isInstance(name, JSONArray.class, current)) {
                current.getJSONArray(name).put(index, value);
            } else {
                final JSONArray newArray = new JSONArray();
                current.put(name, newArray);
                newArray.put(value);
            }
        } else {
            current.put(name, value);
        }
    }

    /**
     * Converts given String to a regular JSON-supported value.
     * <p>
     * The value can be a Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
     *
     * @param propertyValue The value to convert
     * @return The resulting value; either Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object
     */
    private static Object asJSObject(final String propertyValue) {
        if (null == propertyValue) {
            return null;
        }
        try {
            return new JSONTokener(propertyValue).nextValue();
        } catch (final Exception e) {
            return propertyValue;
        }
    }

    /**
     * <ul>
     * <li><b><code>preferencePath</code></b>:<br>
     * &nbsp;&nbsp;The preference path</li>
     * <li><b><code>propertyName</code></b>:<br>
     * &nbsp;&nbsp;The property name of the preference item property.</li>
     * <li><b><code>property</code></b>:<br>
     * &nbsp;&nbsp;The property representing a preference item.</li>
     * <li><b><code>path</code></b>:<br>
     * &nbsp;&nbsp;The parsed preference path.</li>
     * </ul>
     */
    private static final class AttributedProperty {

        /**
         * The property name of the preference item property.
         */
        protected final String propertyName;

        /**
         * The preference path; ending with <code>"/value"</code>.
         */
        protected final String preferencePath;

        /**
         * The property representing a preference item.
         */
        protected final ComposedConfigProperty<String> property;

        /**
         * The parsed preference path.
         */
        protected final List<JSONPathElement> path;

        protected AttributedProperty(final String preferencePath, final String propertyName, final ComposedConfigProperty<String> property) throws OXException {
            super();
            this.propertyName = propertyName;
            this.property = property;
            this.preferencePath = preferencePath;
            path = JSONPathElement.parsePath(preferencePath);
        }
    } // End of class AttributedProperty

}
