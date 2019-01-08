package com.atlassian.openid.connect.servlet;

import com.atlassian.openid.connect.auth.AuthenticationHandler;
import com.atlassian.openid.connect.auth.AuthenticationProvider;
import com.atlassian.openid.connect.config.AuthenticationInfoException;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.auth0.client.auth.AuthorizeUrlBuilder;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OauthLoginServletTest {

    @Mock
    private AuthenticationProvider mockAuthenticationProvider;

    @Mock
    private TemplateRenderer mockTemplateRenderer;

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
    private RequestDispatcher mockRequestDispatcher;

    private OauthLoginServlet oauthLoginServlet;

    @Before
    public void setUp() throws Exception {
        when(mockResponse.getWriter()).thenReturn(mockPrintWriter);
        when(mockRequest.getSession()).thenReturn(mockSession);

        when(mockRequest.getScheme()).thenReturn("http");
        when(mockRequest.getServerName()).thenReturn("localhost");
        when(mockRequest.getServerPort()).thenReturn(2990);

        oauthLoginServlet = new OauthLoginServlet(mockAuthenticationProvider);
        FieldUtils.writeField(oauthLoginServlet, "callbackPath", AuthInfo.CALLBACK_PATH, true);
    }

    @Test
    @Ignore
    public void shouldAuthenticateUserSuccessfully() throws Exception {
        // Given
        initMockAuthenticationHandler();

        // When
        oauthLoginServlet.doPost(mockRequest, mockResponse);

        // Then
        verify(mockResponse).sendRedirect(AuthInfo.expectedRedirectUriToAuthServer());
    }

    private void initMockAuthenticationHandler() throws Exception {
        HttpUrl baseUrl = HttpUrl.parse("https://user.domain.com");
        String clientId = "client_id";

        Method m = AuthorizeUrlBuilder.class.getDeclaredMethod("newInstance", HttpUrl.class, String.class, String.class);
        m.setAccessible(true);
        AuthorizeUrlBuilder authorizeUrlBuilder = (AuthorizeUrlBuilder) m.invoke(null, baseUrl, clientId, AuthInfo.REDIRECT_URI);

        when(mockAuthenticationProvider.getInstance()).thenReturn(authenticationHandler);
        when(authenticationHandler.authorizeUrl(AuthInfo.REDIRECT_URI)).thenReturn(authorizeUrlBuilder);
        when(authenticationHandler.getDomain()).thenReturn(AuthInfo.DOMAIN_URI);
    }

    @Test(expected = AuthenticationInfoException.class)
    public void shouldThrowExceptionWhenConfigIsNotSet() throws Exception {
        // When
        when(mockAuthenticationProvider.getInstance()).thenThrow(new AuthenticationInfoException("Auth config is not set up"));

        // Then
        oauthLoginServlet.doPost(mockRequest, mockResponse);
    }

    private static class AuthInfo {

        private static final String DOMAIN = "user.domain.com";
        private static final String DOMAIN_URI = "https://" + DOMAIN;
        private static final String CALLBACK_PATH = "/jira/plugins/servlet/callback";
        private static final String REDIRECT_URI = "http://localhost:2990" + CALLBACK_PATH;
        private static final String CLIENT_ID = "client_id";
        private static final String AUDIENCE_URI = DOMAIN_URI + "/userinfo";
        private static final String AUTHORIZE_URI = DOMAIN_URI + "/authorize";
        private static final String SCOPE = "openid%20profile%20email";
        private static final String RESPONSE_TYPE = "code";

        private static String expectedRedirectUriToAuthServer() {
            return String.format("%s?redirect_uri=%s&client_id=%s&audience=%s&scope=%s&response_type=%s",
                    AUTHORIZE_URI, REDIRECT_URI, CLIENT_ID, AUDIENCE_URI, SCOPE, RESPONSE_TYPE);
        }

    }

}
