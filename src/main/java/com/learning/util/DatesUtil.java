package com.learning.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DatesUtil {

	public static LocalDateTime toLocalDateTime(final Date date)
	{
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}
}
