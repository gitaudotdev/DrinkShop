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

import gitau.dev.drinkshop.Adapters.ViewHolders.menuViewHolder;
import gitau.dev.drinkshop.DrinkListActivity;
import gitau.dev.drinkshop.Interface.IItemClickListener;
import gitau.dev.drinkshop.Model.Category;
import gitau.dev.drinkshop.R;
import gitau.dev.drinkshop.UpdateActivity;
import gitau.dev.drinkshop.Utils.Common;

public class MenuAdapter extends RecyclerView.Adapter<menuViewHolder> {

    Context mContext;
    List<Category> mCategoryList;


    public MenuAdapter(Context context, List<Category> categoryList) {
        mContext = context;
        mCategoryList = categoryList;
    }

    @NonNull
    @Override
    public menuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.menu_item_layout, parent, false);
        return new menuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull menuViewHolder holder, final int position) {
        Picasso.with(mContext).load(mCategoryList.get(position).Link)
                .into(holder.product_image);

        holder.txt_product_name.setText(mCategoryList.get(position).Name);

        //implement item Click
        holder.setItemClickListener(new IItemClickListener() {

            @Override
            public void onClick(View view, boolean isLongClick) {
                if (isLongClick) {
                    //assign global category variable
                    Common.currentCategory = mCategoryList.get(position);
                    // start newActivity
                    mContext.startActivity(new Intent(mContext, UpdateActivity.class));
                } else {
                    ///assign global category variable
                    Common.currentCategory = mCategoryList.get(position);
                    // start newActivity
                    mContext.startActivity(new Intent(mContext, DrinkListActivity.class));
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return mCategoryList.size();
    }
}
