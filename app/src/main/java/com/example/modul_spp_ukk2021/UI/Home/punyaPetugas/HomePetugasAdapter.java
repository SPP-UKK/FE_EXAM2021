package com.example.modul_spp_ukk2021.UI.Home.punyaPetugas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.modul_spp_ukk2021.R;
import com.example.modul_spp_ukk2021.UI.Home.punyaSiswa.DataSiswa;
import com.example.modul_spp_ukk2021.UI.Home.punyaSiswa.HomeSiswaAdapter;

import java.util.ArrayList;

public class HomePetugasAdapter extends RecyclerView.Adapter<HomePetugasAdapter.ViewhHolder> {
    private ArrayList<DataSiswa> arrayList = new ArrayList<>();

    public HomePetugasAdapter(ArrayList<DataSiswa> arrayList){
        this.arrayList = arrayList;
    }

    @Override
    public ViewhHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.container_nama_tagihan, parent, false);
        return new HomePetugasAdapter.ViewhHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewhHolder holder, int position) {
        holder.nama.setText(arrayList.get(position).getNama());
        holder.nominal.setText(arrayList.get(position).getNominal());
        holder.tgl_bayar.setText((CharSequence) arrayList.get(position).getTgl_bayar());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewhHolder extends RecyclerView.ViewHolder {

        TextView nama, nominal, tgl_bayar;

        public ViewhHolder(View itemView) {
            super(itemView);
            nama = (TextView) itemView.findViewById(R.id.textView);
            nominal = (TextView) itemView.findViewById(R.id.textView2);
            tgl_bayar = (TextView) itemView.findViewById(R.id.textView3);
        }
    }

}
