package it.com.atlassian.openid.connect.config;

import com.atlassian.jira.pageobjects.components.DropDown;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class AdminConfigPage implements Page  {

    public static final String PLUGIN_AUTHENTICATION_CONFIG_URI = "/plugins/servlet/oauth/configuration";

    @Inject
    private PageElementFinder pageElementFinder;

    @Inject
    protected PageBinder pageBinder;

    @ElementBy(id = "domainElement")
    private PageElement domainElement;

    @ElementBy(id = "client-id")
    private PageElement clientIdElement;

    @ElementBy(id = "client-secret")
    private PageElement clientSecretElement;

    @ElementBy(id = "save-button")
    private PageElement saveButton;

    private DropDown profileDropdown;

    @Init
    public void initialise() throws Exception {
        profileDropdown = pageBinder.bind(DropDown.class, By.id("header-details-user-fullname"), By.id("user-options-content"));
        TimeUnit.SECONDS.sleep(3);
    }

    public void triggerLogout() {
        profileDropdown.openAndClick(By.id("log_out"));
    }

    @Override
    public String getUrl() {
        return PLUGIN_AUTHENTICATION_CONFIG_URI;
    }

    public void save() {
        saveButton.click();
//        Poller.waitUntilTrue("Configuration plugin error", new AbstractTimedCondition(new DefaultTimeouts().timeoutFor(TimeoutType.PAGE_LOAD), Timeouts.DEFAULT_INTERVAL) {
//            @Override
//            protected Boolean currentValue() {
//                return configResponse.isVisible();
//            }
//        });
    }

//    @WaitUntil
//    public void doWait() {
//        Poller.waitUntilTrue("Can not load Authentication config page", clientSecretElement.timed().isPresent());
//    }

    public void setDomain(String domain) {
        this.domainElement.clear().type(domain);
    }

    public void setClientId(String clientId) {
        this.clientIdElement.clear().type(clientId);
    }

    public void setClientSecretElement(String clientSecretElement) {
        this.clientSecretElement.clear().type(clientSecretElement);
    }

}
