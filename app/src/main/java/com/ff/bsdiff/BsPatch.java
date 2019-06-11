package com.ff.bsdiff;

/**
 * description:
 * author: FF
 * time: 2019-06-10 22:19
 */
public class BsPatch {

    static {
        System.loadLibrary("bspatch");
    }

    /**
     * 合并
     *
     * @param oldFile   已安装apk路径
     * @param newFile   生成的新apk路径
     * @param patchFile 差分包路径
     */
    public native static void patch(String oldFile, String newFile, String patchFile);
}
