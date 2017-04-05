package com.openexchange.folderstorage.internal;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ImmutablePermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.server.services.ServerServiceRegistry;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigViewFactory.class, ServerServiceRegistry.class})
public class ConfiguredDefaultPermissionsTest {
    
    private static final String PARENTFOLDER_PERMISSIONS = "parent=group_2@2.4.0.0,admin_user_5@8.4.4.4";
    private static final String CUSTOM_FOLDER_PERMISSIONS = "15=admin_group_2@author";
    private static final String FAIL_PERMISSION_EXPRESSION_PREFIX = "folder=failPerm@author";
    private static final String FAIL_PERMISSION_EXPRESSION_NO_EQUAL = "folderfailPerm@author";
    private static final String FAIL_PERMISSION_EXPRESSION_NO_AT = "folder=admin_user_5author";
    private static final String FAIL_PERMISSION_EXPRESSION_NO_PERMISSIONS = "folder=admin_user_5@";
    private static final String FAIL_PERMISSION_EXPRESSION_ONLY_FOLDER = "folder=admin_user_5@1";
    private static final String FAIL_PERMISSION_EXPRESSION_ONLY_FOLDER_READ = "folder=admin_user_5@1.2";
    private static final String FAIL_PERMISSION_EXPRESSION_ONLY_FOLDER_READ_WRITE = "folder=admin_user_5@1.2.3";

    // ATTENTION, must be equal to tested class: ConfiguredDefaultPermissions member: 'PROP_DEFAULT_PERMISSIONS'
    private static final String PROP_DEFAULT_PERMISSIONS = "com.openexchange.folderstorage.defaultPermissions";
    
    private ConfiguredDefaultPermissions testedClass;
    
    @Mock
    private ConfigViewFactory configViewFactory;
    @Mock
    private ServerServiceRegistry serverServiceRegistry;
    @Mock
    private ConfigView configView;
    @Mock
    private ComposedConfigProperty<String> composedConfigProperty;
    
    
    //ServerServiceRegistry.getInstance().getService
    
    @Before
    public void setUp() throws OXException {
        testedClass = ConfiguredDefaultPermissions.getInstance();
        PowerMockito.mockStatic(ServerServiceRegistry.class);
        PowerMockito.when(ServerServiceRegistry.getInstance()).thenReturn(serverServiceRegistry);
        PowerMockito.when(serverServiceRegistry.getService(ConfigViewFactory.class)).thenReturn(configViewFactory);
        PowerMockito.when(configViewFactory.getView(1,1)).thenReturn(configView);
        PowerMockito.when(configView.property(PROP_DEFAULT_PERMISSIONS, String.class)).thenReturn(composedConfigProperty);
        PowerMockito.when(composedConfigProperty.isDefined()).thenReturn(true);
    }

    @Test
    public void testGetConfiguredDefaultPermissionsFor_successMultiFolder() {
        try {
            PowerMockito.when(composedConfigProperty.get()).thenReturn(PARENTFOLDER_PERMISSIONS + "|" + CUSTOM_FOLDER_PERMISSIONS);
            Permission[] result = testedClass.getConfiguredDefaultPermissionsFor("15", 1, 1);
            assertTrue(result.length == 1);
            assertTrue(result[0].equals(getCustomPermissions()));
            result = testedClass.getConfiguredDefaultPermissionsFor("parent", 1, 1);
            assertTrue(result.length == 2);
            assertTrue(result[0].equals(getAdminPermissions()));
            assertTrue(result[1].equals(getGroupPermissions()));
        } catch (OXException e) {
            fail("Exception thrown");
        }
    }
    
    @Test
    public void testGetConfiguredDefaultPermissionsFor_failNoEqual() {
        testedClass.invalidateCache();
        try {
            PowerMockito.when(composedConfigProperty.get()).thenReturn(FAIL_PERMISSION_EXPRESSION_NO_EQUAL);
            testedClass.getConfiguredDefaultPermissionsFor("folder", 1, 1);
        } catch (OXException e) {
            assertTrue(e.getMessage().contains("Missing '=' character in expression:"));
            return;
        }
        fail("No Exception thrown");
    }
    
    @Test
    public void testGetConfiguredDefaultPermissionsFor_failNoPrefix() {
        testedClass.invalidateCache();
        try {
            PowerMockito.when(composedConfigProperty.get()).thenReturn(FAIL_PERMISSION_EXPRESSION_PREFIX);
            testedClass.getConfiguredDefaultPermissionsFor("folder", 1, 1);
        } catch (OXException e) {
            assertTrue(e.getMessage().contains("Permission expression is required to start with either \"admin_user_\", \"user_\", \"admin_group_\" or \"group_\" prefix:"));
            return;
        }
        fail("No Exception thrown");
    }
    
    @Test
    public void testGetConfiguredDefaultPermissionsFor_failNoAT() {
        testedClass.invalidateCache();
        try {
            PowerMockito.when(composedConfigProperty.get()).thenReturn(FAIL_PERMISSION_EXPRESSION_NO_AT);
            testedClass.getConfiguredDefaultPermissionsFor("folder", 1, 1);
        } catch (OXException e) {
            assertTrue(e.getMessage().contains("Missing '@' character in expression:"));
            return;
        }
        fail("No Exception thrown");
    }
    
    @Test
    public void testGetConfiguredDefaultPermissionsFor_failNoPermissions() {
        testedClass.invalidateCache();
        try {
            PowerMockito.when(composedConfigProperty.get()).thenReturn(FAIL_PERMISSION_EXPRESSION_NO_PERMISSIONS);
            testedClass.getConfiguredDefaultPermissionsFor("folder", 1, 1);
        } catch (OXException e) {
            assertTrue(e.getMessage().contains("Expected a '.' delimiter in rights expression:"));
            return;
        }
        fail("No Exception thrown");
    }
    
    @Test
    public void testGetConfiguredDefaultPermissionsFor_failOnlyFolder() {
        testedClass.invalidateCache();
        try {
            PowerMockito.when(composedConfigProperty.get()).thenReturn(FAIL_PERMISSION_EXPRESSION_ONLY_FOLDER);
            testedClass.getConfiguredDefaultPermissionsFor("folder", 1, 1);
        } catch (OXException e) {
            assertTrue(e.getMessage().contains("Expected a '.' delimiter in rights expression:"));
            return;
        }
        fail("No Exception thrown");
    }
    
    @Test
    public void testGetConfiguredDefaultPermissionsFor_failOnlyFolderRead() {
        testedClass.invalidateCache();
        try {
            PowerMockito.when(composedConfigProperty.get()).thenReturn(FAIL_PERMISSION_EXPRESSION_ONLY_FOLDER_READ);
            testedClass.getConfiguredDefaultPermissionsFor("folder", 1, 1);
        } catch (OXException e) {
            assertTrue(e.getMessage().contains("Expected a '.' delimiter in rights expression:"));
            return;
        }
        fail("No Exception thrown");
    }
    
    @Test
    public void testGetConfiguredDefaultPermissionsFor_failOnlyFolderReadWrite() {
        testedClass.invalidateCache();
        try {
            PowerMockito.when(composedConfigProperty.get()).thenReturn(FAIL_PERMISSION_EXPRESSION_ONLY_FOLDER_READ_WRITE);
            testedClass.getConfiguredDefaultPermissionsFor("folder", 1, 1);
        } catch (OXException e) {
            assertTrue(e.getMessage().contains("Expected a '.' delimiter in rights expression:"));
            return;
        }
        fail("No Exception thrown");
    }
    

    private Object getAdminPermissions() {
        ImmutablePermission.Builder permissionBuilder = ImmutablePermission.builder();
        permissionBuilder.setAdmin(false).setGroup(true).setDeletePermission(0).setEntity(2).setFolderPermission(2).setReadPermission(4).setSystem(0).setWritePermission(0);
        return permissionBuilder.build();
    }

    private Object getGroupPermissions() {
        ImmutablePermission.Builder permissionBuilder = ImmutablePermission.builder();
        permissionBuilder.setAdmin(true).setGroup(false).setDeletePermission(4).setEntity(5).setFolderPermission(8).setReadPermission(4).setSystem(0).setWritePermission(4);
        return permissionBuilder.build();
    }
    
    private Object getCustomPermissions() {
        ImmutablePermission.Builder permissionBuilder = ImmutablePermission.builder();
        permissionBuilder.setAdmin(true).setGroup(true).setDeletePermission(4).setEntity(2).setFolderPermission(8).setReadPermission(4).setSystem(0).setWritePermission(4);
        return permissionBuilder.build();
    }
}
