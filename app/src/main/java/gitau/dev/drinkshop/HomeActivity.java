package gitau.dev.drinkshop;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.util.List;
import java.util.UUID;

import gitau.dev.drinkshop.Adapters.MenuAdapter;
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


public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, UploadCallBack {


    private static final int REQUEST_PERMISSION_CODE = 1000;
    private static final int PICK_FILE_REQUEST = 1;
    RecyclerView menu_recycler;
    CompositeDisposable disposable = new CompositeDisposable();
    IDrinkShopApi mService;

    EditText edt_name;
    ImageView img_category;

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
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    }, REQUEST_PERMISSION_CODE
            );


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddCategoryDialog();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Views
        menu_recycler = findViewById(R.id.menu_recycler);
        menu_recycler.setLayoutManager(new GridLayoutManager(this, 2));
        menu_recycler.setHasFixedSize(true);

        mService = Common.getApi();

        getMenu();
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Category");

        View category_layout = LayoutInflater.from(this).inflate(R.layout.add_category_layout, null);

        edt_name = category_layout.findViewById(R.id.edt_name);
        img_category = category_layout.findViewById(R.id.img_browser);

        //Event
        img_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(Intent.createChooser(FileUtils.createGetContentIntent(), "Select a File"),
                        PICK_FILE_REQUEST);
            }
        });

        builder.setView(category_layout);
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
                if (edt_name.getText().toString().isEmpty()) {
                    Toast.makeText(HomeActivity.this, "Please Enter Name Of category", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (uploaded_image_path.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "Please Select Image For Category", Toast.LENGTH_SHORT).show();
                    return;
                }
                disposable.add(mService.insertNewCategory(edt_name.getText().toString(), uploaded_image_path)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) {
                                Toast.makeText(HomeActivity.this, s, Toast.LENGTH_SHORT).show();

                                getMenu();

                                uploaded_image_path = "";
                                select_uri = null;
                            }
                        }));
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
                        img_category.setImageURI(select_uri);
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
                                    uploaded_image_path = new StringBuilder(Common.BASE_URL)
                                            .append("server/Category/category_img/")
                                            .append(response.body().toString())
                                            .toString();
                                    Log.d("IMGPATH", uploaded_image_path);
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(HomeActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).start();
        }
    }

    private void getMenu() {
        disposable.add(mService.getMenu().observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<Category>>() {
                    @Override
                    public void accept(List<Category> categories) {
                        displayMenuList(categories);
                    }
                }));
    }

    private void displayMenuList(List<Category> categories) {

        Common.menuList = categories;

        MenuAdapter adapter = new MenuAdapter(this, categories);
        menu_recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMenu();
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
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onProgressUpdate(int percentage) {

    }
}
