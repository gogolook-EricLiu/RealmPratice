package com.funky.practice.realm.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by gogolook on 5/4/16.
 */
public class Contact extends RealmObject {
	@PrimaryKey
	public long contactId;
	public String name;
	public RealmList<PhoneNumber> phoneNumbers;
}
