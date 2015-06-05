package com.ilearnrw.reader.utils;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;

public class AppLocales {
	public static void setLocales(Context context, String loc){
		if (loc.equalsIgnoreCase("GR")){
			Locale locale = new Locale("gr");
			Locale.setDefault(locale);
			Configuration config = new Configuration();
			config.locale = locale;
			context.getResources().updateConfiguration(config,
					context.getResources().getDisplayMetrics());
		}
		else {
			Locale locale = new Locale("en");
			Locale.setDefault(locale);
			Configuration config = new Configuration();
			config.locale = locale;
			context.getResources().updateConfiguration(config,
					context.getResources().getDisplayMetrics());
		}
	}
}
