package gitau.dev.drinkshop;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.util.List;
import java.util.UUID;

import gitau.dev.drinkshop.Adapters.DrinkAdapter;
import gitau.dev.drinkshop.Model.Drinks;
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

public class DrinkListActivity extends AppCompatActivity implements UploadCallBack {

    private static final int PICK_FILE_REQUEST = 900;
    private static final int REQUEST_PERMISSION_CODE = 1001;

    IDrinkShopApi mService;
    RecyclerView drinks_recycler;

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    FloatingActionButton btn_add;


    ImageView img_product;
    EditText edt_drink_name, edt_drink_price;

    Uri select_uri = null;
    String uploaded_image_path = "";


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            break;
            default:
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_list);

        mService = Common.getApi();

        btn_add = findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddDrinkDialog();
            }
        });

        drinks_recycler = findViewById(R.id.drinks_recycler);
        drinks_recycler.setHasFixedSize(true);
        drinks_recycler.setLayoutManager(new GridLayoutManager(this, 2));

        loadDrinksList(Common.currentCategory.getID());

    }

    private void showAddDrinkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Product");

        View product_layout = LayoutInflater.from(this).inflate(R.layout.add_new_product_layout, null);

        edt_drink_name = product_layout.findViewById(R.id.edt_drink_name);
        edt_drink_price = product_layout.findViewById(R.id.edt_drink_price);
        img_product = product_layout.findViewById(R.id.img_prod_browser);


        //Event
        img_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(Intent.createChooser(FileUtils.createGetContentIntent(), "Select a File"),
                        PICK_FILE_REQUEST);
            }
        });

        builder.setView(product_layout);
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                uploaded_image_path = "";
                select_uri = null;

            }
        }).setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (edt_drink_name.getText().toString().isEmpty()) {
                    Toast.makeText(DrinkListActivity.this, "Please Enter Name Of product", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (edt_drink_price.getText().toString().isEmpty()) {
                    Toast.makeText(DrinkListActivity.this, "Please Enter price Of product", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (uploaded_image_path.isEmpty()) {
                    Toast.makeText(DrinkListActivity.this, "Please Select Image For Product", Toast.LENGTH_SHORT).show();
                    return;
                }

                compositeDisposable.add(mService.insertNewDrink(edt_drink_name.getText().toString(),
                        uploaded_image_path,
                        edt_drink_price.getText().toString(),
                        Common.currentCategory.ID)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) {
                                Toast.makeText(DrinkListActivity.this, s, Toast.LENGTH_SHORT).show();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                Toast.makeText(DrinkListActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                );

            }
        }).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FILE_REQUEST) {
                if (data != null) {
                    select_uri = data.getData();
                    if (select_uri != null && !select_uri.getPath().isEmpty()) {
                        img_product.setImageURI(select_uri);
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
                    mService.uploadDrinkFile(body)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    //After uploading we will get file name and return string containing link of image
                                    uploaded_image_path = new StringBuilder(Common.BASE_URL)
                                            .append("server/Product/product_img/")
                                            .append(response.body().toString())
                                            .toString();
                                    Log.d("IMGPATH", uploaded_image_path);
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(DrinkListActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).start();
        }
    }

    private void loadDrinksList(String id) {
        compositeDisposable.add(mService.getDrink(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Drinks>>() {
                    @Override
                    public void accept(List<Drinks> drinks) {
                        displayDrinksList(drinks);
                    }
                }));
    }

    private void displayDrinksList(List<Drinks> drinks) {
        DrinkAdapter adapter = new DrinkAdapter(this, drinks);
        drinks_recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        loadDrinksList(Common.currentCategory.ID);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onProgressUpdate(int percentage) {

    }
}
