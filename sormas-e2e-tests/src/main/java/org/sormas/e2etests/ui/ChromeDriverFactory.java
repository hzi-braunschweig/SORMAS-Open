/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2021 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
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

package org.sormas.e2etests.ui;

import static org.openqa.selenium.remote.CapabilityType.SUPPORTS_ALERTS;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.util.HashMap;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

@Slf4j
public class ChromeDriverFactory implements DriverFactory {

  private final boolean headless;
  private final DesiredCapabilities desiredCapabilities;
  private final String userDirProperty;

  @Inject
  public ChromeDriverFactory(
      @Named("HEADLESS") boolean headless,
      DesiredCapabilities desiredCapabilities,
      @Named("USER_DIR") String userDirProperty) {
    this.headless = headless;
    this.desiredCapabilities = desiredCapabilities;
    this.userDirProperty = userDirProperty;
  }

  @Override
  public RemoteWebDriver getRemoteWebDriver() {
    try {
      WebDriverManager.chromedriver().driverVersion("90.0.4430.212").setup();
      log.info(
          "90.0.4430.212 version was set to ChromeDriver  <<<<<<<<<<<<<<<<<<<<<<<<<<<< CHROME INFO");
    } catch (Exception e) {
      log.error("Unable to force ChromeDriver to start with 90.0.4430.212 ");
      log.info("Trying to start ChromeDriver with 90 <<<<<<<<<<<< CHROME INFO");
      WebDriverManager.chromedriver().driverVersion("90").setup();
    } finally {
      WebDriverManager.chromedriver().setup();
      log.info(
          "Starting ChromeDriver with webdrivermanager provided version  <<<<<<<<<<<<<<<<<<<<<<<<<<<< CHROME INFO");
    }

    System.setProperty("webdriver.chrome.silentOutput", "true");
    System.setProperty("javascript.enabled", "true");
    final HashMap<String, Object> chromePreferences = new HashMap<>();
    chromePreferences.put("profile.password_manager_enabled", Boolean.FALSE);
    chromePreferences.put("download.default_directory", userDirProperty + "/downloads");
    final ChromeOptions options = new ChromeOptions();
    options.merge(desiredCapabilities);
    options.setHeadless(headless);
    options.addArguments("--no-default-browser-check");
    options.addArguments("--window-size=1920,1080");
    options.setExperimentalOption("prefs", chromePreferences);
    options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
    options.setCapability(SUPPORTS_ALERTS, false);
    return new ChromeDriver(options);
  }
}
