package gitau.dev.drinkshop.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.List;

import gitau.dev.drinkshop.Adapters.ViewHolders.DrinkListViewHolder;
import gitau.dev.drinkshop.Interface.IItemClickListener;
import gitau.dev.drinkshop.Model.Drinks;
import gitau.dev.drinkshop.R;
import gitau.dev.drinkshop.UpdateProductActivity;
import gitau.dev.drinkshop.Utils.Common;

public class DrinkAdapter extends RecyclerView.Adapter<DrinkListViewHolder> {

    Context mContext;
    List<Drinks> drinksList;


    public DrinkAdapter(Context context, List<Drinks> drinksList) {
        mContext = context;
        this.drinksList = drinksList;
    }

    @NonNull
    @Override
    public DrinkListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.drink_item_layout, viewGroup, false);
        return new DrinkListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DrinkListViewHolder holder, final int position) {
        Picasso.with(mContext).load(drinksList.get(position).Link).into(holder.img_product);
        holder.txt_price.setText(new StringBuilder("Kshs.").append(drinksList.get(position).Price).toString());
        holder.txt_drink_name.setText(drinksList.get(position).Name);

        //Event  - anti crash for null item click
        holder.setItemClickListener(new IItemClickListener() {
            @Override
            public void onClick(View view, boolean isLongClick) {
                Common.currentDrink = drinksList.get(position);
                mContext.startActivity(new Intent(mContext, UpdateProductActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return drinksList.size();
    }
}
