package com.learning.batch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LoadData implements ApplicationRunner{
	
	@Autowired
	private LoadDataService loadDataService;

	@Transactional(propagation = Propagation.REQUIRED)
	public void run(ApplicationArguments args) throws Exception {
		String transactionId=String.valueOf(System.currentTimeMillis());
		loadDataService.loadUserData(transactionId);
		
	}

}
