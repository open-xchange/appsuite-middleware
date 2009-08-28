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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.templating;

import static com.openexchange.templating.TemplateErrorMessage.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.templating.impl.OXFolderHelper;
import com.openexchange.templating.impl.OXInfostoreHelper;
import com.openexchange.tools.session.ServerSession;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class TemplateServiceImpl implements TemplateService {

    public static final String PATH_PROPERTY = "com.openexchange.templating.path";
    public static final String USER_TEMPLATING_PROPERTY = "com.openexchange.templating.usertemplating";

    private ConfigurationService config;

    private OXFolderHelper folders;

    private OXInfostoreHelper infostore;

    public TemplateServiceImpl(ConfigurationService config) {
        this.config = config;
    }

    public OXTemplateImpl loadTemplate(String templateName) throws TemplateException {
        String templatePath = config.getProperty(PATH_PROPERTY);
        if (templatePath == null) {
            return null;
        }

        OXTemplateImpl retval = new OXTemplateImpl();
        retval.setTemplate(loadTemplate(templatePath, templateName));
        return retval;

    }

    protected Template loadTemplate(String templatePath, String templateName) throws TemplateException {
        File path = new File(templatePath);
        if (!path.exists() || !path.isDirectory() || !path.canRead()) {
            return null;
        }

        Template retval = null;
        try {
            TemplateLoader templateLoader = new FileTemplateLoader(path);
            Configuration config = new Configuration();
            config.setTemplateLoader(templateLoader);
            retval = config.getTemplate(templateName);
        } catch (IOException e) {
            throw IOException.create(e);
        }
        if (retval == null) {
            throw TemplateNotFound.create(templateName);
        }
        return retval;
    }

    public OXTemplate loadTemplate(String templateName, String defaultTemplateName, ServerSession session) throws TemplateException {
        if(isEmpty(templateName) || !isUserTemplatingEnabled()) {
            return loadTemplate(defaultTemplateName);
        }
        try {
            FolderObject folder = folders.getPrivateTemplateFolder(session);
            FolderObject privateFolder = folder;
            boolean global = false;
            
            if(null == folder) {
                folder = folders.getGlobalTemplateFolder(session);
                global = true;
            }
            String templateText = (folder == null) ? null : infostore.findTemplateInFolder(session, folder, templateName);
            
            if(templateText == null && ! global) {
                folder = folders.getGlobalTemplateFolder(session);
                global = true;
            }
            
            templateText = (folder == null) ? null : infostore.findTemplateInFolder(session, folder, templateName);
            
            if(templateText == null) {
                templateText = loadFromFileSystem(defaultTemplateName);
                if(privateFolder == null) {
                    folder = folders.createPrivateTemplateFolder(session);
                    privateFolder = folder;
                }
                infostore.storeTemplateInFolder(session, privateFolder, templateName, templateText);
            }
            OXTemplateImpl template = new OXTemplateImpl();
            template.setTemplate(new Template(templateName, new StringReader(templateText), new Configuration()));
            return template;
        } catch (AbstractOXException e) {
            throw new TemplateException(e);
        } catch (IOException e) {
            throw IOException.create(e);
        }
    }

    private boolean isUserTemplatingEnabled() {
        return "true".equalsIgnoreCase(config.getProperty(USER_TEMPLATING_PROPERTY,"true"));
    }

    private boolean isEmpty(String templateName) {
        return templateName == null || "".equals(templateName);
    }

    private String loadFromFileSystem(String defaultTemplateName) throws TemplateException {
        File templateFile = getTemplateFile(defaultTemplateName);
        if (!templateFile.exists() || !templateFile.exists() || !templateFile.canRead()) {
            return "Unfilled Template."; 
        }
        BufferedReader reader = null;
        try {
            StringBuilder builder = new StringBuilder();
            reader = new BufferedReader(new FileReader(templateFile));
            String line = null;
            while((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            throw IOException.create(e);
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // IGNORE
                }
            }
        }
    }

    private File getTemplateFile(String defaultTemplateName) {
        return new File(config.getProperty(PATH_PROPERTY), defaultTemplateName);
    }

    public void setOXFolderHelper(OXFolderHelper helper) {
        this.folders = helper;
    }

    public void setInfostoreHelper(OXInfostoreHelper helper) {
        this.infostore = helper;
    }

}
