package eu.ammw.android.targetpractice;


import eu.ammw.android.targetpractice.util.TargetPracticeLogic;
import eu.ammw.android.targetpractice.util.TargetPracticeUtils;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TargetPracticeActivity extends FragmentActivity implements ActionBar.OnNavigationListener {
	static final String KEY_LOGIC = "Logic"; 
	
	private static final String LOG_TAG = "TP-Main";
	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	
	private TargetPracticeLogic logic;
	
	private View.OnLongClickListener recordListener = new View.OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			onInputSeriesButtonClicked(v);
			LinearLayout parent = ((LinearLayout)v.getParent());
			int index = parent.indexOfChild(v);
			parent.removeView(v);
			logic.remove(index, true);
			((TextView)findViewById(R.id.scoreTotalValueView)).setText(String.valueOf(logic.getTotal()));
			// Load series:
			LinearLayout seriesLayout = ((LinearLayout)findViewById(R.id.seriesInternalLayout));
			for (Integer value : logic.getCurrentSeries()) {
				Button butt = TargetPracticeUtils.buttonFromLabel(value.toString(), TargetPracticeActivity.this);
				butt.setOnLongClickListener(hitListener);
				seriesLayout.addView(butt);
			}
			findViewById(R.id.seriesButton).setEnabled(true);
			return true;
		}
	};
	
	private View.OnLongClickListener hitListener = new View.OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
											
			LinearLayout parent = (LinearLayout)v.getParent();
			int index = parent.indexOfChild(v);
			logic.remove(index, false);
			parent.removeView(v);
			if (!logic.canSubmitSeries()) {
				findViewById(R.id.seriesButton).setEnabled(false);
				if(!logic.canSubmitTraining())
					findViewById(R.id.finishButton).setEnabled(false);
			}
			return true;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tp_main);
		if(savedInstanceState != null && savedInstanceState.containsKey(KEY_LOGIC))
			this.logic = (TargetPracticeLogic)savedInstanceState.getSerializable(KEY_LOGIC);
		else logic = new TargetPracticeLogic(this);
		
		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
				// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(
						actionBar.getThemedContext(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1,
						new String[] {
							getString(R.string.title_section_input),	// 0
							getString(R.string.title_section_history),	// 1
						}),
						this);
	}


	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
		
		// Restore serialized results state:
		if (savedInstanceState.containsKey(KEY_LOGIC)) {
			logic = (TargetPracticeLogic)savedInstanceState.getSerializable(KEY_LOGIC);
		} else logic = new TargetPracticeLogic(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
				getActionBar().getSelectedNavigationIndex());
		// Serialize results state.
		outState.putSerializable(KEY_LOGIC, logic);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.score_main, menu);
		return true;
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the
		// container view.
		Fragment fragment = new TargetPracticeFragment();
		getSupportFragmentManager().beginTransaction()
			.replace(R.id.container, fragment)
			.commit();
		return true;
	}
	
	public void onInputSeriesButtonClicked(View view) {
		Log.d(LOG_TAG, "series button clicked");
		if (!logic.canSubmitSeries()) return;
		final int sum = logic.endSeries();
		((TextView)findViewById(R.id.scoreTotalValueView)).setText(String.valueOf(logic.getTotal()));
		((LinearLayout)findViewById(R.id.seriesInternalLayout)).removeAllViews();
		Button butt = TargetPracticeUtils.buttonFromLabel(String.valueOf(sum), this);
		butt.setOnLongClickListener(recordListener);
		((LinearLayout)findViewById(R.id.resultsInternalLayout)).addView(butt);
		((HorizontalScrollView)findViewById(R.id.resultsScrollView)).fullScroll(View.FOCUS_RIGHT);
		findViewById(R.id.seriesButton).setEnabled(false);
	}
	
	public void onInputFinishButtonClicked(View view) {
		Log.d(LOG_TAG, "end training button clicked");
		if (!logic.canSubmitTraining()) return;
		logic.finishTraining();
		((TextView)findViewById(R.id.scoreTotalValueView)).setText(R.string.zero);
		((LinearLayout)findViewById(R.id.seriesInternalLayout)).removeAllViews();
		((LinearLayout)findViewById(R.id.resultsInternalLayout)).removeAllViews();
		Toast.makeText(TargetPracticeActivity.this, getString(R.string.message_training_saved),
				Toast.LENGTH_SHORT).show();
		findViewById(R.id.finishButton).setEnabled(false);
		findViewById(R.id.seriesButton).setEnabled(false);
	}
	
	TargetPracticeLogic getLogic() {
		return logic;
	}
	
	View.OnLongClickListener getHitListener() {
		return hitListener;
	}
	
	View.OnLongClickListener getRecordListener() {
		return recordListener;
	}
}
