package com.learning.batch;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping(value = {"/emplyoee/batch"})
public class EmployeeBatchController {
	
	@ResponseBody
	@RequestMapping(value = {"/start"}, method = RequestMethod.GET)
	public String startMyBatch()
	{
		System.out.println("started........");
		return "start";
	}

}
