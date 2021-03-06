package com.group5.android.fd.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.group5.android.fd.FdConfig;
import com.group5.android.fd.Main;
import com.group5.android.fd.R;
import com.group5.android.fd.adapter.TaskAdapter;
import com.group5.android.fd.entity.TaskEntity;
import com.group5.android.fd.entity.UserEntity;
import com.group5.android.fd.service.TaskUpdaterService;
import com.group5.android.fd.service.TaskUpdaterServiceReceiver;
import com.group5.android.fd.view.TaskGroupView;

/**
 * The activity to display a list of tasks
 * 
 * @author Tran Viet Son
 * 
 */
public class TaskListActivity extends ServerBasedActivity {

	final public static String EXTRA_DATA_NAME_TASK_OBJ = "taskObj";

	protected UserEntity m_user;
	protected TaskAdapter m_taskAdapter;
	protected View m_vwSelected = null;
	public static boolean showAll = false;

	protected BroadcastReceiver m_broadcastReceiverForNewTask = null;
	protected PowerManager.WakeLock wakeLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		m_user = (UserEntity) intent
				.getSerializableExtra(Main.EXTRA_DATA_NAME_USER_OBJ);

		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				FdConfig.DEBUG_TAG);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// we want to preserve our order information when configuration is
		// change, say.. orientation change?
		return m_taskAdapter.getTaskList();
	}

	@Override
	protected void onResume() {
		super.onResume();

		getTasksAndInitLayoutEverything();

		wakeLock.acquire();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// unbind the TaskUpdaterService
		try {
			unbindService(m_taskAdapter);
		} catch (IllegalArgumentException e) {
			// for some reason the server hasn't started yet
			// that will trigger this exception
			// because that happened in Main, I fixed it here too
			// just in case, who knows, right?
		}

		if (m_broadcastReceiverForNewTask != null) {
			unregisterReceiver(m_broadcastReceiverForNewTask);
			m_broadcastReceiverForNewTask = null;
		}

		wakeLock.release();
	}

	/**
	 * Initiates the layout (inflate from a layout resource named
	 * activity_main). And then maps all the object properties with their view
	 * instance. Finally, initiates required listeners on those views.
	 * 
	 * @param taskList
	 *            a <code>List</code> of {@link TaskEntity} to pre-populate the
	 *            list
	 */
	protected void initLayout(List<TaskEntity> taskList) {
		m_taskAdapter = new TaskAdapter(this, m_user, taskList);
		setListAdapter(m_taskAdapter);

		// start our service
		Intent service = new Intent(this, TaskUpdaterService.class);
		bindService(service, m_taskAdapter, Context.BIND_AUTO_CREATE);

		// listen to the service intent
		m_broadcastReceiverForNewTask = new TaskUpdaterServiceReceiver(this) {

			@Override
			protected void onReceive(Context context, TaskEntity task) {
				m_taskAdapter.addTask(task);
			}

		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.task_list, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.menu_task_list_show_all);

		if (TaskListActivity.showAll) {
			item.setIcon(R.drawable.checkbox_on);
		} else {
			item.setIcon(R.drawable.checkbox_off);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_task_list_refresh:
			m_taskAdapter.clear();
			return true;
		case R.id.menu_task_list_show_all:
			TaskListActivity.showAll = !TaskListActivity.showAll;
			m_taskAdapter.clear();
			return true;
		}

		return false;
	}

	/**
	 * Gets the pending tasks for current user and set them up.
	 */
	@SuppressWarnings("unchecked")
	protected void getTasksAndInitLayoutEverything() {
		Object lastNonConfigurationInstance = getLastNonConfigurationInstance();
		List<TaskEntity> taskList = null;
		if (lastNonConfigurationInstance != null
				&& lastNonConfigurationInstance instanceof List<?>) {
			// found our long lost task list, yay!
			taskList = (List<TaskEntity>) lastNonConfigurationInstance;

			Log.i(FdConfig.DEBUG_TAG, "List<TaskEntity> has been recovered");
		}

		if (taskList == null) {
			// no old task list is found
			// pass an empty one
			initLayout(new ArrayList<TaskEntity>());
		} else {
			// init the layout with existing task list
			initLayout(taskList);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (arg1 instanceof TaskGroupView) {
			((TaskGroupView) arg1).expandTasks();
		}

		if (m_vwSelected != null && m_vwSelected instanceof TaskGroupView
				&& m_vwSelected != arg1) {
			((TaskGroupView) m_vwSelected).collapseTasks();
		}

		m_vwSelected = arg1;
	}
}