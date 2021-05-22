package com.learning.batch.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.learning.batch.model.User;
import com.learning.batch.model.UserProfile;
import com.learning.batch.repository.IUserProfileRepository;
import com.learning.batch.repository.IUserRepository;
import com.learning.batch.util.ApplicationConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED)
public class LoadDataService {

	@Autowired
	IUserProfileRepository userProfileRepository;
	
	@Autowired
	IUserRepository userRepository;

	public void loadUserData(String transactionId) {

		try {
			String profiles[] = new String[] { ApplicationConstants.PROFILE_SUPER_ADMIN,
					ApplicationConstants.PROFILE_ADMIN };
			for (String profile : profiles) {
				UserProfile userProfile = userProfileRepository.findByNameIgnoreCase(profile);
				if (userProfile == null) {
					userProfile = new UserProfile();
					userProfile.setName(profile);
					userProfile.setTransactionId(transactionId);
					userProfile.setCreated(LocalDateTime.now());
					userProfileRepository.save(userProfile);
				}
			}
			
			UserProfile superAdminProfile=userProfileRepository.findByNameIgnoreCase(ApplicationConstants.PROFILE_SUPER_ADMIN);
			UserProfile adminProfile=userProfileRepository.findByNameIgnoreCase(ApplicationConstants.PROFILE_ADMIN);
			
			String userName="batch";
			User batchUser=userRepository.findByUserNameIgnoreCase(userName);
			if(batchUser==null)
			{
				batchUser=new User();
				batchUser.setUserName(userName);
				batchUser.setUserProfile(superAdminProfile);
				batchUser.setEmail("learning-batch@outlook.com");
				batchUser.setFirstName("batch");
				batchUser.setLastName("batch");
				batchUser.setTransactionId(transactionId);
				batchUser.setCreated(LocalDateTime.now());
				userRepository.save(batchUser);
			}
			
			 userName="sanjayrai";
			 User user=userRepository.findByUserNameIgnoreCase(userName);
			if(user==null)
			{
				user=new User();
				user.setUserName(userName);
				user.setUserProfile(superAdminProfile);
				user.setEmail("rai.sanjay2015@outlook.com");
				user.setFirstName("Sanjay");
				user.setLastName("Rai");
				user.setTransactionId(transactionId);
				user.setCreated(LocalDateTime.now());
				user.setCreatedBy(batchUser);
				userRepository.save(user);
			}
			
			userName="ranjeetrai";
			 user=userRepository.findByUserNameIgnoreCase(userName);
			if(user==null)
			{
				user=new User();
				user.setUserName(userName);
				user.setUserProfile(adminProfile);
				user.setEmail("rai.ranjeet2016@gmail.com");
				user.setFirstName("Ranjeet");
				user.setLastName("Rai");
				user.setTransactionId(transactionId);
				user.setCreated(LocalDateTime.now());
				user.setCreatedBy(batchUser);
				userRepository.save(user);
			}
			
			

		} catch (Exception e) {
			
			
		} finally {

		}
	}

}
