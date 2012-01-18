package what.user;

import java.text.DecimalFormat;

import what.gui.ImageLoader;
import what.gui.MyActivity;
import what.gui.R;
import what.torrents.artist.ArtistTabActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import api.user.User;
import api.util.Tuple;

public class UserProfilePopUpActivity extends MyActivity {
	private Intent intent;
	private int userId;
	private User user;
	private ProgressDialog dialog;
	private TextView username, userclass, uploaded, downloaded, ratio, posts;
	private Bitmap bmp;
	private ImageView userImage;
	private DecimalFormat df = new DecimalFormat("#.00");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.userprofilepopup);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.dimAmount = 0.0f;
		getWindow().setAttributes(lp);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

		userImage = (ImageView) this.findViewById(R.id.userImage);

		username = (TextView) this.findViewById(R.id.username);
		userclass = (TextView) this.findViewById(R.id.userclass);
		uploaded = (TextView) this.findViewById(R.id.uploaded);
		downloaded = (TextView) this.findViewById(R.id.downloaded);
		ratio = (TextView) this.findViewById(R.id.ratio);
		posts = (TextView) this.findViewById(R.id.posts);

		getBundle();
		new LoadUser().execute();
	}

	private void getBundle() {
		Bundle b = this.getIntent().getExtras();
		try {
			userId = b.getInt("userId");
		} catch (Exception e) {
			finish();
			Toast.makeText(this, "Could not get user id", Toast.LENGTH_LONG).show();
		}
	}

	private void populateLayout() {
		username.setText(user.getProfile().getUsername());
		userclass.setText(user.getProfile().getPersonal().getUserClass());
		uploaded.setText("U: " + toGBString(user.getProfile().getRanks().getUploaded().doubleValue()) + "GB");
		downloaded.setText("D: " + toGBString(user.getProfile().getRanks().getDownloaded().doubleValue()) + "GB");
		ratio.setText("R: " + user.getProfile().getStats().getRatio().toString());
		posts.setText("P: " + user.getProfile().getCommunity().getPosts().toString());
	}

	public void message(View v) {
		Bundle b = new Bundle();
		intent = new Intent(UserProfilePopUpActivity.this, what.inbox.NewConversationActivity.class);
		b.putInt("userId", userId);
		intent.putExtras(b);
		startActivityForResult(intent, 0);

	}

	public void more(View v) {
		Bundle b = new Bundle();
		intent = new Intent(UserProfilePopUpActivity.this, what.user.UserProfileActivity.class);
		b.putInt("userId", userId);
		intent.putExtras(b);
		startActivityForResult(intent, 0);
	}

	@Override
	public void onPause() {
		try {
			bmp.recycle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
		try {
			bmp.recycle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	private class LoadUser extends AsyncTask<Void, Void, Tuple<Boolean, Boolean>> {
		@Override
		protected void onPreExecute() {
			lockScreenRotation();
			dialog = new ProgressDialog(UserProfilePopUpActivity.this);
			dialog.setIndeterminate(true);
			dialog.setMessage("Loading...");
			dialog.show();
		}

		@Override
		protected Tuple<Boolean, Boolean> doInBackground(Void... params) {
			// user status, image status
			Tuple<Boolean, Boolean> status = new Tuple<Boolean, Boolean>(null, null);
			user = User.userFromId(userId);
			status.setA(user.getStatus());

			try {
				String s = ArtistTabActivity.getArtist().getResponse().getImage();
				bmp = ImageLoader.loadBitmap(s);
				status.setB(true);
			} catch (Exception e) {
				status.setB(false);
			}
			return status;
		}

		@Override
		protected void onPostExecute(Tuple<Boolean, Boolean> status) {
			if (status.getA() == true) {
				populateLayout();
			}
			if (status.getB() == true) {
				userImage.setImageBitmap(bmp);
			}
			if (status.getA() == false) {
				Toast.makeText(UserProfilePopUpActivity.this, "Could not load user profile", Toast.LENGTH_LONG).show();
			}
			if (status.getB() == false) {
				userImage.setImageResource(R.drawable.dne);
			}
			dialog.dismiss();
			unlockScreenRotation();
		}
	}
}
