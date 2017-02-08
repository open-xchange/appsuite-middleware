package com.openexchange.capabilities.internal;

import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.capabilities.osgi.CapabilityCheckerRegistry;
import com.openexchange.capabilities.osgi.PermissionAvailabilityServiceRegistry;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.service.PermissionAvailabilityService;
import com.openexchange.osgi.NearRegistryServiceTracker;
import com.openexchange.server.ServiceLookup;

/**
 * Unit tests for {@link CapabilityServiceImpl}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PermissionAvailabilityServiceRegistry.class })
public class CapabilityServiceImplTest {

    /**
     * Class under test
     */
    @InjectMocks
    private CapabilityServiceImpl capabilityServiceImpl;

    /**
     * Mock of {@link ServiceLookup}
     */
    @Mock(name = "services")
    private ServiceLookup serviceLookup;

    /**
     * Mock of {@link CapabilityCheckerRegistry}
     */
    @Mock(name = "capCheckers")
    private CapabilityCheckerRegistry capabilityCheckerRegistry;

    /**
     * Mock of {@link NearRegistryServiceTracker}
     */
    @Mock
    private PermissionAvailabilityServiceRegistry registry;

    /**
     * The capabilities that should be filtered
     */
    private CapabilitySet capabilities = null;

    /**
     * {@inheritDoc}
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        this.capabilities = new CapabilitySet(64);
        this.capabilities.add(CapabilityServiceImpl.getCapability(Permission.CALDAV.toString().toLowerCase()));
        this.capabilities.add(CapabilityServiceImpl.getCapability(Permission.CARDDAV.toString().toLowerCase()));
        this.capabilities.add(CapabilityServiceImpl.getCapability(Permission.INFOSTORE.toString().toLowerCase()));
        this.capabilities.add(CapabilityServiceImpl.getCapability(Permission.EDIT_PASSWORD.toString().toLowerCase()));
        this.capabilities.add(CapabilityServiceImpl.getCapability(Permission.SUBSCRIPTION.toString().toLowerCase()));
        this.capabilities.add(CapabilityServiceImpl.getCapability(Permission.PUBLICATION.toString().toLowerCase()));
        this.capabilities.add(CapabilityServiceImpl.getCapability(Permission.WEBMAIL.toString().toLowerCase()));
    }

     @Test
     public void testApplyJSONFilter_TrackerNull_ReturnWithoutCheckAndWithoutRemoving() {
        this.capabilityServiceImpl = new CapabilityServiceImpl(this.serviceLookup, this.capabilityCheckerRegistry, null);
        this.capabilityServiceImpl.alignPermissions(this.capabilities);

        Assert.assertEquals(7, this.capabilities.size());
    }

     @Test
     public void testApplyJSONFilter_NoServiceRegistered_RemoveAllControlledPermissions() {
        this.capabilityServiceImpl = new CapabilityServiceImpl(this.serviceLookup, this.capabilityCheckerRegistry, this.registry);

        Mockito.when(this.registry.getServiceMap()).thenReturn(new ConcurrentHashMap<Permission, PermissionAvailabilityService>());

        this.capabilityServiceImpl.alignPermissions(this.capabilities);

        Assert.assertEquals(5, this.capabilities.size());
    }

     @Test
     public void testApplyJSONFilter_AllServicesRegistered_RemoveNoPermission() {
        this.capabilityServiceImpl = new CapabilityServiceImpl(this.serviceLookup, this.capabilityCheckerRegistry, this.registry);

        PermissionAvailabilityService jsonEditPassword = Mockito.mock(PermissionAvailabilityService.class);
        Mockito.when(jsonEditPassword.getRegisteredPermission()).thenReturn(Permission.EDIT_PASSWORD);
        PermissionAvailabilityService jsonSubscription = Mockito.mock(PermissionAvailabilityService.class);
        Mockito.when(jsonSubscription.getRegisteredPermission()).thenReturn(Permission.SUBSCRIPTION);
        PermissionAvailabilityService jsonPublication = Mockito.mock(PermissionAvailabilityService.class);
        Mockito.when(jsonPublication.getRegisteredPermission()).thenReturn(Permission.PUBLICATION);

        ConcurrentHashMap<Permission, PermissionAvailabilityService> registeredServices = new ConcurrentHashMap<Permission, PermissionAvailabilityService>();
        registeredServices.put(Permission.EDIT_PASSWORD, jsonEditPassword);
        registeredServices.put(Permission.SUBSCRIPTION, jsonSubscription);
        registeredServices.put(Permission.PUBLICATION, jsonPublication);

        Mockito.when(this.registry.getServiceMap()).thenReturn(registeredServices);

        this.capabilityServiceImpl.alignPermissions(this.capabilities);

        Assert.assertEquals(7, this.capabilities.size());
    }

     @Test
     public void testApplyJSONFilter_OnlySubscritionsRegistered_RemoveOtherPermissions() {
        this.capabilityServiceImpl = new CapabilityServiceImpl(this.serviceLookup, this.capabilityCheckerRegistry, this.registry);

        PermissionAvailabilityService jsonSubscription = Mockito.mock(PermissionAvailabilityService.class);
        Mockito.when(jsonSubscription.getRegisteredPermission()).thenReturn(Permission.SUBSCRIPTION);

        ConcurrentHashMap<Permission, PermissionAvailabilityService> registeredServices = new ConcurrentHashMap<Permission, PermissionAvailabilityService>();
        registeredServices.put(Permission.SUBSCRIPTION, jsonSubscription);

        Mockito.when(this.registry.getServiceMap()).thenReturn(registeredServices);

        this.capabilityServiceImpl.alignPermissions(this.capabilities);

        Assert.assertEquals(6, this.capabilities.size());
    }

     @Test
     public void testApplyJSONFilter_TwoPermissionsRegistered_RemoveOnePermissions() {
        this.capabilityServiceImpl = new CapabilityServiceImpl(this.serviceLookup, this.capabilityCheckerRegistry, this.registry);

        PermissionAvailabilityService jsonSubscription = Mockito.mock(PermissionAvailabilityService.class);
        Mockito.when(jsonSubscription.getRegisteredPermission()).thenReturn(Permission.SUBSCRIPTION);
        PermissionAvailabilityService jsonEditPassword = Mockito.mock(PermissionAvailabilityService.class);
        Mockito.when(jsonEditPassword.getRegisteredPermission()).thenReturn(Permission.EDIT_PASSWORD);

        ConcurrentHashMap<Permission, PermissionAvailabilityService> registeredServices = new ConcurrentHashMap<Permission, PermissionAvailabilityService>();
        registeredServices.put(Permission.EDIT_PASSWORD, jsonEditPassword);
        registeredServices.put(Permission.SUBSCRIPTION, jsonSubscription);

        Mockito.when(this.registry.getServiceMap()).thenReturn(registeredServices);

        this.capabilityServiceImpl.alignPermissions(this.capabilities);

        Assert.assertEquals(6, this.capabilities.size());
    }
}
