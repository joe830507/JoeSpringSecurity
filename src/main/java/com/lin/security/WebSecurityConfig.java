package com.lin.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;

import net.sf.json.JSONObject;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private DataSource dataSource;

	@Override
	@Bean
	public UserDetailsService userDetailsService() {
		JdbcUserDetailsManager manager = new JdbcUserDetailsManager();
		manager.setDataSource(dataSource);
		if (!manager.userExists("user")) {
			manager.createUser(User.withUsername("user").password(encoder().encode("123")).roles("USER").build());
		}
		if (!manager.userExists("admin")) {
			manager.createUser(
					User.withUsername("admin").password(encoder().encode("123")).roles("USER", "ADMIN").build());
		}
		return manager;
	}

	@Bean
	JsonAuthenticationFilter customAuthenticationFilter() throws Exception {
		JsonAuthenticationFilter filter = new JsonAuthenticationFilter();
		filter.setAuthenticationSuccessHandler(new AuthenticationSuccessHandler() {

			@Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
				response.setContentType("application/json;charset=utf-8");
				PrintWriter out = response.getWriter();
				JSONObject json = new JSONObject();
				json.put("code", "success");
				out.write(json.toString());
			}
		});
		filter.setAuthenticationFailureHandler(new AuthenticationFailureHandler() {

			@Override
			public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {
				response.setContentType("application/json;charset=utf-8");
				response.setStatus(401);
				PrintWriter out = response.getWriter();
				JSONObject json = new JSONObject();
				json.put("code", "error");
				out.write(json.toString());
				exception.printStackTrace();
			}
		});
		filter.setFilterProcessesUrl("/login");
		filter.setAuthenticationManager(authenticationManagerBean());
		return filter;
	}

	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/admin/api/**").hasRole("ADMIN").antMatchers("/user/api/**")
				.hasRole("USER").antMatchers("/app/api/**").permitAll().anyRequest().authenticated().and().formLogin()
				.loginPage("/").and().csrf().disable()
				.addFilterBefore(new VerificationCodeFilter(), UsernamePasswordAuthenticationFilter.class)
				.addFilter(customAuthenticationFilter()).sessionManagement().maximumSessions(1);
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication().dataSource(dataSource).passwordEncoder(encoder());
	}

	@Bean
	public Producer captcha() {
		Properties properties = new Properties();
		properties.setProperty("kaptcha.image.width", "150");
		properties.setProperty("kaptcha.image.height", "50");
		properties.setProperty("kaptcha.textproducer.char.string", "0123456789");
		properties.setProperty("kaptcha.textproducer.char.length", "4");
		Config config = new Config(properties);
		DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
		defaultKaptcha.setConfig(config);
		return defaultKaptcha;
	}

}
