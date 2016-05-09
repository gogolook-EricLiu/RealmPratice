package com.funky.practice.realm;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.funky.practice.realm.model.Contact;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by gogolook on 5/5/16.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

	private RealmResults<Contact> mContactList;

	public void setContactList(RealmResults<Contact> contactList) {
		mContactList = contactList;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_item, parent, false));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, mContactList.get(position).contactId);
		Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
		Glide.with(holder.mIconView.getContext())
				.load(photoUri)
				.placeholder(R.mipmap.ic_launcher)
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.into(holder.mIconView);
		holder.mNameView.setText(mContactList.get(position).name);
	}

	@Override
	public int getItemCount() {
		return null == mContactList || !mContactList.isValid() || !mContactList.isLoaded() ? 0 : mContactList.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		@BindView(android.R.id.icon)
		ImageView mIconView;
		@BindView(android.R.id.text1)
		TextView mNameView;

		public ViewHolder(View view) {
			super(view);
			ButterKnife.bind(this, view);
		}
	}
}
