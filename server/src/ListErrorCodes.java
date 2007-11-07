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


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.tools.exceptions.EchoProcessor;
import com.openexchange.tools.exceptions.OXErrorCode;
import com.openexchange.tools.exceptions.OXErrorCodeProcessor;

import static java.util.Collections.sort;

public class ListErrorCodes {

    private static final String CODE_NAME = "Code";

    private static final String GET_CATEGORY = "getCategory";

    private static final String GET_NUMBER = "getNumber";

    private static final String GET_DETAIL_NUMBER = "getDetailNumber";
    
    private static final String GET_MESSAGE = "getMessage";

    private static final String GET_DESCRIPTION = "getDescription";

    /**
     * @param args
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     * @throws InvocationTargetException 
     * @throws IOException 
     * @throws IllegalArgumentException 
     */
    public static void main(final String[] args) throws ClassNotFoundException,
        InstantiationException, IllegalAccessException, NoSuchMethodException,
        InvocationTargetException, IOException {
    	
    	String packageName = "";
    	if(args.length == 0) {
    		System.err.println("Please specify a .jar file name on the classpath to search for error-codes. Example: java -jar ox_server.jar ox_server.jar");
    		System.exit(-1);
    	}
    	packageName = args[0];
    	
    	OXErrorCodeProcessor processor = new EchoProcessor();
    	
    	if(args.length > 1) {
    		try {
    			processor = (OXErrorCodeProcessor) Class.forName(args[1]).newInstance();
    		} catch (final Exception x) {
    			x.printStackTrace();
    		}
    	}
    	
    	final List<OXErrorCode> allCodes = new ArrayList<OXErrorCode>();
    	
    	collectCodes(allCodes, packageName);
    	sort(allCodes);
    	processCodes(allCodes, processor);
    	
       /* if (args.length == 0) {
            System.out.println("ListOXErrorCodes Exception...");
            System.exit(1);
        }
        for (String className : args) {
            final Class<? extends AbstractOXException> exceptionClass =
                (Class<? extends AbstractOXException>) Class.forName(className);
            System.out.println(exceptionClass.getName());
            Class[] classes = exceptionClass.getDeclaredClasses();
            for (Class test : classes) {
                if (test.getName().endsWith(CODE_NAME)) {
                    final Class<? extends Enum> codeClass =
                        (Class<? extends Enum>) test;
                    for (Enum e : codeClass.getEnumConstants()) {
                        OXErrorCode code = getCode(e);
                        System.out.println(code);
                    }
                    
                }
            }
            System.out.println();
        } */
    }

    private static void processCodes(final List<OXErrorCode> allCodes, final OXErrorCodeProcessor processor) {
		for (final OXErrorCode code : allCodes) {
			processor.process(code);
		}
		processor.done();
	}

	

	private static void collectCodes(final List<OXErrorCode> allCodes, final String packageName) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		final JarFile jar = new JarFile(new File(packageName));
		final Enumeration<JarEntry> e = jar.entries();
		
		final List<String> exceptionsAnalyzed = new ArrayList<String>();
		final StringBuilder errBuilder = new StringBuilder(50);
		while(e.hasMoreElements()) {
			final JarEntry entry = e.nextElement();
			try {
				if(entry.getName().endsWith(".class") && !entry.getName().contains("$")) {
					final String classname = entry.getName().replaceAll("\\/",".").substring(0, entry.getName().length()-6);
					final Class analyzeMe = Class.forName(classname);
                    
					if (isAbstractOXExceptionSubClass(analyzeMe)) {
							analyzeAbstractOXException(analyzeMe, allCodes);
							exceptionsAnalyzed.add(analyzeMe.getName());
					}
					/*try {
						analyzeClass(analyzeMe, allCodes);
					} catch (Exception x) {
						System.err.println("Couldn't analyze Class "+analyzeMe);
						x.printStackTrace();
					}*/
				}
			} catch (final Throwable x) {
				System.err.println(errBuilder.append("Couldn't analyze entry ").append(entry).append(' ').append(x).toString());
				errBuilder.setLength(0);
			}
		}
		
		System.out.println("=======================\n\n"+exceptionsAnalyzed+"\n\n=============================");
	}

    private static boolean isAbstractOXExceptionSubClass(final Class clazz) {
        if (Object.class.equals(clazz)) {
            return false;
        }
        final Class superClazz = clazz.getSuperclass();
        if (AbstractOXException.class.equals(superClazz)) {
            return true;
        }
        return isAbstractOXExceptionSubClass(superClazz);
    }
    
	private static void analyzeAbstractOXException(final Class analyzeMe, final List<OXErrorCode> allCodes) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		System.out.println(analyzeMe.getName()+" looking for inner classes whose names end with "+CODE_NAME);
        final Class[] classes = analyzeMe.getDeclaredClasses();
        int count = 0;
        for (final Class test : classes) {
            if (test.getName().endsWith(CODE_NAME)) {
                final Class<? extends Enum> codeClass =
                    (Class<? extends Enum>) test;
                for (final Enum e : codeClass.getEnumConstants()) {
                    final OXErrorCode code = getCode(e);
                    fillComponent(e, code);
                    allCodes.add(code);
                    count++;
                }
            }
        }
        System.out.println("Found "+count+" codes.");
	}

	private static OXErrorCode getCode(final Enum e) throws NoSuchMethodException,
        InstantiationException, IllegalAccessException,
        InvocationTargetException {
    	final Class< ? extends Enum> enumClass = e.getClass();
        final OXErrorCode retval = new OXErrorCode();
        retval.clazz = enumClass.getEnclosingClass().asSubclass(
                AbstractOXException.class);
        final Method getCategoryMethod = enumClass.getMethod(GET_CATEGORY,
            new Class[0]);
        retval.category = (Category) getCategoryMethod.invoke(e, new Object[0]);
        Method getNumberMethod;
        try {
            getNumberMethod = enumClass.getMethod(GET_NUMBER, new Class[0]);
        } catch (final NoSuchMethodException nsme) {
            getNumberMethod = enumClass.getMethod(GET_DETAIL_NUMBER,
                new Class[0]);
        }
        retval.number = (Integer) getNumberMethod.invoke(e, new Object[0]);
        final Method getMessageMethod = enumClass.getMethod(GET_MESSAGE,
            new Class[0]);
        retval.message = (String) getMessageMethod.invoke(e, new Object[0]);
        try {
            final Method getDescriptionMethod = enumClass.getMethod(
                GET_DESCRIPTION, new Class[0]);
            retval.description = (String) getDescriptionMethod.invoke(e,
                new Object[0]);
        } catch (final NoSuchMethodException nsme) {
            retval.description = "TODO";
        }
        return retval;
    }

    private static void fillComponent(final Enum e, final OXErrorCode code) {
        final Class< ? extends Enum> enumClass = e.getClass();
        try {
            final Constructor<? extends AbstractOXException> constructor =
                code.clazz.getConstructor(new Class[] { enumClass,
                    Object[].class });
            final Object tmp = constructor.newInstance(new Object[] { e,
                new Object[0] });
            final AbstractOXException exception = (AbstractOXException) tmp;
            code.component = exception.getComponent();
        } catch (final Throwable t) {
            System.out.println("Cannot get component of class "
                + code.clazz.getName());
        }
    }
}
