package com.group5.android.fd.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.group5.android.fd.FdConfig;
import com.group5.android.fd.helper.HttpRequestAsyncTask;
import com.group5.android.fd.helper.UriStringHelper;

public class OrderEntity extends AbstractEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2436126416686043271L;

	// an OrderEntity has information about table and items for this table
	public int orderId = 0;
	public TableEntity table = null;
	public ArrayList<OrderItemEntity> orderItems = new ArrayList<OrderItemEntity>();

	public void setTable(TableEntity table) {
		this.table = table;

		Log.i(FdConfig.DEBUG_TAG, "Order.setTable: " + table.tableName + " ("
				+ table.tableId + ")");

		selfInvalidate(TARGET_REMOTE_SERVER);
	}

	public int getTableId() {
		if (table != null) {
			return table.tableId;
		} else {
			return 0;
		}
	}

	public String getTableName() {
		if (table != null) {
			return table.tableName;
		} else {
			return "";
		}
	}

	public boolean setOrderItemQuantity(OrderItemEntity orderItem, int quantity) {
		int position = orderItems.indexOf(orderItem);

		// add if exists and more than 0, remove if exists and less than 0
		if (position > -1) {
			if (quantity <= 0) {
				orderItems.remove(position);
			} else {
				OrderItemEntity order = orderItems.get(position);
				order.quantity = quantity;
			}

			selfInvalidate(AbstractEntity.TARGET_REMOTE_SERVER);

			return true;
		} else {
			return false;
		}
	}

	/*
	 * add OrderItem
	 */
	public void addOrderItem(OrderItemEntity newItem) {
		if (newItem.itemId > 0 && newItem.quantity > 0) {
			// check if duplicate add more
			Iterator<OrderItemEntity> iterator = orderItems.iterator();
			OrderItemEntity existingItem = null;
			OrderItemEntity duplicateItem = null;

			while (iterator.hasNext()) {
				existingItem = iterator.next();

				if (existingItem.itemId == newItem.itemId) {
					duplicateItem = existingItem;
					break; // get better performance here, a little
				}
			}

			if (duplicateItem != null) {
				duplicateItem.quantity += newItem.quantity;
			} else {
				orderItems.add(newItem);
			}

			Log.i(FdConfig.DEBUG_TAG,
					"Order.addItem: " + newItem.itemName + " (#"
							+ newItem.itemId + ", quantity: "
							+ newItem.quantity + ", total items now: "
							+ orderItems.size() + ")");

			selfInvalidate(AbstractEntity.TARGET_REMOTE_SERVER);
		} else {
			// do nothing
		}
	}

	public void addItem(ItemEntity item) {
		OrderItemEntity orderItem = new OrderItemEntity();
		orderItem.setup(item, 1);

		addOrderItem(orderItem);
	}

	/*
	 * return a List for post to server
	 */
	protected List<NameValuePair> getOrderAsParams() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		if (table != null) {
			params.add(new BasicNameValuePair("table_id", String
					.valueOf(table.tableId)));
		}

		if (!orderItems.isEmpty()) {
			Iterator<OrderItemEntity> i = orderItems.iterator();
			int count = 0;
			while (i.hasNext()) {
				OrderItemEntity orderItem = i.next();
				// if quantity is n, add n item which amount is one
				for (int j = 0; j < orderItem.quantity; j++) {
					params.add(new BasicNameValuePair("item_ids[" + count++
							+ "]", String.valueOf(orderItem.itemId)));
				}
			}
		}

		return params;
	}

	public void submit(Context context, String csrfToken) {
		String newOrderUrl = UriStringHelper.buildUriString("new-order");
		List<NameValuePair> params = getOrderAsParams();

		new HttpRequestAsyncTask(context, newOrderUrl, csrfToken, params) {

			@Override
			protected Object process(JSONObject jsonObject) {
				try {
					JSONObject order = jsonObject.getJSONObject("order");
					orderId = order.getInt("order_id");

					return orderId > 0;
				} catch (JSONException e) {
					// invalid response from server!
				}

				return false;
			}

			@Override
			protected void onSuccess(JSONObject jsonObject, Object processed) {
				boolean confirmed = false;

				if (processed != null && processed instanceof Boolean) {
					confirmed = (Boolean) processed;
				}

				if (confirmed) {
					onUpdated(AbstractEntity.TARGET_REMOTE_SERVER);
				} else {
					selfInvalidate(AbstractEntity.TARGET_REMOTE_SERVER);
				}
			}
		}.execute();
	}

	/*
	 * calculate total price
	 */
	public double getPriceTotal() {
		double total = 0;

		Iterator<OrderItemEntity> iterator = orderItems.iterator();
		OrderItemEntity item = null;

		while (iterator.hasNext()) {
			item = iterator.next();
			total += item.quantity * item.price;
		}

		return total;
	}
}
