package org.techtown.recipe.main;

import android.view.View;

import org.techtown.recipe.main.MainAdapter;

public interface OnMainItemClickListener {
    public void onItemClick(MainAdapter.ViewHolder holder, View view, int position);
}