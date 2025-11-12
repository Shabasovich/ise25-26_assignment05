package de.seuhd.campuscoffee.acctest;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.*;

/**
 * Test runner for the Cucumber tests.
 */
@Suite
@IncludeEngines("cucumber")
@SelectPackages("de.seuhd.campuscoffee.acctest")
@ConfigurationParameter(
        key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/cucumber-report/cucumber.html"
)
@ConfigurationParameter(
        key = Constants.GLUE_PROPERTY_NAME,
        value = "de.seuhd.campuscoffee.acctest")
public class CucumberTests {
    // This class remains empty
}
