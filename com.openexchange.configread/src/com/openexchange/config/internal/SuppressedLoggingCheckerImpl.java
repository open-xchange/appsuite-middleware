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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.config.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.PropertyEvent;
import com.openexchange.config.PropertyEvent.Type;
import com.openexchange.config.PropertyListener;
import com.openexchange.exception.Categories;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.SuppressedLoggingChecker;
import com.openexchange.java.Strings;


/**
 * {@link SuppressedLoggingCheckerImpl} - The <code>SuppressedLoggingChecker</code> implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SuppressedLoggingCheckerImpl implements SuppressedLoggingChecker, PropertyListener {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(SuppressedLoggingCheckerImpl.class);

    /**
     * Creates a new instance.
     *
     * @param configurationService The configuration service to read from
     * @return The new instance
     */
    public static SuppressedLoggingCheckerImpl newInstance(final ConfigurationService configurationService) {
        final SuppressedLoggingCheckerImpl instance = new SuppressedLoggingCheckerImpl();
        final String property = configurationService.getProperty("com.openexchange.log.suppressedCategories", "USER_INPUT", instance);
        instance.parseProperty(property);
        return instance;
    }

    // ---------------------------------------------------------------------------- //

    /** The reference to currently applicable suppressed categories */
    private final AtomicReference<Collection<Category>> suppressedCategoriesRef;

    /**
     * Initializes a new {@link SuppressedLoggingCheckerImpl}.
     */
    private SuppressedLoggingCheckerImpl() {
        super();
        suppressedCategoriesRef = new AtomicReference<Collection<Category>>(Collections.<Category> emptySet());
    }

    private void parseProperty(final String property) {
        final Set<Category> suppressedCategories;
        if (property.isEmpty()) {
            suppressedCategories = Collections.<Category> emptySet();
        } else {
            final String[] names = Strings.splitByComma(property);
            suppressedCategories = new HashSet<Category>(names.length);
            for (final String name : names) {
                final Category category = Categories.getKnownCategoryByName(name, null);
                if (null != category) {
                    suppressedCategories.add(category);
                }
            }
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("Suppressed categories set to: " + suppressedCategories);
        }

        suppressedCategoriesRef.set(suppressedCategories);
    }

    @Override
    public boolean isSuppressed(final OXException e) {
        final Category category = e.getCategory();
        return suppressedCategoriesRef.get().contains(category);
    }

    @Override
    public void onPropertyChange(final PropertyEvent event) {
        final Type type = event.getType();
        if (Type.DELETED.equals(type)) {
            suppressedCategoriesRef.set(Collections.<Category> singletonList(Category.CATEGORY_USER_INPUT));
        } else {
            parseProperty(event.getValue());
        }
    }

}
