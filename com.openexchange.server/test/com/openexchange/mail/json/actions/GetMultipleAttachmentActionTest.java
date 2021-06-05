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

package com.openexchange.mail.json.actions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link GetMultipleAttachmentActionTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */

public class GetMultipleAttachmentActionTest {

    @InjectMocks
    private GetMultipleAttachmentAction action;

    @Mock
    private MailMessage message;

    private String subject = "subject";

    private String fileName = subject + ".zip";

    private String defaultFileName = MailStrings.DEFAULT_SUBJECT + ".zip";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(message.getSubject()).thenReturn(subject);
    }

    @Test
    public void testGetFileName_localeNull_fileNameNotNull() {
        String ret = action.getFileName(null, message);
        Assert.assertNotNull(ret);
    }

    @Test
    public void testGetFileName_localeNull_fileNameSet() {
        String ret = action.getFileName(null, message);
        Assert.assertEquals(fileName, ret);
    }

    @Test
    public void testGetFileName_subjectNull_returnDefault() {
        Mockito.when(message.getSubject()).thenReturn(null);

        String ret = action.getFileName(null, message);
        Assert.assertEquals(defaultFileName, ret);
    }
}
