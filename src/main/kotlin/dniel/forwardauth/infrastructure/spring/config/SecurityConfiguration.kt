package dniel.forwardauth.infrastructure.spring.config

import dniel.forwardauth.infrastructure.spring.filters.AnonymousFilter
import dniel.forwardauth.infrastructure.spring.filters.AuthenticationBasicFilter
import dniel.forwardauth.infrastructure.spring.filters.AuthenticationTokenFilter
import dniel.forwardauth.infrastructure.spring.filters.LoggingUserFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfiguration : WebSecurityConfigurerAdapter() {

    @Autowired
    private val jwtAuthFilter: AuthenticationTokenFilter? = null

    @Autowired
    private val basicAuthFilter: AuthenticationBasicFilter? = null

    @Autowired
    private val anonymousFilter: AnonymousFilter? = null

    @Autowired
    private val loggingFilter: LoggingUserFilter? = null

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .logout().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/v3/api-docs/").permitAll()
                .antMatchers("/authorize").permitAll()
                .antMatchers("/signin").permitAll()
                .antMatchers("/").permitAll()
                .antMatchers("/login").permitAll()
                .antMatchers("/logout").permitAll()
                .antMatchers("/actuator/info").permitAll()
                .antMatchers("/actuator/health").permitAll()
                .antMatchers("/events").hasAuthority("admin:forwardauth")
                .antMatchers("/ui/**").hasAuthority("admin:forwardauth")
                .antMatchers("/siren/**").hasAuthority("admin:forwardauth")
                .anyRequest().authenticated();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        http.addFilterBefore(basicAuthFilter, AuthenticationTokenFilter::class.java)
        http.addFilterBefore(anonymousFilter, AuthenticationBasicFilter::class.java)
        http.addFilterBefore(loggingFilter, AnonymousFilter::class.java)
    }
}