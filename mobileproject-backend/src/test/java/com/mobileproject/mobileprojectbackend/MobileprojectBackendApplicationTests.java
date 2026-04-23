package com.mobileproject.mobileprojectbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.auth.token-secret=test-token-secret-for-spring-tests")
class MobileprojectBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
