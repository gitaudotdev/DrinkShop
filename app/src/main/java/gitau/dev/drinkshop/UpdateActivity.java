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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.UUID;

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

public class UpdateActivity extends AppCompatActivity implements UploadCallBack {

    private static final int PICK_FILE_REQUEST = 5555;
    ImageView img_browser;
    EditText edt_name;
    Button btn_update, btn_delete;

    IDrinkShopApi mService;

    CompositeDisposable disposable;

    Uri select_uri = null;
    String uploaded_img_path = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        //Views
        edt_name = findViewById(R.id.edt_upname);
        img_browser = findViewById(R.id.up_img_browser);

        btn_delete = findViewById(R.id.btn_delete);
        btn_update = findViewById(R.id.btn_update);

        mService = Common.getApi();

        disposable = new CompositeDisposable();

        displayData();

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

    }

    private void deleteCategory() {
        disposable.add(mService.deleteCategory(Common.currentCategory.getID()).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        Toast.makeText(UpdateActivity.this, s, Toast.LENGTH_SHORT).show();


                        finish();


                    }
                }));
    }

    private void updateCategory() {
        if (!edt_name.getText().toString().isEmpty())
            disposable.add(mService.updateCategory(Common.currentCategory.ID,
                    edt_name.getText().toString(),
                    uploaded_img_path)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            Toast.makeText(UpdateActivity.this, s, Toast.LENGTH_SHORT).show();
                            uploaded_img_path = "";
                            select_uri = null;

                            Common.currentCategory = null;

                            finish();


                        }
                    }));
    }

    private void displayData() {
        if (Common.currentCategory != null) {
            Picasso.with(this)
                    .load(Common.currentCategory.getLink())
                    .into(img_browser);

            edt_name.setText(Common.currentCategory.getName());

            uploaded_img_path = Common.currentCategory.getLink();
        }
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
                                    Toast.makeText(UpdateActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).start();
        }
    }

    @Override
    protected void onDestroy() {
        disposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        disposable.clear();
        super.onStop();
    }

    @Override
    public void onProgressUpdate(int percentage) {

    }
}
