//package com.backend.config;
//
//import com.backend.repository.IUserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import javax.sql.DataSource;
//import java.security.SecureRandom;
//
//
//@Configuration
//@EnableWebSecurity
//public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
//
//    @Autowired
//    private DataSource dataSource;
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(4, new SecureRandom());
//    }
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Autowired
//    IUserRepository userRepository;
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .authorizeRequests()
//                    .antMatchers("/resources/css/paper-login.css").permitAll() // Cho phép tất cả mọi người truy cập vào 2 địa chỉ này
//                    .anyRequest().authenticated() // Tất cả các request khác đều cần phải xác thực mới được truy cập
//                    .and()
//                .formLogin() // Cho phép người dùng xác thực bằng form login
//                .loginPage("/login")
//                    .defaultSuccessUrl("/", true)
//                    .permitAll() //Tất cả đều được truy cập vào địa chỉ này
//                    .and()
//                .logout()// Cho phép logout
//                    .permitAll()
//        ;
//        http.csrf().disable();
//    }
//
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.jdbcAuthentication()
//                .dataSource(dataSource)
//                .usersByUsernameQuery("")
//                .authoritiesByUsernameQuery("")
//                .passwordEncoder(passwordEncoder);
//    }
//}
