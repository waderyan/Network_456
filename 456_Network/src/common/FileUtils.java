package common;

import java.io.File;

public class FileUtils {

	public static String getExtension(File f) {
		String fileName = f.getName();
		int i = fileName.lastIndexOf('.');
		int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
		if (i > p) {
			return fileName.substring(i+1);
		} 
		return "";
	}
}
