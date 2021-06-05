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

package com.openexchange.i18n.tools.replacement;

import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.i18n.tools.TemplateReplacement;
import com.openexchange.i18n.tools.TemplateToken;

/**
 * {@link ModuleReplacement}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ModuleReplacement implements TemplateReplacement {

    private final static String[] MODULES = { "calendar", "tasks" };

    public static final int MODULE_UNKNOWN = -1;

    public static final int MODULE_CALENDAR = 0;

    public static final int MODULE_TASK = 1;

    private String repl;

    private boolean changed;

    /**
     * Initializes a new {@link ModuleReplacement}
     *
     * @param module The module; supposed to be either {@link #MODULE_CALENDAR}
     *            or {@link #MODULE_TASK}
     */
    public ModuleReplacement(final int module) {
        super();
        repl = module < 0 || module >= MODULES.length ? "unknown" : MODULES[module];
    }

    @Override
    public String getReplacement() {
        return repl;
    }

    @Override
    public TemplateToken getToken() {
        return TemplateToken.MODULE;
    }

    @Override
    public boolean changed() {
        return changed;
    }

    @Override
    public boolean relevantChange() {
        return changed();
    }

    @Override
    public TemplateReplacement setChanged(final boolean changed) {
        this.changed = changed;
        return this;
    }

    @Override
    public TemplateReplacement setLocale(final Locale locale) {
        return this;
    }

    @Override
    public TemplateReplacement setTimeZone(final TimeZone timeZone) {
        return this;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public TemplateReplacement getClone() throws CloneNotSupportedException {
        return (TemplateReplacement) clone();
    }

    @Override
    public boolean merge(final TemplateReplacement other) {
        if (!ModuleReplacement.class.isInstance(other)) {
            /*
             * Class mismatch or null
             */
            return false;
        }
        if (!getToken().equals(other.getToken())) {
            /*
             * Token mismatch
             */
            return false;
        }
        if (!other.changed()) {
            /*
             * Other replacement does not reflect a changed value; leave
             * unchanged
             */
            return false;
        }
        final ModuleReplacement o = (ModuleReplacement) other;
        this.repl = o.repl;
        this.changed = true;
        return true;
    }

    @Override
    public TemplateReplacement setDateFormat(DateFormat dateFormat) {
        return this;
    }
}
