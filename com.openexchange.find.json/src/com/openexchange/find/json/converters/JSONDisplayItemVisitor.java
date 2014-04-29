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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.find.json.converters;

import java.util.Locale;
import com.openexchange.find.calendar.RecurringTypeDisplayItem;
import com.openexchange.find.calendar.RelativeDateDisplayItem;
import com.openexchange.find.calendar.StatusDisplayItem;
import com.openexchange.find.common.ContactDisplayItem;
import com.openexchange.find.common.ContactTypeDisplayItem;
import com.openexchange.find.common.FolderTypeDisplayItem;
import com.openexchange.find.common.FormattableDisplayItem;
import com.openexchange.find.common.SimpleDisplayItem;
import com.openexchange.find.drive.FileDisplayItem;
import com.openexchange.find.drive.FileSizeDisplayItem;
import com.openexchange.find.drive.FileTypeDisplayItem;
import com.openexchange.find.facet.DisplayItemVisitor;
import com.openexchange.find.facet.NoDisplayItem;
import com.openexchange.find.tasks.TaskStatusDisplayItem;
import com.openexchange.find.tasks.TaskTypeDisplayItem;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class JSONDisplayItemVisitor implements DisplayItemVisitor {

    private final StringTranslator translator;

    private final Locale locale;

    private String result;

    public JSONDisplayItemVisitor(final StringTranslator translator, final Locale locale) {
        super();
        this.translator = translator;
        this.locale = locale;
    }

    @Override
    public void visit(ContactDisplayItem item) {
        result = item.getDefaultValue();
    }

    @Override
    public void visit(SimpleDisplayItem item) {
        if (item.isLocalizable()) {
            result = translator.translate(locale, item.getDefaultValue());
        } else {
            result = item.getDefaultValue();
        }
    }

    @Override
    public void visit(final FolderTypeDisplayItem item) {
        result = translator.translate(locale, item.getDefaultValue());
    }

    @Override
    public void visit(FileTypeDisplayItem item) {
        result = translator.translate(locale, item.getDefaultValue());
    }

    @Override
    public void visit(TaskStatusDisplayItem item) {
        result = translator.translate(locale, item.getDefaultValue());
    }

    @Override
    public void visit(StatusDisplayItem item) {
        result = translator.translate(locale, item.getDefaultValue());
    }

    @Override
    public void visit(TaskTypeDisplayItem item) {
        result = translator.translate(locale, item.getDefaultValue());
    }

    @Override
    public void visit(RelativeDateDisplayItem item) {
        result = translator.translate(locale, item.getDefaultValue());
    }

    @Override
    public void visit(ContactTypeDisplayItem item) {
        result = translator.translate(locale, item.getDefaultValue());
    }

    @Override
    public void visit(RecurringTypeDisplayItem item) {
        result = translator.translate(locale, item.getDefaultValue());
    }

    @Override
    public void visit(FileDisplayItem item) {
        result = item.getDefaultValue();
    }

    @Override
    public void visit(FormattableDisplayItem item) {
        Object[] args = item.getItem();
        String defaultValue = translator.translate(locale, item.getDefaultValue());
        result = String.format(locale, defaultValue, args);
    }

    @Override
    public void visit(FileSizeDisplayItem item) {
        result = item.getDefaultValue();
    }

    @Override
    public void visit(NoDisplayItem noDisplayItem) {
        result = null;
    }

    /**
     * Gets the value to set for the 'display_name' attribute. This value
     * is only valid if DisplayItem.accept(visitor) has been called.
     *
     * @return The display name or <code>null</code> if it should
     * not be included in the response object.
     */
    public String getDisplayName() {
        return result;
    }
}
