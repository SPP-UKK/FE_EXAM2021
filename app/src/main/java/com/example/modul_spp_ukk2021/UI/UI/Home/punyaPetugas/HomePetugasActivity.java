package com.example.modul_spp_ukk2021.UI.UI.Home.punyaPetugas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.example.modul_spp_ukk2021.R;
import com.example.modul_spp_ukk2021.UI.DB.ApiEndPoints;
import com.example.modul_spp_ukk2021.UI.Data.Helper.DrawerAdapter;
import com.example.modul_spp_ukk2021.UI.Data.Helper.DrawerItem;
import com.example.modul_spp_ukk2021.UI.Data.Helper.SimpleItem;
import com.example.modul_spp_ukk2021.UI.Data.Model.Petugas;
import com.example.modul_spp_ukk2021.UI.Data.Model.Siswa;
import com.example.modul_spp_ukk2021.UI.Data.Repository.PetugasRepository;
import com.example.modul_spp_ukk2021.UI.Data.Repository.SiswaRepository;
import com.example.modul_spp_ukk2021.UI.UI.Splash.OnboardingActivity;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.modul_spp_ukk2021.UI.DB.baseURL.url;

public class HomePetugasActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener {
    private static final int POS_DASHBOARD = 0;
    private static final int POS_LOGOUT = 1;

    private EditText searchSiswa;
    private String[] screenTitles;
    private Drawable[] screenIcons;
    private RecyclerView recyclerView;
    private HomePetugasAdapter adapter;
    private SlidingRootNav slidingRootNav;
    private SharedPreferences sharedprefs;
    private TextView tagihan_count, nama, level;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean doubleBackToExitPressedOnce = false;
    private final List<Siswa> siswa = new ArrayList<>();
    private String username, password, nama_petugas, rank;
    private LottieAnimationView lottieAnim, loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pp_activity_home);
        sharedprefs = getSharedPreferences("myprefs", Context.MODE_PRIVATE);
        rank = sharedprefs.getString("levelStaff", null);
        username = sharedprefs.getString("usernameStaff", null);
        password = sharedprefs.getString("passwordStaff", null);

        nama = findViewById(R.id.nama);
        level = findViewById(R.id.level);
        tagihan_count = findViewById(R.id.siswa_count);
        lottieAnim = findViewById(R.id.lottieAnim);
        loadingProgress = findViewById(R.id.loadingProgress);
        searchSiswa = findViewById(R.id.searchSiswa);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        recyclerView = findViewById(R.id.recyclerSiswa);
        adapter = new HomePetugasAdapter(this, siswa);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refreshing();
            }
        });

        searchSiswa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 1) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(url)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    ApiEndPoints api = retrofit.create(ApiEndPoints.class);

                    Call<SiswaRepository> call = api.searchSiswa(s.toString().trim());
                    call.enqueue(new Callback<SiswaRepository>() {
                        @Override
                        public void onResponse(Call<SiswaRepository> call, Response<SiswaRepository> response) {
                            String value = response.body().getValue();
                            List<Siswa> results = response.body().getResult();

                            if (value.equals("1")) {
                                recyclerView.setVisibility(View.VISIBLE);
                                lottieAnim.pauseAnimation();
                                lottieAnim.setVisibility(LottieAnimationView.GONE);

                                adapter = new HomePetugasAdapter(HomePetugasActivity.this, results);
                                recyclerView.setAdapter(adapter);
                                runLayoutAnimation(recyclerView);

                                tagihan_count.setText("(" + results.size() + ")");

                            } else {
                                tagihan_count.setText("(0)");
                                recyclerView.setVisibility(View.GONE);
                                lottieAnim.setAnimation(R.raw.nodata);
                                lottieAnim.playAnimation();
                                lottieAnim.setVisibility(LottieAnimationView.VISIBLE);
                            }
                        }

                        @Override
                        public void onFailure(Call<SiswaRepository> call, Throwable t) {
                            tagihan_count.setText("(0)");
                            searchSiswa.setEnabled(false);
                            recyclerView.setVisibility(View.GONE);

                            lottieAnim.setAnimation(R.raw.nointernet);
                            lottieAnim.playAnimation();
                            lottieAnim.setVisibility(LottieAnimationView.VISIBLE);

                            if (swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            }

                            loadingProgress.pauseAnimation();
                            loadingProgress.setVisibility(LottieAnimationView.GONE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Toast.makeText(HomePetugasActivity.this, "Gagal koneksi sistem, silahkan coba lagi...", Toast.LENGTH_LONG).show();
                            Log.e("DEBUG", "Error: ", t);
                        }
                    });

                } else {
                    loadDataSiswa();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        SideNavSetup();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadingProgress.playAnimation();
        loadingProgress.setVisibility(LottieAnimationView.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        loadProfil();
    }

    @Override
    public void onBackPressed() {
        slidingRootNav.openMenu();
        if (slidingRootNav.isMenuOpened()) {
            if (doubleBackToExitPressedOnce) {
                finishAffinity();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Tekan lagi untuk keluar...", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    public void Refreshing() {
        swipeRefreshLayout.setRefreshing(true);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        loadProfil();
    }

    public void SideNavSetup() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        slidingRootNav = new SlidingRootNavBuilder(this)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withMenuLayout(R.layout.activity_sidenav)
                .withDragDistance(100)
                .withRootViewScale(0.8f)
                .withRootViewElevation(5)
                .inject();

        screenIcons = loadScreenIcons();
        screenTitles = loadScreenTitles();

        DrawerAdapter adapter = new DrawerAdapter(Arrays.asList(
                createItemFor(POS_DASHBOARD).setChecked(true),
                createItemFor(POS_LOGOUT)));
        adapter.setListener(this);
        adapter.setSelected(POS_DASHBOARD);

        RecyclerView list = findViewById(R.id.list);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(int position) {
        if (position == POS_LOGOUT) {
            sharedprefs.edit().clear().apply();
            Intent intent = new Intent(HomePetugasActivity.this, OnboardingActivity.class);
            intent.putExtra("skipBoarding", "skipBoarding");
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_profile) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_profile, findViewById(R.id.layoutDialogContainer));
            builder.setView(view);
            AlertDialog alertDialog = builder.create();

            ((TextView) view.findViewById(R.id.tvFillNama2)).setText(nama_petugas);
            ((TextView) view.findViewById(R.id.tvLevel)).setText("Staff level : " + rank);
            ((TextView) view.findViewById(R.id.tvUsername)).setText("Username : " + username);
            ((TextView) view.findViewById(R.id.tvPassword2)).setText("Password : " + password);

            view.findViewById(R.id.layoutDialog).setVisibility(View.GONE);
            view.findViewById(R.id.layoutDialog2).setVisibility(View.VISIBLE);
            view.findViewById(R.id.clear2).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                }
            });
            if (alertDialog.getWindow() != null) {
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            alertDialog.show();

        } else if (id == R.id.action_refresh) {
            Refreshing();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void runLayoutAnimation(final RecyclerView recyclerView) {
        Context context = recyclerView.getContext();
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_from_bottom);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.scheduleLayoutAnimation();
    }

    private void loadProfil() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiEndPoints api = retrofit.create(ApiEndPoints.class);

        Call<PetugasRepository> call = api.readProfilPetugas(username);
        call.enqueue(new Callback<PetugasRepository>() {
            @Override
            public void onResponse(Call<PetugasRepository> call, Response<PetugasRepository> response) {
                String value = response.body().getValue();
                List<Petugas> results = response.body().getResult();

                if (value.equals("1")) {
                    for (int i = 0; i < results.size(); i++) {
                        String[] strList = results.get(i).getNama_petugas().split(" ");
                        String first2Words = strList[0] + " " + strList[1];

                        nama.setText(first2Words);
                        nama_petugas = results.get(i).getNama_petugas();
                    }
                    level.setText("Staff level: " + rank);

                    searchSiswa.setEnabled(true);
                    loadingProgress.pauseAnimation();
                    loadingProgress.setVisibility(LottieAnimationView.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    loadDataSiswa();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    }, 700);
                }
            }

            @Override
            public void onFailure(Call<PetugasRepository> call, Throwable t) {
                tagihan_count.setText("(0)");
                searchSiswa.setEnabled(false);
                recyclerView.setVisibility(View.GONE);

                lottieAnim.setAnimation(R.raw.nointernet);
                lottieAnim.playAnimation();
                lottieAnim.setVisibility(LottieAnimationView.VISIBLE);

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                loadingProgress.pauseAnimation();
                loadingProgress.setVisibility(LottieAnimationView.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Toast.makeText(HomePetugasActivity.this, "Gagal koneksi sistem, silahkan coba lagi...", Toast.LENGTH_LONG).show();
                Log.e("DEBUG", "Error: ", t);
            }
        });
    }

    private void loadDataSiswa() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiEndPoints api = retrofit.create(ApiEndPoints.class);

        Call<SiswaRepository> call = api.readSiswa();
        call.enqueue(new Callback<SiswaRepository>() {
            @Override
            public void onResponse(Call<SiswaRepository> call, Response<SiswaRepository> response) {
                String value = response.body().getValue();
                List<Siswa> results = response.body().getResult();

                if (value.equals("1")) {
                    recyclerView.setVisibility(View.VISIBLE);
                    lottieAnim.pauseAnimation();
                    lottieAnim.setVisibility(LottieAnimationView.GONE);

                    adapter = new HomePetugasAdapter(HomePetugasActivity.this, results);
                    recyclerView.setAdapter(adapter);
                    runLayoutAnimation(recyclerView);

                    tagihan_count.setText("(" + results.size() + ")");

                }
            }

            @Override
            public void onFailure(Call<SiswaRepository> call, Throwable t) {
                Log.e("DEBUG", "Error: ", t);
            }
        });
    }

    @SuppressWarnings("rawtypes")
    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.grey300))
                .withTextTint(color(R.color.grey300))
                .withSelectedIconTint(color(R.color.red500))
                .withSelectedTextTint(color(R.color.red500));
    }

    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.pp_sideNavTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.pp_sideNavIcons);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }
}