package it.com.atlassian.openid.connect;

import com.atlassian.jira.functest.rule.SkipCacheCheck;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import it.com.atlassian.openid.connect.config.AdminConfigPage;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import static org.junit.Assert.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SkipCacheCheck
public class AuthenticationFlowTest extends BaseJiraWebTest {

    private static ClientAndServer mockServer;

    private HttpClient httpClient;
    private String baseUrl;
    private String servletUrl;

    @BeforeClass
    public static void startServer() {
        mockServer = startClientAndServer(1080);
    }

    @AfterClass
    public static void stopServer() {
        mockServer.stop();
    }

    @Before
    public void setUp() throws Exception {
        initialSetUp();

        httpClient = HttpClientBuilder.create()
                .build();
        baseUrl = "http://localhost:8000";
        servletUrl = baseUrl + "/jira/plugins/servlet/oauth-login";
    }

    private void initialSetUp() {
        jira.getTester().getDriver().getDriver().manage().deleteAllCookies();
        // todo: implement loading configurations from backup
        // todo: implement loading configurations from backup
//        backdoor.dataImport().restoreDataFromResource("backup.zip",
//                ServiceDeskLicenses.LICENSE_SERVICE_DESK_JIRA7.getLicenseString());
//        backdoor.darkFeatures().enableForSite("jira.onboarding.feature.disabled");
    }

    // todo: need to set up configurations for authorization client
    @Test
    public void test() throws Exception {
        jira.quickLoginAsAdmin();
        final AdminConfigPage configPage = jira.goTo(AdminConfigPage.class);

        configPage.save();
        jira.logout();

        new MockServerClient("localhost", 1080)
                .when(
                        request()
                                .withPath("/authorize")
                )
                .respond(
                        response()
                                .withBody("some_response_body")
                );
        HttpGet httpget = new HttpGet(servletUrl);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = httpClient.execute(httpget, responseHandler);
        assertEquals("some_response_body", responseBody);
    }

}
