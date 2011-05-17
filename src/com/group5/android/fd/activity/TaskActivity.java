package com.group5.android.fd.activity;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.group5.android.fd.FdConfig;
import com.group5.android.fd.adapter.TaskAdapter;
import com.group5.android.fd.entity.TaskEntity;
import com.group5.android.fd.helper.HttpRequestAsyncTask;
import com.group5.android.fd.helper.UriStringHelper;

public class TaskActivity extends ListActivity implements
		OnItemClickListener {

	@Override
	public void onResume() {
		super.onResume();

		getTasksAndInitLayoutEverything();
	}

	private void getTasksAndInitLayoutEverything() {
		String tasksUrl = UriStringHelper.buildUriString("tasks");

		new HttpRequestAsyncTask(this, tasksUrl) {

			@Override
			protected Object preProcess(JSONObject jsonObject) {
				List<TaskEntity> taskList = new LinkedList<TaskEntity>();
				try {
					JSONObject tasks = jsonObject.getJSONObject("tasks");
					JSONArray taskIds = tasks.names();
					for (int i = 0; i < taskIds.length(); i++) {
						TaskEntity task = new TaskEntity();
						JSONObject jsonObject2 = tasks.getJSONObject(taskIds
								.getString(i));
						task.parse(jsonObject2);
						taskList.add(task);

					}
				} catch (NullPointerException e) {
					Log.d(FdConfig.DEBUG_TAG, "getTasks got NULL response");
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return taskList;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void process(JSONObject jsonObject, Object preProcessed) {
				if (preProcessed != null && preProcessed instanceof List<?>) {
					initLayout((List<TaskEntity>) preProcessed);
				}
			}

		}.execute();
	}

	protected void initLayout(List<TaskEntity> taskList) {
		TaskAdapter taskAdapter = new TaskAdapter(this, taskList);

		setListAdapter(taskAdapter);

		ListView listView = getListView();
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

	}
}