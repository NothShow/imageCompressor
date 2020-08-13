package com.zhou.compress.compressor;

import com.zhou.compress.file.FileUtils;
import com.zhou.compress.tinify.TinifyKeyUtils;
import com.zhou.compress.util.StringUtil;
import com.tinify.AccountException;
import com.tinify.ClientException;
import com.tinify.ConnectionException;
import com.tinify.ServerException;
import com.tinify.Tinify;

import java.util.List;

public class TinypngCompressor implements ICompressor {

    private static final int MAX_PRE_KEY = 500 ;

    private String inPath;
    private String outPath;

    long rawFileSize = 0;
    long compressedFileSize = 0;
    boolean compressSuccess = false;

    @Override
    public void setPath(String inPath,String outPath) {
        this.inPath = inPath;
        this.outPath = outPath;
    }

    private boolean checkTiny(){
        boolean result = false;
        if (StringUtil.isEmpty(Tinify.key())) {
            Tinify.setKey(TinifyKeyUtils.get().getKey());
        }
        try {
            result = Tinify.validate();
        }catch (AccountException e){
            e.printStackTrace();
        }finally {
            if (result == false){
                Tinify.setKey(TinifyKeyUtils.get().getNextKey());
                result = checkTiny();
            }else if (Tinify.compressionCount() >= MAX_PRE_KEY) {
                Tinify.setKey(TinifyKeyUtils.get().getNextKey());
                result = checkTiny();
            }
        }

        return result;
    }

    @Override
    public long getAvailableCount() {
        long total = 0 ;
        List<String> keyList = TinifyKeyUtils.get().getTinifyKeyList();
        if (keyList != null && keyList.size() > 0){
            for (String key : keyList) {
                try {
                    Tinify.setKey(key);
                    Tinify.validate();
                    total += (MAX_PRE_KEY - Tinify.compressionCount());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        Tinify.setKey(null);
        return total;
    }

    @Override
    public long getRawFileSize() {
        return rawFileSize;
    }

    @Override
    public long getCompressedFileSize() {
        return compressedFileSize;
    }

    public boolean isCompressSuccess() {
        return compressSuccess;
    }

    @Override
    public boolean check() {
        boolean result = false;
        try {
            result = checkTiny();
        } catch (IllegalStateException e1) {
            e1.printStackTrace();
        } catch (java.lang.Exception e) {
            e.printStackTrace();

        }
        return result;
    }

    @Override
    public long caculateRawFileSize() {
        return rawFileSize = FileUtils.getFileSize(inPath);
    }

    @Override
    public boolean compress() {
        boolean result = false;
        try {
            Tinify.fromFile(inPath).toFile(outPath);
            result = true;
        } catch(AccountException e) {
            System.out.println("The error message is: " + e.getMessage());
            // Verify your API key and account limit.
            boolean checkResult = check();
            if (checkResult){
                compress();
            }
        } catch(ClientException e) {
            // Check your source image and request options.
            e.printStackTrace();
        } catch(ServerException e) {
            // Temporary issue with the Tinify API
            e.printStackTrace();
        } catch(ConnectionException e) {
            // A network connection error occurred.
            e.printStackTrace();
        } catch(java.lang.Exception e) {
            // Something else went wrong, unrelated to the Tinify API.
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public long caculateCompressedFileSize() {
        return compressedFileSize = FileUtils.getFileSize(outPath);
    }

    @Override
    public void done() {
        boolean checkSuccess = check();
        if (checkSuccess) {
            rawFileSize = caculateRawFileSize();
            compressSuccess = compress();
            compressedFileSize = caculateCompressedFileSize();
        }
    }
}
