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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.templating.OXTemplate.TemplateLevel;
import com.openexchange.templating.impl.OXFolderHelper;
import com.openexchange.templating.impl.OXInfostoreHelper;
import com.openexchange.tools.session.ServerSession;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class TemplateServiceImpl implements TemplateService {

    public static final String PATH_PROPERTY = "com.openexchange.templating.path";

    public static final String USER_TEMPLATING_PROPERTY = "com.openexchange.templating.usertemplating";

	private static final Map<String, Map<String,Set<String>>> cachedTags = new ConcurrentHashMap<String, Map<String,Set<String>>>();

	private static final Log LOG = LogFactory.getLog(TemplateServiceImpl.class);

    private ConfigurationService config;

    private OXFolderHelper folders;

    private OXInfostoreHelper infostore;
    
    private TemplateExceptionHandler exceptionHandler;

    public TemplateServiceImpl(ConfigurationService config) {
        this.config = config;
        exceptionHandler = null;
    }

    public OXTemplateImpl loadTemplate(String templateName) throws TemplateException {
        String templatePath = config.getProperty(PATH_PROPERTY);
        if (templatePath == null) {
            return null;
        }

        OXTemplateImpl retval = new OXTemplateImpl();
        retval.setLevel(TemplateLevel.SERVER);
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
            if (exceptionHandler != null) {
                config.setTemplateExceptionHandler(exceptionHandler);
            }
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
        if (isEmpty(templateName) || !isUserTemplatingEnabled(session)) {
            return loadTemplate(defaultTemplateName);
        }
        try {
            FolderObject folder = folders.getPrivateTemplateFolder(session);
            FolderObject privateFolder = folder;
            boolean global = false;

            if (null == folder) {
                folder = folders.getGlobalTemplateFolder(session);
                global = true;
            }
            String templateText = (folder == null) ? null : infostore.findTemplateInFolder(session, folder, templateName);

            Configuration config = new Configuration();
            if (exceptionHandler != null) {
                config.setTemplateExceptionHandler(exceptionHandler);
            }
            if (templateText != null) {
                OXTemplateImpl template = new OXTemplateImpl();
                template.setTemplate(new Template(templateName, new StringReader(templateText), config));
                template.setLevel(TemplateLevel.USER);
                return template;
            }

            if (!global) {
                folder = folders.getGlobalTemplateFolder(session);
                global = true;

                templateText = (folder == null) ? null : infostore.findTemplateInFolder(session, folder, templateName);
            }

            if (templateText == null) {
                
                if (existsInFilesystem(templateName)) {
                    templateText = loadFromFileSystem(templateName);
                    OXTemplateImpl template = new OXTemplateImpl();
                    template.setTemplate(new Template(templateName, new StringReader(templateText), config));
                    template.setLevel(TemplateLevel.SERVER);
                    return template;
                }
                
                templateText = loadFromFileSystem(defaultTemplateName);
                if (privateFolder == null) {
                    folder = folders.createPrivateTemplateFolder(session);
                    privateFolder = folder;
                }
                infostore.storeTemplateInFolder(session, privateFolder, templateName, templateText);
            }
            OXTemplateImpl template = new OXTemplateImpl();
            template.setTemplate(new Template(templateName, new StringReader(templateText), config));
            template.setLevel(TemplateLevel.USER);
            return template;
        } catch (AbstractOXException e) {
            throw new TemplateException(e);
        } catch (IOException e) {
            throw IOException.create(e);
        }
    }

    private boolean isUserTemplatingEnabled(ServerSession session) {
        return "true".equalsIgnoreCase(config.getProperty(USER_TEMPLATING_PROPERTY, "true")) && session.getUserConfiguration().hasInfostore();
    }

    private boolean isEmpty(String templateName) {
        return templateName == null || "".equals(templateName);
    }
    
    protected boolean existsInFilesystem(String templateName) {
        File templateFile = getTemplateFile(templateName);
        if (!templateFile.exists() || !templateFile.exists() || !templateFile.canRead()) {
            return false;
        }
        return true;
    }

    protected String loadFromFileSystem(String defaultTemplateName) throws TemplateException {
        File templateFile = getTemplateFile(defaultTemplateName);
        if (!templateFile.exists() || !templateFile.exists() || !templateFile.canRead()) {
            return "Unfilled Template.";
        }
        BufferedReader reader = null;
        try {
            StringBuilder builder = new StringBuilder();
            reader = new BufferedReader(new FileReader(templateFile));
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            throw IOException.create(e);
        } finally {
            if (reader != null) {
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

    public List<String> getBasicTemplateNames(String...filter) {
        String templatePath = config.getProperty(PATH_PROPERTY);
        File templateDir = new File(templatePath);
        if (!templateDir.isDirectory() || !templateDir.exists()) {
            return new ArrayList<String>(0);
        }
        
        Set<String> sieve = new HashSet<String>(Arrays.asList(filter));
        
        Map<String, Set<String>> tagMap = getTagMap(templateDir);
        
        File[] files = templateDir.listFiles();
        if (files == null) {
            return new ArrayList<String>(0);
        }
        Set<String> names = new HashSet<String>();
        Set<String> defaults = new HashSet<String>();
        for (File file : files) {
            Set<String> tags = tagMap.get(file.getName());
            if (tags == null) {
                tags = Collections.emptySet();
            }
            if (file.isFile() && file.canRead() && file.getName().endsWith(".tmpl") && (tags.containsAll(sieve))) {
                if (tags.contains("default")) {
                    defaults.add(file.getName());
                } else {
                    names.add(file.getName());
                }
                
            }
        }
        List<String> a = new ArrayList(defaults);
        List<String> b = new ArrayList(names);
        Collections.sort(a);
        Collections.sort(b);
        a.addAll(b);
        
        return a;
    }

    private Map<String, Set<String>> getTagMap(File templateDir) {
    	String absolutePath = templateDir.getAbsolutePath();
    	
		if(cachedTags.containsKey(absolutePath)){
    		return cachedTags.get(absolutePath);
		}
		
    	File[] files = templateDir.listFiles(new FileFilter(){
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".properties")
				&& pathname.canRead() && pathname.isFile();
			}
		});
    	if(files == null){
    		Map<String, Set<String>> emptyMap = Collections.emptyMap();
			cachedTags.put(absolutePath, emptyMap);
    		return emptyMap;
    	}
    	
        HashMap<String, Set<String>> tagMap = new HashMap<String, Set<String>>();
    	for (File file : files) {
    		Properties index = new Properties();
    		InputStream inStream = null;
    		try {
				inStream = new FileInputStream(file);
				index.load(inStream);
				Set<Entry<Object, Object>> entrySet = index.entrySet();
				
				for (Entry<Object, Object> entry : entrySet) {
					String filename = (String) entry.getKey();
					String[] categoriesArr = ((String) entry.getValue()).split("\\s*,\\s*");
					HashSet<String> categories = new HashSet<String>(Arrays.asList(categoriesArr));
					tagMap.put(filename, categories);
				}
			} catch (FileNotFoundException e) {
				LOG.error(e.getMessage(), e);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			} finally {
				if (inStream != null) {
					try {
						inStream.close();
					} catch (IOException e) {
					}
				}
			}
    	}
        cachedTags.put(absolutePath, tagMap);
        return tagMap;

	}

	public List<String> getTemplateNames(ServerSession session,
			String... filter) throws TemplateException {
		if(filter == null) {
			filter = new String[0];
		}
        Set<String> names = new HashSet<String>();
        if (!isUserTemplatingEnabled(session)) {
            return getBasicTemplateNames(filter);
        }

        try {
            FolderObject globalTemplateFolder = folders.getGlobalTemplateFolder(session);
            FolderObject privateTemplateFolder = folders.getPrivateTemplateFolder(session);

            if (globalTemplateFolder != null) {
                names.addAll(infostore.getNames(session, globalTemplateFolder, filter));
            }
            if (privateTemplateFolder != null) {
                names.addAll(infostore.getNames(session, privateTemplateFolder, filter));
            }

        } catch (AbstractOXException e) {
            throw new TemplateException(e);
        }
        List<String> basicTemplateNames = getBasicTemplateNames(filter);
        ArrayList<String> userTemplates = new ArrayList<String>(names);
        Collections.sort(userTemplates);
        basicTemplateNames.addAll(userTemplates);
        return basicTemplateNames;
	}

    public OXTemplate loadTemplate(String templateName, OXTemplateExceptionHandler exceptionHandler) throws TemplateException {
             
        return loadTemplate(templateName);
    }

    public OXTemplate loadTemplate(String templateName, String defaultTemplateName, ServerSession session, OXTemplateExceptionHandler exceptionHandler) throws TemplateException {
        setExceptionHandler(exceptionHandler);   
        return loadTemplate(templateName, defaultTemplateName, session);
    }
	
    private void setExceptionHandler(OXTemplateExceptionHandler exceptionHandler) {
        TemplateExceptionHandler wrapper = new TemplateExceptionHandlerWrapper(exceptionHandler);
        this.exceptionHandler = wrapper;
    }

}
