package what.whatandroid.torrentgroup.group;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

import api.torrents.torrents.TorrentGroup;
import what.whatandroid.R;
import what.whatandroid.WhatApplication;
import what.whatandroid.callbacks.LoadingListener;
import what.whatandroid.torrentgroup.TorrentGroupActivity;

/**
 * Fragment for showing swipeable views of the torrent group overview and comments
 */
public class TorrentGroupFragment extends Fragment implements LoadingListener<TorrentGroup> {
	private TorrentGroupPagerAdapter torrentGroupPagerAdapter;
	private TorrentGroup group;
	/**
	 * Track the bookmark menu item so we can change the icon depending on the group's bookmark
	 * status and hide it if we haven't loaded yet
	 */
	private MenuItem bookmarkMenu;

	public static TorrentGroupFragment newInstance(int groupId) {
		TorrentGroupFragment f = new TorrentGroupFragment();
		Bundle args = new Bundle();
		args.putInt(TorrentGroupActivity.GROUP_ID, groupId);
		f.setArguments(args);
		return f;
	}

	public TorrentGroupFragment() {
		//Required empty ctor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		int groupId = getArguments().getInt(TorrentGroupActivity.GROUP_ID);
		View view = inflater.inflate(R.layout.fragment_view_pager_tabs, container, false);
		ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
		PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
		torrentGroupPagerAdapter = new TorrentGroupPagerAdapter(getChildFragmentManager(), groupId);
		viewPager.setAdapter(torrentGroupPagerAdapter);
		tabs.setViewPager(viewPager);
		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.torrent_group, menu);
		bookmarkMenu = menu.findItem(R.id.action_bookmark);
		if (group != null) {
			if (group.getResponse().getGroup().isBookmarked()) {
				bookmarkMenu.setIcon(R.drawable.ic_bookmark_24dp);
			} else {
				bookmarkMenu.setIcon(R.drawable.ic_bookmark_border_24dp);
			}
		} else {
			bookmarkMenu.setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_bookmark) {
			new ToggleBookmarkTask().execute();
			return true;
		}else if (item.getItemId() == R.id.action_share){
			int id  = group.getId();
			ClipboardManager clipboard = (ClipboardManager)
					getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("torrent group url", WhatApplication.DEFAULT_SITE + "/torrents.php?id=" + id);
					Toast.makeText(getActivity(), "Link saved to clipboard", Toast.LENGTH_LONG).show();
			clipboard.setPrimaryClip(clip);

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onLoadingComplete(TorrentGroup data) {
		group = data;
		torrentGroupPagerAdapter.onLoadingComplete(group);
		if (bookmarkMenu != null) {
			bookmarkMenu.setVisible(true);
			if (group.getResponse().getGroup().isBookmarked()) {
				bookmarkMenu.setIcon(R.drawable.ic_bookmark_24dp);
			} else {
				bookmarkMenu.setIcon(R.drawable.ic_bookmark_border_24dp);
			}
		}
	}

	/**
	 * Async task to toggle the torrent group's bookmark status
	 */
	private class ToggleBookmarkTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			if (group.getResponse().getGroup().isBookmarked()) {
				return group.removeBookmark();
			}
			return group.addBookmark();
		}

		@Override
		protected void onPreExecute() {
			//Be optimistic that the change will succeed and show updated icon
			if (group.getResponse().getGroup().isBookmarked()) {
				bookmarkMenu.setIcon(R.drawable.ic_bookmark_border_24dp);
			} else {
				bookmarkMenu.setIcon(R.drawable.ic_bookmark_24dp);
			}
			if (isAdded()) {
				getActivity().setProgressBarIndeterminate(true);
				getActivity().setProgressBarIndeterminateVisibility(true);
			}
		}

		@Override
		protected void onPostExecute(Boolean status) {
			if (isAdded()) {
				getActivity().setProgressBarIndeterminate(false);
				getActivity().setProgressBarIndeterminateVisibility(false);
			}
			if (!status) {
				if (group.getResponse().getGroup().isBookmarked()) {
					Toast.makeText(getActivity(), "Could not remove bookmark", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getActivity(), "Could not add bookmark", Toast.LENGTH_LONG).show();
				}
			}
			if (bookmarkMenu != null) {
				bookmarkMenu.setVisible(true);
				if (group.getResponse().getGroup().isBookmarked()) {
					bookmarkMenu.setIcon(R.drawable.ic_bookmark_24dp);
				} else {
					bookmarkMenu.setIcon(R.drawable.ic_bookmark_border_24dp);
				}
			}
		}
	}
}
