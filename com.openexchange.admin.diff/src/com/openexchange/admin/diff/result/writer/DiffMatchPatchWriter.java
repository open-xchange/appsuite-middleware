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

package com.openexchange.admin.diff.result.writer;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.admin.diff.file.domain.ConfigurationFile;
import com.openexchange.admin.diff.result.DiffResult;
import com.openexchange.admin.diff.result.domain.PropertyDiff;
import com.openexchange.admin.diff.util.ConfigurationFileSearch;
import com.openexchange.admin.diff.util.DiffMatchPatch;
import com.openexchange.admin.diff.util.DiffMatchPatch.Diff;
import com.openexchange.admin.diff.util.DiffMatchPatch.Operation;



/**
 * {@link DiffMatchPatchWriter}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class DiffMatchPatchWriter implements DiffWriter {

    /**
     * {@inheritDoc}
     */
    @Override
    public void addOutputToDiffResult(DiffResult diffResult, List<ConfigurationFile> lOriginalFiles, List<ConfigurationFile> lInstalledFiles) {

        for (ConfigurationFile origFile : lOriginalFiles) {
            final String fileName = origFile.getName();

            List<ConfigurationFile> result = new ConfigurationFileSearch().search(lInstalledFiles, fileName);

            if (result.isEmpty()) {
                // Missing in installation, but already tracked in file diff
                continue;
            }

            DiffMatchPatch diffMatchPatch = new DiffMatchPatch();
            LinkedList<Diff> diff_main = diffMatchPatch.diff_main(origFile.getContent(), result.get(0).getContent(), false);
            diffMatchPatch.diff_cleanupSemantic(diff_main);

            if (diff_main.size() > 1) {
                String difference = "";
                for (Diff d : diff_main) {
                    if (d.operation != Operation.EQUAL) {
                        difference = difference.concat("\n" + d.operation + ": " + d.text);
                    }
                }
                diffResult.getChangedProperties().add(new PropertyDiff(origFile.getFileNameWithExtension(), difference, null));
            }
        }
    }
}
