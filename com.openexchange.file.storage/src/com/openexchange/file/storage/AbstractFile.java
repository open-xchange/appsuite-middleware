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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.file.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.file.storage.meta.FileComparator;
import com.openexchange.file.storage.meta.FileFieldHandling;


/**
 * {@link AbstractFile}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractFile implements File {

    
    public File dup() {
        return FileFieldHandling.dup(this);
    }
    
    public void copyInto(File other) {
        FileFieldHandling.copy(this, other);
    }
    
    public void copyFrom(File other) {
        FileFieldHandling.copy(other, this);
    }
    
    public Set<File.Field> differences(final File other) {
        return Field.inject(new AbstractFileFieldHandler() {

            public Object handle(Field field, Object... args) {
                Set set = get(0,Set.class,args);
                int comparison = new FileComparator(field).compare(AbstractFile.this, other);
                if(comparison != 0) {
                    set.add(field);
                }
                return set;
            }
            
        }, new HashSet<File.Field>());
    }
    
    public boolean equals(File other, Field criterium, Field...criteria) {
        List<Field> fields = new ArrayList<Field>(1 + criteria.length);
        
        for (Field field : fields) {
            if(0 != new FileComparator(field).compare(this, other)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        return FileFieldHandling.toString(this);
    }


}
