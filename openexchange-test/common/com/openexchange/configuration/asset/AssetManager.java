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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.configuration.asset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.tools.encoding.Base64;

/**
 * {@link AssetManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AssetManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetManager.class);

    private final Map<AssetType, Map<AssetKey, Asset>> assetRegistry;

    /**
     * Initialises a new {@link AssetManager}.
     */
    public AssetManager() {
        super();
        assetRegistry = new EnumMap<AssetType, Map<AssetKey, Asset>>(AssetType.class);
        readAssets();
    }

    /**
     * Reads the assets folder
     */
    private void readAssets() {
        String path = AJAXConfig.getProperty(Property.TEST_DIR);
        File assetsPath = new File(path);
        if (!assetsPath.exists()) {
            throw new IllegalArgumentException("The path '" + path + "' for the assets folder is invalid.");
        }

        if (!assetsPath.isDirectory()) {
            throw new IllegalArgumentException("The path '" + path + "' is not a folder.");
        }

        scanDir(assetsPath);
    }

    /**
     * Scans the specified directory
     * 
     * @param dir The directory to scan
     */
    private void scanDir(File dir) {
        if (!dir.isDirectory()) {
            LOGGER.debug("The specified path '{}' does not denote a directory.", dir.getAbsolutePath());
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                scanDir(file);
            }
            examineFile(file);
        }
    }

    /**
     * Examines the specified file and adds it to the asset registry if it has a known extension
     * 
     * @param file The file to examine
     */
    private void examineFile(File file) {
        LOGGER.debug("Examining path '{}'...", file.getAbsolutePath());
        String filename = file.getName();
        int indexOfDot = filename.lastIndexOf('.');
        if (indexOfDot < 0) {
            LOGGER.warn("Unable to extract any extensions from '{}'. Skipping.", filename);
            return;
        }
        String extension = filename.substring(indexOfDot + 1);
        AssetType assetType;
        try {
            assetType = AssetType.valueOf(extension);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Unknown asset type detected '{}'", extension);
            return;
        }

        AssetKey key = new AssetKey(assetType, filename);
        Asset asset = new Asset(assetType, file.getAbsolutePath(), filename);

        putInRegistry(key, asset);
    }

    /**
     * Puts the specified asset in to the registry
     * 
     * @param key The asset's key
     * @param asset The asset
     */
    private void putInRegistry(AssetKey key, Asset asset) {
        Map<AssetKey, Asset> assets = assetRegistry.get(key.getAssetType());
        if (assets == null) {
            assets = new HashMap<AssetKey, Asset>();
        }
        assets.put(key, asset);
        assetRegistry.put(key.getAssetType(), assets);
    }

    /**
     * Get the asset specified by the asset type and filename
     * 
     * @param assetType The asset type
     * @param filename The filename with extension
     * @return The asset
     * @throws Exception if the asset is not known by the asset manager
     */
    public Asset getAsset(AssetType assetType, String filename) throws Exception {
        Map<AssetKey, Asset> assets = assetRegistry.get(assetType);
        if (assets == null) {
            throw new Exception("The asset with type '" + assetType + "' and name '" + filename + "' is not known to the AssetManager");
        }
        Asset asset = assets.get(new AssetKey(assetType, filename));
        if (asset == null) {
            throw new Exception("The asset with type '" + assetType + "' and name '" + filename + "' is not known to the AssetManager");
        }
        return asset;
    }

    /**
     * Reads the specified {@link Asset} and returns it as a raw byte array
     * 
     * @param asset The {@link Asset} to read
     * @return The asset as a raw byte array
     * @throws IOException if an I/O error occurs
     */
    public byte[] readAssetRaw(Asset asset) throws IOException {
        byte[] bytes = FileUtils.readFileToByteArray(new File(asset.getAbsolutePath()));
        return bytes;
    }

    /**
     * Reads the specified {@link Asset} and returns it as a base 64 string
     * 
     * @param asset The {@link Asset} to read
     * @return The base 64 encoded asset
     * @throws IOException if an I/O error occurs
     */
    public String readAssetBase64(Asset asset) throws IOException {
        byte[] bytes = readAssetRaw(asset);
        return Base64.encode(bytes);
    }

    /**
     * Reads the specified {@link Asset} and returns it as a string
     * 
     * @param asset The {@link Asset} to read
     * @return The content of the asset as string
     * @throws IOException if an I/O error occurs
     */
    public String readAssetString(Asset asset) throws IOException {
        return FileUtils.readFileToString(new File(asset.getAbsolutePath()));
    }

    /**
     * Get a random asset
     * 
     * @param assetType The asset type
     * @return A random asset from the registry
     * @throws Exception if there are no assets of the specified {@link AssetType} in the registry
     */
    public Asset getRandomAsset(AssetType assetType) throws Exception {
        Map<AssetKey, Asset> assets = assetRegistry.get(assetType);
        if (assets == null) {
            throw new Exception("No assets found with type '" + assetType + "' in the asset registry");
        }
        List<AssetKey> keys = new ArrayList<AssetKey>(assets.keySet());

        Random random = new Random();
        // Pick a random key
        AssetKey key = keys.get(random.nextInt(keys.size()));

        return assets.get(key);
    }

    /**
     * Returns an unmodifiable collection of all known assets of the specified asset type
     * 
     * @param assetType The asset type
     * @return an unmodifiable collection of all known assets of the specified asset type
     */
    public Collection<Asset> getAssets(AssetType assetType) {
        Map<AssetKey, Asset> assets = assetRegistry.get(assetType);
        if (assets == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection(assets.values());
    }
}
