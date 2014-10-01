package com.hiwifi.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.provider.MediaStore.Images;

import com.hiwifi.constant.ConfigConstant;
import com.hiwifi.hiwifi.Gl;
import com.hiwifi.model.log.HWFLog;

public class FileUtil {
	private static final String TAG = "FileUtil";

	private FileUtil() {
	}

	public native int insertFivebytes(char in, char out);

	public native int removeFivebytes(char in, char out);

	public native static String stringFromJNI();


	/**
	 * 新建目录
	 * 
	 * @param folderPath
	 *            String 如 c:/fqf
	 * @return boolean
	 */
	public static void newFolder(String folderPath) {
		try {
			java.io.File myFilePath = new java.io.File(folderPath);
			if (!myFilePath.exists()) {
				myFilePath.mkdirs();
			}
		} catch (Exception e) {
			HWFLog.e(TAG, "newFolder Exception ", e);
		}
	}

	/**
	 * 新建文件
	 * 
	 * @param filePathAndName
	 *            String 文件路径及名称 如c:/fqf.txt
	 * @param fileContent
	 *            String 文件内容
	 * @return boolean
	 */
	public static void newFile(String filePathAndName, String fileContent) {

		try {
			File myFilePath = new File(filePathAndName);
			if (!myFilePath.exists()) {
				myFilePath.createNewFile();
			}

			if (fileContent != null && !fileContent.equals("")) {
				FileWriter resultFile = new FileWriter(myFilePath);
				PrintWriter myFile = new PrintWriter(resultFile);
				myFile.println(fileContent);
				resultFile.close();
			}
		} catch (Exception e) {
			HWFLog.e(TAG, "newFile Exception ", e);
		}
	}

	/**
	 * delete system file.
	 */
	public static void delSysFile(Context context, String fileName) {
		context.deleteFile(fileName);
	}

	/**
	 * 删除文件
	 * 
	 * @param filePathAndName
	 *            String 文件路径及名称 如c:/fqf.txt
	 * @param fileContent
	 *            String
	 * @return boolean
	 */
	public static boolean delFile(String filePathAndName) {
		try {
			java.io.File myDelFile = new java.io.File(filePathAndName);
			myDelFile.delete();
			return true;
		} catch (Exception e) {
			HWFLog.e(TAG, "delFile Exception ", e);
			return false;
		}
	}

	/**
	 * 删除文件夹
	 * 
	 * @param filePathAndName
	 *            String 文件夹路径及名称 如c:/fqf
	 * @param fileContent
	 *            String
	 * @return boolean
	 */
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			java.io.File myFilePath = new java.io.File(folderPath);
			myFilePath.delete(); // 删除空文件夹
		} catch (Exception e) {
			HWFLog.e(TAG, "delFolder Exception ", e);
		}
	}

	/**
	 * 删除文件夹里面的所有文件
	 * 
	 * @param path
	 *            String 文件夹路径 如 c:/fqf
	 */
	public static boolean delAllFile(String path) {
		File file = new File(path);
		if (!file.exists() || !file.isDirectory()) {
			return true;
		}

		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);// 再删除空文件夹
			}
		}
		return true;
	}

	/**
	 * 复制单个文件
	 * 
	 * @param srcFile
	 *            String 原文件路径 如：c:/fqf.txt
	 * @param destFile
	 *            String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public static boolean copyFile(String srcFile, String destFile) {
		boolean ok = true;
		FileInputStream fis = null;
		FileOutputStream fos = null;

		try {
			if (new File(srcFile).exists()) {
				fis = new FileInputStream(srcFile);
				fos = new FileOutputStream(destFile);
				byte[] buffer = new byte[ConfigConstant.BUFFER_SIZE];
				int count = 0;
				while ((count = fis.read(buffer)) != -1) {
					fos.write(buffer, 0, count);
				}

				fos.flush();
			}
		} catch (Exception e) {
			HWFLog.e(TAG, "copyFile Exception ", e);
			ok = false;
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
			}
		}

		return ok;
	}

	public static boolean copyFileFromRaw(InputStream in) {
		boolean ok = true;
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(new File(Gl.Ct().getFilesDir(),
					"mojicert.cer"));

			byte[] buffer = new byte[ConfigConstant.BUFFER_SIZE];
			int count = 0;
			while ((count = in.read(buffer)) != -1) {
				fos.write(buffer, 0, count);
			}

			fos.flush();
		} catch (Exception e) {
			HWFLog.e(TAG, "copyFile Exception ", e);
			ok = false;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
			}
		}

		return ok;
	}

	/**
	 * 复制整个文件夹内容
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf/ff
	 * @return boolean
	 */
	public static void copyFolder(String oldPath, String newPath) {

		try {
			(new File(newPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + file[i]);
				} else {
					temp = new File(oldPath + File.separator + file[i]);
				}

				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath
							+ "/" + (temp.getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// 如果是子文件夹
					copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
				}
			}
		} catch (Exception e) {
			HWFLog.e(TAG, "copyFolder  Exception ", e);

		}

	}

	/**
	 * 移动文件到指定目录
	 * 
	 * @param oldPath
	 *            String 如：c:/fqf.txt
	 * @param newPath
	 *            String 如：d:/fqf.txt
	 */
	// public void moveFile(String oldPath, String newPath) {
	// copyFile(oldPath, newPath);
	// delFile(oldPath);
	//
	// }

	/**
	 * 移动文件到指定目录
	 * 
	 * @param oldPath
	 *            String 如：c:/fqf.txt
	 * @param newPath
	 *            String 如：d:/fqf.txt
	 */
	// public void moveFolder(String oldPath, String newPath) {
	// copyFolder(oldPath, newPath);
	// delFolder(oldPath);
	//
	// }

	// public static boolean isHaveFile(String filePath,String fileName){
	// try{
	// StringBuilder sb = new StringBuilder();
	// sb.append(filePath);
	// sb.append("/");
	// sb.append(fileName);
	// String absolutePath = sb.toString();
	// File file = new File(absolutePath);
	// if(file.exists()){
	// return true;
	// }else{
	// return false;
	// }
	// }catch(Exception e){
	// MojiLog.e(TAG, "isHaveFile  Exception ", e);
	// return false;
	// }
	// }

	/**
	 * 是否存在此文件
	 * 
	 * @param filePath
	 *            c:/moji/ad.jpg
	 * @return
	 */
	public static boolean isHaveFile(String filePath) {
		try {
			return new File(filePath).exists();
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean savePrivateFile(String fileName, String content) {
		OutputStreamWriter os = null;
		FileOutputStream fos = null;
		boolean result = false;

		try {
			File file = Gl.Ct().getFileStreamPath(fileName);
			if (file.exists()) {
				file.delete();
			}

			fos = Gl.Ct().openFileOutput(fileName, Context.MODE_PRIVATE);
			os = new OutputStreamWriter(fos, ConfigConstant.ENCODING_UTF_8);
			os.write(content);
			os.flush();
			result = true;
		} catch (Exception e) {
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}

		return result;
	}

	public static byte[] readImageFile(String filename) throws Exception {
		BufferedInputStream bufferedInputStream = null;
		try {
			bufferedInputStream = new BufferedInputStream(new FileInputStream(
					filename));
			int len = bufferedInputStream.available();
			byte[] bytes = new byte[len];
			int r = bufferedInputStream.read(bytes);
			if (len != r) {
				bytes = null;
				// throw new IOException("读取文件不正确");
			}
			return bytes;
		} catch (Exception e) {
			throw e;
		} finally {
			if (bufferedInputStream != null)
				bufferedInputStream.close();
		}

	}


	private static final String IMAGE_MIME_TYPE = "image/jpeg";

	private static void insetData2Dcim(File file) {
		try {
			ContentValues values = new ContentValues(6);

			values.put(Images.Media.TITLE, file.getName());
			values.put(Images.Media.DISPLAY_NAME, System.currentTimeMillis());
			values.put(Images.Media.DATE_TAKEN, file.lastModified());
			values.put(Images.Media.MIME_TYPE, IMAGE_MIME_TYPE);
			values.put(Images.Media.DATA, file.getAbsolutePath());
			values.put(Images.Media.SIZE, file.length());

			Gl.Ct().getContentResolver()
					.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
		} catch (Exception e) {
			HWFLog.e(TAG, e.toString(), e);
		}

	}
	
	public static  void saveObject2File(String fileName, Object o) throws Exception {

        String path = Gl.Ct().getFilesDir() + "/";

        File dir = new File(path);
        dir.mkdirs();

        File f = new File(dir, fileName);

        if (f.exists()) {
            f.delete();
        }
        FileOutputStream os = new FileOutputStream(f);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
        objectOutputStream.writeObject(o);
        objectOutputStream.close();
        os.close();
    }

    public static Object readObjectFromFile(String fileName) throws Exception {
        String path = Gl.Ct().getFilesDir() + "/";
        File dir = new File(path);
        dir.mkdirs();
        File file = new File(dir, fileName);
        InputStream is = new FileInputStream(file);
        ObjectInputStream objectInputStream = new ObjectInputStream(is);
        Object o = objectInputStream.readObject();
        return o;

    }

}
