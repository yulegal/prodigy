package com.vapid_software.prodigy.api;


import com.vapid_software.prodigy.helpers.FilterQueryOptions;
import com.vapid_software.prodigy.helpers.FilterResponse;
import com.vapid_software.prodigy.models.AuthResponseModel;
import com.vapid_software.prodigy.models.BookingCreateModel;
import com.vapid_software.prodigy.models.BookingModel;
import com.vapid_software.prodigy.models.BranchCreateModel;
import com.vapid_software.prodigy.models.BranchModel;
import com.vapid_software.prodigy.models.BranchUpdateModel;
import com.vapid_software.prodigy.models.BroadcastCreateModel;
import com.vapid_software.prodigy.models.CategoryModel;
import com.vapid_software.prodigy.models.ChatModel;
import com.vapid_software.prodigy.models.ForwardMessageModel;
import com.vapid_software.prodigy.models.LoginModel;
import com.vapid_software.prodigy.models.MessageModel;
import com.vapid_software.prodigy.models.NotificationModel;
import com.vapid_software.prodigy.models.RatingModel;
import com.vapid_software.prodigy.models.RebookModel;
import com.vapid_software.prodigy.models.ServiceModel;
import com.vapid_software.prodigy.models.UserEditNameModel;
import com.vapid_software.prodigy.models.UserHandleContactModel;
import com.vapid_software.prodigy.models.UserModel;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
    @GET("user/{id}")
    Call<UserModel> getUserById(@Path("id") String id);
    @POST("service/filter")
    Call<FilterResponse<ServiceModel>> filterServices(@Body()FilterQueryOptions options);
    @POST("user/login")
    Call<AuthResponseModel> login(@Body()LoginModel model);
    @Multipart
    @POST("user")
    Call<AuthResponseModel> register(
            @Part("login") RequestBody login,
            @Part("name") RequestBody name,
            @Part("locale") RequestBody locale,
            @Part("token") RequestBody token,
            @Part MultipartBody.Part image
    );
    @POST("user/edit-name")
    Call<UserModel> editName(@Body()UserEditNameModel model);
    @Multipart
    @POST("user/upload-avatar")
    Call<UserModel> uploadAvatar(@Part MultipartBody.Part file);
    @DELETE("user/avatar")
    Call<UserModel> deleteAvatar();
    @POST("category/filter")
    Call<FilterResponse<CategoryModel>> filterCategories(@Body() FilterQueryOptions options);
    @POST("branch/filter")
    Call<FilterResponse<BranchModel>> filterBranches(@Body() FilterQueryOptions options);
    @DELETE("branch/{id}")
    Call<BranchModel> deleteBranchById(@Path("id") String id);
    @DELETE("service/remove-photo/{id}")
    Call<ServiceModel> removeServicePhoto(@Path("id") String id);
    @GET("service/get-user-service")
    Call<ServiceModel> getUserService();
    @Multipart
    @POST("service")
    Call<ServiceModel> createService(
            @Part("name")RequestBody name,
            @Part("categoryId") RequestBody categoryId,
            @Part("averageSession") RequestBody averageSession,
            @Part MultipartBody.Part file,
            @Part("unit") RequestBody unit,
            @Part("address") RequestBody address,
            @Part("workSchedule") RequestBody workSchedule,
            @Part("extra") RequestBody extra
    );
    @Multipart
    @PUT("service")
    Call<ServiceModel> updateService(
            @Part("id") RequestBody id,
            @Part("name")RequestBody name,
            @Part("categoryId") RequestBody categoryId,
            @Part("averageSession") RequestBody averageSession,
            @Part MultipartBody.Part file,
            @Part("unit") RequestBody unit,
            @Part("address") RequestBody address,
            @Part("workSchedule") RequestBody workSchedule,
            @Part("extra") RequestBody extra
    );
    @GET("user/relogin")
    Call<AuthResponseModel> reLogin();
    @POST("branch")
    Call<BranchModel> createBranch(@Body() BranchCreateModel model);
    @PUT("branch")
    Call<BranchModel> updateBranch(@Body() BranchUpdateModel model);
    @GET("user/find-user-by-login/{login}")
    Call<UserModel> findUserByLogin(@Path("login") String login);
    @POST("booking/filter")
    Call<FilterResponse<BookingModel>> filterBookings(@Body() FilterQueryOptions options);
    @POST("service/toggle-rating")
    Call<Integer> rateService(@Body() RatingModel model);
    @POST("branch/toggle-rating")
    Call<Integer> rateBranch(@Body() RatingModel model);
    @POST("booking")
    Call<BookingModel> book(@Body() BookingCreateModel model);
    @POST("booking/rebook")
    Call<BookingModel> rebook(@Body() RebookModel model);
    @GET("service/toggle-favorites/{id}")
    Call<Integer> toggleFavorites(@Path("id") String id);
    @DELETE("gallery/{photo}")
    Call<Void> deletePhoto(@Path("photo") String photo);
    @Multipart
    @POST("gallery/upload")
    Call<List<String>> uploadPhotosToGallery(
            @Part("serviceId") RequestBody serviceId,
            @Part("branchId") RequestBody branchId,
            @Part MultipartBody.Part[] files
    );
    @POST("gallery/filter/{id}")
    Call<FilterResponse<String>> filterGallery(@Body() FilterQueryOptions options, @Path("id") String id);
    @GET("booking/cancel-booking/{id}")
    Call<BookingModel> cancelBooking(@Path("id") String id);
    @GET("booking/finish-booking/{id}")
    Call<BookingModel> finishBooking(@Path("id") String id);
    @GET("notification/get-count")
    Call<Integer> getNotificationsCount();
    @POST("service/filter-favorites")
    Call<FilterResponse<ServiceModel>> filterFavorites(@Body()FilterQueryOptions options);
    @DELETE("service/remove-from-favorites/{id}")
    Call<ServiceModel> removeFromFavorites(@Path("id") String id);
    @POST("notification/filter")
    Call<FilterResponse<NotificationModel>> filterNotifications(@Body() FilterQueryOptions options);
    @GET("notification/read/{id}")
    Call<Boolean> read(@Path("id") String id);
    @POST("notification/broadcast")
    Call<Boolean> broadcast(@Body() BroadcastCreateModel model);
    @POST("user/add-contacts")
    Call<List<UserModel>> addContacts(@Body() UserHandleContactModel model);
    @POST("user/filter-contacts")
    Call<FilterResponse<UserModel>> filterContacts(@Body FilterQueryOptions options);
    @POST("message/filter/{id}")
    Call<FilterResponse<MessageModel>> filterMessages(@Body() FilterQueryOptions options, @Path("id") String id);
    @Multipart
    @POST("message")
    Call<MessageModel> sendMessage(
            @Part("toId") RequestBody toId,
            @Part("body") RequestBody body,
            @Part("parentId") RequestBody parentId,
            @Part MultipartBody.Part files[]
    );
    @POST("chat/filter")
    Call<FilterResponse<ChatModel>> filterChats(@Body() FilterQueryOptions options);
    @DELETE("message/{id}")
    Call<MessageModel> deleteMessageById(@Path("id") String id);
    @GET("chat/{id}")
    Call<ChatModel> getChatById(@Path("id") String id);
    @DELETE("chat/{id}")
    Call<ChatModel> deleteChatById(@Path("id") String id);
    @POST("message/forward")
    Call<MessageModel> forwardMessage(@Body()ForwardMessageModel model);
}
