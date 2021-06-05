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
package com.openexchange.jsieve.commands;

import java.util.List;



public class RuleComment {

    private List<String> flags;

    private String rulename;

    private String errortext;

    private int uniqueid = -1;

    private int line = -1;

    /**
     * @param flags
     * @param uniqueid TODO
     * @param rulename
     * @param line
     */
    public RuleComment(final List<String> flags, final int uniqueid, final String rulename, final int line) {
        super();
        this.flags = flags;
        this.rulename = rulename;
        this.uniqueid = uniqueid;
        this.line = line;
    }

    /**
     * @param errortext
     * @param line
     */
    public RuleComment(final int line, final String errortext) {
        super();
        this.errortext = errortext;
        this.line = line;
    }

    public RuleComment(final int uniqueid, final String rulename, final int line) {
        super();
        this.uniqueid = uniqueid;
        this.rulename = rulename;
        this.line = line;
    }

    /**
     * @param uniqueid
     */
    public RuleComment(final int uniqueid) {
        super();
        this.uniqueid = uniqueid;
    }

    public RuleComment(final String rulename) {
        super();
        this.rulename = rulename;
    }

    /**
     * @param flags
     */
    public RuleComment(final List<String> flags) {
        super();
        this.flags = flags;
    }

    public final String getRulename() {
        return rulename;
    }

    public final int getLine() {
        return line;
    }

    public final void setRulename(final String comment) {
        this.rulename = comment;
    }

    public final void setLine(final int line) {
        this.line = line;
    }

    /**
     * @return the flags
     */
    public final List<String> getFlags() {
        return flags;
    }

    /**
     * @param flags the flags to set
     */
    public final void setFlags(final List<String> flags) {
        this.flags = flags;
    }


    /**
     * @return the errortext
     */
    public final String getErrortext() {
        return errortext;
    }

    /**
     * @param errortext the errortext to set
     */
    public final void setErrortext(final String errortext) {
        this.errortext = errortext;
    }

    /**
     * @return the uniqueid
     */
    public final int getUniqueid() {
        return uniqueid;
    }

    /**
     * @param uniqueid the uniqueid to set
     */
    public final void setUniqueid(final int uniqueid) {
        this.uniqueid = uniqueid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((flags == null) ? 0 : flags.hashCode());
        result = prime * result + line;
        result = prime * result + ((rulename == null) ? 0 : rulename.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
			return true;
		}
        if (obj == null) {
			return false;
		}
        if (!(obj instanceof RuleComment)) {
			return false;
		}
        final RuleComment other = (RuleComment) obj;
        if (flags == null) {
            if (other.flags != null) {
				return false;
			}
        } else if (!flags.equals(other.flags)) {
			return false;
		}
        if (line != other.line) {
			return false;
		}
        if (rulename == null) {
            if (other.rulename != null) {
				return false;
			}
        } else if (!rulename.equals(other.rulename)) {
			return false;
		}
        return true;
    }

    @Override
    public String toString() {
        return "## Flag: " + this.flags + "|Unique: " + this.uniqueid + "|Name: " + this.rulename + "...line" + this.line;
    }

}
