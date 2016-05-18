package com.funky.practice.realm;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by gogolook on 5/18/16.
 */
public class RecyclerViewFragment extends Fragment implements MainActivity.OnClickFabListener {

	private Unbinder mUnbinder;
	private RandomNumberAdapter mAdapter;

	@BindView(android.R.id.list)
	RecyclerView mRecyclerView;

	public RecyclerViewFragment() {
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.common_fragment, container, false);
		mUnbinder = ButterKnife.bind(this, view);

		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()) {
			@Override
			public RecyclerView.LayoutParams generateDefaultLayoutParams() {
				return super.generateDefaultLayoutParams();
			}
		});
		mAdapter = new RandomNumberAdapter();
		mRecyclerView.setAdapter(mAdapter);

		mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
			private final GestureDetector mGestureDetector = new GestureDetector(getContext(),
					new GestureDetector.SimpleOnGestureListener() {
						@Override
						public boolean onSingleTapUp(MotionEvent e) {
							return true;
						}

						@Override
						public void onLongPress(MotionEvent e) {
							super.onLongPress(e);

							final View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
							Toast.makeText(getContext(), ((TextView) view.findViewById(android.R.id.text1)).getText(), Toast.LENGTH_SHORT).show();
						}
					});

			@Override
			public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
				if (mGestureDetector.onTouchEvent(e)) {
					final View view = rv.findChildViewUnder(e.getX(), e.getY());
					if (null != view) {
						mAdapter.removeItem(rv.getChildAdapterPosition(view));
						return true;
					}
				}
				return false;
			}

			@Override
			public void onTouchEvent(RecyclerView rv, MotionEvent e) {

			}

			@Override
			public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

			}
		});
		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mUnbinder.unbind();
	}

	@Override
	public void onClickFab() {
		if (null != mAdapter) {
			mAdapter.insertItem(0, (new Random()).nextInt(1000));
			mRecyclerView.scrollToPosition(0);
		}
	}

	static class RandomNumberAdapter extends RecyclerView.Adapter<RandomNumberAdapter.ViewHolder> {

		private List<Integer> mList;

		public RandomNumberAdapter() {
			mList = new ArrayList<>();
			Random random = new Random();
			for (int i = 0; i < 10; i++) {
				mList.add(random.nextInt(1000));
			}
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_item, parent, false));
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			holder.mIconView.setImageResource(R.mipmap.ic_launcher);
			holder.mNameView.setText("" + mList.get(position));
		}

		@Override
		public int getItemCount() {
			return mList.size();
		}

		public void insertItem(int position, int value) {
			notifyItemInserted(position);
			mList.add(position, value);
		}

		public void removeItem(int position) {
			if (position < mList.size()) {
				mList.remove(position);
				notifyItemRemoved(position);
			}
		}

		static class ViewHolder extends RecyclerView.ViewHolder {
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
}
