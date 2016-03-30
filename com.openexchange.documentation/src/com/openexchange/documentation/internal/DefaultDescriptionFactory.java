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

import com.openexchange.documentation.DescriptionFactory;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.Type;
import com.openexchange.documentation.descriptions.ActionDescription;
import com.openexchange.documentation.descriptions.AttributeDescription;
import com.openexchange.documentation.descriptions.ContainerDescription;
import com.openexchange.documentation.descriptions.ModuleDescription;
import com.openexchange.documentation.descriptions.ParameterDescription;

/**
 * {@link DefaultDescriptionFactory} - Default {@link DescriptionFactory} implementation.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DefaultDescriptionFactory implements DescriptionFactory {

	@Override
	public ModuleDescription module(final String name, final String description, final ContainerDescription[] containers, final ActionDescription... actions) {
		return new ModuleDescription() {

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getDescription() {
				return null != description ? description : "";
			}

			@Override
			public ContainerDescription[] getContainers() {
				return containers;
			}

			@Override
			public ActionDescription[] getActions() {
				return actions;
			}
		};
	}

	@Override
	public ContainerDescription container(final String name, final String description, final AttributeDescription... attributes) {
		return new ContainerDescription() {

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getDescription() {
				return null != description ? description : "";
			}

			@Override
			public AttributeDescription[] getAttributes() {
				return attributes;
			}
		};
	}

	@Override
	public ActionDescription action(final String name, final String description, final RequestMethod method,
			final String defaultFormat, final String requestBody, final String responseDescription, final boolean deprecated,
			final ParameterDescription... parameters) {
		return new ActionDescription() {

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getDescription() {
				return null != description ? description : "";
			}

			@Override
			public boolean isDeprecated() {
				return deprecated;
			}

			@Override
			public String getResponseDescription() {
				return null != responseDescription ? responseDescription : "";
			}

			@Override
			public String getRequestBody() {
				return null != requestBody ? requestBody : "";
			}

			@Override
			public ParameterDescription[] getParameters() {
				return parameters;
			}

			@Override
			public RequestMethod getMethod() {
				return method;
			}

			@Override
			public String getDefaultFormat() {
				return null != defaultFormat ? defaultFormat : "apiResponse";
			}
		};
	}

	@Override
	public AttributeDescription attribute(final String name, final String description, final Type type, final boolean mandatory) {
		return new AttributeDescription() {

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getDescription() {
				return null != description ? description : "";
			}

			@Override
			public boolean isMandatory() {
				return mandatory;
			}

			@Override
			public Type getType() {
				return null != type ? type : Type.STRING;
			}
		};
	}

	@Override
	public ParameterDescription parameter(final String name, final String description, final Type type, final boolean optional) {
		return new ParameterDescription() {

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getDescription() {
				return null != description ? description : "";
			}

			@Override
			public boolean isOptional() {
				return optional;
			}

			@Override
			public Type getType() {
				return null != type ? type : Type.STRING;
			}
		};
	}

}
