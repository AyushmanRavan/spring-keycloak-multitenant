package com.ineat.spring.keycloak.multitenant.config;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

public class SpringKeycloakSecurityConfiguration {

    @Configuration
    @EnableWebSecurity
    @ConditionalOnProperty(name = "keycloak.enabled", havingValue = "true", matchIfMissing = true)
    @ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
    public static class KeycloakConfigurationAdapter extends KeycloakWebSecurityConfigurerAdapter {
        private final KeycloakClientRequestFactory keycloakClientRequestFactory;

        public KeycloakConfigurationAdapter(KeycloakClientRequestFactory keycloakClientRequestFactory) {
            this.keycloakClientRequestFactory = keycloakClientRequestFactory;
            //to use principal and authentication together with @async
            SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        }
        @Bean
        protected CustomSecurityCorsFilter customCorsFilter(){
            return new CustomSecurityCorsFilter();
        }

        @Bean
        @Override
        protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
            // required for bearer-only applications.
            return new NullAuthenticatedSessionStrategy();
        }

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
            // simple Authority Mapper to avoid ROLE_
            keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
            auth.authenticationProvider(keycloakAuthenticationProvider);
        }
        @Bean
        public KeycloakConfigResolver KeycloakConfigResolver() {
            return new HeaderBasedConfigResolver();
        }

        @Override
        protected void configure(HttpSecurity httpSecurity) throws Exception {
            httpSecurity
                    .sessionManagement()
                        // use previously declared bean
                        .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
                    // keycloak filters for securisation
                    .and()
                        // this is needed to pass the preflight CORS security on BROWSER
                        .addFilterBefore(customCorsFilter(), SecurityContextPersistenceFilter.class)
                        .addFilterBefore(keycloakPreAuthActionsFilter(), LogoutFilter.class)
                        .addFilterBefore(keycloakAuthenticationProcessingFilter(), X509AuthenticationFilter.class)
                        .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint())
                    // add cors options
                    .and().cors()
                    // delegate logout endpoint to spring security
                    .and()
                        .logout()
                        .addLogoutHandler(keycloakLogoutHandler())
                        .logoutUrl("/logout").logoutSuccessHandler(
                            // logout handler for API
                            (HttpServletRequest request, HttpServletResponse response, Authentication authentication) ->
                                    response.setStatus(HttpServletResponse.SC_OK)
                         )
                    .and().apply(new CommonSpringKeycloakTutorialsSecuritAdapter());
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(Arrays.asList("*"));
            configuration.setAllowedMethods(Arrays.asList(HttpMethod.OPTIONS.name(), "GET","POST"));
            configuration.setAllowedHeaders(Arrays.asList("Access-Control-Allow-Headers", "Access-Control-Allow-Origin", "Authorization", "realm"));
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }
    }

    public static class CommonSpringKeycloakTutorialsSecuritAdapter extends AbstractHttpConfigurer<CommonSpringKeycloakTutorialsSecuritAdapter, HttpSecurity> {
        @Override
        public void init(HttpSecurity httpSecurity) throws Exception {
            // must be done in the init method
            httpSecurity
                    // disable csrf because of API mode
                    .csrf().disable()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                        // manage routes securisation here
                        .authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll()
                    // manage routes securisation here
                    .and()
                        .authorizeRequests()
                            .antMatchers(HttpMethod.OPTIONS).permitAll()
                            .antMatchers("/logout", "/", "/unsecured","/keycloak/**").permitAll()
                            .antMatchers("/user").hasRole("USER")
                            .antMatchers("/admin").hasRole("ADMIN")
                            .antMatchers("/all-user").hasAnyRole("USER", "ADMIN")
                            .anyRequest().denyAll();
        }
    }
}