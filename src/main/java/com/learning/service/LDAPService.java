package com.learning.batch.service;

import java.util.Base64;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import com.learning.batch.CustomSecurityException;
import com.learning.batch.dto.UserDto;
import com.learning.batch.util.CustomLoggerUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("!disable-security-mode")
public class LdapService {

	private String providerUrl;
	private String managerDn;
	private String managerPassword;
	private String userDn;
	@Autowired
	private LdapTemplate ldapTemplate;

	public void ldapAuthenticate(String transactionId, String username, String password) {
		String seperator = "\n............................";
		String logPrefix = "[Security Service][Login]";
		StringBuilder logInfo = new StringBuilder();
		logInfo.append(seperator);
		logInfo.append("\n" + logPrefix);
		logInfo.append("\n transactionId: " + transactionId);
		logInfo.append(seperator);
		try {
			String base = userDn;
			String filter = "CN=" + username;
			boolean isFound = false;
			try {
				isFound = ldapTemplate.authenticate(base, filter, new String(Base64.getDecoder().decode(password)));

			} catch (Exception e) {
				if (!isFound) {
					isFound = ldapTemplate.authenticate(base, filter, password);
					password = null;
				}
			}
			if (isFound) {
				logInfo.append("Ldap authentication [OK] for :" + username);
			} else {
				throw new CustomSecurityException(transactionId, HttpStatus.UNAUTHORIZED,
						"Invalid Username/password for :" + username);
			}

		} catch (Exception e) {
			String errorMessage = "[ERROR]" + logPrefix + " " + e.getLocalizedMessage();
			logInfo.append(errorMessage);
			log.error(errorMessage, e);
			throw e;
		} finally {
			// clear password from memory
			password = null;
			logInfo.append(seperator);
			logInfo.append(
					"\n Time Taken ........." + CustomLoggerUtil.calculateTimeTaken(Long.valueOf(transactionId)));
			logInfo.append(seperator);
			log.debug(logInfo.toString());

		}
	}

	public UserDto getLdapDetails(String transactionId, String username) {
		String seperator = "\n............................";
		String logPrefix = "[Security Service][Login]";
		StringBuilder logInfo = new StringBuilder();
		logInfo.append(seperator);
		logInfo.append("\n" + logPrefix);
		logInfo.append("\n transactionId: " + transactionId);
		logInfo.append(seperator);
		UserDto userDto = null;
		try {
			String base = userDn;
			String filter = "CN=" + username;

			List<String> quickSearch = ldapTemplate.search(userDn, filter,
					(AttributesMapper<String>) attrs -> (String) attrs.get("sn").get());
			if (quickSearch == null || quickSearch.size() == 0) {
				throw new CustomSecurityException(transactionId, HttpStatus.UNAUTHORIZED, "not found in LDAP");
			}
			String[] attributes = new String[] { "cn", "xx-ad-knownAsGivenName", "xx-ad-knownAsLastName", "mail" };
			SearchControls searchControls = new SearchControls();
			searchControls.setCountLimit(1);
			searchControls.setReturningAttributes(attributes);
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			List<UserDto> result = ldapTemplate.search(userDn, filter, searchControls, new LdapUserAttributeMapper());
			if (result != null && result.size() == 1 && result.get(0) != null) {
				userDto = result.get(0);
				logInfo.append("user found in LDAP");
			} else {
				throw new CustomSecurityException(transactionId, HttpStatus.UNAUTHORIZED, "not found");
			}
			userDto.setTransactionId(Long.valueOf(transactionId));
			return userDto;
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

	// class to map Ldap details to dto
	public class LdapUserAttributeMapper implements AttributesMapper<UserDto> {

		@Override
		public UserDto mapFromAttributes(Attributes attributes) throws NamingException {
			if (attributes == null) {
				return null;
			}
			UserDto userDto = new UserDto();
			userDto.setUsername(attributes.get("cn") != null ? attributes.get("cn").get().toString() : "");
			userDto.setFirstName(attributes.get("xx-ad-knownAsGivenName") != null
					? attributes.get("xx-ad-knownAsGivenName").get().toString()
					: "");
			userDto.setLastName(attributes.get("xx-ad-knownAsLastName") != null
					? attributes.get("xx-ad-knownAsLastName").get().toString()
					: "");
			userDto.setEmail(attributes.get("mail") != null ? attributes.get("mail").get().toString() : "");

			return userDto;

		}

	}

}
