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

package com.openexchange.logging.filter;

import static com.openexchange.java.util.Tools.getUnsignedInteger;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Marker;
import com.openexchange.ajax.response.IncludeStackTraceService;
import com.openexchange.exception.Category;
import com.openexchange.exception.Category.EnumType;
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

    /** Wrapper class for categories and derived name */
    private static class CategoriesAndName {

        final Set<Category.EnumType> categories;
        final String name;

        CategoriesAndName() {
            this(EnumSet.noneOf(Category.EnumType.class));
        }

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

    /** The reference used to manage categories */
    private static final AtomicReference<CategoriesAndName> CATEGORIES_AND_NAME_REF = new AtomicReference<>(new CategoriesAndName());

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final IncludeStackTraceService traceService;

    /**
     * Initializes a new {@link ExceptionCategoryFilter}.
     */
    public ExceptionCategoryFilter(final IncludeStackTraceService traceService) {
        super();
        this.traceService = traceService;
    }

    @Override
    public String getName() {
        return CATEGORIES_AND_NAME_REF.get().name;
    }

    @Override
    public int getRanking() {
        // Return default ranking
        return DEFAULT_RANKING;
    }

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if (OXException.class.isInstance(t)) {
            Category category = ((OXException) t).getCategory();
            if (null != category && CATEGORIES_AND_NAME_REF.get().categories.contains(category.getType())) {
                if (traceService.isEnabled()) {
                    try {
                        final int contextId = getUnsignedInteger(LogProperties.get(LogProperties.Name.SESSION_CONTEXT_ID));
                        final int userId = getUnsignedInteger(LogProperties.get(LogProperties.Name.SESSION_USER_ID));
                        if (userId <= 0 || contextId <= 0 || false == traceService.includeStackTraceOnError(userId, contextId)) {
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

    /**
     * Sets the categories to apply.
     *
     * @param categories The categories
     */
    public static void setCategories(Set<String> categories) {
        Set<Category.EnumType> newCategories = EnumSet.noneOf(Category.EnumType.class);
        for (String category : categories) {
            try {
                newCategories.add(Category.EnumType.valueOf(Category.EnumType.class, category));
            } catch (IllegalArgumentException e) {
                // Skip this value
            }
        }
        CATEGORIES_AND_NAME_REF.set(new CategoriesAndName(newCategories));
    }

    /**
     * Sets the categories to apply as comma-separated string
     *
     * @param categories The categories as comma-separated string
     */
    public static void setCategories(String categories) {
        Set<String> c = new HashSet<String>();
        for (String category : Strings.splitByComma(categories)) {
            c.add(category.trim());
        }
        setCategories(c);
    }

    /**
     * Gets the currently applicable categories as comma-separated string.
     *
     * @return The currently applicable categories
     */
    public static String getCategories() {
        Iterator<EnumType> it = CATEGORIES_AND_NAME_REF.get().categories.iterator();
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
}
