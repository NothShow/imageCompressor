package com.zhou.compress.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MyFileWriter {
    static MyFileWriter sMyFileWriter;


    public static MyFileWriter getInstance(){
        if(sMyFileWriter == null) {
            synchronized (MyFileWriter.class){
                if(sMyFileWriter == null){
                    sMyFileWriter = new MyFileWriter();
                }
            }
        }
        return sMyFileWriter;
    }

    private MyFileWriter(){

    }

    /**
     * 一个向文件写入的函数
     *
     * @param content 待写入文件的内容
     * @param url 待写入文件的路径url
     * @param append 表示是否追加
     *
     * @return 返回true表示成功写入，返回false表示写入失败
     */
    public boolean writeIntoFile(String content, String url, boolean append) {
        try{
            File file = new File(url);
            if (!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }
            if (!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file, append);

            content = content + "\n";
            byte [] bytes = content.getBytes();
            fos.write(bytes);
            fos.close();
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 向文件内按行写入
     *
     * @param line 要写入的一行内容
     * @param path 文件路径
     * @param append 是否在结尾追加（false则覆盖）
     * @return 是否成功写入
     */
    public boolean writeLineIntoFile(String line, String path, boolean append){
        try {
            FileWriter fileWriter = new FileWriter(new File(path), append);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(line + "\n");
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 将文件按行读取出来
     *
     * @param path 文件路径
     * @return
     */
    public ArrayList<String> loadLineFromFile(String path){
        ArrayList<String> lines = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(new File(path));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            bufferedReader.close();
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}
