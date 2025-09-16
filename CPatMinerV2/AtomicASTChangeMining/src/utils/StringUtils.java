package utils;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class StringUtils {
	public static String[] tokenize(String s) {
		return s.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
	}

	public static boolean CheckIfFileNameContainsTest(String pathString) {
		Path path = FileSystems.getDefault().getPath(pathString);
		String file_name = path.getFileName().toString();
		return file_name.toLowerCase().contains("test");
	}
}
