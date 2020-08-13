package com.zhou.compress;

import com.zhou.compress.compressor.CompressorFactory;
import com.zhou.compress.compressor.ICompressor;
import com.zhou.compress.file.MyFileWriter;
import com.zhou.compress.file.PropertiesUtil;
import com.zhou.compress.git.Committer;
import com.tinify.Tinify;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Compressor {
    private static Compressor sCompressor;

    private Compressor(){}
    public static Compressor getInstance(){
        if(sCompressor == null){
            synchronized (Compressor.class){
                if (sCompressor == null)
                    sCompressor = new Compressor();
            }
        }
        return sCompressor;
    }

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    /**
     * 压缩所有图片，手动声明log地址
     *
     * @param inPath    待压缩图片的路径字符串
     * @param outPath   压缩后图片存储的路径字符串
     * @return          是否成功压缩
     */
    public void compressPicture(ICompressor compressor, String inPath, String outPath){
        compressor.setPath(inPath,outPath);
        compressor.done();
    }

    private void logCompressResult(ICompressor compressor,String projectPath, String filePath){
        long rawSize = compressor.getRawFileSize();
        long compressSize = compressor.getCompressedFileSize();
        boolean compressSuccess = compressor.isCompressSuccess();
        float compressRate = 1f - (float)compressSize / (float)rawSize;
        StringBuffer content = new StringBuffer().append(compressSuccess).append("\t")
                .append(filePath).append("\t")
                .append(rawSize).append("\t")
                .append(compressSize).append("\t")
                .append(compressRate);
    }

    /**
     * 给定一个文件夹，将该目录下所有的jpg和png图片都压缩
     *
     * @param logPath   Log写入的文件路径
     * @param folder    要压缩图片的位置（根目录路径）
     */
    public void compressAllPicture(String folder, String logPath, String replaceable) {
        File file = new File(folder);
        String path;
        if (file.exists()) {
            if (file.isDirectory()){
                File[] files = file.listFiles();
                if (files != null && files.length > 0){
                    for (File file2 : files) {
                        if (file2.isDirectory()) {
                            compressAllPicture(file2.getAbsolutePath(), logPath, replaceable);
                        } else {
                            path = file2.getPath();
                            if (path.toUpperCase().endsWith(".JPG") || path.toUpperCase().endsWith(".PNG")) {
                                System.out.println("开始压缩图片" + path);
//                                compressPicture(path, path, logPath, replaceable);
                                System.out.println("完成压缩");
                            }
                        }
                    }
                }
            }else {

            }
        } else {
            System.out.println("该路径无文件夹或文件！");
        }
    }

    /**
     * 给定一个文件夹，将该目录下所有的jpg和png图片都压缩，默认日志位置在根目录下的TinyLog.txt中
     *
     * @param folder    要压缩图片的位置（根目录路径）
     */
    public void compressAllPicture(String folder) {
        compressAllPicture(folder, folder + "\\TinyLog.txt", null);
    }


    private String setSize(long size) {
        long GB = 1024 * 1024 * 1024;//定义GB的计算常量
        long MB = 1024 * 1024;//定义MB的计算常量
        long KB = 1024;//定义KB的计算常量
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String resultSize = "";
        if (size / GB >= 1) {
            //如果当前Byte的值大于等于1GB
            resultSize = df.format(size / (float) GB) + "GB";
        } else if (size / MB >= 1) {
            //如果当前Byte的值大于等于1MB
            resultSize = df.format(size / (float) MB) + "MB";
        } else if (size / KB >= 1) {
            //如果当前Byte的值大于等于1KB
            resultSize = df.format(size / (float) KB) + "KB";
        } else {
            resultSize = size + "B";
        }
        return resultSize;
    }

    public static void main(String args[]){
        Tinify.setKey("XfXgJcXTZb1LDrsXWhslWSMDJynYfvFh");
        Compressor.getInstance().compressAllPicture(args[0], args[0] + "\\log.txt", args[0]);
    }
}
