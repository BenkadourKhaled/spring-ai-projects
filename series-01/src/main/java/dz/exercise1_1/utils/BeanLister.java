package dz.exercise1_1.utils;


import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class BeanLister {

    @Bean
    CommandLineRunner listSpringAiBeans(ApplicationContext ctx) {
        return args -> {
            System.out.println("===== Beans Spring AI =====");
            Arrays.stream(ctx.getBeanDefinitionNames())
                    .sorted()
                    .filter(name -> {
                        Class<?> type = ctx.getType(name);
                        return type != null && (type.getName().startsWith("org.springframework.ai")
                                || type.getName().startsWith("com.google.genai"));
                    })
                    .forEach(name -> System.out.printf("%-40s -> %s%n", name, ctx.getType(name).getName()));
        };
    }
}