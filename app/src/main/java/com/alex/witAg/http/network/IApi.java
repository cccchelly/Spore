package com.alex.witAg.http.network;

import com.alex.witAg.base.BaseResponse;
import com.alex.witAg.bean.BaseSettingRequestBean;
import com.alex.witAg.bean.BaseSettingResponseBean;
import com.alex.witAg.bean.BindComRequestBean;
import com.alex.witAg.bean.BindComResponseBean;
import com.alex.witAg.bean.BindPhoneResponseBean;
import com.alex.witAg.bean.GetTokenBean;
import com.alex.witAg.bean.HomeBean;
import com.alex.witAg.bean.PhotoDetailRecodeBean;
import com.alex.witAg.bean.PhotoSetResponseBean;
import com.alex.witAg.bean.PicListBean;
import com.alex.witAg.bean.PicMessageBean;
import com.alex.witAg.bean.PostLocationResponseBean;
import com.alex.witAg.bean.PostMsgBean;
import com.alex.witAg.bean.PostMsgResultBean;
import com.alex.witAg.bean.PostTaskMsgBean;
import com.alex.witAg.bean.QiNiuTokenBean;
import com.alex.witAg.bean.SendSmsResponseBean;
import com.alex.witAg.bean.UpdateMsgBean;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by dth
 * Des:所有后台api在此申明
 * Date: 2018-01-23.
 */

public interface IApi {

    //Retrofit的Url组合规则
      //baseUrl                             //和URL有关的注解中提供的值     //最后api的结果url
//    http://localhost:4567/path/to/other/	/post	                    http://localhost:4567/post
//    http://localhost:4567/path/to/other/	post	                    http://localhost:4567/path/to/other/post
//    http://localhost:4567/path/to/other/	https://github.com/ikidou	https://github.com/ikidou
//    如果你在注解中提供的url是完整的url，则url将作为请求的url。
//    如果你在注解中提供的url是不完整的url，且不以 / 开头，则请求的url为baseUrl+注解中提供的值
//    如果你在注解中提供的url是不完整的url，且以 / 开头，则请求的url为baseUrl的主机部分+注解中提供的值
    
    /**
     * 首页数据
     * @return
     */
    @POST("/app/goods/shopIndex")
    Observable<BaseResponse<HomeBean>> getHomePageData();



    /*获取版本信息*/
    @GET("/app/update/index")
    Observable<BaseResponse<UpdateMsgBean>> getVersion(@Query("token") String token,@Query("versionNu") String versionCode
            ,@Query("type")String type);

    /*根据imei登录设备获取token*/
    @POST("/app/device/login")
    Observable<BaseResponse<GetTokenBean>>  getToken(@Query("deviceCode") String imei);

    /*绑定设备与公司信息*/
    @POST("/app/device/bind")
    Observable<BaseResponse<BindComResponseBean>> bindCompany(@Body BindComRequestBean bindComRequestBean);

    /*基础设置*/
    @POST("/app/device/config")
    Observable<BaseResponse<BaseSettingResponseBean>> setBaseSetting(@Body BaseSettingRequestBean baseSettingRequestBean);

    /*设置定时照相参数*/
    @POST("/app/device/photo/setting")
    Observable<BaseResponse<PhotoSetResponseBean>> setPhotoTask(@Query("token") String token,
                                                                @Query("photoStart") String photoStart,
                                                                @Query("photoInterval") Integer photoInterval,
                                                                @Query("photoMark")Integer photoMark,
                                                                @Query("photoQuality")Integer photoQuality);
    /*发送验证码*/
    @POST("/app/app/sms/code")
    Observable<BaseResponse<SendSmsResponseBean>> sendSms(@Query("token") String token,
                                                          @Query("phone")String phone);
    /*绑定手机号*/
    @POST("/app/device/bind/phone")
    Observable<BaseResponse<BindPhoneResponseBean>> bindPhone(@Query("token") String token,
                                                              @Query("phone") String phone,
                                                              @Query("code")String code);
    /*上传定位信息*/
    @POST("/app/device/position")
    Observable<BaseResponse<PostLocationResponseBean>> postLocation(@Query("Latitude") String Latitude,
                                                                    @Query("Longitude") String Longitude);

    /*获取七牛token*/
    @POST("/app/upload/token")
    Observable<BaseResponse<QiNiuTokenBean>> getQiNiuToken(@Query("token") String token);

    /*
    * 设备图片上传到服务器
    */
    @POST("/app/det/photo/add")
    Observable<ResponseBody> postDevicePic(@Body PicMessageBean messageBean);

    /*设备图片列表*/
    @GET("/app/det/photo/find")
    Observable<BaseResponse<PicListBean>> getPicListData(@Query("token") String token,@Query("date") String date);

    /*
    * 设备图片虫情记录
    */
    @GET("/app/det/photo/pest/find/{id}")
    Observable<BaseResponse<PhotoDetailRecodeBean>> getRecodeByPhoto(@Path("id")String id,@Query("token") String token);

    //上传设备信息
    @POST("/app/device/subinfo")
    Observable<BaseResponse<PostMsgResultBean>> postDeviceMsg(@Body PostMsgBean postMsgBean);

    //上传执行任务信息
    @POST("/app/device/task")
    Observable<BaseResponse<PostMsgResultBean>> postTaskMsg(@Body PostTaskMsgBean postTaskMsgBean);

}
