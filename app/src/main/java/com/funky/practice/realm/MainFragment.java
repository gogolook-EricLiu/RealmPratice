package com.funky.practice.realm;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.funky.practice.realm.model.Contact;
import com.funky.practice.realm.model.PhoneNumber;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {
	private static final int REQUEST_CODE_READ_CONTACT_REALM = 1;
	private static final int REQUEST_CODE_READ_CONTACT_SQLITE = 2;

	private static final Uri CONTENT_URI = Uri.parse("content://funky");

	private Realm mRealm;
	private RealmResults<Contact> mContacts;
	private RealmResults<PhoneNumber> mPhoneNumbers;

	private Unbinder mUnbinder;

	@BindView(android.R.id.list)
	RecyclerView mRecyclerView;

	private ContactAdapter mAdapter;

	public MainFragment() {
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main_fragment, container, false);

		mUnbinder = ButterKnife.bind(this, view);

		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mAdapter = new ContactAdapter();
		mRecyclerView.setAdapter(mAdapter);

		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mUnbinder.unbind();
	}

	@Override
	public void onStart() {
		super.onStart();
		mRealm = Realm.getDefaultInstance();

		mContacts = mRealm.where(Contact.class).findAllSortedAsync("name");
		mPhoneNumbers = mRealm.where(PhoneNumber.class).findAll();

		mContacts.addChangeListener(new RealmChangeListener<RealmResults<Contact>>() {
			@Override
			public void onChange(RealmResults<Contact> contacts) {
				if (null == mAdapter || mRealm.isClosed() || null == contacts || !contacts.isValid() || !contacts.isLoaded()) {
					return;
				}
				mAdapter.setContactList(contacts);
			}
		});
	}

	@Override
	public void onStop() {
		super.onStop();
		mContacts.removeChangeListeners();
		mRealm.close();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.main_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int id = item.getItemId();

		if (id == R.id.action_realm) {
			syncContactToRealm();
			return true;
		} else if (id == R.id.action_sqlite) {
			syncContactToSqlite();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void syncContactToRealm() {
		if (!isVisible() || null == getActivity()) {
			return;
		}

		final boolean hasReadContactPermission = PackageManager.PERMISSION_GRANTED
				== ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS);

		if (!hasReadContactPermission) {
			requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_READ_CONTACT_REALM);
			return;
		}

		Observable.just(System.currentTimeMillis())
				.subscribeOn(Schedulers.computation())
				.map(new Func1<Long, Cursor>() {
					@Override
					public Cursor call(Long startTime) {
						Log.e("funky", "map: thread=" + Thread.currentThread().getName());
						if (isVisible() && null != getActivity()) {
							return getActivity().getContentResolver().query(
									Phone.CONTENT_URI,
									new String[]{Phone.CONTACT_ID, Phone.DISPLAY_NAME, Phone.NUMBER},
									null,
									null,
									Phone.CONTACT_ID + " ASC");
						}
						return null;
					}
				})
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<Cursor>() {
					@Override
					public void call(final Cursor cursor) {
						Log.e("funky", "subscribe: thread=" + Thread.currentThread().getName());
						if (!isVisible() || null == getActivity() || null == cursor && cursor.isClosed()) {
							return;
						}
						final long startTime = System.currentTimeMillis();
						mRealm.executeTransactionAsync(new Realm.Transaction() {
							@Override
							public void execute(Realm realm) {
								if (null != cursor) {
									if (cursor.moveToFirst()) {
										realm.where(Contact.class).findAll().deleteAllFromRealm();
										realm.where(PhoneNumber.class).findAll().deleteAllFromRealm();

										PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
										final String networkRegion = ((TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE)).getNetworkCountryIso();
										final String defaultRegion = TextUtils.isEmpty(networkRegion) ? "US" : networkRegion.toUpperCase();
										Contact contact = null;
										do {
											if (null == contact || contact.contactId != cursor.getLong(0)) {
												contact = realm.createObject(Contact.class);
												contact.contactId = cursor.getLong(0);
												contact.name = cursor.getString(1);
											}
											final String number = cursor.getString(2);
											if (!TextUtils.isEmpty(number)) {
												PhoneNumber phoneNumber = realm.createObject(PhoneNumber.class);
												phoneNumber.raw = number;
												try {
													phoneNumber.e164 = phoneUtil.format(phoneUtil.parse(number, defaultRegion), PhoneNumberUtil.PhoneNumberFormat.E164);
												} catch (NumberParseException e) {
												}
												contact.phoneNumbers.add(phoneNumber);
											}
										} while (cursor.moveToNext());
									}
									cursor.close();
								}
							}
						}, new Realm.Transaction.OnSuccess() {
							@Override
							public void onSuccess() {
								for (Contact contact : mContacts) {
									if (contact.phoneNumbers.size() > 1) {
										Log.e("funky", "" + contact.name);
									}
								}
								Toast.makeText(getActivity(), mContacts.size() + " contacts spend " + (System.currentTimeMillis() - startTime) + "ms", Toast.LENGTH_SHORT).show();
							}
						});
					}
				});
	}

	private void syncContactToSqlite() {
		if (!isVisible() || null == getActivity()) {
			return;
		}

		final boolean hasReadContactPermission = PackageManager.PERMISSION_GRANTED
				== ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS);

		if (!hasReadContactPermission) {
			requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_READ_CONTACT_REALM);
			return;
		}

		Observable.just(System.currentTimeMillis())
				.subscribeOn(Schedulers.computation())
				.map(new Func1<Long, Long>() {
					@Override
					public Long call(Long startTime) {
						Log.e("funky", "map: thread=" + Thread.currentThread().getName());
						if (isVisible() && null != getActivity()) {
							ContentResolver cr = getActivity().getContentResolver();
							cr.delete(CONTENT_URI, null, null);

							Cursor cursor = cr.query(
									Phone.CONTENT_URI,
									new String[]{Phone.CONTACT_ID, Phone.DISPLAY_NAME, Phone.NUMBER},
									null,
									null,
									Phone.CONTACT_ID + " ASC");

							startTime = System.currentTimeMillis();
							if (null != cursor) {
								if (cursor.moveToFirst()) {
									PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
									final String networkRegion = ((TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE)).getNetworkCountryIso();
									final String defaultRegion = TextUtils.isEmpty(networkRegion) ? "US" : networkRegion.toUpperCase();
									ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
									do {
										ContentValues cvs = new ContentValues();
										cvs.put("contact_id", cursor.getLong(0));
										cvs.put("name", cursor.getString(1));
										final String number = cursor.getString(2);
										cvs.put("number", number);
										try {
											cvs.put("e164", phoneUtil.format(phoneUtil.parse(number, defaultRegion), PhoneNumberUtil.PhoneNumberFormat.E164));
										} catch (NumberParseException e) {
											e.printStackTrace();
										}
										operations.add(ContentProviderOperation.newInsert(CONTENT_URI).withValues(cvs).build());

										if (operations.size() >= 50) {
											try {
												cr.applyBatch("funky", operations);
											} catch (RemoteException e) {
												e.printStackTrace();
											} catch (OperationApplicationException e) {
												e.printStackTrace();
											}
											operations.clear();
										}
									} while (cursor.moveToNext());

									if (operations.size() > 0) {
										try {
											cr.applyBatch("funky", operations);
										} catch (RemoteException e) {
											e.printStackTrace();
										} catch (OperationApplicationException e) {
											e.printStackTrace();
										}
										operations.clear();
									}
								}
								cursor.close();
							}
							return System.currentTimeMillis() - startTime;
						}
						return null;
					}
				})
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<Long>() {
					@Override
					public void call(final Long spendTime) {
						Toast.makeText(getActivity(), mContacts.size() + " contacts spend " + spendTime + "ms", Toast.LENGTH_SHORT).show();
					}
				});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (REQUEST_CODE_READ_CONTACT_REALM == requestCode) {
			syncContactToRealm();
		} else if (REQUEST_CODE_READ_CONTACT_SQLITE == requestCode) {
			syncContactToSqlite();
		}
	}
}
