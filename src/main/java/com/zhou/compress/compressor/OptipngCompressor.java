package com.zhou.compress.compressor;

import com.zhou.compress.file.FileUtils;
import com.zhou.compress.file.PropertiesUtil;
import com.zhou.compress.util.StringUtil;

import java.io.IOException;

public class OptipngCompressor implements ICompressor {

    private String inPath;
    private String outPath;

    private long rawFileSize = 0;
    private long compressedFileSize = 0;
    private boolean compressSuccess = false;

    @Override
    public void setPath(String inPath, String outPath) {
        this.inPath = inPath;
        this.outPath = outPath;
    }

    @Override
    public long getAvailableCount() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getRawFileSize() {
        return rawFileSize;
    }

    @Override
    public long getCompressedFileSize() {
        return compressedFileSize;
    }

    @Override
    public boolean isCompressSuccess() {
        return compressSuccess;
    }

    @Override
    public boolean check() {
        if (!StringUtil.isEmpty(inPath) && inPath.toUpperCase().endsWith(".PNG")){
            return true;
        }
        return false;
    }

    @Override
    public long caculateRawFileSize() {
        return rawFileSize = FileUtils.getFileSize(inPath);
    }

    @Override
    public boolean compress() {
        Runtime runtime = Runtime.getRuntime();
        String[] commandArgs = {
                PropertiesUtil.get().getToolPath() + "\\optipng-0.7.7-win32\\optipng.exe"
                ,inPath
        };
        try {
            Process process = runtime.exec(commandArgs);
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public long caculateCompressedFileSize() {
        return compressedFileSize = FileUtils.getFileSize(outPath);
    }

    @Override
    public void done() {
        compressSuccess = check();
        if (compressSuccess) {
            rawFileSize = caculateRawFileSize();
            compressSuccess = compress();
            compressedFileSize = caculateCompressedFileSize();
        }
    }
}
