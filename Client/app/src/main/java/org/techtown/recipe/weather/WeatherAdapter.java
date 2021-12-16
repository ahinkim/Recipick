package org.techtown.recipe.weather;

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


public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> implements OnWeatherItemClickListener {

    ArrayList<WeatherItem> items=new ArrayList<WeatherItem>();

    OnWeatherItemClickListener listener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(viewGroup.getContext());
        View itemView=inflater.inflate(R.layout.weather_item,viewGroup,false);
        return new ViewHolder(itemView,this);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.ViewHolder viewHolder, int position) {
        WeatherItem item=items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(WeatherItem item){
        items.add(item);
    }

    public void setItems(ArrayList<WeatherItem> items){
        this.items=items;
    }

    public WeatherItem getItem(int position){
        return items.get(position);
    }
    public void setOnItemClickListener(OnWeatherItemClickListener listener){
        this.listener=listener;
    }
    @Override
    public void onItemClick(ViewHolder holder, View view, int position) {
        if(listener!=null){
            listener.onItemClick(holder,view,position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView WeatherImageView;
        TextView WeatherTitleTextView;

        public ViewHolder(@NonNull View itemView, final OnWeatherItemClickListener listener) {
            super(itemView);
            WeatherImageView=itemView.findViewById(R.id.weatherImageView);
            WeatherTitleTextView=itemView.findViewById(R.id.weatherTitleTextView);
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
        public void setItem(WeatherItem item){
            String imgPath=item.getMenuImg();

            if(imgPath!=null&&!imgPath.equals("")){
                Glide.with(itemView.getContext()).load(imgPath).into(WeatherImageView);
            }
            WeatherTitleTextView.setText(item.getRecipeTitle());

        }
    }
}