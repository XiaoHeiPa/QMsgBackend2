package org.cubewhy.chat.config;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Permission;
import org.cubewhy.chat.entity.RestBean;
import org.cubewhy.chat.entity.UserDetailsImpl;
import org.cubewhy.chat.entity.vo.AuthorizeVO;
import org.cubewhy.chat.filter.JwtAuthorizeFilter;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.UserDetailsServiceImpl;
import org.cubewhy.chat.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Resource
    JwtUtil jwtUtil;
    @Resource
    AccountService accountService;
    @Resource
    JwtAuthorizeFilter jwtAuthorizeFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(
                        conf -> conf
                                .requestMatchers("/").permitAll()
                                .requestMatchers("/ws/**").permitAll()
                                .requestMatchers("/dashboard").hasAuthority(Permission.DASHBOARD.name())
                                .anyRequest().authenticated()
                )
                .formLogin(
                        conf -> conf
                                .loginProcessingUrl("/api/user/login")
                                .successHandler(this::onAuthenticationSuccessful)
                                .failureHandler(this::onAuthenticationFailure)
                )
                .logout(
                        conf -> conf
                                .logoutUrl("/api/user/logout")
                                .logoutSuccessHandler(this::onLogoutSuccess)
                )
                .exceptionHandling(conf -> conf
                        .authenticationEntryPoint(this::onUnauthorized)
                        .accessDeniedHandler(this::onAccessDeny)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(conf -> conf
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private void onAccessDeny(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(403);
        response.getWriter().write(RestBean.forbidden(exception).toJson());
    }

    private void onUnauthorized(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(401);
        response.getWriter().write(RestBean.unauthorized(exception).toJson());
    }

    private void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        String auth = request.getHeader("Authorization");
        if (jwtUtil.invalidateJwt(auth)) {
            // make token invalidate
            writer.write(RestBean.success().toJson());
        } else {
            writer.write(RestBean.failure(400, "Failed to logout").toJson());
        }
    }

    private void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        request.setCharacterEncoding("utf-8");
        response.setStatus(401);
        response.getWriter().write(RestBean.unauthorized(exception).toJson());
    }

    @Transactional
    protected void onAuthenticationSuccessful(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        request.setCharacterEncoding("utf-8");
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        Account account = user.getAccount();
        String token = jwtUtil.createJwt(user, account.getId(), account.getUsername());
        AuthorizeVO authorizeVO = account.asViewObject(AuthorizeVO.class, authorizeVO1 -> {
            authorizeVO1.setExpire(jwtUtil.getExpireDate().getTime());
            authorizeVO1.setToken(token);
        });
        response.getWriter().write(RestBean.success(authorizeVO).toJson());
    }
}
