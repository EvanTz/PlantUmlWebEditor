package com.example.plantumlwebeditorv2;


import com.example.plantumlwebeditorv2.controller.*;
import com.example.plantumlwebeditorv2.service.*;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;


@Suite
@SelectClasses({
        // Controller Tests
        AuthControllerTest.class,
        ProjectControllerTest.class,
        PlantUmlControllerTest.class,

        // Service Tests
        UserServiceTest.class,
        ProjectServiceTest.class,
        PlantUmlServiceTest.class,

        // Integration Tests
        PlantUmlServerIntegrationTest.class
})


public class PlantUmlServerTestSuite {
}
