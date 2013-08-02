package com.openexchange.capabilities.internal;

import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.osgi.CapabilityCheckerRegistry;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.service.PermissionAvailabilityService;
import com.openexchange.java.ConcurrentHashSet;
import com.openexchange.java.ConcurrentList;
import com.openexchange.osgi.NearRegistryServiceTracker;
import com.openexchange.server.ServiceLookup;
import com.openexchange.test.mock.test.AbstractMockTest;

/**
 * Unit tests for {@link CapabilityServiceImpl}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
@PrepareForTest({ NearRegistryServiceTracker.class })
public class CapabilityServiceImplTest extends AbstractMockTest {

    /**
     * Class under test
     */
    private CapabilityServiceImpl capabilityServiceImpl = null;

    /**
     * Mock of {@link ServiceLookup}
     */
    private ServiceLookup serviceLookup = null;

    /**
     * Mock of {@link CapabilityCheckerRegistry}
     */
    private CapabilityCheckerRegistry capabilityCheckerRegistry = null;

    /**
     * Mock of {@link NearRegistryServiceTracker}
     */
    private NearRegistryServiceTracker<PermissionAvailabilityService> nearRegistryServiceTracker = null;

    /**
     * The capabilities that should be filtered
     */
    Set<Capability> capabilities = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception {
        this.serviceLookup = Mockito.mock(ServiceLookup.class);
        this.capabilityCheckerRegistry = Mockito.mock(CapabilityCheckerRegistry.class);
        this.nearRegistryServiceTracker = PowerMockito.mock(NearRegistryServiceTracker.class);

        this.capabilities = new ConcurrentHashSet<Capability>(64);
        this.capabilities.add(new Capability(Permission.CALDAV.toString()));
        this.capabilities.add(new Capability(Permission.CARDDAV.toString()));
        this.capabilities.add(new Capability(Permission.INFOSTORE.toString()));
        this.capabilities.add(new Capability(Permission.EDIT_PASSWORD.toString()));
        this.capabilities.add(new Capability(Permission.SUBSCRIPTION.toString()));
        this.capabilities.add(new Capability(Permission.PUBLICATION.toString()));
        this.capabilities.add(new Capability(Permission.WEBMAIL.toString()));
    }

    @Test
    public void testApplyJSONFilter_TrackerNull_ReturnWithoutCheckAndWithoutRemoving() {
        this.capabilityServiceImpl = new CapabilityServiceImpl(
            this.serviceLookup,
            this.capabilityCheckerRegistry,
            null);
        this.capabilityServiceImpl.applyUIFilter(this.capabilities);

        Assert.assertEquals(7, this.capabilities.size());
    }

    @Test
    public void testApplyJSONFilter_NoServiceRegistered_RemoveAllControlledPermissions() {
        this.capabilityServiceImpl = new CapabilityServiceImpl(
            this.serviceLookup,
            this.capabilityCheckerRegistry,
            this.nearRegistryServiceTracker);

        Mockito.when(this.nearRegistryServiceTracker.getServiceList()).thenReturn(new ConcurrentList<PermissionAvailabilityService>());

        this.capabilityServiceImpl.applyUIFilter(this.capabilities);

        Assert.assertEquals(4, this.capabilities.size());
    }

    @Test
    public void testApplyJSONFilter_AllServicesRegistered_RemoveNoPermission() {
        this.capabilityServiceImpl = new CapabilityServiceImpl(
            this.serviceLookup,
            this.capabilityCheckerRegistry,
            this.nearRegistryServiceTracker);

        PermissionAvailabilityService jsonEditPassword = Mockito.mock(PermissionAvailabilityService.class);
        Mockito.when(jsonEditPassword.getRegisteredPermission()).thenReturn(Permission.EDIT_PASSWORD);
        PermissionAvailabilityService jsonSubscription = Mockito.mock(PermissionAvailabilityService.class);
        Mockito.when(jsonSubscription.getRegisteredPermission()).thenReturn(Permission.SUBSCRIPTION);
        PermissionAvailabilityService jsonPublication = Mockito.mock(PermissionAvailabilityService.class);
        Mockito.when(jsonPublication.getRegisteredPermission()).thenReturn(Permission.PUBLICATION);

        ConcurrentList<PermissionAvailabilityService> registeredServices = new ConcurrentList<PermissionAvailabilityService>();
        registeredServices.add(jsonEditPassword);
        registeredServices.add(jsonSubscription);
        registeredServices.add(jsonPublication);

        Mockito.when(this.nearRegistryServiceTracker.getServiceList()).thenReturn(registeredServices);

        this.capabilityServiceImpl.applyUIFilter(this.capabilities);

        Assert.assertEquals(7, this.capabilities.size());
    }

    @Test
    public void testApplyJSONFilter_OnlySubscritionsRegistered_RemoveOtherPermissions() {
        this.capabilityServiceImpl = new CapabilityServiceImpl(
            this.serviceLookup,
            this.capabilityCheckerRegistry,
            this.nearRegistryServiceTracker);

        PermissionAvailabilityService jsonSubscription = Mockito.mock(PermissionAvailabilityService.class);
        Mockito.when(jsonSubscription.getRegisteredPermission()).thenReturn(Permission.SUBSCRIPTION);

        ConcurrentList<PermissionAvailabilityService> registeredServices = new ConcurrentList<PermissionAvailabilityService>();
        registeredServices.add(jsonSubscription);

        Mockito.when(this.nearRegistryServiceTracker.getServiceList()).thenReturn(registeredServices);

        this.capabilityServiceImpl.applyUIFilter(this.capabilities);

        Assert.assertEquals(5, this.capabilities.size());
    }

    @Test
    public void testApplyJSONFilter_TwoPermissionsRegistered_RemoveOnePermissions() {
        this.capabilityServiceImpl = new CapabilityServiceImpl(
            this.serviceLookup,
            this.capabilityCheckerRegistry,
            this.nearRegistryServiceTracker);

        PermissionAvailabilityService jsonSubscription = Mockito.mock(PermissionAvailabilityService.class);
        Mockito.when(jsonSubscription.getRegisteredPermission()).thenReturn(Permission.SUBSCRIPTION);
        PermissionAvailabilityService jsonEditPassword = Mockito.mock(PermissionAvailabilityService.class);
        Mockito.when(jsonEditPassword.getRegisteredPermission()).thenReturn(Permission.EDIT_PASSWORD);

        ConcurrentList<PermissionAvailabilityService> registeredServices = new ConcurrentList<PermissionAvailabilityService>();
        registeredServices.add(jsonSubscription);
        registeredServices.add(jsonEditPassword);

        Mockito.when(this.nearRegistryServiceTracker.getServiceList()).thenReturn(registeredServices);

        this.capabilityServiceImpl.applyUIFilter(this.capabilities);

        Assert.assertEquals(6, this.capabilities.size());
    }
}
