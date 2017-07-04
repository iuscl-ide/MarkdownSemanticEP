/* Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org */
package org.markdownsemanticep.activator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/** Common desktop-model-apk log, standard JRE */
public class L {

	/** Global log */
	private static Logger logger;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd 'at' HH:mm:ss.SSS");

    /** Initialize the log and log file */
    public static void initLog(File logFile) {

    	logger = Logger.getLogger("org.markdownsemanticep.log");
    	logger.setUseParentHandlers(false);
    	logger.setLevel(Level.INFO);

    	Formatter formatter = new Formatter() {
            @Override
            public String format(LogRecord r) {
                String levelName = r.getLevel().getName();
                String logRecordText = "[" + "        ".substring(levelName.length()) + levelName + "] org.markdownsemanticep; on ";
                String timeFormatted = dateFormat.format(r.getMillis());
                logRecordText = logRecordText + timeFormatted;

                StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[8];
                String methodName = stackTraceElement.getMethodName();
                String fullClassName = stackTraceElement.getClassName();
                int dIndex = fullClassName.indexOf("$");
                if (dIndex > -1) {
                	fullClassName = fullClassName.substring(0, dIndex);
                }
                String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
                String fullMethodName = fullClassName + "." + methodName;
                int codeLineNumber = stackTraceElement.getLineNumber();

                String text = "logged from " + fullMethodName + "(" + className + ".java:" + codeLineNumber + "):";
                logRecordText = logRecordText + ", " + text;
                
                logRecordText = logRecordText + "\n" + r.getMessage() + "\n";

                return logRecordText;
            }
        };

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);
        logger.addHandler(consoleHandler);

        if (logFile != null) {
            try {
                FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath(), 50000, 5, true);
                fileHandler.setFormatter(formatter);
                logger.addHandler(fileHandler);
            }
            catch (IOException ioException) {
                e("IOException in initLog", ioException);
            }
        }
        else {
            e("NullPointerException in initLog", new NullPointerException("Parameter logFile is null"));
        }
    }
    
    /** Log text lines */
    private static String logTextLines(String logText, Throwable throwable) {

    	if (logText.length() > 110000) {
    		logText = logText.substring(0, 50000) + " ... " + logText.substring(logText.length() - 50000, logText.length());
    	}
    	
        if (throwable != null) {
        	logText = logText + "\n";
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(byteArrayOutputStream, true);
            throwable.printStackTrace(printStream);
            logText = logText + byteArrayOutputStream.toString();
        }
        String[] lines = logText.replace("\r", "").split("\n");
        int size = lines.length;

        String concatText = "";
        for (int index = 0; index < size; index++) {
            String lineNumber = "(" + (index + 1) + "/" + size + ")";
            lineNumber = "            ".substring(0, 3 + 2 * (("" + size).length())).substring(lineNumber.length()) + lineNumber;
            concatText = concatText + lineNumber + ": " + lines[index];
            if (index < size - 1){
                concatText = concatText +"\n";
            }
        }
        return concatText;
    }

    /** Used only in development, will be false in the apk */
    public static boolean isP() {
//        return true;
        return false;
    }

    /** Used only in development, will be commented in the apk */
    public static void p(String devMessage) {
        //for (String line : logTextLines(devMessage + "\n ", null).split("\n")) {
    	for (String line : logTextLines(devMessage, null).split("\n")) {    	
            //System.out.println("MarkdownSemanticEP [P] " + line);
            System.out.println("[P] " + line);
        }
    }

    /** Log.i replacement, for jUnits */
    public static void i(String message) {
        logger.log(Level.INFO, logTextLines(message, null));
    }

    /** Log.w replacement, for jUnits */
    public static void w(String message) {
        logger.log(Level.WARNING, logTextLines(message, null));
    }

    /** Log.e replacement, for jUnits */
    public static void e(String message, Throwable throwable) {
        logger.log(Level.SEVERE, logTextLines(message, throwable));
    }


}
