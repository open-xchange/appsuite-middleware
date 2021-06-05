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

package com.openexchange.admin.diff.file.handler.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;
import com.openexchange.admin.diff.ConfigDiff;
import com.openexchange.admin.diff.file.domain.ConfigurationFile;
import com.openexchange.admin.diff.result.DiffResult;
import com.openexchange.admin.diff.result.domain.PropertyDiff;
import com.openexchange.admin.diff.util.ConfigurationFileSearch;

/**
 * Handler for .xml configuration files
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class XmlHandler extends AbstractFileHandler {

    private volatile static XmlHandler instance;

    private XmlHandler() {
        ConfigDiff.register(this);
    }

    public static synchronized XmlHandler getInstance() {
        if (instance == null) {
            synchronized (XmlHandler.class) {
                if (instance == null) {
                    instance = new XmlHandler();
                }
            }
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiffResult getDiff(DiffResult diffResult, List<ConfigurationFile> lOriginalFiles, List<ConfigurationFile> lInstalledFiles) {
        configureXMLUnitComparator();

        for (ConfigurationFile origFile : lOriginalFiles) {

            final String fileName = origFile.getName();
            List<ConfigurationFile> result = new ConfigurationFileSearch().search(lInstalledFiles, fileName);

            if (result.isEmpty()) {
                // Missing in installation, but already tracked in file diff
                continue;
            }

            String originalFileContent = origFile.getContent();
            String installedFileContent = result.get(0).getContent();
            try {
                DetailedDiff xmlDetailedDiff = new DetailedDiff(new Diff(originalFileContent, installedFileContent));

                if (xmlDetailedDiff.getAllDifferences().isEmpty()) {
                    continue;
                }
                String difference = "";
                @SuppressWarnings("unchecked") Iterator<Difference> iterator = xmlDetailedDiff.getAllDifferences().iterator();
                while (iterator.hasNext()) {
                    Difference next = iterator.next();
                    difference = difference.concat(next.toString() + "\n");
                }
                diffResult.getChangedProperties().add(new PropertyDiff(origFile.getFileNameWithExtension(), difference, null));
            }
            catch (SAXException e) {
                diffResult.getProcessingErrors().add("Error while xml diff: " + e.getLocalizedMessage() + "\n");
            } catch (IOException e) {
                diffResult.getProcessingErrors().add("Error while xml diff: " + e.getLocalizedMessage() + "\n");
            }

        }
        return diffResult;
    }

    private void configureXMLUnitComparator() {
        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
    }
}
