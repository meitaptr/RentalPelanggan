package com.example.meita.rentalpelanggan.MenuPembayaran;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.example.meita.rentalpelanggan.MenuPencarian.RentalModel;
import com.example.meita.rentalpelanggan.R;
import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DaftarRekeningPembayaran extends AppCompatActivity {
    private List<RentalModel> rentalModel;
    private RecyclerView recyclerView;
    private DaftarRekeningPembayaranAdapter adapter;
    private DatabaseReference mDatabase;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Daftar Rekening Pembayaran");
        setContentView(R.layout.activity_daftar_rekening_pembayaran);
        Firebase.setAndroidContext(this);

        recyclerView = (RecyclerView) findViewById(R.id.listViewRekeningPembayaran);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        rentalModel = new ArrayList<>();

        progressBar = (ProgressBar) findViewById(R.id.progress_circle);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FEBD3D"), PorterDuff.Mode.SRC_ATOP);

        getDaftarRekeningPembayaran();

    }

    public void getDaftarRekeningPembayaran() {
        final String idRental = getIntent().getStringExtra("idRental");
        final String idKendaraan = getIntent().getStringExtra("idKendaraan");
        final String idPemesanan = getIntent().getStringExtra("idPemesanan");
        final String kategoriKendaraan = getIntent().getStringExtra("kategoriKendaraan");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("rentalKendaraan").child(idRental).child("rekeningPembayaran").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    progressBar.setVisibility(View.VISIBLE);
                    RentalModel dataRental = postSnapshot.getValue(RentalModel.class);
                    rentalModel.add(dataRental);
                }
                adapter = new DaftarRekeningPembayaranAdapter(rentalModel, DaftarRekeningPembayaran.this, idRental, idKendaraan, idPemesanan, kategoriKendaraan);
                //adding adapter to recyclerview
                recyclerView.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
