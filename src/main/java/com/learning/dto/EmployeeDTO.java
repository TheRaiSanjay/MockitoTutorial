package com.learning.dto;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class EmployeeDTO {
	private int id;
	private String firstName;
	private String lastName;
	private Date dob;
	private List<AddressDTO> address;

}
