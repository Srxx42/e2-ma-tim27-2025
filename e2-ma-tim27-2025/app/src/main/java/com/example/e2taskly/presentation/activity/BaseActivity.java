package com.example.e2taskly.presentation.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import androidx.annotation.MenuRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import com.example.e2taskly.R;
import java.lang.reflect.Method;

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract int getMenuResourceId();
    protected abstract boolean handleMenuItemClick(MenuItem item);

    protected abstract void preparePopupMenu(PopupMenu popupMenu);
    protected void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ImageView menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this, view);
            popupMenu.getMenuInflater().inflate(getMenuResourceId(), popupMenu.getMenu());

            try {
                Method method = popupMenu.getMenu().getClass().getDeclaredMethod("setOptionalIconsVisible", boolean.class);
                method.setAccessible(true);
                method.invoke(popupMenu.getMenu(), true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            preparePopupMenu(popupMenu);
            popupMenu.setOnMenuItemClickListener(this::handleMenuItemClick);
            popupMenu.show();
        });
    }
}