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

package com.openexchange.tools.exceptions;

import static java.util.Arrays.asList;
import static java.util.Collections.sort;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;



import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ClassType;

public class OXExceptionAnnotationFactory implements AnnotationProcessorFactory {

	public AnnotationProcessor getProcessorFor(
			Set<AnnotationTypeDeclaration> declarations,
			AnnotationProcessorEnvironment env) {
		
		OXErrorCodeProcessor processor = null;
		processor = getProcessor(env.getOptions());
		
		
		return new OXErrorCodeAnnotationProcessor(processor, env);
	}

	private OXErrorCodeProcessor getProcessor(Map<String, String> options) {
		for(String key : options.keySet()){
			if(key.startsWith("-AerrorCodeProcessor=")) {
				String classname = key.substring(21);
				try {
					return (OXErrorCodeProcessor) Class.forName(classname).newInstance();
				} catch (Exception x) {
					x.printStackTrace();
				}
			}
		}
		return new EchoProcessor();
	}

	public Collection<String> supportedAnnotationTypes() {
		return asList("com.openexchange.groupware.OXExceptionSource", "com.openexchange.groupware.OXThrows","com.openexchange.groupware.OXThrowsMultiple");
	}

	public Collection<String> supportedOptions() {
		return asList("-AerrorCodeProcessor", "-AanalyzeOXExceptions");
	}
	
	private static final class OXErrorCodeAnnotationProcessor implements AnnotationProcessor{

		private AnnotationProcessorEnvironment env;
		private OXErrorCodeProcessor processor;

		public OXErrorCodeAnnotationProcessor(OXErrorCodeProcessor processor, AnnotationProcessorEnvironment env) {
			this.processor = processor;
			this.env = env;
		}

		public void process() {
			Collection<TypeDeclaration> types = env.getTypeDeclarations();
			List<OXErrorCode> codes = new ArrayList<OXErrorCode>();
			
			for(TypeDeclaration decl : types) {
				if(decl instanceof ClassDeclaration) {
					ClassDeclaration classDecl = (ClassDeclaration) decl;
					analyze(classDecl, codes);
				}
			}
			
			sort(codes);
			
			for(OXErrorCode errorCode : codes) {
				processor.process(errorCode);
			}
			processor.done();
		}
		
		private void analyze(ClassDeclaration classDecl, List<OXErrorCode> allCodes) {
			OXExceptionSource exceptionSource  = (OXExceptionSource) classDecl.getAnnotation(OXExceptionSource.class);
			if(exceptionSource == null)
				return;
			
			int classId = exceptionSource.classId();
			
			for(MethodDeclaration methodDecl : classDecl.getMethods()) {
				OXThrows throwsInfo = (OXThrows) methodDecl.getAnnotation(OXThrows.class);
				if(throwsInfo != null) {
					OXErrorCode errorCode = new OXErrorCode();
					errorCode.component = exceptionSource.component();
					errorCode.category = throwsInfo.category();
					errorCode.description = throwsInfo.desc();
					errorCode.message = throwsInfo.msg();
					errorCode.number=classId*100+throwsInfo.exceptionId();
					allCodes.add(errorCode);
				}
					
				
				OXThrowsMultiple multiple = (OXThrowsMultiple) methodDecl.getAnnotation(OXThrowsMultiple.class);
				if(multiple != null) {
					for(int i = 0; i < multiple.exceptionId().length; i++) {
						OXErrorCode errorCode = new OXErrorCode();
						errorCode.component = exceptionSource.component();
						errorCode.category = multiple.category()[i];
						if(multiple.desc().length > i)
							errorCode.description = multiple.desc()[i];
						else
							errorCode.description = "No Description";
						errorCode.message = multiple.msg()[i];
						errorCode.number=classId*100+multiple.exceptionId()[i];
						allCodes.add(errorCode);
					}
				}
			}
		}	
	}
}
