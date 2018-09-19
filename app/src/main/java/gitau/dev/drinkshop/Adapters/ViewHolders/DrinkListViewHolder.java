package gitau.dev.drinkshop.Adapters.ViewHolders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import gitau.dev.drinkshop.Interface.IItemClickListener;
import gitau.dev.drinkshop.R;

public class DrinkListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView img_product;
    public TextView txt_drink_name, txt_price;

    IItemClickListener itemClickListener;

    public DrinkListViewHolder(@NonNull View itemView) {
        super(itemView);
        img_product = itemView.findViewById(R.id.img_product);
        txt_drink_name = itemView.findViewById(R.id.txt_drink_name);
        txt_price = itemView.findViewById(R.id.txt_drink_price);

        itemView.setOnClickListener(this);

    }

    public void setItemClickListener(IItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, false);
    }
}
