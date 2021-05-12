package com.learning.batch.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.learning.batch.CustomSecurityException;
import com.learning.batch.dto.LoginRequestDTO;
import com.learning.batch.dto.UserDto;
import com.learning.batch.model.User;
import com.learning.batch.repository.IUserRepository;
import com.learning.batch.util.CustomLoggerUtil;

import lombok.extern.slf4j.Slf4j;

/* No roll back so that failed attempt can be logged*/
@Slf4j
//@Service
@Component
@Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Throwable.class)
@Profile("!disable-security-mode")
public class SecurityService implements UserDetailsService {

	@Autowired
	private IUserRepository userRepository;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserService userService;

	@Autowired
	private LdapService ldapService;

	@Autowired
	private TokenService tokenService;

	private Long maxFailedLoginAttempts;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String seperator = "\n............................";
		long transactionId = System.currentTimeMillis();
		String logPrefix = "[Security Service][Login]";
		StringBuilder logInfo = new StringBuilder();
		logInfo.append(seperator);
		logInfo.append("\n" + logPrefix);
		logInfo.append("\n transactionId: " + transactionId);
		logInfo.append(seperator);
		UserDetails userDetails = null;
		try {
			User user = userRepository.findByUserNameIgnoreCase(username);
			if (user == null) {
				throw new UsernameNotFoundException("User Not Found : " + username);
			}
			logInfo.append("User found in DB :\n " + username.toString());
			BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
			userDetails = org.springframework.security.core.userdetails.User.builder().username(user.getUserName())
					.password(bCryptPasswordEncoder.encode(user.getPassword())).accountLocked(user.isDisabled())
					.accountExpired(user.isDisabled()).roles(user.getUserProfile().getName()).build();
			return userDetails;
		} catch (Exception e) {
			String errorMessage = "[ERROR]" + logPrefix + " " + e.getLocalizedMessage();
			logInfo.append(errorMessage);
			log.error(errorMessage, e);
			throw e;
		} finally {
			logInfo.append(seperator);
			logInfo.append("\n Time Taken ........." + CustomLoggerUtil.calculateTimeTaken(transactionId));
			logInfo.append(seperator);
			log.debug(logInfo.toString());
		}

	}

	public UserDto login(String transactionId, HttpServletRequest req, HttpServletResponse res,
			LoginRequestDTO loginRequestDto) {
		String seperator = "\n............................";
		String logPrefix = "[Security Service][Login]";
		StringBuilder logInfo = new StringBuilder();
		logInfo.append(seperator);
		logInfo.append("\n" + logPrefix);
		logInfo.append("\n transactionId: " + transactionId);
		logInfo.append(seperator);
		UserDto userDto = null;
		try {
			if (loginRequestDto == null || loginRequestDto.getUserName() == null
					|| StringUtils.isBlank(loginRequestDto.getUserName())) {
				throw new CustomSecurityException(transactionId, HttpStatus.UNAUTHORIZED, "username is empty");
			} else if (StringUtils.isBlank(loginRequestDto.getPassword())) {
				throw new CustomSecurityException(transactionId, HttpStatus.UNAUTHORIZED, "password is empty");
			}
			// Database check -- Let Spring security do it
			Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
					loginRequestDto.getUserName(), loginRequestDto.getPassword()));
			SecurityContextHolder.getContext().setAuthentication(authentication);

			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			if (userDetails == null || userDetails.getAuthorities().size() == 0) {
				throw new CustomSecurityException(transactionId, HttpStatus.UNAUTHORIZED, "User not found");
			}
			userDto = userService.getUserByUsername(transactionId, userDetails.getUsername());

			ldapService.ldapAuthenticate(transactionId, loginRequestDto.getUserName(), loginRequestDto.getPassword());
			String token = tokenService.generateToken(userDetails.getUsername());
			res.addHeader(TokenService.HEADER_STRING, TokenService.TOKEN_PREFIX + token);
			userDto.setToken(TokenService.TOKEN_PREFIX + token);
			if (userDto.isDisabled()) {
				throw new CustomSecurityException(transactionId, HttpStatus.UNAUTHORIZED, "User is disabled/Inactive");
			}
			// successful so failed any failedLoginAttemptt counter to zero.
			if (userDto.getFailedLoginAttempts() != null || userDto.getFailedLoginAttempts() != 0L) {
				User user = userRepository.findByUserNameIgnoreCase(loginRequestDto.getUserName());
				user.setFailedLoginAttempts(0L);
				userRepository.save(user);

			}

			return userDto;
		} catch (Exception e) {
			String errorMessage = "[ERROR]" + logPrefix + " " + e.getLocalizedMessage();
			logInfo.append(errorMessage);
			log.error(errorMessage, e);
			// Catch errors and log it as failed login in database
			User user = userRepository.findByUserNameIgnoreCase(loginRequestDto.getUserName());
			if (user != null && user.getId() != null) {
				Long failedLoginAttempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0L;
				failedLoginAttempts = failedLoginAttempts + 1;
				user.setFailedLoginAttempts(failedLoginAttempts);
				if (failedLoginAttempts >= maxFailedLoginAttempts) {
					user.setDisabled(true);
					throw new CustomSecurityException(transactionId, HttpStatus.UNAUTHORIZED,
							"User account is Locked. Please contact system administrator");

				}
				userRepository.save(user);

			}

			throw e;
		} finally {
			logInfo.append(seperator);
			logInfo.append(
					"\n Time Taken ........." + CustomLoggerUtil.calculateTimeTaken(Long.valueOf(transactionId)));
			logInfo.append(seperator);
			log.debug(logInfo.toString());
		}

	}

	public boolean logoff(String transactionId, HttpServletRequest req, HttpServletResponse res) throws UsernameNotFoundException {
		String seperator = "\n............................";
		String logPrefix = "[Security Service][Logoff]";
		StringBuilder logInfo = new StringBuilder();
		logInfo.append(seperator);
		logInfo.append("\n" + logPrefix);
		logInfo.append("\n transactionId: " + transactionId);
		logInfo.append(seperator);
		try {
			String authorizationHeader = req.getHeader(TokenService.HEADER_STRING);
			String token = null;
			String username = null;

			if (authorizationHeader != null && authorizationHeader.startsWith(TokenService.TOKEN_PREFIX)) {
				token = authorizationHeader.replace(TokenService.TOKEN_PREFIX, "");
				username = tokenService.extractUsername(token);
				tokenService.logOut(token);
				if (username != null) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			String errorMessage = "[ERROR]" + logPrefix + " " + e.getLocalizedMessage();
			logInfo.append(errorMessage);
			log.error(errorMessage, e);
			throw e;
		} finally {
			logInfo.append(seperator);
			logInfo.append(
					"\n Time Taken ........." + CustomLoggerUtil.calculateTimeTaken(Long.valueOf(transactionId)));
			logInfo.append(seperator);
			log.debug(logInfo.toString());
		}

	}

}
