package gitau.dev.drinkshop.Utils;

import java.util.ArrayList;
import java.util.List;

import gitau.dev.drinkshop.Model.Category;
import gitau.dev.drinkshop.Model.Drinks;
import gitau.dev.drinkshop.Retrofit.IDrinkShopApi;
import gitau.dev.drinkshop.Retrofit.RetrofitClient;

public class Common {

    public static Category currentCategory; //variable to hold current category clicked
    public static Drinks currentDrink;

    //Variable to hold Menu
    public static List<Category> menuList = new ArrayList<>();

    public static final String BASE_URL = "http://192.168.9.101/ecommerce/";

    public static IDrinkShopApi getApi() {
        return RetrofitClient.getClient(BASE_URL).create(IDrinkShopApi.class);
    }
}
