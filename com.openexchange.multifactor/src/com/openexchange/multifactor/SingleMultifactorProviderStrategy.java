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

package com.openexchange.multifactor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;

/**
 * {@link SingleMultifactorProviderStrategy} - An implementation of {@link MultifactorProviderStrategy} which does only authenticate against one single Provider chosen by the client
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class SingleMultifactorProviderStrategy implements MultifactorProviderStrategy {

    private final MultifactorAuthenticatorFactory multifactorAuthFactory;
    private final MultifactorDevice device;
    private final ChallengeAnswer answer;

    /**
     * Initializes a new {@link SingleMultifactorProviderStrategy}.
     *
     * @param multifactorLockoutService An instance of {@link MultifactorLockoutService} to use
     * @param device The {@link MultifactorDevice to use for authentication}
     */
    public SingleMultifactorProviderStrategy(MultifactorAuthenticatorFactory multifactorAuthFactory, MultifactorDevice device, ChallengeAnswer answer) {
        this.multifactorAuthFactory = Objects.requireNonNull(multifactorAuthFactory, "multifactorLockoutService must not be null");
        this.device = device;
        this.answer = answer;
    }

    /**
     * Internal method to get a set of providers which have at least one enabled device
     *
     * @param multifactorRequest The request
     * @param providers The set of providers
     * @return All providers from the given collection which have enabled devices.
     * @throws OXException
     */
    private Collection<MultifactorProvider> getEnabledProviders(MultifactorRequest multifactorRequest, Collection<MultifactorProvider> providers) throws OXException {

        if (providers == null || providers.isEmpty()) {
            return Collections.emptyList();
        }

        final List<MultifactorProvider> ret = new ArrayList<MultifactorProvider>();
        for (final MultifactorProvider p : providers) {
            final Collection<? extends MultifactorDevice> enabledDevices = p.getEnabledDevices(multifactorRequest);
            if (enabledDevices != null && !enabledDevices.isEmpty()) {
                ret.add(p);
            }
        }
        return ret;
    }

    /**
     * Gets the provider to use
     *
     * @param providers The list of available providers
     * @param name The name of the provider to get
     * @return The provider with the given name
     */
    private Optional<MultifactorProvider> getProvider(Collection<MultifactorProvider> providers, String name){
       if(providers.size() == 1) {
           return providers.stream().findFirst();
       }
       return providers.stream().filter(p -> p.getName().equals(name)).findFirst();
    }

    @Override
    public boolean requireAuthentication(Collection<MultifactorProvider> providers, MultifactorRequest multifactorRequest) throws OXException {
        final Collection<MultifactorProvider> enabledProviders = getEnabledProviders(multifactorRequest, providers);
        if(!enabledProviders.isEmpty()) {
            //Multifactor-authentication required because at least one provider is enabled for the session.

            //Get the name of the provider the client wants to authenticate against
            //Can be omitted if there is exactly one provider enabled
            if(device.getProviderName() == null && enabledProviders.size() != 1) {
                //No provider was chosen by the client but authentication against at least one provider is required
                throw MultifactorExceptionCodes.MISSING_PROVIDER_NAME.create();
            }

            final Optional<MultifactorProvider> provider = getProvider(enabledProviders, device.getProviderName());
            if(provider.isPresent()){
                //Authenticate

                final MultifactorAuthenticator authenticator = multifactorAuthFactory.createAuthenticator(provider.get());
                authenticator.requireAuthentication(multifactorRequest, device.getId(), answer);
                return true;
            }
            //The given provider is unknown
            throw MultifactorExceptionCodes.PROVIDER_NOT_AVAILABLE.create(device.getProviderName());
        }
        return false;
    }
}
