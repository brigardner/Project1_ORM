import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ORMExceptionLogger {
    private static ORMExceptionLogger ORMExceptionLogger;
    private static String filePath;
    private static boolean consoleOutputOn;
    private static int stackTraceSize;

    private ORMExceptionLogger() {
        filePath = "src/main/resources/ORM_Exception_Logs/";
        consoleOutputOn = false;
        stackTraceSize = 10;
    }

    public static ORMExceptionLogger getExceptionLogger() {
        if (ORMExceptionLogger == null) {
            ORMExceptionLogger = new ORMExceptionLogger();
        }

        return ORMExceptionLogger;
    }

    public static ORMExceptionLogger getExceptionLogger(String filePath) {
        if (ORMExceptionLogger == null) {
            ORMExceptionLogger = new ORMExceptionLogger();
        }

        ORMExceptionLogger.filePath = filePath;
        return ORMExceptionLogger;
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
        ORMExceptionLogger.filePath = filePath;
    }

    public static void setConsoleOutputOn(boolean consoleOutputOn) {
        ORMExceptionLogger.consoleOutputOn = consoleOutputOn;
    }

    public static void setStackTraceSize(int stackTraceSize) {
        ORMExceptionLogger.stackTraceSize = stackTraceSize;
    }

    public void log(Exception e) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ORMExceptionLogger.getTimestamp())
                .append(" - ")
                .append(e.getMessage())
                .append("\n")
                .append(ORMExceptionLogger.formatStackTrace(e));
        writeToLog(stringBuilder.toString());
    }

    public void log(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ORMExceptionLogger.getTimestamp())
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

        //Attempt to write to a log file in the filePath directory
        try (Writer fileWriter = new FileWriter(fileName, true)) {
            fileWriter.write(logText);

            //Attempt to write to a log in the overall project directory if filePath directory not found
        } catch (IOException e) {
            try (Writer fileWriterBackUp = new FileWriter(getFileName(), true)) {
                fileWriterBackUp.write(logText);

                //Print stack trace if it all goes horribly wrong
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
}
