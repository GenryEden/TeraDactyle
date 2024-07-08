package ru.kernelpunik.teradactyle.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.kernelpunik.teradactyle.AntiplagiatApplication;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AntiplagiatApplication.class)
@TestPropertySource(
        locations = "classpath:application-integrationtest.properties")
public class PlagiarsimDetectorServiceTest extends PlagiarismDetectorTest {


    @Autowired
    public PlagiarsimDetectorServiceTest(PlagiarismDetectorService plagiarismDetectorService) {
        super(plagiarismDetectorService);
    }
}
