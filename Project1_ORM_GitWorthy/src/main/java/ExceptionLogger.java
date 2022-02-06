import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExceptionLogger {
    private static ExceptionLogger exceptionLogger;
    private static String filePath;
    private static boolean consoleOutputOn;
    private static int stackTraceSize;

    private ExceptionLogger() {
        filePath = "src/main/resources/logs/";
        consoleOutputOn = false;
        stackTraceSize = 10;
    }

    public static ExceptionLogger getExceptionLogger() {
        if (exceptionLogger == null) {
            exceptionLogger = new ExceptionLogger();
        }

        return exceptionLogger;
    }

    public static ExceptionLogger getExceptionLogger(String filePath) {
        if (exceptionLogger == null) {
            exceptionLogger = new ExceptionLogger();
        }

        ExceptionLogger.filePath = filePath;
        return exceptionLogger;
    }


    public static String getFilePath() {
        return filePath;
    }

    public static boolean isConsoleOutputOn() {
        return consoleOutputOn;
    }

    public static int getStackTraceSize() {
        return stackTraceSize;
    }

    public static void setFilePath(String filePath) {
        ExceptionLogger.filePath = filePath;
    }

    public static void setConsoleOutputOn(boolean consoleOutputOn) {
        ExceptionLogger.consoleOutputOn = consoleOutputOn;
    }

    public static void setStackTraceSize(int stackTraceSize) {
        ExceptionLogger.stackTraceSize = stackTraceSize;
    }

    public void log(Exception e) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(exceptionLogger.getTimestamp())
                .append(" - ")
                .append(e.getMessage())
                .append("\n")
                .append(exceptionLogger.formatStackTrace(e));
        writeToLog(stringBuilder.toString());
    }

    public void log(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(exceptionLogger.getTimestamp())
                .append(" - ")
                .append(s);
        writeToLog(stringBuilder.toString());
    }

    private String getTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("[yyyy-MM-dd HH:mm:ss]"));
    }

    private String getFileName() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log";
    }

    private String formatStackTrace(Exception e) {
        StackTraceElement[] stackTraceElements = e.getStackTrace();

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < stackTraceElements.length && i < stackTraceSize; i++) {
            stringBuilder.append("\t");
            stringBuilder.append(stackTraceElements[i]);
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    private void writeToLog(String logText) {
        String fileName = getFilePath() + getFileName();

        try(Writer fileWriter = new FileWriter(fileName, true)) {
            fileWriter.write(logText);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
