package com.catchbest;


/**
 * Created by terry on 17-9-14.
 */



public class cam {

    public int m_devicecount;


    public cam() {

        m_devicecount = 0;
    }


    public native int Init();

    public native int CaptureRawData(int index, Object buffer);

    public native int CaptureRGBData(int index, Object buffer);

    public native int DeviceGetCount();

    public native int CaptureGetSize(int index, int[] width, int[] height);

    public native int DeviceGetInformation(int index, int[] DeviceType, int[] Serials,int [] FirmwareVersion);

    //0  0  2592  1944  500w相机设置最大分辨率
    public native int CaptureSetFieldOfView(int nIndex, int nxStart, int nyStart, int nWidth, int nHeight);

    public native int GetTriggerMode(int index);

    public native int SetTriggerMode(int index, int mode);

    public native int SetBayerMode(int index, int mode);

    public native int SoftStartCapture(int index);

    public native int ReadRawData(int index, Object buffer);

    public native int ReadRGBData(int index, Object buffer);

    public native byte[] CaptureRGBdataArray(int index, int width, int height);

    public native byte[] CaptureRAWdataArray(int index, int width, int height);


    public native int[] CaptureRGBdataIntArray(int index, int width, int height);


    public native int[] CaptureRGBdataIntArrayAfterStart(int index, int width, int height);


    public native int ExposureTimeSet(int index, int time);

    public native int WhiteBalanceSet(int index, int mode);

    public native int WhiteBalancePresettingSet(int index,int mode);

    public native int CaptureBySurface(int index,Object surface,int save);

    public native int  CaptureBitmap(int index, String fullpath);

    public native int SetParam(int index,int param,int value);

    public native int PreInit(int fd);

    public native int CaptureGetFieldOfView(int index,int[] nxStart, int[] nyStart ,int[] nWidth, int[] nHeight);

    public native int GetParamRange(int index,int param,int[] min, int[] max);

    public native int CaptureBySurfaceSave(int index,Object surface,int save,String fullpath);

    public native int CaptureSetRecover(int index,int value);

    public native int CaptureGetSizeEx(int index,int[] nWidth, int[] nHeight, int[] nBitscount);

    public native int LutSetEnable(int index,int value);

    public native int SensitivitySetMode(int index,int value);

    public native int QueryFunction(int index,int function);

    public native int AEStart(int index,int nStart,int nMaxCount,int nTarget);

    //设置自动曝光区域
    public native int AESetRegion(int index, int nX, int nY, int nW, int nH);
    //设置自动调节的范围
    public native int AESetExposureTimeRange(int index, int min, int max);

    public native  int AEGetStatusEx(int index, int nStart, int nMaxCount, int nTarget);

    /*
jint Java_com_catchbest_cam_AEGetStatusEx(JNIEnv* env, jobject thiz, int index, jintArray nStart, jintArray nMaxCount, jintArray nTarget)
jint Java_com_catchbest_cam_AEStart(JNIEnv* env, jobject thiz, int index, int nStart, int nMaxCount, int nTarget)
第二个接口就是开始自动曝光

int nStart ： 1 开始自动曝光      int nMaxCount ：运算次数，一般单次写个20，如果是连续就-1，  int nTarget阈值(128-255)，一般128
 Java_com_catchbest_cam_AEGetStatusEx 取回来当前自动曝光状态*/


    public native int  UnInit();


    static {
        System.loadLibrary("usb1.0");
        System.loadLibrary("ksjapijni");
    }

}
