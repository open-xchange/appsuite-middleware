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
 *    trademarks of the OX Software GmbH. group of companies.
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

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((flags == null) ? 0 : flags.hashCode());
        result = prime * result + line;
        result = prime * result + ((rulename == null) ? 0 : rulename.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "## Flag: " + this.flags + "|Unique: " + this.uniqueid + "|Name: " + this.rulename + "...line" + this.line;
    }

}
