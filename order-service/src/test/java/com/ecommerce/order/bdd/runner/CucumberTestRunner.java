package com.ecommerce.order.bdd.runner;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME,
        value = "com.ecommerce.order.bdd.steps")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/cucumber-reports/order-report.html, json:target/cucumber-reports/order-report.json")
public class CucumberTestRunner {
    // BC References: BC-014, BC-015, BC-016, BC-026..028, BC-056
}
