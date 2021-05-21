package com.learning.batch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.learning.batch.dto.UserDto;
import com.learning.batch.model.User;
import com.learning.batch.repository.IUserProfileRepository;
import com.learning.batch.repository.IUserRepository;
import com.learning.batch.util.CustomLoggerUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("!disable-security-mode")
public class UserService {

	@Autowired
	private IUserRepository userRepository;
	private IUserProfileRepository userProfileRepository;

	public UserDto getUserByUsername(String transactionId, String username) {
		String seperator = "\n............................";
		String logPrefix = "[Security Service][Login]";
		StringBuilder logInfo = new StringBuilder();
		logInfo.append(seperator);
		logInfo.append("\n" + logPrefix);
		logInfo.append("\n transactionId: " + transactionId);
		logInfo.append(seperator);
		try {
			User user = userRepository.findByUserNameIgnoreCase(username);
			if (user == null)
				throw new SecurityException("User not found: " + username);
			UserDto userDto = new UserDto();
			userDto = userDto.toDto(user);
			userDto.setProfile(user.getUserProfile().getName());
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

}
