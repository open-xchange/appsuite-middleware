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

package com.openexchange.folderstorage.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.ContentTypeDiscoveryService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageComparator;
import com.openexchange.folderstorage.virtual.VirtualFolderStorage;
import com.openexchange.folderstorage.virtual.VirtualFolderType;

/**
 * {@link ContentTypeRegistry} - A registry for a tree's content types.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ContentTypeRegistry implements ContentTypeDiscoveryService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContentTypeRegistry.class);

    private static final ContentTypeRegistry instance = new ContentTypeRegistry();

    /**
     * Gets the {@link ContentTypeRegistry} instance.
     *
     * @return The {@link ContentTypeRegistry} instance
     */
    public static ContentTypeRegistry getInstance() {
        return instance;
    }

    private static final class Element {

        private final ConcurrentMap<ContentType, FolderStorage> concreteStorages;
        private volatile Queue<FolderStorage> generalStorages;

        Element() {
            super();
            concreteStorages = new ConcurrentHashMap<ContentType, FolderStorage>();
            generalStorages = new ConcurrentLinkedQueue<FolderStorage>();
        }

        public ConcurrentMap<ContentType, FolderStorage> getConcreteStorages() {
            return concreteStorages;
        }

        public Queue<FolderStorage> getGeneralStorages() {
            return generalStorages;
        }

        /**
         * Performs an atomic clear&addAll by reassigning volatile variable.
         *
         * @param replacement The replacement
         */
        public void replaceGeneralStorages(final List<FolderStorage> replacement) {
            generalStorages = new ConcurrentLinkedQueue<FolderStorage>(replacement);
        }

    }

    /*
     * Member section
     */

    private final ConcurrentMap<String, Element> registry;

    /**
     * Initializes a new {@link ContentTypeRegistry}.
     */
    private ContentTypeRegistry() {
        super();
        registry = new ConcurrentHashMap<String, Element>();
    }

    private Element getElementForTreeId(final String treeId) {
        return getElementForTreeId(treeId, true);
    }

    private Element getElementForTreeId(final String treeId, final boolean createIfAbsent) {
        Element element = registry.get(treeId);
        if (null == element && createIfAbsent) {
            final Element inst = new Element();
            element = registry.putIfAbsent(treeId, inst);
            if (null == element) {
                element = inst;
            }
        }
        return element;
    }

    /**
     * Associates specified folder storage to given content type.
     *
     * @param treeId The folder storage's tree identifier
     * @param contentType The content type to register
     * @param folderStorage The content type's folder storage
     * @return <code>true</code> if content type was successfully registered; otherwise <code>false</code>
     */
    public boolean addContentType(final String treeId, final ContentType contentType, final FolderStorage folderStorage) {
        final ConcurrentMap<ContentType, FolderStorage> types = getElementForTreeId(treeId).getConcreteStorages();
        final boolean added = (null == types.putIfAbsent(contentType, folderStorage));
        if (!added) {
            final StringBuilder sb = new StringBuilder(32);
            sb.append("Could not register content type \"");
            sb.append(contentType.getClass().getName());
            sb.append("\" for tree identifier \"").append(treeId).append("\". Duplicate content type detected.");
            LOG.error(sb.toString());
        }
        return added;
    }

    /**
     * Adds a general-purpose folder storage (capable to serve every content type) to this registry.
     *
     * @param treeId The folder storage's tree identifier
     * @param folderStorage The general-purpose folder storage
     * @return <code>true</code> if folder storage was successfully registered; otherwise <code>false</code>
     */
    public boolean addGeneralContentType(final String treeId, final FolderStorage folderStorage) {
        final Element element = getElementForTreeId(treeId);
        final List<FolderStorage> generalStorages = new ArrayList<FolderStorage>(element.getGeneralStorages());
        generalStorages.add(folderStorage);
        // Order by storage priority
        Collections.sort(generalStorages, FolderStorageComparator.getInstance());
        element.replaceGeneralStorages(generalStorages);
        return true;
    }

    /**
     * Gets the available content types.
     *
     * @return The available content types
     */
    public Map<Integer, List<ContentType>> getAvailableContentTypes() {
        ConcurrentMap<ContentType, FolderStorage> concreteStorages = getElementForTreeId(FolderStorage.REAL_TREE_ID).getConcreteStorages();
        if (concreteStorages.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Integer, List<ContentType>> ret = new HashMap<Integer, List<ContentType>>(concreteStorages.size());
        for (ContentType contentType : concreteStorages.keySet()) {
            com.openexchange.tools.arrays.Collections.put(ret, I(contentType.getModule()), contentType);
        }
        return ret;
    }

    /**
     * Gets the specified content type's storage.
     *
     * @param treeId The tree identifier
     * @param contentType The content type
     * @return The content type's storage or <code>null</code>
     */
    public FolderStorage getFolderStorageByContentType(final String treeId, final ContentType contentType) {
        final Element element = getElementForTreeId(treeId, false);
        if (null == element) {
            if (VirtualFolderType.getInstance().servesTreeId(treeId)) {
                return VirtualFolderStorage.getInstance();
            }
            return null;
        }
        // Look-up in general-purpose folder storages
        final Queue<FolderStorage> generalStorages = element.getGeneralStorages();
        if (!generalStorages.isEmpty()) {
            return generalStorages.peek();
        }
        final ConcurrentMap<ContentType, FolderStorage> types = element.getConcreteStorages();
        if (null == types) {
            return null;
        }
        return types.get(contentType);
    }

    @Override
    public ContentType getByString(final String contentTypeString) {
        for (final Entry<String, Element> entry : registry.entrySet()) {
            final Queue<FolderStorage> generalStorages = entry.getValue().getGeneralStorages();
            /*
             * Iterate general storages' content types
             */
            ContentType candidate = null;
            for (final FolderStorage genStorage : generalStorages) {
                final FolderStorage folderStorage = genStorage;
                final ContentType[] supportedContentTypes = folderStorage.getSupportedContentTypes();
                for (final ContentType supportedContentType : supportedContentTypes) {
                    if (supportedContentType.toString().equals(contentTypeString) && (null == candidate || candidate.getPriority() < supportedContentType.getPriority())) {
                        candidate = supportedContentType;
                    }
                }
            }
            if (candidate != null) {
                return candidate;
            }
            /*
             * Iterate concrete content types
             */
            final Set<ContentType> concreteCTs = entry.getValue().getConcreteStorages().keySet();
            for (final ContentType contentType : concreteCTs) {
                if (contentType.toString().equals(contentTypeString) && (null == candidate || candidate.getPriority() < contentType.getPriority())) {
                    candidate = contentType;
                }
            }
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Removes specified content type.
     *
     * @param treeId The tree identifier
     * @param contentType The content type
     */
    public void removeContentType(final String treeId, final ContentType contentType) {
        final Element element = getElementForTreeId(treeId, false);
        if (null == element) {
            return;
        }
        // Remove from concrete storages
        element.getConcreteStorages().remove(contentType);
    }

    /**
     * Removes specified general-purpose folder storage.
     *
     * @param treeId The tree identifier
     * @param folderStorage The general-purpose folder storage
     */
    public void removeGeneralContentType(final String treeId, final FolderStorage folderStorage) {
        final Element element = getElementForTreeId(treeId, false);
        if (null == element) {
            return;
        }
        // Remove from general storages
        final List<FolderStorage> generalStorages = new ArrayList<FolderStorage>(element.getGeneralStorages());
        generalStorages.remove(folderStorage);
        Collections.sort(generalStorages, FolderStorageComparator.getInstance());
        element.replaceGeneralStorages(generalStorages);
    }

    /**
     * Removes tree's content types.
     *
     * @param treeId The tree identifier
     */
    public void removeTreeContentTypes(final String treeId) {
        registry.remove(treeId);
    }

    @Override
    public ContentType getByModule(int module) {
        for (final Entry<String, Element> entry : registry.entrySet()) {
            final Queue<FolderStorage> generalStorages = entry.getValue().getGeneralStorages();
            /*
             * Iterate general storages' content types
             */
            ContentType candidate = null;
            for (final FolderStorage genStorage : generalStorages) {
                final FolderStorage folderStorage = genStorage;
                final ContentType[] supportedContentTypes = folderStorage.getSupportedContentTypes();
                for (final ContentType supportedContentType : supportedContentTypes) {
                    if (supportedContentType.getModule() == module && (null == candidate || candidate.getPriority() < supportedContentType.getPriority())) {
                        candidate = supportedContentType;
                    }
                }
            }
            if (candidate != null) {
                return candidate;
            }
            /*
             * Iterate concrete content types
             */
            final Set<ContentType> concreteCTs = entry.getValue().getConcreteStorages().keySet();
            for (final ContentType contentType : concreteCTs) {
                if (contentType.getModule() == module && (null == candidate || candidate.getPriority() < contentType.getPriority())) {
                    candidate = contentType;
                }
            }
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

}
