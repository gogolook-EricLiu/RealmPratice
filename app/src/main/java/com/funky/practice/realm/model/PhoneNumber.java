package com.funky.practice.realm.model;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by gogolook on 5/4/16.
 */
public class PhoneNumber extends RealmObject {
	@Required
	public String raw;
	public String e164;
}
