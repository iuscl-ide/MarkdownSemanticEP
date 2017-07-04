/* Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org */
package org.markdownsemanticep.activator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Properties;

/** File operations */
public class F {

	/** Loads an entire file in one string */
	public static String loadFileInString(File file) {
	
	    RandomAccessFile randomAccessFile = null;
	    String content = null;
	    try {
	        randomAccessFile = new RandomAccessFile(file, "r");
	        byte[] buffer = new byte[(int)randomAccessFile.length()];
	        randomAccessFile.readFully(buffer);
	        content = new String(buffer, "utf-8");
	        /* UTF8_BOM */
	        if (content.startsWith("\uFEFF")) {
	        	content = content.substring(1);
	        }
	    }
	    catch (FileNotFoundException fileNotFoundException) {
	        L.e("FileNotFoundException in loadFileInString", fileNotFoundException);
	    }
	    catch (IOException ioException) {
	    	L.e("IOException in loadFileInString" + file, ioException);
	    }
	    finally {
	        try {
	            if (randomAccessFile != null) {
	                randomAccessFile.close();
	            }
	        }
	        catch (IOException ioException) {
	        	L.e("IOException in finally loadFileInString", ioException);
	        }
	    }
	    return content;
	}

	/** Load properties file */
	public static Properties loadPropertiesFile(File file) {
		
		Properties properties = new Properties();
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			try {
				properties.load(fileInputStream);
			}
			catch (IOException ioException) {
				L.e("IOException in loadPropertiesFile", ioException);
			}
			finally {
				try {
					if (fileInputStream != null) {
						fileInputStream.close();
					}
				}
				catch (IOException ioException) {
					L.e("IOException in finally loadPropertiesFile", ioException);
				}
			}
		}
		catch (FileNotFoundException fileNotFoundException) {
			/* Temporary... */
			L.e("FileNotFoundException in loadPropertiesFile", fileNotFoundException);
		}
		return properties;
	}

	/** Loads an entire input stream in one string */
	public static String loadInputStreamInString(InputStream inputStream) {

		Reader reader = new InputStreamReader(inputStream);
		StringBuilder sb = new StringBuilder();
		char buffer[] = new char[16384];  /* read 16k blocks */
		int len; /* how much content was read? */
		try {
			while ((len = reader.read(buffer)) > 0) {
				sb.append(buffer, 0, len);
			}
		}
		catch (IOException ioException) {
			L.e("IOException in loadInputStreamInString", ioException);
		}
		finally {
			try {
				reader.close();
			}
			catch (IOException ioException) {
				L.e("IOException in finally loadInputStreamInString", ioException);
			}
		}
		return sb.toString();
	}

	/** Saves properties in file */
	public static void savePropertiesInFile(Properties properties, String comments, File file) {
		
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(file); 
			properties.store(fileOutputStream, comments);
		}
		catch (FileNotFoundException fileNotFoundException) {
			L.e("FileNotFoundException in savePropertiesInFile", fileNotFoundException);
		}
		catch (IOException ioException) {
			L.e("IOException in savePropertiesInFile", ioException);
		}
		finally {
			try {
				fileOutputStream.close();
			} 
			catch (IOException ioException) {
				L.e("IOException in finally savePropertiesInFile", ioException);
			}
		}
	}
	
	/** Saves an entire input stream in a file */
    public static void saveInputStreamInFile(InputStream inputStream, File file) {

        byte buffer[] = new byte[16384];  /* read 16k blocks */
        int len; /* how much content was read? */
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            while ((len = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, len);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
        }
        catch (IOException ioException) {
            L.e("IOException in saveInputStreamInFile", ioException);
        }
    }

    /** Delete folder and contents */
    public static void deleteFolder(File file) {

        if (file.isDirectory()) {
            for (String child : file.list()) {
                deleteFolder(new File(file, child));
            }
        }
        try {
        	//OutputStream outputStream = new FileOutputStream(file); // to test the exception
			Files.delete(file.toPath());
		} catch (IOException ioException) {
			L.e("IOException in deleteFolder", ioException);
		}
    }
    
}
