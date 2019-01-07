package com.atlassian.openid.connect.servlet;

import com.atlassian.openid.connect.auth.AuthenticationException;
import com.atlassian.openid.connect.auth.AuthenticationHandler;
import com.atlassian.openid.connect.auth.AuthenticationProvider;
import com.atlassian.openid.connect.model.Tokens;
import com.atlassian.openid.connect.util.SessionConstants;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.UserInfo;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CallbackAuthorizeServletTest {
    @Mock
    private AuthenticationProvider mockAuthenticationProvider;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private PrintWriter mockPrintWriter;

    @Mock
    private HttpSession mockSession;

    @Mock
    private AuthenticationHandler authenticationHandler;

    @Mock
    private UserInfo mockUserInfo = mock(UserInfo.class);

    private CallbackAuthorizeServlet callbackAuthorizeServlet;

    private HashMap<String, Object> userInfoValues;

    @Before
    public void setUp() throws Exception {
        when(mockResponse.getWriter()).thenReturn(mockPrintWriter);
        when(mockRequest.getSession()).thenReturn(mockSession);

        when(mockRequest.getScheme()).thenReturn("http");
        when(mockRequest.getServerName()).thenReturn("localhost");
        when(mockRequest.getServerPort()).thenReturn(2990);

        when(mockAuthenticationProvider.getInstance()).thenReturn(authenticationHandler);
        initCallbackServlet();
    }

    private void initCallbackServlet() throws IllegalAccessException, Auth0Exception {
        callbackAuthorizeServlet = new CallbackAuthorizeServlet(mockAuthenticationProvider);
        FieldUtils.writeField(callbackAuthorizeServlet, "redirectOnSuccess", CallbackAuthorizeServletInfo.REDIRECT_ON_SUCCESS, true);
        FieldUtils.writeField(callbackAuthorizeServlet, "redirectOnFail", CallbackAuthorizeServletInfo.REDIRECT_ON_FAIL, true);
    }

    @Test
    public void shouldRedirectOnSuccessPageWhenAuthenticationSuccessful() throws Exception {
        // Given
        initAuthHandlerWhenAuthenticationSuccesful();

        // When
        callbackAuthorizeServlet.doGet(mockRequest, mockResponse);

        // Then
        verify(mockSession).setAttribute(SessionConstants.ACCESS_TOKEN, CallbackAuthorizeServletInfo.EXPECTED_TOKEN.getAccessToken());
        verify(mockSession).setAttribute(SessionConstants.ID_TOKEN, CallbackAuthorizeServletInfo.EXPECTED_TOKEN.getIdToken());
        verify(mockSession).setAttribute(SessionConstants.USER_INFO, mockUserInfo.getValues());
        verify(mockResponse).sendRedirect(CallbackAuthorizeServletInfo.REDIRECT_ON_SUCCESS);
    }

    private void initAuthHandlerWhenAuthenticationSuccesful() throws Auth0Exception {
        when(authenticationHandler.handle(mockRequest)).thenReturn(CallbackAuthorizeServletInfo.EXPECTED_TOKEN);
        userInfoValues = new HashMap<String, Object>() {{
            put("sub", "nickname");
            put("email", "sample@mail.com");
            put("nickname", "sample");
            put("name", "name");
        }};
        when(mockUserInfo.getValues()).thenReturn(userInfoValues);
        when(authenticationHandler.getUserInfo(CallbackAuthorizeServletInfo.EXPECTED_TOKEN.getAccessToken())).thenReturn(mockUserInfo);
    }

    @Test
    @Ignore
    public void shouldRedirectOnLoginPageWhenAuthenticationExceptionWasThrown() throws Exception {
        // Given
        when(authenticationHandler.handle(mockRequest)).thenThrow(new AuthenticationException());

        // When
        callbackAuthorizeServlet.doGet(mockRequest, mockResponse);

        // Then
        verify(mockResponse).sendRedirect(CallbackAuthorizeServletInfo.REDIRECT_ON_FAIL);
    }

    @Test
    public void shouldRedirectOnLoginPageWhenAuthenticationNotSuccessful() throws Exception {
        // Given
        when(authenticationHandler.handle(mockRequest)).thenReturn(CallbackAuthorizeServletInfo.NOT_EXPECTED_TOKEN);
        userInfoValues = new HashMap<>();
        when(mockUserInfo.getValues()).thenReturn(userInfoValues);
        when(authenticationHandler.getUserInfo(CallbackAuthorizeServletInfo.NOT_EXPECTED_TOKEN.getAccessToken())).thenThrow(new Auth0Exception("Token is not valid!"));

        // When
        callbackAuthorizeServlet.doGet(mockRequest, mockResponse);

        // Then
        verify(mockResponse).sendRedirect(CallbackAuthorizeServletInfo.REDIRECT_ON_FAIL);
    }

    private static class CallbackAuthorizeServletInfo {
        private final static String REDIRECT_ON_SUCCESS = "/jira/plugins/servlet/success";
        private final static String REDIRECT_ON_FAIL = "/jira/plugins/servlet/oauth-login";

        private static final String ACCESS_TOKEN = "123";
        private static final String ID_TOKEN = "12345";
        private static final String REFRESH_TOKEN = "12";
        private static final String TYPE = "type";
        private static final long EXPIRES_IN = 200L;

        private final static Tokens EXPECTED_TOKEN = new Tokens(ACCESS_TOKEN, ID_TOKEN, REFRESH_TOKEN, TYPE, EXPIRES_IN);

        private final static Tokens NOT_EXPECTED_TOKEN = new Tokens("42", "42", "42", "42", 42L);

    }

}
