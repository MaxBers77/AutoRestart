package org.example;

import com.profesorfalken.jpowershell.PowerShell;

import java.io.*;
import java.util.Properties;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ){
        File file=new File("C:\\AutoRestart\\autoRestart.properties");
        Properties props=new Properties();
        try {
            props.load(new FileReader(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String fileName=props.getProperty("fileName");
        String keyPhrase= props.getProperty("keyPhrase");
        String period=props.getProperty("period");
        String delay=props.getProperty("delay");
        String pathToLog= props.getProperty("pathToLog");

        File logFile=new File(pathToLog);
        String currentLastLine=null;
        String previousLastLine=null;

        while (true){
             currentLastLine=readLastLine(logFile);
            if (!currentLastLine.equals(previousLastLine) && currentLastLine.contains(keyPhrase)){
                previousLastLine=currentLastLine;
                stopProcess(fileName);

                waiting(delay);

                startProcess(fileName);
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
    private static String readLastLine(File file){
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
            throw new RuntimeException(e);
        }
        return result;
    }
    private static void startProcess(String fileName){
        ProcessBuilder pb=new ProcessBuilder(fileName);
        try {
            pb.start();
        } catch (IOException e) {
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
}
