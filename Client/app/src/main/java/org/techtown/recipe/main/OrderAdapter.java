package org.techtown.recipe.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.techtown.recipe.R;
import org.w3c.dom.Text;

import java.util.ArrayList;


public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> implements OnOrderItemClickListener {

    ArrayList<OrderItem> items=new ArrayList<OrderItem>();

    OnOrderItemClickListener listener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(viewGroup.getContext());
        View itemView=inflater.inflate(R.layout.order_item,viewGroup,false);
        return new ViewHolder(itemView,this);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.ViewHolder viewHolder, int position) {
        OrderItem item=items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(OrderItem item){
        items.add(item);
    }

    public void setItems(ArrayList<OrderItem> items){
        this.items=items;
    }

    public OrderItem getItem(int position){
        return items.get(position);
    }
    public void setOnItemClickListener(OnOrderItemClickListener listener){
        this.listener=listener;
    }
    @Override
    public void onItemClick(ViewHolder holder, View view, int position) {
        if(listener!=null){
            listener.onItemClick(holder,view,position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView orderNumberTextView;
        TextView orderContentTextView;

        public ViewHolder(@NonNull View itemView, final OnOrderItemClickListener listener) {
            super(itemView);
            orderNumberTextView=itemView.findViewById(R.id.orderNumberTextView);
            orderContentTextView=itemView.findViewById(R.id.orderContentTextView);
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
        public void setItem(OrderItem item){
            orderNumberTextView.setText(item.getRecipe_order());
            orderContentTextView.setText(item.getDescription());

        }
    }
}