package it.com.atlassian.openid.connect;

import com.atlassian.jira.functest.rule.SkipCacheCheck;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.testkit.client.UsersAndGroupsControl;
import it.com.atlassian.openid.connect.config.AdminConfigPage;
import it.com.atlassian.openid.connect.util.TestConstants;
import junit.framework.AssertionFailedError;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.verify.VerificationTimes;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SkipCacheCheck
public class AuthenticationFlowTest extends BaseJiraWebTest {

    private static final String AUTH_CALLBACK_PATH = "/jira/plugins/servlet/callback";
    private static final String OPENID_LOGIN_PATH = "/jira/plugins/servlet/oauth-login";

    private static ClientAndServer mockServer;

    private HttpClient httpClient;
    private String servletLoginUrl;

    @BeforeClass
    public static void startServer() {
        mockServer = startClientAndServer(TestConstants.AUTH_PORT);
    }

    @AfterClass
    public static void stopServer() {
        mockServer.stop();
    }

    private void initConfigPage() {
        jira.quickLoginAsAdmin();
        final AdminConfigPage configPage = jira.goTo(AdminConfigPage.class);

        configPage.setDomain(TestConstants.AUTH_DOMAIN);
        configPage.setClientId(TestConstants.AUTH_CLIENT_ID);
        configPage.setClientSecret(TestConstants.AUTH_CLIENT_SECRET);
        configPage.save();
        jira.logout();
    }

    @Before
    public void setUp() throws Exception {
        initialSetUp();

        httpClient = HttpClientBuilder.create()
                .build();

        servletLoginUrl = TestConstants.JIRA_DOMAIN + OPENID_LOGIN_PATH;
    }

    private void initialSetUp() {
        jira.getTester().getDriver().getDriver().manage().deleteAllCookies();
        // todo: implement loading configurations from backup
//        backdoor.dataImport().restoreDataFromResource("backup.zip",
//                ServiceDeskLicenses.LICENSE_SERVICE_DESK_JIRA7.getLicenseString());
//        backdoor.darkFeatures().enableForSite("jira.onboarding.feature.disabled");
    }

    // todo: need to set up configurations for authorization client
    @Test
    public void test() throws Exception {
//        initConfigPage();

        MockServerClient mockServerClient = new MockServerClient(TestConstants.AUTH_HOST, TestConstants.AUTH_PORT);
        initAuthorizeEndpoint(mockServerClient);

        HttpGet httpget = new HttpGet(servletLoginUrl);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = httpClient.execute(httpget, responseHandler);

        mockServerClient
                .verify(
                        request()
                                .withPath("/authorize")
                                .withMethod("GET")
                                .withQueryStringParameter("audience", TestConstants.AUTH_DOMAIN + "/userinfo")
                                .withQueryStringParameter("scope", "openid profile email")
                                .withQueryStringParameter("response_type", "code")
                                .withQueryStringParameter("redirect_uri", TestConstants.JIRA_DOMAIN + AUTH_CALLBACK_PATH)
                                .withQueryStringParameter("client_id", TestConstants.AUTH_CLIENT_ID),
                        VerificationTimes.exactly(1)
                );
        System.out.println(responseBody);

        URIBuilder uriBuilder = new URIBuilder(TestConstants.JIRA_DOMAIN + AUTH_CALLBACK_PATH);
        uriBuilder.setParameter("code", "1234");

        initOauthTokenEndpoint(mockServerClient);
        initUserInfoEndPoint(mockServerClient);

        HttpGet httpgetCallback = new HttpGet(uriBuilder.build());
        httpClient.execute(httpgetCallback, responseHandler);

        mockServerClient
                .verify(
                        request()
                                .withPath("/oauth/token")
                                .withMethod("POST")
                                .withHeader(new Header("Content-Type", "application/json"))
                                .withBody("{\"code\":\"1234\",\"grant_type\":\"authorization_code\",\"client_secret\":\"123\",\"redirect_uri\":\"http://localhost:8000/jira/plugins/servlet/callback\",\"client_id\":\"1\"}"),
                        VerificationTimes.exactly(1)
                );
        mockServerClient
                .verify(
                        request()
                                .withPath("/userinfo")
                                .withMethod("GET")
                                .withHeader(new Header("Authorization", "Bearer " + TestConstants.ACCESS_TOKEN)),
                        VerificationTimes.exactly(2)
                );

        UsersAndGroupsControl usersAndGroupsControl = backdoor.usersAndGroups();

        usersAndGroupsControl.getAllUsers().stream()
                .filter(user -> TestUser.NICKNAME.equals(user.getName()))
                .findAny()
                .orElseThrow(() -> new AssertionFailedError("User does not exist in the system!"));

    }


    private void initAuthorizeEndpoint(MockServerClient mockServerClient) {
        mockServerClient
                .when(
                        request()
                                .withPath("/authorize")
                )
                .respond(
                        response()
                                .withBody("some_response_body")
                );
    }


    private void initUserInfoEndPoint(MockServerClient mockServerClient) {
        mockServerClient
                .when(
                        request()
                                .withPath("/userinfo")
                                .withMethod("GET")
                )
                .respond(
                        response()
                                .withHeader(new Header("Content-Type", "application/json"))
                                .withBody("{\n" +
                                        "  \"sub\": \"" + TestUser.SUB + "\",\n" +
                                        "  \"name\": \"" + TestUser.NAME + "\",\n" +
                                        "  \"nickname\": \"" + TestUser.NICKNAME + "\",\n" +
                                        "  \"email\": \"" + TestUser.EMAIL + "\"\n" +
                                        "}\n")
                );
    }

    private void initOauthTokenEndpoint(MockServerClient mockServerClient) {
        mockServerClient
                .when(
                        request()
                                .withPath("/oauth/token")
                                .withMethod("POST")
                )
                .respond(
                        response()
                                .withHeader(new Header("Content-Type", "application/json"))
                                .withBody("{\n" +
                                        "  \"access_token\": \"" + TestConstants.ACCESS_TOKEN + "\",\n" +
                                        "  \"id_token\": \"" + TestConstants.ID_TOKEN + "\",\n" +
                                        "  \"refresh_token\": \"3\",\n" +
                                        "  \"token_type\": \"code\",\n" +
                                        "  \"expires_in\": 3600\n" +
                                        "}")
                );
    }

    private static final class TestUser {
        public static final String SUB = "some|sub";
        public static final String NAME = "Name";
        public static final String NICKNAME = "name";
        public static final String EMAIL = "sample@mail.com";
    }

}
