package demo.repositories;

import demo.Application;
import demo.Config;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.assertEquals;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@DataJpaTest
@ContextConfiguration(classes = Config.class, loader= AnnotationConfigContextLoader.class)
public class EmployeeRepositoryTest {


    @Autowired
    @Qualifier("InitDB")
    private CommandLineRunner commandLineRunner;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    public void testSetupRepoCount() throws Exception {
        commandLineRunner.run();
        assertEquals(2, employeeRepository.count());
    }
}