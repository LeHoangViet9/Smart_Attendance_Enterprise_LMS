package edufit_com_lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import edufit_com_lms.module.auth.service.DataSeederService;

@SpringBootApplication
public class SmartAttendanceEnterpriseLmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartAttendanceEnterpriseLmsApplication.class, args);
    }

    @Bean
    public CommandLineRunner runSeeder(DataSeederService dataSeederService) {
        return args -> {
            dataSeederService.seedData();
        };
    }
}
