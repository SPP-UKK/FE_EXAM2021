package com.example.modul_spp_ukk2021.UI.UI.Home.punyaSiswa;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.modul_spp_ukk2021.R;
import com.example.modul_spp_ukk2021.UI.Data.Model.Pembayaran;
import com.google.android.material.card.MaterialCardView;

import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TagihanSiswaAdapter extends RecyclerView.Adapter<TagihanSiswaAdapter.ViewHolder> {
    private Context context;
    private List<Pembayaran> pembayaran;

    public TagihanSiswaAdapter(Context context, List<Pembayaran> pembayaran) {
        this.context = context;
        this.pembayaran = pembayaran;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ps_container_data_tagihan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Pembayaran pembayaran = this.pembayaran.get(position);

        Locale localeID = new Locale("in", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
        format.setMaximumFractionDigits(0);

        DateFormatSymbols symbols = new DateFormatSymbols(localeID);
        String[] monthNames = symbols.getMonths();
        holder.tvBulan.setText(monthNames[pembayaran.getBulan_bayar() - 1] + " " + pembayaran.getTahun_bayar());

        if (pembayaran.getTgl_bayar() != null) {
            SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy", localeID);
            String strDt = simpleDate.format(pembayaran.getTgl_bayar());
            holder.tvTanggal.setText(strDt);
        } else {
            holder.tvTanggal.setText("--/--/----");
        }

        if (pembayaran.getKurang_bayar() == 0) {
            holder.tvStatus.setText(pembayaran.getStatus_bayar());
            holder.tvNominal.setText(format.format(pembayaran.getNominal()));
            holder.tvNominal.setTextColor(Color.parseColor("#FF4646"));
            holder.materialCardView.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FF4646")));
        } else {
            holder.tvStatus.setText("Kurang");
            holder.tvNominal.setText(format.format(pembayaran.getKurang_bayar()));
            holder.tvNominal.setTextColor(Color.parseColor("#FFC700"));
            holder.materialCardView.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFC700")));
        }
    }

    @Override
    public int getItemCount() {
        return pembayaran.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView materialCardView;
        TextView tvBulan, tvNominal, tvStatus, tvTanggal;

        public ViewHolder(View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.status);
            tvBulan = itemView.findViewById(R.id.namaBulan);
            tvNominal = itemView.findViewById(R.id.nominal);
            tvTanggal = itemView.findViewById(R.id.tanggal);
            materialCardView = itemView.findViewById(R.id.materialCardView);
        }
    }
}