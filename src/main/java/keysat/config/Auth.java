package keysat.config;

import keysat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity

public class Auth {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DataSource dataSource;

	@Bean
	public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(); 
}

	@Bean

	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
		            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
				config.setAllowedOriginPatterns(List.of("*"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true); 
                return config;
            }))
				.httpBasic() 
				.and()
				.authorizeHttpRequests((requests) -> requests
						.requestMatchers("/", "/home", "/login").permitAll()
						.requestMatchers("/secret").hasRole("USER")
						.requestMatchers("/teachersecret").hasRole("TEACHER")
						.requestMatchers("/users/create").permitAll()
						.requestMatchers("/users").permitAll())
				.csrf().disable(); 

		return http.build();
	}
	@Bean
		public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
			return authenticationConfiguration.getAuthenticationManager();
    }
	@Bean
	public UserDetailsService userDetailsService() {
		JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
		return manager;
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication()
				.dataSource(dataSource)
				.usersByUsernameQuery("SELECT username, password, enabled FROM users WHERE username=?")
				.authoritiesByUsernameQuery("SELECT username, authority FROM user_roles WHERE username=?");
	}
}