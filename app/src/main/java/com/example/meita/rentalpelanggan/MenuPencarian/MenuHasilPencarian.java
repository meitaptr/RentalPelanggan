package com.example.meita.rentalpelanggan.MenuPencarian;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.example.meita.rentalpelanggan.MainActivity;
import com.example.meita.rentalpelanggan.MenuPemesanan.PemesananModel;
import com.example.meita.rentalpelanggan.R;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.google.android.gms.vision.text.Line;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MenuHasilPencarian extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MenuHasilPencarianAdapter adapter;
    private List<KendaraanModel> kendaraanModel;
    private DatabaseReference mDatabase;
    Date tglSewaPencarian, tglKembaliPencarian, tglSewaDipesan, tglKembaliDipesan;
    int jmlKendaraan, jmlKendaraanPencarian, jmlKendaraanDipesan, sum, hargaAwal, hargaAkhir;
    String idKendaraanChecking;
    TextView kategoriToolbar, tglToolbar;
    ProgressBar progressBar;
    Button buttonFilter, buttonTerdekat;
    ImageView kendaraanTidakTersedia;
    LinearLayout linearLayoutListKendaraan;
    ActionBar actionBar;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_hasil_pencarian);
        Firebase.setAndroidContext(this);

        recyclerView = (RecyclerView) findViewById(R.id.listViewKendaraan);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        kendaraanModel = new ArrayList<>();
        kategoriToolbar = (TextView) findViewById(R.id.kategoriKendaraan);
        tglToolbar = (TextView) findViewById(R.id.tglSewa);
        buttonFilter = (Button) findViewById(R.id.btn_filter);
        buttonTerdekat = (Button) findViewById(R.id.btn_terdekat);
        kendaraanTidakTersedia = (ImageView) findViewById(R.id.ic_kendaran_noavailable);
        linearLayoutListKendaraan = (LinearLayout) findViewById(R.id.linearLayoutListKendaraan);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progress_circle);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FEBD3D"), PorterDuff.Mode.SRC_ATOP);
        progressBar.setVisibility(View.VISIBLE);
        kendaraanTidakTersedia.setVisibility(View.GONE);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        final String kategoriKendaraanPencarian = getIntent().getStringExtra("kategoriKendaraanPencarian");
        final String tanggalSewaPencarian = getIntent().getStringExtra("tglSewaPencarian");
        final String tanggalKembaliPencarian = getIntent().getStringExtra("tglKembaliPencarian");

        String tgl = tanggalSewaPencarian + " - " + tanggalKembaliPencarian;
        kategoriToolbar.setText(kategoriKendaraanPencarian);
        tglToolbar.setText(tgl);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        buttonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                showDialogFilterPencarian();
            }
        });

        kendaraanModel.clear();
        getHasilPencarian();
    }

    public void showDialogFilterPencarian() {
        final Dialog dialog = new Dialog(MenuHasilPencarian.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog_filter);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        dialog.getWindow().setAttributes(layoutParams);

        final CrystalRangeSeekbar rangeSeekbar = (CrystalRangeSeekbar) dialog.findViewById(R.id.rangeSeekbar1);
        final TextView txthargaAwal = (TextView) dialog.findViewById(R.id.txt_harga_awal);
        final TextView txthargaAkhir = (TextView) dialog.findViewById(R.id.txt_harga_akhir);
        final CheckBox denganSopir = (CheckBox) dialog.findViewById(R.id.checkBoxDenganSupir);
        final CheckBox tanpaSopir = (CheckBox) dialog.findViewById(R.id.checkBoxTanpaSupir);
        final CheckBox denganBahanBakar = (CheckBox) dialog.findViewById(R.id.checkBoxDenganBahanBakar);
        final CheckBox tanpaBahanBakar = (CheckBox) dialog.findViewById(R.id.checkBoxTanpaBahanBakar);
        Button btnYa = (Button) dialog.findViewById(R.id.btn_filter_ya);
        Button btnTidak = (Button) dialog.findViewById(R.id.btn_filter_tidak);


        rangeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                if ((maxValue.intValue() < 2900000 && maxValue.intValue() != 0) || (maxValue.intValue() < 2900000)) {
                    txthargaAwal.setText(String.valueOf(minValue));
                    txthargaAkhir.setText(String.valueOf(maxValue));
                    hargaAwal = minValue.intValue();
                    hargaAkhir = maxValue.intValue();
                    denganSopir.setEnabled(false);
                    tanpaSopir.setEnabled(false);
                    denganBahanBakar.setEnabled(false);
                    tanpaBahanBakar.setEnabled(false);
                } else {
                    denganSopir.setEnabled(true);
                    tanpaSopir.setEnabled(true);
                    denganBahanBakar.setEnabled(true);
                    tanpaBahanBakar.setEnabled(true);
                }


            }
        });

        denganSopir.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (denganSopir.isChecked()) {
                    tanpaSopir.setEnabled(false);
                    denganBahanBakar.setEnabled(false);
                    tanpaBahanBakar.setEnabled(false);
                    rangeSeekbar.setEnabled(false);
                } else if (!denganSopir.isChecked()) {
                    tanpaSopir.setEnabled(true);
                    denganBahanBakar.setEnabled(true);
                    tanpaBahanBakar.setEnabled(true);
                    rangeSeekbar.setEnabled(true);
                }
            }
        });

        tanpaSopir.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (tanpaSopir.isChecked()) {
                    denganSopir.setEnabled(false);
                    denganBahanBakar.setEnabled(false);
                    tanpaBahanBakar.setEnabled(false);
                    rangeSeekbar.setEnabled(false);
                } else if (!denganSopir.isChecked()) {
                    denganSopir.setEnabled(true);
                    denganBahanBakar.setEnabled(true);
                    tanpaBahanBakar.setEnabled(true);
                    rangeSeekbar.setEnabled(true);
                }
            }
        });

        denganBahanBakar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (denganBahanBakar.isChecked()) {
                    denganSopir.setEnabled(false);
                    tanpaSopir.setEnabled(false);
                    tanpaBahanBakar.setEnabled(false);
                    rangeSeekbar.setEnabled(false);
                } else if (!denganSopir.isChecked()) {
                    denganSopir.setEnabled(true);
                    tanpaSopir.setEnabled(true);
                    tanpaBahanBakar.setEnabled(true);
                    rangeSeekbar.setEnabled(true);
                }
            }
        });

        tanpaBahanBakar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (tanpaBahanBakar.isChecked()) {
                    denganSopir.setEnabled(false);
                    tanpaSopir.setEnabled(false);
                    denganBahanBakar.setEnabled(false);
                    rangeSeekbar.setEnabled(false);
                } else if (!denganSopir.isChecked()) {
                    denganSopir.setEnabled(true);
                    tanpaSopir.setEnabled(true);
                    denganBahanBakar.setEnabled(true);
                    rangeSeekbar.setEnabled(true);
                }
            }
        });

        btnYa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        btnYa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((hargaAkhir < 2900000 && hargaAkhir != 0) || (hargaAkhir < 2900000)) {
                    Toast.makeText(dialog.getContext(), String.valueOf(hargaAwal) + " - " + String.valueOf(hargaAkhir), Toast.LENGTH_SHORT).show();
                    getFilterHarga();
                    dialog.dismiss();
                } else if (denganSopir.isChecked()) {
                    getFilterDenganSopir();
                    dialog.dismiss();
                } else if (tanpaSopir.isChecked()) {
                    getFilterTanpaSopir();
                    dialog.dismiss();
                } else if (denganBahanBakar.isChecked()) {
                    getFilterDenganBahanBakar();
                    dialog.dismiss();
                } else if (tanpaBahanBakar.isChecked()) {
                    getFilterTanpaBahanBakar();
                    dialog.dismiss();
                }

            }
        });

        btnTidak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                progressBar.setVisibility(View.GONE);
            }
        });

        dialog.show();

    }

    public void getHasilPencarian() {
        kendaraanModel.clear();
        final String kategoriKendaraanPencarian = getIntent().getStringExtra("kategoriKendaraanPencarian");
        final String jumlahKendaraanPencarian = getIntent().getStringExtra("jumlahKendaraanPencarian");
        final String tanggalSewaPencarian = getIntent().getStringExtra("tglSewaPencarian");
        final String tanggalKembaliPencarian = getIntent().getStringExtra("tglKembaliPencarian");
        jmlKendaraanPencarian = Integer.parseInt(jumlahKendaraanPencarian);
        final ArrayList<Integer> listJumlah = new ArrayList<>();

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            tglSewaPencarian = format.parse(tanggalSewaPencarian);
            tglKembaliPencarian = format.parse(tanggalKembaliPencarian);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mDatabase.child("kendaraan").child(kategoriKendaraanPencarian).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        final KendaraanModel kendaraan = postSnapshot.getValue(KendaraanModel.class);
                        jmlKendaraan = kendaraan.getJumlahKendaraan();
                        final int jmlKendaraanModel = jmlKendaraan;

                        Firebase ref = new Firebase("https://bismillahskripsi-44a73.firebaseio.com/cekKetersediaanKendaraan");
                        Query query = ref.orderByChild("idKendaraan").equalTo(kendaraan.getIdKendaraan());
                        query.addValueEventListener(new com.firebase.client.ValueEventListener() {
                            @Override
                            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                                for (com.firebase.client.DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    PemesananModel pemesanan = postSnapshot.getValue(PemesananModel.class);
                                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                    jmlKendaraanDipesan = pemesanan.getJumlahKendaraan();
                                    try {
                                        tglSewaDipesan = format.parse(pemesanan.getTglSewa());
                                        tglKembaliDipesan = format.parse(pemesanan.getTglKembali());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    if ((tglSewaPencarian.before(tglKembaliDipesan)
                                            || tglSewaPencarian.equals(tglKembaliDipesan)) && (tglKembaliPencarian.after(tglSewaDipesan)
                                            || tglKembaliPencarian.equals(tglSewaDipesan))
                                            || tglSewaPencarian.equals(tglSewaDipesan) && tglKembaliPencarian.equals(tglKembaliDipesan)) {
                                        listJumlah.add(jmlKendaraanDipesan);
                                        sum = 0;
                                        for (int i = 0; i < listJumlah.size(); i++) {
                                            sum += listJumlah.get(i);
                                            jmlKendaraanDipesan = sum;
                                        }
                                        int a = jmlKendaraanPencarian + jmlKendaraanDipesan;
                                        if (jmlKendaraanModel < a) {
                                            kendaraanModel.remove(kendaraan);
                                            adapter.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                    break;
                                }
                                progressBar.setVisibility(View.GONE);

                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });

                        mDatabase.child("cekKetersediaanKendaraan").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.child(kendaraan.getIdKendaraan()).exists()) {
                                    kendaraanModel.add(kendaraan);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    linearLayoutListKendaraan.setVisibility(View.GONE);
                    kendaraanTidakTersedia.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        adapter = new MenuHasilPencarianAdapter(MenuHasilPencarian.this, kendaraanModel, tanggalSewaPencarian, tanggalKembaliPencarian, jumlahKendaraanPencarian);
        recyclerView.setAdapter(adapter);
    }

    public void getFilterHarga() {
        kendaraanModel.clear();
        final String kategoriKendaraanPencarian = getIntent().getStringExtra("kategoriKendaraanPencarian");
        final String jumlahKendaraanPencarian = getIntent().getStringExtra("jumlahKendaraanPencarian");
        final String tanggalSewaPencarian = getIntent().getStringExtra("tglSewaPencarian");
        final String tanggalKembaliPencarian = getIntent().getStringExtra("tglKembaliPencarian");
        jmlKendaraanPencarian = Integer.parseInt(jumlahKendaraanPencarian);

        final ArrayList<Integer> listJumlah = new ArrayList<>();

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            tglSewaPencarian = format.parse(tanggalSewaPencarian);
            tglKembaliPencarian = format.parse(tanggalKembaliPencarian);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mDatabase.child("kendaraan").child(kategoriKendaraanPencarian).orderByChild("hargaSewa").startAt(hargaAwal).endAt(hargaAkhir).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    kendaraanTidakTersedia.setVisibility(View.GONE);
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        final KendaraanModel kendaraan = postSnapshot.getValue(KendaraanModel.class);
                        idKendaraanChecking = kendaraan.getIdKendaraan();
                        jmlKendaraan = kendaraan.getJumlahKendaraan();
                        final int jmlKendaraanModel = jmlKendaraan;

                        Firebase ref = new Firebase("https://bismillahskripsi-44a73.firebaseio.com/cekKetersediaanKendaraan");
                        Query query = ref.orderByChild("idKendaraan").equalTo(kendaraan.getIdKendaraan());
                        query.addValueEventListener(new com.firebase.client.ValueEventListener() {
                            @Override
                            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                                for (com.firebase.client.DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    PemesananModel pemesanan = postSnapshot.getValue(PemesananModel.class);
                                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                    jmlKendaraanDipesan = pemesanan.getJumlahKendaraan();

                                    try {
                                        tglSewaDipesan = format.parse(pemesanan.getTglSewa());
                                        tglKembaliDipesan = format.parse(pemesanan.getTglKembali());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    if ((tglSewaPencarian.before(tglKembaliDipesan) || tglSewaPencarian.equals(tglKembaliDipesan)) && (tglKembaliPencarian.after(tglSewaDipesan) || tglKembaliPencarian.equals(tglSewaDipesan))
                                            || tglSewaPencarian.equals(tglSewaDipesan) && tglKembaliPencarian.equals(tglKembaliDipesan)) {
                                        listJumlah.add(jmlKendaraanDipesan);
                                        sum = 0;
                                        for (int i = 0; i < listJumlah.size(); i++) {
                                            sum += listJumlah.get(i);
                                            jmlKendaraanDipesan = sum;
                                        }
                                        int a = jmlKendaraanPencarian + jmlKendaraanDipesan;
                                        //int abc = jmlKendaraanModel;
                                        if (jmlKendaraanModel < a) {
                                            Toast.makeText(getApplicationContext(), "REMOVE DI EKSEKUSI", Toast.LENGTH_SHORT).show();
                                            kendaraanModel.remove(kendaraan);
                                            adapter.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                    break;
                                }
                                progressBar.setVisibility(View.GONE);

                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });

                        mDatabase.child("cekKetersediaanKendaraan").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.child(kendaraan.getIdKendaraan()).exists()) {
                                    kendaraanModel.add(kendaraan);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(null);
                    kendaraanTidakTersedia.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }); // breakpoint addValueEventListener query child kendaraan
        progressBar.setVisibility(View.GONE);
        adapter = new MenuHasilPencarianAdapter(MenuHasilPencarian.this, kendaraanModel, tanggalSewaPencarian, tanggalKembaliPencarian, jumlahKendaraanPencarian);
        adapter.notifyDataSetChanged();
        //adding adapter to recyclerview
        recyclerView.setAdapter(adapter);
        //progress bar berhenti ketika cardview muncul

    }

    public void getFilterDenganSopir() {
        kendaraanModel.clear();
        final String kategoriKendaraanPencarian = getIntent().getStringExtra("kategoriKendaraanPencarian");
        final String jumlahKendaraanPencarian = getIntent().getStringExtra("jumlahKendaraanPencarian");
        final String tanggalSewaPencarian = getIntent().getStringExtra("tglSewaPencarian");
        final String tanggalKembaliPencarian = getIntent().getStringExtra("tglKembaliPencarian");
        jmlKendaraanPencarian = Integer.parseInt(jumlahKendaraanPencarian);

        final ArrayList<Integer> listJumlah = new ArrayList<>();

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            tglSewaPencarian = format.parse(tanggalSewaPencarian);
            tglKembaliPencarian = format.parse(tanggalKembaliPencarian);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mDatabase.child("kendaraan").child(kategoriKendaraanPencarian).orderByChild("supir").equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    kendaraanTidakTersedia.setVisibility(View.GONE);
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        final KendaraanModel kendaraan = postSnapshot.getValue(KendaraanModel.class);
                        idKendaraanChecking = kendaraan.getIdKendaraan();
                        jmlKendaraan = kendaraan.getJumlahKendaraan();
                        final int jmlKendaraanModel = jmlKendaraan;

                        Firebase ref = new Firebase("https://bismillahskripsi-44a73.firebaseio.com/cekKetersediaanKendaraan");
                        Query query = ref.orderByChild("idKendaraan").equalTo(kendaraan.getIdKendaraan());
                        query.addValueEventListener(new com.firebase.client.ValueEventListener() {
                            @Override
                            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                                for (com.firebase.client.DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    PemesananModel pemesanan = postSnapshot.getValue(PemesananModel.class);
                                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                    jmlKendaraanDipesan = pemesanan.getJumlahKendaraan();

                                    try {
                                        tglSewaDipesan = format.parse(pemesanan.getTglSewa());
                                        tglKembaliDipesan = format.parse(pemesanan.getTglKembali());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    if ((tglSewaPencarian.before(tglKembaliDipesan) || tglSewaPencarian.equals(tglKembaliDipesan)) && (tglKembaliPencarian.after(tglSewaDipesan) || tglKembaliPencarian.equals(tglSewaDipesan))
                                            || tglSewaPencarian.equals(tglSewaDipesan) && tglKembaliPencarian.equals(tglKembaliDipesan)) {
                                        listJumlah.add(jmlKendaraanDipesan);
                                        sum = 0;
                                        for (int i = 0; i < listJumlah.size(); i++) {
                                            sum += listJumlah.get(i);
                                            jmlKendaraanDipesan = sum;
                                        }
                                        int a = jmlKendaraanPencarian + jmlKendaraanDipesan;
                                        //int abc = jmlKendaraanModel;
                                        if (jmlKendaraanModel < a) {
                                            Toast.makeText(getApplicationContext(), "REMOVE DI EKSEKUSI", Toast.LENGTH_SHORT).show();
                                            kendaraanModel.remove(kendaraan);
                                            adapter.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                    break;
                                }
                                progressBar.setVisibility(View.GONE);

                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });

                        mDatabase.child("cekKetersediaanKendaraan").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.child(kendaraan.getIdKendaraan()).exists()) {
                                    kendaraanModel.add(kendaraan);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(null);
                    kendaraanTidakTersedia.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }); // breakpoint addValueEventListener query child kendaraan
        progressBar.setVisibility(View.GONE);
        adapter = new MenuHasilPencarianAdapter(MenuHasilPencarian.this, kendaraanModel, tanggalSewaPencarian, tanggalKembaliPencarian, jumlahKendaraanPencarian);
        adapter.notifyDataSetChanged();
        //adding adapter to recyclerview
        recyclerView.setAdapter(adapter);
    }

    public void getFilterTanpaSopir() {
        kendaraanModel.clear();
        final String kategoriKendaraanPencarian = getIntent().getStringExtra("kategoriKendaraanPencarian");
        final String jumlahKendaraanPencarian = getIntent().getStringExtra("jumlahKendaraanPencarian");
        final String tanggalSewaPencarian = getIntent().getStringExtra("tglSewaPencarian");
        final String tanggalKembaliPencarian = getIntent().getStringExtra("tglKembaliPencarian");
        jmlKendaraanPencarian = Integer.parseInt(jumlahKendaraanPencarian);

        final ArrayList<Integer> listJumlah = new ArrayList<>();

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            tglSewaPencarian = format.parse(tanggalSewaPencarian);
            tglKembaliPencarian = format.parse(tanggalKembaliPencarian);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mDatabase.child("kendaraan").child(kategoriKendaraanPencarian).orderByChild("supir").equalTo(false).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    kendaraanTidakTersedia.setVisibility(View.GONE);
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        final KendaraanModel kendaraan = postSnapshot.getValue(KendaraanModel.class);
                        idKendaraanChecking = kendaraan.getIdKendaraan();
                        jmlKendaraan = kendaraan.getJumlahKendaraan();
                        final int jmlKendaraanModel = jmlKendaraan;

                        Firebase ref = new Firebase("https://bismillahskripsi-44a73.firebaseio.com/cekKetersediaanKendaraan");
                        Query query = ref.orderByChild("idKendaraan").equalTo(kendaraan.getIdKendaraan());
                        query.addValueEventListener(new com.firebase.client.ValueEventListener() {
                            @Override
                            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                                for (com.firebase.client.DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    PemesananModel pemesanan = postSnapshot.getValue(PemesananModel.class);
                                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                    jmlKendaraanDipesan = pemesanan.getJumlahKendaraan();

                                    try {
                                        tglSewaDipesan = format.parse(pemesanan.getTglSewa());
                                        tglKembaliDipesan = format.parse(pemesanan.getTglKembali());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    if ((tglSewaPencarian.before(tglKembaliDipesan) || tglSewaPencarian.equals(tglKembaliDipesan)) && (tglKembaliPencarian.after(tglSewaDipesan) || tglKembaliPencarian.equals(tglSewaDipesan))
                                            || tglSewaPencarian.equals(tglSewaDipesan) && tglKembaliPencarian.equals(tglKembaliDipesan)) {
                                        listJumlah.add(jmlKendaraanDipesan);
                                        sum = 0;
                                        for (int i = 0; i < listJumlah.size(); i++) {
                                            sum += listJumlah.get(i);
                                            jmlKendaraanDipesan = sum;
                                        }
                                        int a = jmlKendaraanPencarian + jmlKendaraanDipesan;
                                        //int abc = jmlKendaraanModel;
                                        if (jmlKendaraanModel < a) {
                                            Toast.makeText(getApplicationContext(), "REMOVE DI EKSEKUSI", Toast.LENGTH_SHORT).show();
                                            kendaraanModel.remove(kendaraan);
                                            adapter.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                    break;
                                }
                                progressBar.setVisibility(View.GONE);

                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
                        mDatabase.child("cekKetersediaanKendaraan").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.child(kendaraan.getIdKendaraan()).exists()) {
                                    kendaraanModel.add(kendaraan);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(null);
                    kendaraanTidakTersedia.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }); // breakpoint addValueEventListener query child kendaraan
        progressBar.setVisibility(View.GONE);
        adapter = new MenuHasilPencarianAdapter(MenuHasilPencarian.this, kendaraanModel, tanggalSewaPencarian, tanggalKembaliPencarian, jumlahKendaraanPencarian);
        adapter.notifyDataSetChanged();
        //adding adapter to recyclerview
        recyclerView.setAdapter(adapter);
    }

    public void getFilterDenganBahanBakar() {
        kendaraanModel.clear();
        final String kategoriKendaraanPencarian = getIntent().getStringExtra("kategoriKendaraanPencarian");
        final String jumlahKendaraanPencarian = getIntent().getStringExtra("jumlahKendaraanPencarian");
        final String tanggalSewaPencarian = getIntent().getStringExtra("tglSewaPencarian");
        final String tanggalKembaliPencarian = getIntent().getStringExtra("tglKembaliPencarian");
        jmlKendaraanPencarian = Integer.parseInt(jumlahKendaraanPencarian);

        final ArrayList<Integer> listJumlah = new ArrayList<>();

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            tglSewaPencarian = format.parse(tanggalSewaPencarian);
            tglKembaliPencarian = format.parse(tanggalKembaliPencarian);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mDatabase.child("kendaraan").child(kategoriKendaraanPencarian).orderByChild("bahanBakar").equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    kendaraanTidakTersedia.setVisibility(View.GONE);
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        final KendaraanModel kendaraan = postSnapshot.getValue(KendaraanModel.class);
                        idKendaraanChecking = kendaraan.getIdKendaraan();
                        jmlKendaraan = kendaraan.getJumlahKendaraan();
                        final int jmlKendaraanModel = jmlKendaraan;

                        Firebase ref = new Firebase("https://bismillahskripsi-44a73.firebaseio.com/cekKetersediaanKendaraan");
                        Query query = ref.orderByChild("idKendaraan").equalTo(kendaraan.getIdKendaraan());
                        query.addValueEventListener(new com.firebase.client.ValueEventListener() {
                            @Override
                            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                                for (com.firebase.client.DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    PemesananModel pemesanan = postSnapshot.getValue(PemesananModel.class);
                                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                    jmlKendaraanDipesan = pemesanan.getJumlahKendaraan();

                                    try {
                                        tglSewaDipesan = format.parse(pemesanan.getTglSewa());
                                        tglKembaliDipesan = format.parse(pemesanan.getTglKembali());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    if ((tglSewaPencarian.before(tglKembaliDipesan) || tglSewaPencarian.equals(tglKembaliDipesan)) && (tglKembaliPencarian.after(tglSewaDipesan) || tglKembaliPencarian.equals(tglSewaDipesan))
                                            || tglSewaPencarian.equals(tglSewaDipesan) && tglKembaliPencarian.equals(tglKembaliDipesan)) {
                                        listJumlah.add(jmlKendaraanDipesan);
                                        sum = 0;
                                        for (int i = 0; i < listJumlah.size(); i++) {
                                            sum += listJumlah.get(i);
                                            jmlKendaraanDipesan = sum;
                                        }
                                        int a = jmlKendaraanPencarian + jmlKendaraanDipesan;
                                        //int abc = jmlKendaraanModel;
                                        if (jmlKendaraanModel < a) {
                                            Toast.makeText(getApplicationContext(), "REMOVE DI EKSEKUSI", Toast.LENGTH_SHORT).show();
                                            kendaraanModel.remove(kendaraan);
                                            adapter.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                    break;
                                }
                                progressBar.setVisibility(View.GONE);

                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
                        mDatabase.child("cekKetersediaanKendaraan").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.child(kendaraan.getIdKendaraan()).exists()) {
                                    kendaraanModel.add(kendaraan);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(null);
                    kendaraanTidakTersedia.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }); // breakpoint addValueEventListener query child kendaraan
        progressBar.setVisibility(View.GONE);
        adapter = new MenuHasilPencarianAdapter(MenuHasilPencarian.this, kendaraanModel, tanggalSewaPencarian, tanggalKembaliPencarian, jumlahKendaraanPencarian);
        adapter.notifyDataSetChanged();
        //adding adapter to recyclerview
        recyclerView.setAdapter(adapter);
    }

    public void getFilterTanpaBahanBakar() {
        kendaraanModel.clear();
        final String kategoriKendaraanPencarian = getIntent().getStringExtra("kategoriKendaraanPencarian");
        final String jumlahKendaraanPencarian = getIntent().getStringExtra("jumlahKendaraanPencarian");
        final String tanggalSewaPencarian = getIntent().getStringExtra("tglSewaPencarian");
        final String tanggalKembaliPencarian = getIntent().getStringExtra("tglKembaliPencarian");
        jmlKendaraanPencarian = Integer.parseInt(jumlahKendaraanPencarian);

        final ArrayList<Integer> listJumlah = new ArrayList<>();

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            tglSewaPencarian = format.parse(tanggalSewaPencarian);
            tglKembaliPencarian = format.parse(tanggalKembaliPencarian);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mDatabase.child("kendaraan").child(kategoriKendaraanPencarian).orderByChild("bahanBakar").equalTo(false).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    kendaraanTidakTersedia.setVisibility(View.GONE);
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        final KendaraanModel kendaraan = postSnapshot.getValue(KendaraanModel.class);
                        idKendaraanChecking = kendaraan.getIdKendaraan();
                        jmlKendaraan = kendaraan.getJumlahKendaraan();
                        final int jmlKendaraanModel = jmlKendaraan;

                        Firebase ref = new Firebase("https://bismillahskripsi-44a73.firebaseio.com/cekKetersediaanKendaraan");
                        Query query = ref.orderByChild("idKendaraan").equalTo(kendaraan.getIdKendaraan());
                        query.addValueEventListener(new com.firebase.client.ValueEventListener() {
                            @Override
                            public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                                for (com.firebase.client.DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    PemesananModel pemesanan = postSnapshot.getValue(PemesananModel.class);
                                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                    jmlKendaraanDipesan = pemesanan.getJumlahKendaraan();

                                    try {
                                        tglSewaDipesan = format.parse(pemesanan.getTglSewa());
                                        tglKembaliDipesan = format.parse(pemesanan.getTglKembali());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    if ((tglSewaPencarian.before(tglKembaliDipesan) || tglSewaPencarian.equals(tglKembaliDipesan)) && (tglKembaliPencarian.after(tglSewaDipesan) || tglKembaliPencarian.equals(tglSewaDipesan))
                                            || tglSewaPencarian.equals(tglSewaDipesan) && tglKembaliPencarian.equals(tglKembaliDipesan)) {
                                        listJumlah.add(jmlKendaraanDipesan);
                                        sum = 0;
                                        for (int i = 0; i < listJumlah.size(); i++) {
                                            sum += listJumlah.get(i);
                                            jmlKendaraanDipesan = sum;
                                        }
                                        int a = jmlKendaraanPencarian + jmlKendaraanDipesan;
                                        //int abc = jmlKendaraanModel;
                                        if (jmlKendaraanModel < a) {
                                            Toast.makeText(getApplicationContext(), "REMOVE DI EKSEKUSI", Toast.LENGTH_SHORT).show();
                                            kendaraanModel.remove(kendaraan);
                                            adapter.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                    break;
                                }
                                progressBar.setVisibility(View.GONE);

                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
                        mDatabase.child("cekKetersediaanKendaraan").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.child(kendaraan.getIdKendaraan()).exists()) {
                                    kendaraanModel.add(kendaraan);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(null);
                    kendaraanTidakTersedia.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }); // breakpoint addValueEventListener query child kendaraan
        progressBar.setVisibility(View.GONE);
        adapter = new MenuHasilPencarianAdapter(MenuHasilPencarian.this, kendaraanModel, tanggalSewaPencarian, tanggalKembaliPencarian, jumlahKendaraanPencarian);
        adapter.notifyDataSetChanged();
        //adding adapter to recyclerview
        recyclerView.setAdapter(adapter);
    }
}