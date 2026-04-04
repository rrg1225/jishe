package com.example.student.university_career_platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAsync
@EnableScheduling
class UniversityCareerPlatformApplicationTests {
	@Test
	void contextLoads() {
	}
}
