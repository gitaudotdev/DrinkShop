package gitau.dev.drinkshop;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import gitau.dev.drinkshop.Model.Category;
import gitau.dev.drinkshop.Retrofit.IDrinkShopApi;
import gitau.dev.drinkshop.Utils.Common;
import gitau.dev.drinkshop.Utils.ProgressRequestBody;
import gitau.dev.drinkshop.Utils.UploadCallBack;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateProductActivity extends AppCompatActivity implements UploadCallBack {

    private static final int PICK_FILE_REQUEST = 8888;
    MaterialSpinner spiner_menu;

    HashMap<String, String> menu_data_to_get_key = new HashMap<>();
    HashMap<String, String> menu_data_to_get_value = new HashMap<>();

    List<String> menu_data = new ArrayList<>();

    ImageView img_browser;
    EditText edt_name, edt_price;
    Button btn_update, btn_delete;


    IDrinkShopApi mService;

    CompositeDisposable disposable;

    Uri select_uri = null;
    String uploaded_img_path = "", selected_category = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_product);

        mService = Common.getApi();

        if (Common.currentDrink != null) {
            uploaded_img_path = Common.currentDrink.Link;
            selected_category = Common.currentDrink.MenuId;

        }

        disposable = new CompositeDisposable();


        spiner_menu = findViewById(R.id.spinner_menu_id);

        edt_name = findViewById(R.id.edt_drink_name);
        edt_price = findViewById(R.id.edt_drink_price);
        img_browser = findViewById(R.id.img_prod_browser);

        btn_delete = findViewById(R.id.btn_delete);
        btn_update = findViewById(R.id.btn_update);

        img_browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(Intent.createChooser(FileUtils.createGetContentIntent(), "Select a File"),
                        PICK_FILE_REQUEST);
            }
        });


        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCategory();
            }
        });
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCategory();
            }
        });


        spiner_menu.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                selected_category = menu_data_to_get_key.get(menu_data.get(position));
            }
        });

        setSpinnerMenu();

        setProductInfo();

    }

    private void setProductInfo() {
        if (Common.currentDrink != null) {
            edt_name.setText(Common.currentDrink.Name);
            edt_price.setText(Common.currentDrink.Price);

            Picasso.with(this).load(Common.currentDrink.Link).into(img_browser);

            spiner_menu.setSelectedIndex(menu_data.indexOf(menu_data_to_get_value.get(Common.currentCategory.getID())));
        }
    }

    private void deleteCategory() {
        disposable.add(mService.deleteProduct(Common.currentCategory.ID).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                               @Override
                               public void accept(String s) {
                                   Toast.makeText(UpdateProductActivity.this, s, Toast.LENGTH_SHORT).show();
                                   finish();
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) {
                                   Toast.makeText(UpdateProductActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                   finish();
                               }
                           }
                ));
    }

    private void updateCategory() {
        if (!edt_name.getText().toString().isEmpty())
            disposable.add(mService.updateProduct(Common.currentCategory.ID,
                    edt_name.getText().toString(),
                    uploaded_img_path,
                    edt_price.getText().toString(),
                    selected_category
            )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            Toast.makeText(UpdateProductActivity.this, s, Toast.LENGTH_SHORT).show();
//                            uploaded_img_path="";
//                            select_uri=null;
//
//                            Common.currentCategory= null;
                            finish();


                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            Toast.makeText(UpdateProductActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FILE_REQUEST) {
                if (data != null) {
                    select_uri = data.getData();
                    if (select_uri != null && !select_uri.getPath().isEmpty()) {
                        img_browser.setImageURI(select_uri);
                        uploadFileToSever();
                    } else
                        Toast.makeText(this, "Cannot Upload File To server", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void uploadFileToSever() {

        if (select_uri != null) {
            File file = FileUtils.getFile(this, select_uri);

            String file_name = new StringBuilder(UUID.randomUUID().toString())
                    .append(FileUtils.getExtension(file.toString())).toString();

            ProgressRequestBody requestFile = new ProgressRequestBody(file, this);

            final MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", file_name, requestFile);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    mService.uploadCategoryFile(body)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    //After uploading we will get file name and return string containing link of image
                                    uploaded_img_path = new StringBuilder(Common.BASE_URL)
                                            .append("server/Category/category_img/")
                                            .append(response.body().toString())
                                            .toString();
                                    Log.d("IMGPATH", uploaded_img_path);
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(UpdateProductActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).start();
        }
    }

    private void setSpinnerMenu() {
        for (Category category : Common.menuList) {
            menu_data_to_get_key.put(category.getName(), category.getID());
            menu_data_to_get_value.put(category.getID(), category.getName());

            menu_data.add(category.getName());
        }

        spiner_menu.setItems(menu_data);
    }

    @Override
    public void onProgressUpdate(int percentage) {

    }
}
