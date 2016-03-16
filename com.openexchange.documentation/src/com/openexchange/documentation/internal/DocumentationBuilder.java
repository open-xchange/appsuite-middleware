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

package com.openexchange.documentation.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.documentation.DescriptionFactory;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Actions;
import com.openexchange.documentation.annotations.Attribute;
import com.openexchange.documentation.annotations.Container;
import com.openexchange.documentation.annotations.Module;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.documentation.descriptions.ActionDescription;
import com.openexchange.documentation.descriptions.AttributeDescription;
import com.openexchange.documentation.descriptions.ContainerDescription;
import com.openexchange.documentation.descriptions.ModuleDescription;
import com.openexchange.documentation.descriptions.ParameterDescription;

/**
 * {@link DocumentationBuilder} - The documentation builder.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DocumentationBuilder {

    private final Collection<ActionDescription> knownActions;
    private final Collection<ContainerDescription> knownContainers;
    private String moduleName;
    private String moduleDescription;
    private final DescriptionFactory factory;

    /**
     * Initializes a new {@link DocumentationBuilder}.
     */
    public DocumentationBuilder(final DescriptionFactory factory) {
        super();
        this.factory = factory;
        this.knownActions = new ArrayList<ActionDescription>();
        this.knownContainers = new ArrayList<ContainerDescription>();
    }

    /**
     * Adds specified classes' annotations to this documentation builder considering the annotation types:
     * <ul>
     * <li><tt><b>com.openexchange.documentation.annotations.Action</b></tt></li>
     * <li><tt><b>com.openexchange.documentation.annotations.Container</b></tt></li>
     * <li><tt><b>com.openexchange.documentation.annotations.Module</b></tt></li>
     * </ul>
     *
     * @param c The class to add
     * @return This documentation builder with specified classes added
     */
    public DocumentationBuilder add(final Class<?> clazz) {
    	this.addAnnotations(clazz);
    	return this;
    }

    /**
     * Adds specified collection objects' classes annotations to this documentation builder considering the annotation types:
     * <ul>
     * <li><tt><b>com.openexchange.documentation.annotations.Action</b></tt></li>
     * <li><tt><b>com.openexchange.documentation.annotations.Container</b></tt></li>
     * <li><tt><b>com.openexchange.documentation.annotations.Module</b></tt></li>
     * </ul>
     *
     * @param collection The collection objects' classes to add
     * @return This documentation builder with specified classes added
     */
    public DocumentationBuilder add(final Collection<?> collection) {
    	if (null != collection) {
	        for (final Object object : collection) {
	            addAnnotations(object.getClass());
	        }
    	}
        return this;
    }

    /**
     * Adds annotation of specified class to this documentation builder
     *
     * @param clazz The class
     */
    protected void addAnnotations(final Class<?> clazz) {
        addAnnotations((AnnotatedElement) clazz);
        /*
         * Gather annotated elements of specified class
         */
        final Set<AnnotatedElement> elements = new HashSet<AnnotatedElement>();
        /*
         * ... fields
         */
        for (final Field field : clazz.getFields()) {
            elements.add(field);
        }
        /*
         * ... declared fields
         */
        for (final Field field : clazz.getDeclaredFields()) {
            elements.add(field);
        }
        /*
         * ... methods
         */
        for (final Method method : clazz.getMethods()) {
            elements.add(method);
        }
        /*
         * ... declared methods
         */
        for (final Method method : clazz.getDeclaredMethods()) {
            elements.add(method);
        }
        /*
         * ... constructors
         */
        for (final Constructor<?> constructor : clazz.getConstructors()) {
            elements.add(constructor);
        }
        /*
         * ... annotated elements
         */
        for (final AnnotatedElement element : elements) {
            addAnnotations(element);
        }
        /*
         * ... classes
         */
        final Set<Class<?>> seenClasses = new HashSet<Class<?>>();
        for (final Class<?> c : clazz.getClasses()) {
            addAnnotations(c);
            seenClasses.add(c);
        }
        /*
         * ... declared classes
         */
        for (final Class<?> c : clazz.getDeclaredClasses()) {
            if (!seenClasses.contains(c)) {
                addAnnotations(c);
            }
        }
    }

    /**
     * Adds annotation of specified annotated element to this documentation builder.
     * <p>
     * Considers the annotation types:
     * <ul>
     * <li><tt>com.openexchange.documentation.annotations.Action</tt></li>
     * <li><tt>com.openexchange.documentation.annotations.Container</tt></li>
     * <li><tt>com.openexchange.documentation.annotations.Module</tt></li>
     * </ul>
     *
     * @param element The annotated element
     */
    protected void addAnnotations(final AnnotatedElement element) {
        for (final Annotation annotation : element.getAnnotations()) {
            if (Module.class.isInstance(annotation)) {
            	if (this.hasModule()) {
            		throw new IllegalStateException("Only one module definition allowed");
            	}
                final Module module = (Module)annotation;
                this.moduleName = module.name();
                this.moduleDescription = module.description();
            } else if (Container.class.isInstance(annotation)) {
            	this.knownContainers.add(this.getContainer((Container)annotation));
            } else if (Action.class.isInstance(annotation)) {
                this.knownActions.add(this.getAction((Action)annotation));
            } else if (Actions.class.isInstance(annotation)) {
                this.knownActions.addAll(this.getActions((Actions)annotation));
            }
        }
    }

    protected ContainerDescription getContainer(final Container container) {
    	return factory.container(container.name(), container.description(), this.getAttributes(container.attributes()));
    }

    protected AttributeDescription getAttribute(final Attribute attribute) {
    	return factory.attribute(attribute.name(), attribute.description(), attribute.type(), attribute.mandatory());
    }

    protected AttributeDescription[] getAttributes(final Attribute[] attributes) {
    	final AttributeDescription[] attributeDescriptions = new AttributeDescription[attributes.length];
    	for (int i = 0; i < attributes.length; i++) {
    		attributeDescriptions[i] = this.getAttribute(attributes[i]);
		}
    	return attributeDescriptions;
    }

    protected ParameterDescription getParameter(final Parameter parameter) {
    	return factory.parameter(parameter.name(), parameter.description(), parameter.type(), parameter.optional());
    }

    protected ParameterDescription[] getParameters(final Parameter[] parameters) {
    	final ParameterDescription[] parameterDescriptions = new ParameterDescription[parameters.length];
    	for (int i = 0; i < parameters.length; i++) {
    		parameterDescriptions[i] = this.getParameter(parameters[i]);
		}
    	return parameterDescriptions;
    }

    protected ActionDescription getAction(final Action action) {
        return factory.action(action.name(), action.description(), action.method(), action.defaultFormat(),
                action.requestBody(), action.responseDescription(), action.deprecated(), getParameters(action.parameters()));
    }

    protected Collection<ActionDescription> getActions(final Actions actions) {
        final Collection<ActionDescription> actionDescriptions = new ArrayList<ActionDescription>();
        if (null != actions.value()) {
            for (final Action action : actions.value()) {
                actionDescriptions.add(this.getAction(action));
            }
        }
        return actionDescriptions;
    }

    public boolean hasModule() {
    	return null != this.moduleName;
    }

    public ContainerDescription[] getContainerDescriptions() {
    	return this.knownContainers.toArray(new ContainerDescription[knownContainers.size()]);
    }

    public ActionDescription[] getActionDescriptions() {
    	return this.knownActions.toArray(new ActionDescription[knownActions.size()]);
    }

    public ModuleDescription getModuleDescription() {
    	if (this.hasModule()) {
    		return factory.module(moduleName, moduleDescription, getContainerDescriptions(), getActionDescriptions());
    	} else {
    		return null;
    	}
    }

}
