package com.example.zaverecka;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    DBHelper dbHelper;
    SQLiteDatabase db;
    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = findViewById(R.id.skillList);
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        // talciko na novy habbit /skill
        Button addBtn = findViewById(R.id.addSkill);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddSkillDialog();
            }
        });

        // nacte skilly z db
        loadSkills();

        // znovuzapnuti notifikaci
        SharedPreferences prefs = getSharedPreferences("notif_prefs", MODE_PRIVATE);
        boolean aktivni = prefs.getBoolean("notifikaceZapnuta", false);
        int hour = prefs.getInt("hour", 8);    // výchozí hodina
        int minute = prefs.getInt("minute", 0); // výchozí minuta

        if (aktivni) {
            UpozorneniHelper.scheduleDailyNotification(this, hour, minute);
        }
    }

    // nacteni skill z db a udela z nich klikaci tlacitka
    private void loadSkills() {
        layout.removeAllViews(); // vyčisti seznam
        Cursor c = db.rawQuery("SELECT * FROM skills", null);
        while (c.moveToNext()) {
            String name = c.getString(1); // název skillu
            int id = c.getInt(0);         // id skillu

            // vytvareni tlacitka
            Button btn = new Button(this);
            btn.setText(name);
            btn.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDetail(id);
                }
            });

            layout.addView(btn); // přidání do layoutu
        }
        c.close();
    }

    // vyskakovaci okno pri davani noveho inputu
    private void showAddSkillDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Nový habbit");

        final EditText input = new EditText(this); // pole pro zadání jména
        b.setView(input);

        // tlacitko pridat
        b.setPositiveButton("Přidat", (dialog, which) -> {
            String name = input.getText().toString();
            String sql = "INSERT INTO skills (name) VALUES ('" + name + "')";
            db.execSQL(sql);

            loadSkills(); // aktualizace seznamu
        });

        // zrusit cudlik
        b.setNegativeButton("Zrušit", null);
        b.show();
    }

    // otevre uz habbit /skill s kalendarem atd
    private void openDetail(int skillId) {
        Intent intent = new Intent(this, SkillDetail.class);
        intent.putExtra("skill_id", skillId); // přenášíme id skillu
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSkills(); // po návratu z detailu obnoví seznam skillů
    }
}
