package com.example.basics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authorization.EnableMultiFactorAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import javax.sql.DataSource;
import java.security.Principal;
import java.util.Map;

@EnableMultiFactorAuthentication(authorities = {
        FactorGrantedAuthority.OTT_AUTHORITY,
        FactorGrantedAuthority.PASSWORD_AUTHORITY
})
@SpringBootApplication
public class BasicsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicsApplication.class, args);
    }

    @Bean
    Customizer<HttpSecurity> securityCustomizer() {
        return http -> http
                .webAuthn(a -> a
                        .rpName("voxxeddaythessaloniki")
                        .rpId("localhost")
                        .allowedOrigins("http://localhost:8080")
                )
                .oneTimeTokenLogin(ott -> ott.tokenGenerationSuccessHandler(
                        (_, response, oneTimeToken) -> {

                            response.getWriter().println("you've got console mail!");
                            response.setContentType(MediaType.TEXT_PLAIN_VALUE);

                            IO.println("hey, " + oneTimeToken.getUsername() + ", please go to " +
                                    "http://localhost:8080/login/ott?token=" + oneTimeToken.getTokenValue());


                        }));
    }

    @Bean
    JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
        var u = new JdbcUserDetailsManager(dataSource);
        u.setEnableUpdatePassword(true);
        return u;
    }

    @Bean
    RouterFunction<ServerResponse> routerFunction(DogRepository repository) {
        return RouterFunctions.route()
                .GET("/dogs", _ -> ServerResponse.ok().body(repository.findAll()))
                .GET("/", request -> {
                    var name = request.principal().map(Principal::getName).orElse("World");
                    var message = Map.of("message", "Hello " + name);
                    return ServerResponse.ok().body(message);
                })
                .build();
    }
}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

record Dog(@Id int id, String name, String description) {
}