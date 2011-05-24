package com.group5.android.fd.view;

import java.util.Iterator;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.group5.android.fd.FdConfig;
import com.group5.android.fd.R;
import com.group5.android.fd.entity.AbstractEntity;
import com.group5.android.fd.entity.TaskEntity;
import com.group5.android.fd.entity.TaskGroupEntity;
import com.group5.android.fd.entity.UserEntity;

/**
 * A view for {@link TaskGroupEntity}
 * 
 * @author Dao Hoang Son
 * 
 */
public class TaskGroupView extends LinearLayout implements
		OnCheckedChangeListener {

	protected static int m_expandedGroupId = 0;

	protected Context m_context;
	protected TextView m_vwTaskGroupName;
	protected TextView m_vwTaskGroupInfo;
	protected CheckBox m_vwGroupCompleted;
	protected LinearLayout m_vwTaskGroupInfoContainter;
	protected LinearLayout m_vwTasks;

	protected UserEntity m_user;
	public TaskGroupEntity group;

	/**
	 * Constructs itself. Get references of subviews.
	 * 
	 * @param context
	 * @param user
	 * @param group
	 */
	public TaskGroupView(Context context, UserEntity user, TaskGroupEntity group) {
		super(context);

		m_context = context;
		m_user = user;

		LayoutInflater li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		li.inflate(R.layout.view_task_group, this, true);

		m_vwTaskGroupName = (TextView) findViewById(R.id.txtTaskGroupName);
		m_vwTaskGroupInfo = (TextView) findViewById(R.id.txtTaskGroupInfo);
		m_vwGroupCompleted = (CheckBox) findViewById(R.id.chkGroupCompleted);
		m_vwGroupCompleted.setOnCheckedChangeListener(this);
		m_vwTaskGroupInfoContainter = (LinearLayout) findViewById(R.id.llTaskGroupInfoContainer);
		m_vwTasks = (LinearLayout) findViewById(R.id.llTasks);

		setGroup(group);
	}

	/**
	 * Setup the view to display a new {@link TaskGroupEntity}
	 * 
	 * @param group
	 */
	public void setGroup(TaskGroupEntity group) {
		this.group = group;

		m_vwTasks.removeAllViews();

		StringBuilder sb = new StringBuilder();
		boolean someAreWaiting = false;
		boolean someAreNotCompleted = false;

		Iterator<TaskEntity> iterator = group.tasks.iterator();
		while (iterator.hasNext()) {
			TaskEntity task = iterator.next();

			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(task.itemName);

			if (!task.isSynced(AbstractEntity.TARGET_ALL)) {
				someAreWaiting = true;
			}
			if (!task.isCompleted(m_user)) {
				someAreNotCompleted = true;
			}

			TaskView taskView = new TaskView(m_context, m_user, task);
			m_vwTasks.addView(taskView);
		}

		m_vwTaskGroupName.setText(sb.toString());
		m_vwTaskGroupInfo.setText(group.getInfo(m_context));
		m_vwGroupCompleted.setEnabled(!someAreWaiting);
		m_vwGroupCompleted.setChecked(!someAreNotCompleted);

		if (group.tasks.size() > 1) {
			m_vwTaskGroupInfoContainter.setVisibility(View.VISIBLE);
		} else {
			m_vwTaskGroupInfoContainter.setVisibility(View.GONE);
		}

		if (TaskGroupView.m_expandedGroupId == group.groupId) {
			expandTasks();
		} else {
			collapseTasks();
		}
	}

	/**
	 * Expands the list of subtasks
	 */
	public void expandTasks() {
		TaskGroupView.m_expandedGroupId = group.groupId;

		m_vwTasks.setVisibility(View.VISIBLE);

		Log.d(FdConfig.DEBUG_TAG, "TaskGroupView.expandTasks(): #"
				+ group.groupId);

		m_vwTasks.postInvalidate();
	}

	/**
	 * Collapses the list of subtasks
	 */
	public void collapseTasks() {
		if (TaskGroupView.m_expandedGroupId == group.groupId) {
			TaskGroupView.m_expandedGroupId = 0;
		}

		if (group.tasks.size() > 1) {
			m_vwTasks.setVisibility(View.GONE);
		} else {
			m_vwTasks.setVisibility(View.VISIBLE);
		}

		Log.d(FdConfig.DEBUG_TAG, "TaskGroupView.collapseTasks(): #"
				+ group.groupId);

		m_vwTasks.postInvalidate();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked != group.isCompleted(m_user)) {
			if (isChecked == true) {
				group.markCompleted(m_context, m_user.csrfToken);
			} else {
				group.revertCompleted(m_context, m_user.csrfToken);
			}
		}
	}
}
