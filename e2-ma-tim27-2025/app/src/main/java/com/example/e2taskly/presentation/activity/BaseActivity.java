package com.example.e2taskly.presentation.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import androidx.annotation.MenuRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import com.example.e2taskly.R;

public abstract class BaseActivity extends AppCompatActivity {

    // Apstraktna metoda koja vraća ID menu resursa.
    // Svaka aktivnost koja naslijedi BaseActivity MORA je implementirati.
    @MenuRes
    protected abstract int getMenuResourceId();

    // Apstraktna metoda za obradu klikova na stavke menija.
    // Svaka aktivnost će pružiti svoju specifičnu logiku.
    protected abstract boolean handleMenuItemClick(MenuItem item);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Pomoćna metoda za postavljanje Toolbara.
    // Pozvat ćete je iz onCreate() svake vaše aktivnosti.
    protected void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ImageView menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this, view);
            // Koristi menu ID koji je definiran u pod-klasi (npr. ProfileActivity)
            popupMenu.getMenuInflater().inflate(getMenuResourceId(), popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(this::handleMenuItemClick);
            popupMenu.show();
        });
    }
}