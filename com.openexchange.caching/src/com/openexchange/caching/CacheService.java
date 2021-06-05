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

package com.openexchange.caching;

import java.io.InputStream;
import java.util.Properties;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link CacheService} - The cache service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface CacheService extends CacheKeyService {

    /**
     * Indicates if this cache is distributed.
     * <ul>
     * <li>
     * <p>
     * Data in the cluster is almost evenly distributed (partitioned) across all nodes. So each node carries ~ (1/n
     * <code class="literal">*</code> total-data) + backups , n being the number of nodes in the cluster.
     * </p>
     * </li>
     * <li>
     * <p>
     * If a member goes down, its backup replica that also holds the same data, will dynamically redistribute the data including the
     * ownership and locks on them to remaining live nodes. As a result, no data will get lost.
     * </p>
     * </li>
     * <li>
     * <p>
     * When a new node joins the cluster, new node takes ownership(responsibility) and load of -some- of the entire data in the cluster.
     * Eventually the new node will carry almost (1/n <code class="literal">*</code> total-data) + backups and becomes the new partition
     * reducing the load on others.
     * </p>
     * </li>
     * <li>
     * <p>
     * There is no single cluster master or something that can cause single point of failure. Every node in the cluster has equal rights and
     * responsibilities. No-one is superior. And no dependency on external 'server' or 'master' kind of concept.
     * </p>
     * </li>
     * </ul>
     *
     * @return <code>true</code> if this cache has a distributed nature; otherwise <code>false</code> (a replicated nature)
     */
    public boolean isDistributed();

    /**
     * Indicates if this cache is replicated.
     * <p>
     * Data is kept redundantly on every linked node.
     *
     * @return <code>true</code> if this cache has a replicated nature; otherwise <code>false</code> (a distributed nature)
     */
    public boolean isReplicated();

    /**
     * Gets a cache which accesses the provided region.
     * <p>
     * An already initialized cache for specified region is kept in a map to avoid multiple instantiations for the same region.
     *
     * @param name The region name
     * @return A cache which accesses the provided region.
     * @throws OXException If cache cannot be obtained
     */
    public Cache getCache(String name) throws OXException;

    /**
     * The cache identified through given name is removed from this cache service and all of its items are going to be disposed.<br>
     * <b>Note</b>: Caches which were added through default configuration are not freed from this cache service; meaning those invocations
     * are treated as a no-op.
     * <p>
     * The cache is then no more accessible through this cache service; meaning attempts to get the cache by {@link #getCache(String)} will
     * fail.
     * <p>
     * The freed cache is re-accessible if its configuration is again fed into this cache service via {@link #loadConfiguration(String)} or
     * {@link #loadDefaultConfiguration()}.
     *
     * @param name The name of the cache region that ought to be freed
     */
    public void freeCache(String name) throws OXException;

    /**
     * Additionally feeds the cache manager with specified cache configuration file.
     * <p>
     * The cache manager reads a default configuration - defined through property "com.openexchange.caching.configfile" in
     * 'system.properties' file - on initialization automatically. Therefore this method is useful to extend or overwrite the loaded default
     * configuration and needs <b>not</b> to be invoked to initialize the cache manager at all.
     *
     * @param cacheConfigFile The cache configuration file
     * @throws OXException If configuration fails
     */
    public void loadConfiguration(String cacheConfigFile) throws OXException;

    /**
     * Additionally feeds the cache manager with specified input stream. The stream will be closed.
     * <p>
     * The cache manager reads a default configuration - defined through property "com.openexchange.caching.configfile" in
     * 'system.properties' file - on initialization automatically. Therefore this method is useful to extend or overwrite the loaded default
     * configuration and needs <b>not</b> to be invoked to initialize the cache manager at all.
     *
     * @param inputStream The input stream to read from
     * @throws OXException If configuration fails
     */
    public void loadConfiguration(InputStream inputStream) throws OXException;

    /**
     * Additionally feeds the cache manager with specified input stream. The stream will be closed.
     * <p>
     * The cache manager reads a default configuration - defined through property "com.openexchange.caching.configfile" in
     * 'system.properties' file - on initialization automatically. Therefore this method is useful to extend or overwrite the loaded default
     * configuration and needs <b>not</b> to be invoked to initialize the cache manager at all.
     *
     * @param inputStream The input stream to read from
     * @param overwrite The flag whether to overwrite possibly existing region definition
     * @throws OXException If configuration fails
     */
    public void loadConfiguration(InputStream inputStream, boolean overwrite) throws OXException;

    /**
     * Additionally feeds the cache manager with specified configuration properties.
     * <p>
     * The cache manager reads a default configuration - defined through property "com.openexchange.caching.configfile" in
     * 'system.properties' file - on initialization automatically. Therefore this method is useful to extend or overwrite the loaded default
     * configuration and needs <b>not</b> to be invoked to initialize the cache manager at all.
     *
     * @param properties The properties to read from
     * @throws OXException If configuration fails
     */
    void loadConfiguration(Properties properties) throws OXException;

    /**
     * Re-Loads the cache manager's default configuration.
     *
     * @throws OXException If configuration fails
     */
    public void loadDefaultConfiguration() throws OXException;

}
