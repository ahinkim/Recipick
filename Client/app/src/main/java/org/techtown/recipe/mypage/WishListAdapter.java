package org.techtown.recipe.mypage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.techtown.recipe.R;

import java.util.ArrayList;


public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.ViewHolder> implements OnWishListItemClickListener {

    ArrayList<WishListItem> items=new ArrayList<WishListItem>();

    OnWishListItemClickListener listener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(viewGroup.getContext());
        View itemView=inflater.inflate(R.layout.favorite_item,viewGroup,false);
        return new ViewHolder(itemView,this);
    }

    @Override
    public void onBindViewHolder(@NonNull WishListAdapter.ViewHolder viewHolder, int position) {
        WishListItem item=items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(WishListItem item){
        items.add(item);
    }

    public void setItems(ArrayList<WishListItem> items){
        this.items=items;
    }

    public WishListItem getItem(int position){
        return items.get(position);
    }
    public void setOnItemClickListener(OnWishListItemClickListener listener){
        this.listener=listener;
    }
    @Override
    public void onItemClick(ViewHolder holder, View view, int position) {
        if(listener!=null){
            listener.onItemClick(holder,view,position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView favoriteImageView;
        TextView favoriteTitleTextView;

        public ViewHolder(@NonNull View itemView, final OnWishListItemClickListener listener) {
            super(itemView);
            favoriteImageView=itemView.findViewById(R.id.favoriteImageView);
            favoriteTitleTextView=itemView.findViewById(R.id.favoriteTitleTextView);
            itemView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    int position=getAdapterPosition();
                    if(listener!=null){
                        listener.onItemClick(ViewHolder.this,view,position);
                    }
                }
            });
        }
        public void setItem(WishListItem item){
            String imgPath=item.getMenuImg();

            if(imgPath!=null&&!imgPath.equals("")){
                Glide.with(itemView.getContext()).load(imgPath).into(favoriteImageView);
            }
            favoriteTitleTextView.setText(item.getRecipeTitle());

        }
    }
}