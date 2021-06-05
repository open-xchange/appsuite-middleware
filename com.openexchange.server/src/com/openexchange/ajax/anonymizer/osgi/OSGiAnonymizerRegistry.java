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

package com.openexchange.ajax.anonymizer.osgi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.anonymizer.AnonymizerRegistryService;
import com.openexchange.ajax.anonymizer.AnonymizerService;
import com.openexchange.ajax.anonymizer.Module;
import com.openexchange.exception.OXException;


/**
 * {@link OSGiAnonymizerRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OSGiAnonymizerRegistry extends ServiceTracker<AnonymizerService<?>, AnonymizerService<?>> implements AnonymizerRegistryService {

    private final ConcurrentMap<Module, AnonymizerChain<?>> anonymizers;

    /**
     * Initializes a new {@link OSGiAnonymizerRegistry}.
     */
    public OSGiAnonymizerRegistry(BundleContext context) {
        super(context, AnonymizerService.class.getName(), null);
        anonymizers = new ConcurrentHashMap<Module, AnonymizerChain<?>>(8, 0.9f, 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public AnonymizerService<?> addingService(ServiceReference<AnonymizerService<?>> reference) {
        AnonymizerService<Object> service = (AnonymizerService<Object>) context.getService(reference);

        AnonymizerChain<Object> chain = (AnonymizerChain<Object>) anonymizers.get(service.getModule());
        if (null == chain) {
            AnonymizerChain<Object> newChain = new AnonymizerChain<Object>(service.getModule());
            chain = (AnonymizerChain<Object>) anonymizers.putIfAbsent(service.getModule(), newChain);
            if (null == chain) {
                chain = newChain;
            }
        }

        if (chain.addAnonymizer(service, getRanking(reference, 0))) {
            return service;
        }

        context.ungetService(reference);
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removedService(ServiceReference<AnonymizerService<?>> reference, AnonymizerService<?> service) {
        AnonymizerChain<Object> chain = (AnonymizerChain<Object>) anonymizers.get(service.getModule());
        if (null != chain) {
            chain.removeAnonymizer((AnonymizerService<Object>) service, getRanking(reference, 0));
        }
        context.ungetService(reference);
    }

    @Override
    public <E> AnonymizerService<E> getAnonymizerFor(String name) throws OXException {
        return getAnonymizerFor(Module.moduleFor(name));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> AnonymizerService<E> getAnonymizerFor(Module module) throws OXException {
        return (null == module ? null : (AnonymizerService<E>) anonymizers.get(module));
    }

    // -------------------------------------------------------------------------------------------

    /**
     * Gets the service ranking by look-up of <code>"service.ranking"</code> property.
     * <p>
     * See {@link Constants#SERVICE_RANKING}.
     *
     * @param reference The service reference providing properties Dictionary object of the service
     * @param defaultRanking The default ranking if {@link Constants#SERVICE_RANKING} property is absent
     * @return The ranking or <code>0</code> (zero) if absent
     */
    private static <S> int getRanking(ServiceReference<S> reference, int defaultRanking) {
        int ranking = defaultRanking;
        {
            Object oRanking = reference.getProperty(Constants.SERVICE_RANKING);
            if (null != oRanking) {
                if (oRanking instanceof Integer) {
                    ranking = ((Integer) oRanking).intValue();
                } else {
                    try {
                        ranking = Integer.parseInt(oRanking.toString().trim());
                    } catch (@SuppressWarnings("unused") NumberFormatException e) {
                        ranking = defaultRanking;
                    }
                }
            }
        }
        return ranking;
    }

}
