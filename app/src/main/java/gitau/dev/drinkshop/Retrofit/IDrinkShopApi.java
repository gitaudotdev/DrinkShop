package gitau.dev.drinkshop.Retrofit;

import java.util.List;

import gitau.dev.drinkshop.Model.Category;
import gitau.dev.drinkshop.Model.Drinks;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface IDrinkShopApi {

    /*
    CATEGORY MANAGEMENT
     */
    @GET("getmenu.php")
    Observable<List<Category>> getMenu();

    @FormUrlEncoded
    @POST("server/Category/add_new_category.php")
    Observable<String> insertNewCategory(@Field("name") String name, @Field("imgPath") String imgPath);


    @Multipart
    @POST("server/Category/upload_category_img.php")
    Call<String> uploadCategoryFile(@Part MultipartBody.Part file);

    @FormUrlEncoded
    @POST("server/Category/update_category.php")
    Observable<String> updateCategory(@Field("id") String id,
                                      @Field("name") String name,
                                      @Field("imgPath") String imgPath);

    @FormUrlEncoded
    @POST("server/Category/delete_category.php")
    Observable<String> deleteCategory(@Field("id") String id);

    /*
    DRINK MANAGEMENT
     */
    @FormUrlEncoded
    @POST("getdrink.php")
    Observable<List<Drinks>> getDrink(@Field("menuid") String menuID);

    @FormUrlEncoded
    @POST("server/Product/add_new_drink.php")
    Observable<String> insertNewDrink(@Field("name") String name,
                                      @Field("imgPath") String imgPath,
                                      @Field("price") String price,
                                      @Field("menuId") String menuId);

    @Multipart
    @POST("server/Product/upload_drink_img.php")
    Call<String> uploadDrinkFile(@Part MultipartBody.Part file);

    @FormUrlEncoded
    @POST("server/Product/update_product.php")
    Observable<String> updateProduct(@Field("id") String id,
                                     @Field("name") String name,
                                     @Field("imgPath") String imgPath,
                                     @Field("price") String price,
                                     @Field("menuId") String menuId);

    @FormUrlEncoded
    @POST("server/Product/delete_product.php")
    Observable<String> deleteProduct(@Field("id") String id);

}