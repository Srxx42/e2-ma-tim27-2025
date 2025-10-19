package com.example.e2taskly.presentation.adapter;




import static androidx.core.content.ContextCompat.getDrawable;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.e2taskly.R;
import com.example.e2taskly.model.TaskCategory;
import com.example.e2taskly.presentation.activity.ManageCategoryActivity;
import com.example.e2taskly.service.TaskCategoryService;
import com.example.e2taskly.service.TaskService;

import java.util.ArrayList;

public class CategoryListAdapter extends ArrayAdapter<TaskCategory> {
    private ArrayList<TaskCategory> aCategories;

    private TaskCategoryService taskCategoryService;

    private TaskService taskService;

    private Context context;

    public CategoryListAdapter(@NonNull Context context, @NonNull ArrayList<TaskCategory> categories) {
        super(context, R.layout.item_category, categories);
        aCategories = categories;

        taskCategoryService = new TaskCategoryService(context);
        taskService = new TaskService(context);

        this.context = context;
    }

    @Override
    public int getCount() {
        return aCategories.size();
    }

    @Nullable
    @Override
    public TaskCategory getItem(int position) {
        return aCategories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View converView, @NonNull ViewGroup parent) {
        TaskCategory category = getItem(position);
        if (converView == null) {
            converView = LayoutInflater.from(getContext()).inflate(R.layout.item_category,
                    parent, false);
        }
        LinearLayout categoryCard = converView.findViewById(R.id.categoryItem);
        TextView categoryName = converView.findViewById(R.id.categoryName);
        View categoryColor = converView.findViewById(R.id.colorView);
        ImageButton deleteButton = converView.findViewById(R.id.deleteButton);

        if (category != null) {
            categoryName.setText(category.getName());

            GradientDrawable drawable = (GradientDrawable) getDrawable(getContext(), R.drawable.colored_circles);
            drawable = (GradientDrawable) drawable.mutate();
            drawable.setColor(Color.parseColor(category.getColorHex()));
            categoryColor.setBackground(drawable);

            deleteButton.setOnClickListener(v -> {
                if(taskService.isThereTaskWithCategory(category.getId())){
                    Toast.makeText(context, "Pre nego što obrišete kategoriju, promenite sve njene taskove!", Toast.LENGTH_LONG).show();
                }else {
                    taskCategoryService.deleteById(category.getId());
                    aCategories.remove(category);
                    this.notifyDataSetChanged();
                }
            });


            categoryCard.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ManageCategoryActivity.class);
                intent.putExtra("CATEGORY_ID",category.getId());
                getContext().startActivity(intent);

            });
        }
            return converView;
    }
}
