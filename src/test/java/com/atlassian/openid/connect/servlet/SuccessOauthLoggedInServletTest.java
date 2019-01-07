package com.atlassian.openid.connect.servlet;

import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.UserDetails;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.openid.connect.util.SessionConstants;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.auth0.json.auth.UserInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SuccessOauthLoggedInServletTest {

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
    private UserInfo mockUserInfo;

    private HashMap<String, Object> userInfoValues;

    @Mock
    private UserManager mockUserManager;

    @Mock
    private UserSearchService mockUserSearchService;

    @Mock
    private JiraAuthenticationContext mockJiraAuthContext;

    @Mock
    private UserUtil userUtil;

    @Mock
    private GroupManager groupManager;

    @Mock
    private ApplicationUser mockAppUser;

    private UserDetails userDetails;

    private SuccessOauthLoggedInServlet successOauthLoggedInServlet;

    @Before
    public void setUp() throws Exception {
        when(mockResponse.getWriter()).thenReturn(mockPrintWriter);
        when(mockRequest.getSession()).thenReturn(mockSession);
        successOauthLoggedInServlet = new SuccessOauthLoggedInServlet(mockTemplateRenderer, mockUserManager,
                mockUserSearchService, mockJiraAuthContext, userUtil, groupManager);
        userInfoValues = new HashMap<String, Object>() {{
            put("sub", "nickname");
            put("email", "sample@mail.com");
            put("nickname", "sample");
            put("name", "name");
        }};
        when(mockUserInfo.getValues()).thenReturn(userInfoValues);

        when(mockSession.getAttribute(SessionConstants.USER_INFO)).thenReturn(userInfoValues);

        mockAppUser = new MockApplicationUser("nickname", "name", "sample@mail.com");
    }

    @Test
    public void shouldCreateAndAuthenticateUserWhenUserNotExist() throws Exception {
        // Given
        when(mockUserSearchService.findUsersByEmail("sample@mail.com")).thenReturn(Collections.EMPTY_LIST);

        userDetails = new UserDetails((String) userInfoValues.get("nickname"), (String) userInfoValues.get("name"))
                .withEmail((String) userInfoValues.get("email"))
                .withPassword("1");
        when(mockUserManager.createUser(userDetails)).thenReturn(mockAppUser);

        // When
        successOauthLoggedInServlet.doGet(mockRequest, mockResponse);

        // Then
        verify(mockUserManager).createUser(userDetails);
        verify(mockJiraAuthContext).setLoggedInUser(mockAppUser);
        verify(mockSession).setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, mockAppUser);
        verify(mockSession).setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
    }

    @Test
    public void shouldAuthenticateUserWhenUserExistInSystem() throws Exception {
        // Given
        when(mockUserSearchService.findUsersByEmail("sample@mail.com")).thenReturn(Collections.singletonList(mockAppUser));

        // When
        successOauthLoggedInServlet.doGet(mockRequest, mockResponse);

        // Then
        verify(mockUserManager, never()).createUser(userDetails);

        verify(mockJiraAuthContext).setLoggedInUser(mockAppUser);
        verify(mockSession).setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, mockAppUser);
        verify(mockSession).setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
    }

    @Test
    public void shouldNotCreateNewUserWhenCreateExceptionWasThrown() throws Exception {
        // Given
        when(mockUserSearchService.findUsersByEmail("sample@mail.com")).thenReturn(Collections.EMPTY_LIST);

        userDetails = new UserDetails((String) userInfoValues.get("nickname"), (String) userInfoValues.get("name"))
                .withEmail((String) userInfoValues.get("email"))
                .withPassword("1");
        when(mockUserManager.createUser(userDetails)).thenThrow(new CreateException());

        // When
        successOauthLoggedInServlet.doGet(mockRequest, mockResponse);

        // Then
        verify(mockUserManager).createUser(userDetails);

        verify(mockJiraAuthContext, never()).setLoggedInUser(mockAppUser);
        verify(mockSession, never()).setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, mockAppUser);
        verify(mockSession, never()).setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
    }

}