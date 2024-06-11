package com.example.topiccatcher_android

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface MyService{
    // 次のGETでHTTP関数を呼び出すときはこれだけ書けばよさそう
    @GET("posts")
    fun getRawResponseForPosts(): Call<ResponseBody>
    // GET通信だけならここ以下はなくても大丈夫そう
    /*
    @POST("posts")
    fun postRawRequestForPosts(@Body body:RequestBody):Call<ResponseBody>

    @PUT("posts/{id}")
    fun putRawRequestForPosts(@Path("id") id:String, @Body body:RequestBody):Call<ResponseBody>

    @DELETE("posts/{id}")
    fun deletePathParam(@Path("id") id:String ):Call<ResponseBody>
    */
}
