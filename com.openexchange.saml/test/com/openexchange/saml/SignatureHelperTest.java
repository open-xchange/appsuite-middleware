package com.openexchange.saml;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.security.SigningUtil;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.SignableXMLObject;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import com.openexchange.saml.tools.SignatureHelper;
import com.openexchange.saml.validation.ValidationError;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SignatureHelper.class, SigningUtil.class})
public class SignatureHelperTest {
    
    private List<Credential> credentials;
    
    @Mock
    SignableXMLObject object;
    
    @Mock
    Credential credOne;
    
    @Mock
    Credential credTwo;
    
    @Mock
    Signature signature;
    
    @Mock
    SAMLSignatureProfileValidator samlSignatureProfileValidator;
    
    @Mock
    SignatureValidator signatureValidator;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(SigningUtil.class);
        this.credentials = new ArrayList<>();
        this.credentials.add(credOne);
        this.credentials.add(credTwo);
        Mockito.when(object.getSignature()).thenReturn(signature);
        PowerMockito.whenNew(SAMLSignatureProfileValidator.class).withNoArguments().thenReturn(samlSignatureProfileValidator);
        PowerMockito.doNothing().when(samlSignatureProfileValidator).validate(signature);

    }
    
    @Test
    public void testValidateSignature_TwoSigfail() throws Exception {
        PowerMockito.whenNew(SignatureValidator.class).withAnyArguments().thenReturn(signatureValidator);
        Mockito.doThrow(org.opensaml.xml.validation.ValidationException.class).when(signatureValidator).validate(signature);
        ValidationError error = SignatureHelper.validateSignature(object, credentials);
        assertTrue("Expected validation failure, but did not fail", error != null && error.getThrowable() instanceof org.opensaml.xml.validation.ValidationException);
    }
    
    @Test
    public void testValidateSignature_TwoSigPass() throws Exception {
        PowerMockito.whenNew(SignatureValidator.class).withAnyArguments().thenReturn(signatureValidator);
        ValidationError error = SignatureHelper.validateSignature(object, credentials);
        assertTrue("Expected no validation failure, but did fail", error == null);

    }

    @Test
    public void testValidateSignature_OneSigPass() throws Exception {
        PowerMockito.whenNew(SignatureValidator.class).withArguments(credOne).thenReturn(signatureValidator);
        Mockito.doThrow(org.opensaml.xml.validation.ValidationException.class).when(signatureValidator).validate(signature);
        
        SignatureValidator signatureValidatorPass = Mockito.mock(SignatureValidator.class);
        PowerMockito.whenNew(SignatureValidator.class).withArguments(credTwo).thenReturn(signatureValidatorPass);
        
        ValidationError error = SignatureHelper.validateSignature(object, credentials);
        assertTrue("Expected no validation failure, but did fail", error == null);

    }
    
    @Test
    public void validateURISignature_TwoSigfail() throws Exception {
        PowerMockito.doReturn(false).when(SigningUtil.class, "verifyWithURI", ArgumentMatchers.any(Credential.class), ArgumentMatchers.anyString(), ArgumentMatchers.any(byte[].class), ArgumentMatchers.any(byte[].class));
        ValidationError error = Whitebox.invokeMethod(SignatureHelper.class, "getValidationError", credentials, "", "", new StringBuilder());
        assertTrue("Expected validation failure, but did not fail", error != null && error.getThrowable() instanceof org.opensaml.xml.validation.ValidationException);
    }
    
    @Test
    public void validateURISignature_TwoSigPass() throws Exception {
        PowerMockito.doReturn(true).when(SigningUtil.class, "verifyWithURI", ArgumentMatchers.any(Credential.class), ArgumentMatchers.anyString(), ArgumentMatchers.any(byte[].class), ArgumentMatchers.any(byte[].class));
        ValidationError error = Whitebox.invokeMethod(SignatureHelper.class, "getValidationError", credentials, "", "", new StringBuilder());
        assertTrue("Expected no validation failure, but did fail", error == null);
    }

    @Test
    public void validateURISignature_OneSigPass() throws Exception {
        byte[] empty = {};
        PowerMockito.doReturn(false).when(SigningUtil.class, "verifyWithURI", credOne, "", empty, empty);
        PowerMockito.doReturn(true).when(SigningUtil.class, "verifyWithURI", credTwo, "", empty, empty);
        ValidationError error = Whitebox.invokeMethod(SignatureHelper.class, "getValidationError", credentials, "", "", new StringBuilder());
        assertTrue("Expected no validation failure, but did fail", error == null);
    }
}
