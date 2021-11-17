package org.techtown.recipe.grade;

import android.view.View;

public interface OnGradeItemClickListener {
    public void onItemClick(GradeAdapter.ViewHolder holder, View view, int position);
}