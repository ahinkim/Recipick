package org.techtown.recipe.weather;

import android.view.View;

import org.techtown.recipe.main.MainAdapter;

public interface OnWeatherItemClickListener {
    public void onItemClick(WeatherAdapter.ViewHolder holder, View view, int position);
}