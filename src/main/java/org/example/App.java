package org.example;

import com.profesorfalken.jpowershell.PowerShell;
import org.slf4j.Logger;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ){
        Logger logger = org.slf4j.LoggerFactory.getLogger(App.class);
        File file=new File("C:\\AutoRestart\\autoRestart.properties");
        Properties props=new Properties();
        try {
            props.load(new FileReader(file));
        } catch (IOException e) {
            logger.error("Ошибка при чтении файла C:\\AutoRestart\\autoRestart.properties");
            throw new RuntimeException(e);
        }

        String fileName=props.getProperty("fileName");
        String keyPhrase= props.getProperty("keyPhrase");
        String period=props.getProperty("period");
        String delay=props.getProperty("delay");
        String pathToLog= props.getProperty("pathToLog");

        String currentLastLine=null;
        String previousLastLine=null;


        while (true){
             File logFile=new File(getCurrentLogFile(pathToLog));
             currentLastLine=readLastLine(logFile, logger);
            if (!currentLastLine.equals(previousLastLine) && currentLastLine.contains(keyPhrase)){
                logger.info("Перезапуск целевого файла");
                previousLastLine=currentLastLine;
                stopProcess(fileName);

                waiting(delay);

                startProcess(fileName,logger);
            }
            waiting(period);
        }
    }

    private static void stopProcess(String fileName){
        String command="get-process |where-object{$_.Path -Like \""+fileName+"\"}|Stop-Process";
        PowerShell powerShell=PowerShell.openSession();
        powerShell.executeCommand(command);
        powerShell.close();
    }
    private static String readLastLine(File file, Logger logger){
        String result=null;
        try (RandomAccessFile raf=new RandomAccessFile(file,"r")){
            long startIndex= file.length();
            while (startIndex>=0 && (result==null||result.length()==0)){
                raf.seek(startIndex);
                if (startIndex > 0)
                    raf.readLine();
                result = raf.readLine();
                startIndex--;
            }
        } catch (IOException e) {

            logger.error("Ошибка при чтении log файла!");
            throw new RuntimeException(e);
        }
        return result;
    }
    private static void startProcess(String fileName, Logger logger){
        ProcessBuilder pb=new ProcessBuilder(fileName);
        try {
            pb.start();
        } catch (IOException e) {
            logger.error("Ошибка при попытке запуска целевого файла!");
            throw new RuntimeException(e);
        }
    }

    private static void waiting(String sec){
        int msc=Integer.parseInt(sec)*1000;
        try {
            Thread.sleep(msc);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private static String getCurrentLogFile(String pathToLog){
        DateFormat dFYearMonth=new SimpleDateFormat("yyyy.MM");
        DateFormat dFYearMonthDay=new SimpleDateFormat("yyyy.MM.dd");
        String result=pathToLog+"\\Logs_"+dFYearMonth.format(new Date())+"\\stack."+dFYearMonthDay.format(new Date())+".log";
        return result;
    }
}
