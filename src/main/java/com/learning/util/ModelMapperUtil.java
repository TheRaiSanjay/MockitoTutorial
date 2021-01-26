package com.learning.util;

import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;

public class ModelMapperUtil extends ModelMapper {
	public ModelMapperUtil() {
		this.getConfiguration().setFieldMatchingEnabled(true).setFieldAccessLevel(AccessLevel.PRIVATE);
	}

}
