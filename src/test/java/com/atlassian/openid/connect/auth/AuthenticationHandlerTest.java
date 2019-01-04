package com.atlassian.openid.connect.auth;

import com.atlassian.openid.connect.config.AuthenticationInfo;
import com.atlassian.openid.connect.model.Tokens;
import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.auth.UserInfo;
import com.auth0.net.AuthRequest;
import com.auth0.net.Request;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationHandlerTest {

    @Mock
    private AuthAPI authAPI;

    @Mock
    private AuthenticationInfo authInfo;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Request mockRequestUserInfo;

    private AuthenticationHandler authenticationHandler;

    @Before
    public void setUp() throws Exception {
        when(request.getParameter("code")).thenReturn("l9YhZkuYALSbSsno");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:2990/jira/plugins/servlet/callback"));

        TokenHolder mockTokenHolder = getTokenHolder();

        AuthRequest mockExchangeCodeRequest = mock(AuthRequest.class);
        when(authAPI.exchangeCode(anyString(), anyString())).thenReturn(mockExchangeCodeRequest);
        when(mockExchangeCodeRequest.execute()).thenReturn(mockTokenHolder);

        when(authAPI.userInfo(anyString())).thenReturn(mockRequestUserInfo);

        authenticationHandler = new AuthenticationHandler(authAPI, authInfo);
    }

    @Test
    public void shouldAuthorizeUserCorrectly() throws Exception {
        // Given
        UserInfo mockUserInfo = mock(UserInfo.class);
        when(mockUserInfo.getValues()).thenReturn(new HashMap<String, Object>() {{
            put("sub", "nickname");
        }});
        when(mockRequestUserInfo.execute()).thenReturn(mockUserInfo);

        Tokens expectedTokens = new Tokens("1", "1", "1", "1", 1L);

        // When
        Tokens tokens = authenticationHandler.handle(request);

        // Then
        assertEquals(expectedTokens, tokens);
    }

    @Test(expected = AuthenticationException.class)
    public void shouldThrowAuthenticationExceptionWhenErrorOccuredWhileExchangingAuthCode() throws Exception {
        // Given
        AuthRequest mockExchangeCodeRequest = mock(AuthRequest.class);
        when(authAPI.exchangeCode(anyString(), anyString())).thenReturn(mockExchangeCodeRequest);
        when(mockExchangeCodeRequest.execute()).thenThrow(new Auth0Exception("Error"));

        // When
        authenticationHandler.handle(request);
    }


    @Test(expected = AuthenticationException.class)
    public void shouldThrowAuthenticationExceptionWhenErrorOccurredWhileRetrievingUserInfoAndSubParameterNotSet() throws Exception {
        // Given
        UserInfo mockUserInfo = mock(UserInfo.class);
        when(mockUserInfo.getValues()).thenReturn(new HashMap<>());
        when(mockRequestUserInfo.execute()).thenReturn(mockUserInfo);

        // When
        authenticationHandler.handle(request);
    }

    private TokenHolder getTokenHolder() {
        TokenHolder mockTokenHolder = mock(TokenHolder.class);
        when(mockTokenHolder.getTokenType()).thenReturn("1");
        when(mockTokenHolder.getIdToken()).thenReturn("1");
        when(mockTokenHolder.getAccessToken()).thenReturn("1");
        when(mockTokenHolder.getRefreshToken()).thenReturn("1");
        when(mockTokenHolder.getExpiresIn()).thenReturn(1L);
        return mockTokenHolder;
    }

}