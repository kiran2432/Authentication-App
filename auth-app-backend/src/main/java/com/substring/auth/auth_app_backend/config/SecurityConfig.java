package com.substring.auth.auth_app_backend.config;

import com.substring.auth.auth_app_backend.dtos.ApiError;
import com.substring.auth.auth_app_backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Configuration
public class SecurityConfig {

//    @Bean
//    public UserDetailsService users(){
//        User.UserBuilder userBuilder = User.withDefaultPasswordEncoder();
//        UserDetails user1 = userBuilder.username("kiran").password("abc").roles("ADMIN").build();
//        UserDetails user2 = userBuilder.username("chets").password("xyz").roles("USER").build();
//        return new InMemoryUserDetailsManager(user1, user2);
//    }
//till now the user in memory is used for authentication but the users mentioned above will be used for
//authentication but now we want to use the user from database that are signed up

    //this is written bcoz to customize the authentication to certain API calls only
    //i.e this auth is not required for signup/register of user

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        //csrf is disabled as no need to worry abt csrf attacks when we have seperate frontend and backend app
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        .authorizeHttpRequests(authorizeHttpRequests ->
                authorizeHttpRequests
                        .requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/auth/logout").permitAll()
                        .anyRequest().authenticated()
                )
                //.httpBasic(Customizer.withDefaults());
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, e) -> {
                    //e.printStackTrace();
                    response.setStatus(401);
                    response.setContentType("application/json");
                    String message = e.getMessage();
                    String error = (String) request .getAttribute("error");
                    if(error != null){
                        message = error;
                    }
                    //Map<String, Object> errorMap = Map.of("message", message, "statusCode", Integer.toString(401));
                    var apiError = ApiError.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorized access", message, request.getRequestURI(), true);
                    var objectMapper = new ObjectMapper();
                    response.getWriter().write(objectMapper.writeValueAsString(apiError));
                }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();    
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

}
