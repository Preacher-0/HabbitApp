package taluzek.apps.habbit;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DBHelper dbHelper;
    SQLiteDatabase db;
    LinearLayout layout;
    DrawerLayout drawerLayout;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ujisti se, že máš layout s NavigationDrawer

        // připojení k databázi
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        // inicializace sdílených preferencí
        prefs = getSharedPreferences("notif_prefs", MODE_PRIVATE);
        editor = prefs.edit();

        // nastavení toolbaru a drawer menu
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // hlavní layout pro skilly
        layout = findViewById(R.id.skillList);

        // tlačítko pro přidání nového skillu
        Button addBtn = findViewById(R.id.addSkill);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddSkillDialog();
            }
        });

        // načti existující skilly
        loadSkills();

        // pokud je zapnutá notifikace, nastav ji znovu
        boolean aktivni = prefs.getBoolean("notifikaceZapnuta", false);
        int hour = prefs.getInt("hour", 8);
        int minute = prefs.getInt("minute", 0);

        if (aktivni) {
            UpozorneniHelper.scheduleDailyNotification(this, hour, minute);
        }

        // zobraz info o další notifikaci
        updateNotificationTexts();

        // pro Android 13+ musíme explicitně žádat o právo na notifikace
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

    }

    // načti všechny skilly z databáze a vytvoř z nich tlačítka
    private void loadSkills() {
        layout.removeAllViews();
        Cursor c = db.rawQuery("SELECT * FROM skills", null);
        while (c.moveToNext()) {
            String name = c.getString(1);
            int id = c.getInt(0);

            Button btn = new Button(this);
            btn.setText(name);
            // nastavení okrajů (margin)
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 16, 0, 16); // nahoře a dole 16px mezera
            btn.setLayoutParams(params);
            //btn.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            btn.setBackgroundResource(R.drawable.zaobleno);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDetail(id);
                }
            });
            layout.addView(btn);
        }
        c.close();
    }

    // otevři dialog pro přidání nového skillu
    private void showAddSkillDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Nový habbit");

        final EditText input = new EditText(this);
        b.setView(input);

        b.setPositiveButton("Přidat", (dialog, which) -> {
            String name = input.getText().toString();
            String sql = "INSERT INTO skills (name) VALUES ('" + name + "')";
            db.execSQL(sql);
            loadSkills();
        });

        b.setNegativeButton("Zrušit", null);
        b.show();
    }

    // otevři detail konkrétního skillu
    private void openDetail(int skillId) {
        Intent intent = new Intent(this, SkillDetail.class);
        intent.putExtra("skill_id", skillId);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSkills();
        updateNotificationTexts(); // pro jistotu obnov i text notifikace
    }

    // zpracování kliknutí v menu
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_toggle_notification) {
            boolean aktivni = prefs.getBoolean("notifikaceZapnuta", false);
            if (aktivni) {
                UpozorneniHelper.cancelNotification(this);
                editor.putBoolean("notifikaceZapnuta", false).apply();
                Toast.makeText(this, "Notifikace vypnuta", Toast.LENGTH_SHORT).show();
            } else {
                int h = prefs.getInt("hour", 8);
                int m = prefs.getInt("minute", 0);
                UpozorneniHelper.scheduleDailyNotification(this, h, m);
                editor.putBoolean("notifikaceZapnuta", true).apply();
                Toast.makeText(this, "Notifikace zapnuta", Toast.LENGTH_SHORT).show();
            }
            updateNotificationTexts();
        }

        if (id == R.id.menu_pick_time) {
            int h = prefs.getInt("hour", 8);
            int m = prefs.getInt("minute", 0);

            TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    editor.putInt("hour", hourOfDay);
                    editor.putInt("minute", minute);
                    editor.apply();

                    if (prefs.getBoolean("notifikaceZapnuta", false)) {
                        UpozorneniHelper.scheduleDailyNotification(MainActivity.this, hourOfDay, minute);
                    }

                    Toast.makeText(MainActivity.this, "Čas nastaven na " + hourOfDay + ":" + (minute < 10 ? "0" + minute : minute), Toast.LENGTH_SHORT).show();
                    updateNotificationTexts();
                }
            }, h, m, true);

            dialog.show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // metoda která aktualizuje text o stavu notifikace v navigation headeru
    private void updateNotificationTexts() {
        boolean aktivni = prefs.getBoolean("notifikaceZapnuta", false);
        int hour = prefs.getInt("hour", 8);
        int minute = prefs.getInt("minute", 0);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        View headerView = navigationView.getHeaderView(0);
        TextView infoText = headerView.findViewById(R.id.notificationInfoText);

        if (infoText != null) {
            if (aktivni) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }

                String timeText = String.format(
                        Locale.getDefault(),
                        "Další upozornění přijde: %tH:%tM, %tA %<td.%<tm.%<tY",
                        calendar, calendar, calendar
                );
                infoText.setText(timeText);
            } else {
                infoText.setText("Notifikace jsou vypnuté.");
            }
        }
    }
}
