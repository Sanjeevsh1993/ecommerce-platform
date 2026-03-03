package com.ecommerce.auth.bdd.runner;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME,
        value = "com.ecommerce.auth.bdd.steps")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/cucumber-reports/auth-report.html, json:target/cucumber-reports/auth-report.json")
@ConfigurationParameter(key = Constants.FEATURES_PROPERTY_NAME,
        value = "classpath:features/authentication.feature")
public class CucumberTestRunner {
    // BC References: BC-034..042 — Cucumber JUnit Platform Suite runner
}
