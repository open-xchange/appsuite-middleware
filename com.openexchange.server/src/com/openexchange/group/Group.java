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

package com.openexchange.group;

import static com.openexchange.java.Autoboxing.I;
import java.io.Serializable;
import java.util.Date;
import com.openexchange.ajax.AJAXServlet;

/**
 * This is the data container class for group.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Group implements Cloneable, Serializable {

    private static final long serialVersionUID = -2056806379007998512L;

    public enum Field {
        ID(1, AJAXServlet.PARAMETER_ID, "id"),
        LAST_MODIFIED(5, "last_modified", "lastModified"),
        SIMPLE_NAME(700, "name", "identifier"), //Confused yet?
        FULL_NAME(701, "display_name", "displayName"),
        MEMBERS(702, "members", null);

        private final int colNumber;
        private final String ajaxName;
        private final String dbName;

        private Field(int colNumber, String ajaxName, String dbName) {
            this.colNumber = colNumber;
            this.ajaxName = ajaxName;
            this.dbName = dbName;
        }

        public int getColNumber() {
            return colNumber;
        }

        public String getAjaxName() {
            return ajaxName;
        }

        public String getDbName() {
            return dbName;
        }

        public static Field getByColumnNumber(int num) {
            if (num > 0) {
                for (Field val : values()) {
                    if (val.getColNumber() == num) {
                        return val;
                    }
                }
            }
            return null;
        }

        public static int[] intValues() {
            Field[] vals = values();
            int[] retVal = new int[vals.length];
            for (int i = 0; i < vals.length; i++) {
                retVal[i] = vals[i].getColNumber();
            }
            return retVal;
        }

    }

    public static int[] ALL_COLUMNS = new int[] { Field.FULL_NAME.getColNumber(), Field.ID.getColNumber(), Field.LAST_MODIFIED.getColNumber(), Field.SIMPLE_NAME.getColNumber(), Field.MEMBERS.getColNumber() };

    public static int[] ALL_COLUMNS_EXCEPT_MEMBERS = new int[] { Field.FULL_NAME.getColNumber(), Field.ID.getColNumber(), Field.LAST_MODIFIED.getColNumber(), Field.SIMPLE_NAME.getColNumber() };

    /**
     * Unique numeric identifier of this group.
     */
    private int identifier = -1;

    /**
     * For remembering if the identifier was set.
     */
    private boolean identifierSet;

    /**
     * This name of the group has some restrictions for the character that can
     * be used.
     */
    private String simpleName;

    /**
     * For remembering if the simpleName was set.
     */
    private boolean simpleNameSet;

    /**
     * Member of this group.
     */
    private int[] member = new int[0];

    /**
     * For remembering if the member were set.
     */
    private boolean memberSet;

    /**
     * Display name of this group.
     */
    private String displayName;

    /**
     * For remembering if the displayName was set.
     */
    private boolean displayNameSet;

    /**
     * Time stamp of the last modification of the group.
     */
    private Date lastModified;

    /**
     * For remembering if the lastModified was set.
     */
    private boolean lastModifiedSet;

    /**
     * Default constructor.
     */
    public Group() {
        super();
    }

    /**
     * Copy constructor.
     */
    public Group(Group group) {
        if (group.isDisplayNameSet()) {
            this.displayName = group.getDisplayName();
        }
        if (group.isIdentifierSet()) {
            this.identifier = group.getIdentifier();
        }
        if (group.isLastModifiedSet()) {
            this.lastModified = group.getLastModified();
        }
        if (group.isMemberSet()) {
            this.member = group.getMember();
        }
        if (group.isSimpleNameSet()) {
            this.simpleName = group.getSimpleName();
        }
    }

    /**
     * Setter for displayName.
     *
     * @param displayName Display name.
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
        displayNameSet = true;
    }

    /**
     * Getter for displayName.
     *
     * @return Display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Setter for unique identifier.
     *
     * @param identifier unique identifier.
     */
    public void setIdentifier(final int identifier) {
        this.identifier = identifier;
        identifierSet = true;
    }

    /**
     * Getter for unique identifier.
     *
     * @return unique identifier.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Setter for member.
     *
     * @param member Member.
     */
    public void setMember(final int[] member) {
        this.member = member;
        memberSet = true;
    }

    /**
     * Getter for member.
     *
     * @return the members of the group. If the group doesn't have any members
     *         an empty array will be returned.
     */
    public int[] getMember() {
        return member;
    }

    /**
     * @return Returns the lastModified.
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * @param lastModified The lastModified to set.
     */
    public void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
        lastModifiedSet = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() {
        try {
            Group retval = (Group) super.clone();
            if (null != lastModified) {
                retval.lastModified = (Date) lastModified.clone();
            }
            if (null != member) {
                retval.member = new int[member.length];
                System.arraycopy(member, 0, retval.member, 0, member.length);
            }
            return retval;
        } catch (CloneNotSupportedException e) {
            throw new InternalError("CloneNotSupportedException although Cloneable.");
        }
    }

    /**
     * @return the simpleName
     */
    public final String getSimpleName() {
        return simpleName;
    }

    /**
     * @param simpleName the simpleName to set
     */
    public final void setSimpleName(final String simpleName) {
        this.simpleName = simpleName;
        simpleNameSet = true;
    }

    /**
     * @return the identifierSet
     */
    public final boolean isIdentifierSet() {
        return identifierSet;
    }

    /**
     * @return the simpleNameSet
     */
    public final boolean isSimpleNameSet() {
        return simpleNameSet;
    }

    /**
     * @return the memberSet
     */
    public final boolean isMemberSet() {
        return memberSet;
    }

    /**
     * @return the displayNameSet
     */
    public final boolean isDisplayNameSet() {
        return displayNameSet;
    }

    /**
     * @return the lastModifiedSet
     */
    public final boolean isLastModifiedSet() {
        return lastModifiedSet;
    }

    public Object get(Group.Field field) {
        if (field == Field.FULL_NAME) {
            return getDisplayName();
        }
        if (field == Field.SIMPLE_NAME) {
            return getSimpleName();
        }
        if (field == Field.ID) {
            return I(getIdentifier());
        }
        if (field == Field.LAST_MODIFIED) {
            return getLastModified();
        }
        if (field == Field.MEMBERS) {
            return getMember();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("(Group ID:");
        sb.append(getIdentifier());
        sb.append(" name:");
        sb.append(getSimpleName());
        sb.append(" displayName:");
        sb.append(getDisplayName());
        sb.append(" member:");
        for (final int member : getMember()) {
            sb.append(member);
            sb.append(',');
        }
        sb.setCharAt(sb.length() - 1, ')');
        return sb.toString();
    }
}
