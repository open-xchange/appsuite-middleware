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

package com.openexchange.logging.filter;

import static com.openexchange.java.util.Tools.getUnsignedInteger;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Marker;
import com.openexchange.ajax.response.IncludeStackTraceService;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.FilterReply;

/**
 * {@link ExceptionCategoryFilter}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ExceptionCategoryFilter extends ExtendedTurboFilter {

    /** The reference for current instance */
    private static final AtomicReference<ExceptionCategoryFilter> CURRENT_INSTANCE = new AtomicReference<>(null);

    /**
     * Gets the current applicable instance.
     *
     * @return The instance or empty
     */
    public static Optional<ExceptionCategoryFilter> getCurrentInstance() {
        return Optional.ofNullable(CURRENT_INSTANCE.get());
    }

    /**
     * Creates (and applies) a new exception category filter instance.
     *
     * @param categories The categories to filter given as a comma-separated string
     * @param traceService The trace service to use
     * @param rankingAwareTurboFilterList The turbo filter list to apply to
     * @return The newly created instance
     */
    public static synchronized ExceptionCategoryFilter createInstance(String categories, IncludeStackTraceService traceService, RankingAwareTurboFilterList rankingAwareTurboFilterList) {
        dropInstance(rankingAwareTurboFilterList);

        ExceptionCategoryFilter newInstance = new ExceptionCategoryFilter(categories, traceService, rankingAwareTurboFilterList);
        rankingAwareTurboFilterList.addTurboFilter(newInstance);
        CURRENT_INSTANCE.set(newInstance);
        return newInstance;
    }

    /**
     * Drops the exception category filter instance.
     *
     * @param rankingAwareTurboFilterList The turbo filter list to remove from
     */
    public static synchronized void dropInstance(RankingAwareTurboFilterList rankingAwareTurboFilterList) {
        ExceptionCategoryFilter previousInstance = CURRENT_INSTANCE.getAndSet(null);
        if (previousInstance != null) {
            rankingAwareTurboFilterList.removeTurboFilter(previousInstance);
        }
    }

    /**
     * Sets (and applies) given categories given as comma-separated string.
     *
     * @param categories The categories as comma-separated string
     * @throws IllegalStateException If exception category filter has not yet been initialized
     */
    public static synchronized void setCategories(String categories) {
        ExceptionCategoryFilter previousInstance = CURRENT_INSTANCE.getAndSet(null);
        if (previousInstance == null) {
            throw new IllegalStateException("Cannot set new categories to filter since filter not yet initialized");
        }

        IncludeStackTraceService traceService = previousInstance.traceService;
        RankingAwareTurboFilterList rankingAwareTurboFilterList = previousInstance.rankingAwareTurboFilterList;

        rankingAwareTurboFilterList.removeTurboFilter(previousInstance);

        if (categories != null) {
            ExceptionCategoryFilter newInstance = new ExceptionCategoryFilter(categories, traceService, rankingAwareTurboFilterList);
            rankingAwareTurboFilterList.addTurboFilter(newInstance);
            CURRENT_INSTANCE.set(newInstance);
        }
    }

    /**
     * Gets the currently applicable categories as comma-separated string.
     *
     * @return The currently applicable categories
     * @throws IllegalStateException If exception category filter has not yet been initialized
     */
    public static Set<Category.EnumType> getCategories() {
        ExceptionCategoryFilter currentInstance = CURRENT_INSTANCE.get();
        if (currentInstance == null) {
            throw new IllegalStateException("Cannot set new categories to filter since filter not yet initialized");
        }

        return currentInstance.categoriesAndName.categories;
    }

    /**
     * Gets the currently applicable categories as comma-separated string.
     *
     * @return The currently applicable categories
     * @throws IllegalStateException If exception category filter has not yet been initialized
     */
    public static String getCategoriesAsString() {
        Iterator<Category.EnumType> it = getCategories().iterator();
        if (!it.hasNext()) {
            return "";
        }

        StringBuilder sb = new StringBuilder(it.next().getName());
        while (it.hasNext()) {
            Category.EnumType c = it.next();
            sb.append(", ").append(c.getName());
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final IncludeStackTraceService traceService;
    private final CategoriesAndName categoriesAndName;
    private final RankingAwareTurboFilterList rankingAwareTurboFilterList;
    private final boolean hasAny;

    /**
     * Initializes a new {@link ExceptionCategoryFilter}.
     *
     * @param categories The categories as comma-separated string
     * @param traceService The trace service to use
     * @param rankingAwareTurboFilterList The turbo filter list
     */
    private ExceptionCategoryFilter(String categories, IncludeStackTraceService traceService, RankingAwareTurboFilterList rankingAwareTurboFilterList) {
        super();
        this.traceService = traceService;
        this.rankingAwareTurboFilterList = rankingAwareTurboFilterList;
        this.categoriesAndName = parseCategories(categories);
        this.hasAny = false == this.categoriesAndName.categories.isEmpty();
    }

    @Override
    public String getName() {
        return categoriesAndName.name;
    }

    @Override
    public int getRanking() {
        // Return default ranking
        return DEFAULT_RANKING;
    }

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if (hasAny && OXException.class.isInstance(t)) {
            Category category = ((OXException) t).getCategory();
            if (null != category && categoriesAndName.categories.contains(category.getType())) {
                if (traceService.isEnabled()) {
                    try {
                        int contextId = getUnsignedInteger(LogProperties.get(LogProperties.Name.SESSION_CONTEXT_ID));
                        int userId = getUnsignedInteger(LogProperties.get(LogProperties.Name.SESSION_USER_ID));
                        if (contextId <= 0 || userId <= 0 || false == traceService.includeStackTraceOnError(userId, contextId)) {
                            dropStackTraceFor(t);
                        }
                    } catch (Exception e) {
                        dropStackTraceFor(t);
                    }
                } else {
                    dropStackTraceFor(t);
                }
            }
        }

        return FilterReply.NEUTRAL;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];

    private static void dropStackTraceFor(Throwable t) {
        if (null != t) {
            // Drop our stack trace
            t.setStackTrace(EMPTY_STACK_TRACE);

            // Drop stack traces for suppressed exceptions, if any
            Throwable[] suppressedOnes = t.getSuppressed();
            if (null != suppressedOnes && suppressedOnes.length > 0) {
                for (Throwable suppressed : suppressedOnes) {
                    dropStackTraceFor(suppressed);
                }
            }

            // Drop stack traces for cause, if any
            dropStackTraceFor(t.getCause());
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Parses the categories given as comma-separated string,
     *
     * @param categories The categories as comma-separated string
     * @return The resulting categories and filter name
     */
    private static CategoriesAndName parseCategories(String categories) {
        if (Strings.isEmpty(categories)) {
            return new CategoriesAndName(EnumSet.noneOf(Category.EnumType.class));
        }

        Set<String> c;
        {
            String[] catz = Strings.splitByComma(categories);
            c = new HashSet<String>(catz.length);
            for (String category : catz) {
                c.add(category.trim());
            }
        }

        Set<Category.EnumType> newCategories = EnumSet.noneOf(Category.EnumType.class);
        for (String category : c) {
            try {
                newCategories.add(Category.EnumType.valueOf(Category.EnumType.class, category));
            } catch (IllegalArgumentException e) {
                // Skip this value
            }
        }
        return new CategoriesAndName(newCategories);
    }

    /** Wrapper class for categories and derived name */
    private static class CategoriesAndName {

        final Set<Category.EnumType> categories;
        final String name;

        CategoriesAndName(Set<Category.EnumType> categories) {
            super();
            this.categories = categories;
            name = generateName(categories);
        }

        private static String generateName(Set<Category.EnumType> categories) {
            if (categories.isEmpty()) {
                return ExceptionCategoryFilter.class.getSimpleName();
            }

            StringBuilder nameBuilder = new StringBuilder(1024).append(ExceptionCategoryFilter.class.getSimpleName());
            boolean added = false;
            for (Category.EnumType category : categories) {
                if (null != category) {
                    if (!added) {
                        added = true;
                        nameBuilder.append(':');
                    } else {
                        nameBuilder.append(',');
                    }
                    nameBuilder.append(category.getName());
                }
            }
            return nameBuilder.toString();
        }
    }

}
