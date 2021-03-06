package com.ryanli.rxdownloader.data.retrofit.download;

/**
 * Auther: RyanLi
 * Data: 2018-06-18 22:26
 * Description:
 */
public class   DownloadFlag {
    public static final int NORMAL = 9990;      //未下载
    public static final int WAITING = 9991;     //等待中
    public static final int STARTED = 9992;     //已开始下载
    public static final int PAUSED = 9993;      //已暂停
    public static final int CANCELED = 9994;    //已取消
    public static final int COMPLETED = 9995;   //已完成
    public static final int FAILED = 9996;      //下载失败
    public static final int INSTALL = 9997;     //安装中,暂未使用
    public static final int INSTALLED = 9998;   //已安装,暂未使用
    public static final int DELETED = 9999;     //已删除
}