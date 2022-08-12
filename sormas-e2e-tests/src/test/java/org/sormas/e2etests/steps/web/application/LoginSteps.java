/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2022 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.sormas.e2etests.steps.web.application;

import static org.sormas.e2etests.pages.application.LoginPage.FAILED_LOGIN_ERROR_MESSAGE;
import static org.sormas.e2etests.pages.application.LoginPage.LOGIN_BUTTON;
import static org.sormas.e2etests.pages.application.NavBarPage.ACTION_CONFIRM_GDPR_POPUP;
import static org.sormas.e2etests.pages.application.NavBarPage.ACTION_CONFIRM_GDPR_POPUP_DE;
import static org.sormas.e2etests.pages.application.NavBarPage.DISCARD_USER_SETTINGS_BUTTON;
import static org.sormas.e2etests.pages.application.NavBarPage.GDPR_CHECKBOX;
import static org.sormas.e2etests.pages.application.NavBarPage.USER_SETTINGS_LANGUAGE_COMBOBOX_TEXT;
import static org.sormas.e2etests.pages.application.dashboard.Surveillance.SurveillanceDashboardPage.LOGOUT_BUTTON;
import static org.sormas.e2etests.steps.BaseSteps.locale;

import com.google.inject.Inject;
import cucumber.api.java8.En;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.sormas.e2etests.enums.UserRoles;
import org.sormas.e2etests.envconfig.dto.EnvUser;
import org.sormas.e2etests.envconfig.manager.RunningConfiguration;
import org.sormas.e2etests.helpers.AssertHelpers;
import org.sormas.e2etests.helpers.WebDriverHelpers;
import org.sormas.e2etests.pages.application.LoginPage;
import org.sormas.e2etests.pages.application.NavBarPage;
import org.sormas.e2etests.pages.application.dashboard.Surveillance.SurveillanceDashboardPage;
import org.sormas.e2etests.steps.web.application.users.EditUserSteps;

@Slf4j
public class LoginSteps implements En {

  @Inject
  public LoginSteps(
      WebDriverHelpers webDriverHelpers,
      RunningConfiguration runningConfiguration,
      AssertHelpers assertHelpers) {

    Given(
        "^I am logged in with name ([^\"]*)$",
        (String name) -> {
          webDriverHelpers.waitUntilIdentifiedElementIsVisibleAndClickable(
              SurveillanceDashboardPage.LOGOUT_BUTTON, 60);
        });

    Given(
        "^I navigate to SORMAS login page$",
        () -> {
          webDriverHelpers.accessWebSite(runningConfiguration.getEnvironmentUrlForMarket(locale));
        });

    Given(
        "^I navigate to ([^\"]*) via URL append$",
        (String path) -> {
          webDriverHelpers.accessWebSite(
              runningConfiguration.getEnvironmentUrlForMarket(locale) + path);
          TimeUnit.SECONDS.sleep(2);
        });

    Given(
        "I click on the Log In button",
        () -> webDriverHelpers.clickOnWebElementBySelector(LoginPage.LOGIN_BUTTON));

    And(
        "I log in with National User",
        () -> {
          EnvUser user =
              runningConfiguration.getUserByRole(locale, UserRoles.NationalUser.getRole());
          webDriverHelpers.accessWebSite(runningConfiguration.getEnvironmentUrlForMarket(locale));
          webDriverHelpers.waitForPageLoaded();
          webDriverHelpers.waitUntilIdentifiedElementIsVisibleAndClickable(
              LoginPage.USER_NAME_INPUT, 100);
          log.info("Filling username");
          webDriverHelpers.fillInWebElement(LoginPage.USER_NAME_INPUT, user.getUsername());
          log.info("Filling password");
          webDriverHelpers.fillInWebElement(LoginPage.USER_PASSWORD_INPUT, user.getPassword());
          log.info("Click on Login button");
          webDriverHelpers.clickOnWebElementBySelector(LoginPage.LOGIN_BUTTON);
          webDriverHelpers.waitForPageLoaded();
          webDriverHelpers.waitUntilIdentifiedElementIsVisibleAndClickable(
              SurveillanceDashboardPage.LOGOUT_BUTTON, 100);
        });

    And(
        "I try to log in with {string} and password {string}",
        (String userName, String password) -> {
          webDriverHelpers.accessWebSite(runningConfiguration.getEnvironmentUrlForMarket(locale));
          webDriverHelpers.waitForPageLoaded();
          webDriverHelpers.waitUntilIdentifiedElementIsVisibleAndClickable(
              LoginPage.USER_NAME_INPUT, 100);
          log.info("Filling username");
          webDriverHelpers.fillInWebElement(LoginPage.USER_NAME_INPUT, userName);
          log.info("Filling password");
          webDriverHelpers.fillInWebElement(LoginPage.USER_PASSWORD_INPUT, password);
          log.info("Click on Login button");
          webDriverHelpers.clickOnWebElementBySelector(LoginPage.LOGIN_BUTTON);
          webDriverHelpers.waitForPageLoaded();
        });

    Given(
        "^I log in as a ([^\"]*)$",
        (String userRole) -> {
          webDriverHelpers.accessWebSite(runningConfiguration.getEnvironmentUrlForMarket(locale));
          webDriverHelpers.waitUntilIdentifiedElementIsPresent(LoginPage.USER_NAME_INPUT);
          EnvUser user = runningConfiguration.getUserByRole(locale, userRole);
          log.info("Filling username");
          webDriverHelpers.fillInWebElement(LoginPage.USER_NAME_INPUT, user.getUsername());
          log.info("Filling password");
          webDriverHelpers.fillInWebElement(LoginPage.USER_PASSWORD_INPUT, user.getPassword());
          log.info("Clicking on login button");
          webDriverHelpers.clickOnWebElementBySelector(LoginPage.LOGIN_BUTTON);
          webDriverHelpers.waitForPageLoaded();
          if (webDriverHelpers.isElementVisibleWithTimeout(GDPR_CHECKBOX, 10)) {
            webDriverHelpers.clickOnWebElementBySelector(GDPR_CHECKBOX);
            if (webDriverHelpers.isElementVisibleWithTimeout(ACTION_CONFIRM_GDPR_POPUP, 5)) {
              webDriverHelpers.clickOnWebElementBySelector(ACTION_CONFIRM_GDPR_POPUP);
            } else {
              webDriverHelpers.clickOnWebElementBySelector(ACTION_CONFIRM_GDPR_POPUP_DE);
            }
          }
          webDriverHelpers.waitUntilIdentifiedElementIsVisibleAndClickable(LOGOUT_BUTTON, 50);
        });

    Then(
        "I login with last edited user",
        () -> {
          webDriverHelpers.waitUntilIdentifiedElementIsVisibleAndClickable(
              LoginPage.USER_NAME_INPUT, 100);
          log.info("Filling username");
          webDriverHelpers.fillInWebElement(
              LoginPage.USER_NAME_INPUT, EditUserSteps.collectedUser.getUserName());
          log.info("Filling password");
          webDriverHelpers.fillInWebElement(
              LoginPage.USER_PASSWORD_INPUT, EditUserSteps.collectedUser.getPassword());
          log.info("Click on Login button");
          webDriverHelpers.clickOnWebElementBySelector(LoginPage.LOGIN_BUTTON);
          webDriverHelpers.waitForPageLoaded();
        });

    When(
        "I check that German word for Configuration is present in the left main menu",
        () -> {
          webDriverHelpers.checkWebElementContainsText(
              NavBarPage.CONFIGURATION_BUTTON, "Einstellungen");
        });

    When(
        "I check that English word for User Settings is present in the left main menu",
        () -> {
          webDriverHelpers.checkWebElementContainsText(
              NavBarPage.USER_SETTINGS_BUTTON, "User Settings");
        });
    When(
        "I check that German word for User Settings is present in the left main menu",
        () -> {
          webDriverHelpers.checkWebElementContainsText(
              NavBarPage.USER_SETTINGS_BUTTON, "Benutzereinstellungen");
        });
    Then(
        "I check that ([^\"]*) language is selected in User Settings",
        (String expectedLanguageText) -> {
          webDriverHelpers.waitUntilElementIsVisibleAndClickable(
              USER_SETTINGS_LANGUAGE_COMBOBOX_TEXT);
          String selectedLanguageText =
              webDriverHelpers.getValueFromWebElement(USER_SETTINGS_LANGUAGE_COMBOBOX_TEXT);
          Assert.assertEquals(
              "Selected language is not correct", expectedLanguageText, selectedLanguageText);
          webDriverHelpers.clickOnWebElementBySelector(DISCARD_USER_SETTINGS_BUTTON);
        });
    And(
        "I click on logout button",
        () -> {
          webDriverHelpers.clickOnWebElementBySelector(LOGOUT_BUTTON);
          webDriverHelpers.waitUntilElementIsVisibleAndClickable(LoginPage.LOGIN_BUTTON);
        });

    Then(
        "Login failed message should be displayed",
        () -> {
          assertHelpers.assertWithPoll20Second(
              () ->
                  org.testng.Assert.assertTrue(
                      webDriverHelpers.isElementVisibleWithTimeout(FAILED_LOGIN_ERROR_MESSAGE, 5),
                      "Login failed error message is not displayed"));
        });

    Then(
        "Login page should be displayed",
        () -> {
          assertHelpers.assertWithPoll20Second(
              () ->
                  org.testng.Assert.assertTrue(
                      webDriverHelpers.isElementVisibleWithTimeout(LOGIN_BUTTON, 5),
                      "Login page is not displayed"));
        });
  }
}
