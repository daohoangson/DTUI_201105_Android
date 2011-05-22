package com.group5.android.fd.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.group5.android.fd.FdConfig;

abstract public class ImageHelper extends AsyncTask<Void, Void, File> {

	protected static HashMap<String, File> m_cachedFiles = new HashMap<String, File>();
	// where to store in SD card
	final protected static File packageDirectory = new File(Environment
			.getExternalStorageDirectory().toString()
			+ "/Android/data/com.group5.android.fd/cache/");

	protected String imageUrl;

	// get image file from server and save in cache in SD card

	public ImageHelper(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	@Override
	protected File doInBackground(Void... arg0) {
		File file = ImageHelper.getCachedFileUnchecked(imageUrl);

		if (file != null && file.exists() == false) {
			ImageHelper.packageDirectory.mkdirs();

			try {
				// receive image from server
				InputStream in = HttpHelper.getRaw(imageUrl);
				FileOutputStream out = new FileOutputStream(file);

				// create buffer
				byte[] buffer = new byte[4096];
				int bufferTemp = 0;
				// writting

				while ((bufferTemp = in.read(buffer)) > 0) {
					out.write(buffer, 0, bufferTemp);
				}

				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		ImageHelper.m_cachedFiles.put(imageUrl, file);
		return file;

	}

	@Override
	protected void onPostExecute(File cachedFile) {
		onSuccess(cachedFile);
	}

	abstract protected void onSuccess(File cachedFile);

	// get image name from url and create file
	protected static File getCachedFileUnchecked(String url) {
		if (url == null) {
			return null;
		}

		String[] parts = url.split("/");

		return new File(ImageHelper.packageDirectory, parts[parts.length - 1]);
	}

	public static File getCachedFile(String imageUrl) {
		if (imageUrl == null) {
			return null;
		}
		File cachedFile = ImageHelper.m_cachedFiles.get(imageUrl);

		if (cachedFile == null) {
			File file = ImageHelper.getCachedFileUnchecked(imageUrl);

			if (file.exists()) {
				cachedFile = file;

				ImageHelper.m_cachedFiles.put(imageUrl, file);
			}
		}

		return cachedFile;
	}

	public static void removeCachedFiles() {
		String[] files = ImageHelper.packageDirectory.list();
		File file;
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				file = new File(ImageHelper.packageDirectory, files[i]);
				file.delete();

				Log.d(FdConfig.DEBUG_TAG, files[i]);
			}
		}

		ImageHelper.m_cachedFiles.clear();
	}
}
