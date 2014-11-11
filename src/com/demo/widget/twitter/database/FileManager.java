package com.demo.widget.twitter.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

/**
 * Singleton class to manage image saving/fetching to and fro from Sdcard
 * 
 */
public enum FileManager {
	INSTANCE;

	// Image Save path
	private String fileDirPath = "kosta/widget/";
	public static final String SEPARATOR = ",", EXTENSION = ".png";

	/**
	 * @return boolean checking the availability of sdcard
	 */
	public boolean isSDCardAvailable() {
		String sdCardState = Environment.getExternalStorageState();
		if (Environment.MEDIA_CHECKING.equals(sdCardState))
			return false;
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdCardState))
			return false;
		else if (Environment.MEDIA_MOUNTED.equals(sdCardState))
			return true;
		return false;
	}

	/**
	 * @param appWidgetId
	 * @param bitmap
	 * @param idValue
	 * @param dbManager
	 *            Store bitmap to images on folde
	 *            kosta/widget/appwidgetId/listId.png
	 */
	public void storeBitmap(int appWidgetId, Bitmap bitmap, String idValue,
			DatabaseManager dbManager) {
		if (isSDCardAvailable()) {
			String[] idValues = idValue.split(SEPARATOR);
			String fileName = createFileName(appWidgetId, idValues[0]);

			try {
				FileOutputStream fileOutputStream = new FileOutputStream(
						fileName);
				bitmap.compress(Bitmap.CompressFormat.PNG, 95, fileOutputStream);
				fileOutputStream.close();
				fileOutputStream.flush();
				for (int i = 0; i < idValues.length; i++) {
					dbManager.saveFileName(appWidgetId, idValues[i], fileName);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * @param appWidgetId
	 * @return Creating directory name as per appWidgetId
	 */
	private String createDirectoryName(int appWidgetId) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(Environment.getExternalStorageDirectory());
		stringBuilder.append(File.separator);
		stringBuilder.append(fileDirPath);
		stringBuilder.append(Integer.toString(appWidgetId));
		String dirName = stringBuilder.toString();
		stringBuilder.delete(0, stringBuilder.length());
		stringBuilder = null;
		Log.i("directoryName", dirName);
		return dirName;
	}

	/**
	 * @param appWidgetId
	 * @param fileName
	 * @return Create FileName specially images as per listId
	 */
	private String createFileName(int appWidgetId, String fileName) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(createDirectoryName(appWidgetId));
		stringBuilder.append(File.separator);
		stringBuilder.append(fileName);
		stringBuilder.append(EXTENSION);
		fileName = stringBuilder.toString();
		Log.i("FileName", fileName);
		stringBuilder.delete(0, stringBuilder.length());
		stringBuilder = null;
		return fileName;
	}

	/**
	 * @param appWidgetId
	 * @return Method to deleteDirectory as per appWidgetId
	 */
	public File deleteDirectory(int appWidgetId) {
		File file = new File(createDirectoryName(appWidgetId));
		deleteRecursively(file);
		return file;
	}

	public void deleteAndCreateDirectory(int appWidgetId) {
		if (isSDCardAvailable()) {
			Log.i("create directory", "@FileManager");
			File file = new File(createDirectoryName(appWidgetId));
			deleteRecursively(file);
			file.mkdirs();
			Log.i("making file",
					file.getAbsolutePath() + " :: " + file.getPath()
							+ " hollla");
		} else {
			Log.i("unable to make fle", "unable to make file");

		}

	}

	/**
	 * @param directoryOrFile
	 *            Recursively delete folder and its content
	 */
	private void deleteRecursively(File directoryOrFile) {
		if (directoryOrFile.exists()) {
			if (directoryOrFile.isDirectory()) {
				for (File file : directoryOrFile.listFiles())
					deleteRecursively(file);
			}
			directoryOrFile.delete();
		}
	}
}
