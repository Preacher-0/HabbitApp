package taluzek.apps.habbit;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SkillDetail extends AppCompatActivity {
    DBHelper dbHelper;
    SQLiteDatabase db;
    int skillId;
    TextView skillName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        // pouzivani DB - otvoreni
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        // ziskani id habbitu z mainu
        skillId = getIntent().getIntExtra("skill_id", -1);

        // nazev habbitu
        skillName = findViewById(R.id.skillName);
        loadSkillName();

        // nastaveni kalendara
        MaterialCalendarView calendarView = findViewById(R.id.calendarView);
        List<CalendarDay> progressDates = getProgressDates(skillId); // načti dny kdy byl habbit plněn
        Decor decorator = new Decor(androidx.core.content.ContextCompat.getColor(this, R.color.colorPrimary), progressDates); // nastaveni "oznaceni"
        calendarView.addDecorator(decorator); // uz nastaveni - dani tecky do kolendare

        // pridani nebo odebrani tecky do kalendare
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(MaterialCalendarView widget, CalendarDay date, boolean selected) {
                String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", date.getYear(), date.getMonth() + 1, date.getDay());

                String query = "SELECT id FROM progress WHERE skill_id = " + skillId + " AND date = '" + dateStr + "'";
                Cursor c = db.rawQuery(query, null);

                if (c.moveToFirst()) {
                    // pokud uz mame zaznam a chceme ho smazat tak tady to delame
                    String deleteQuery = "DELETE FROM progress WHERE skill_id = " + skillId + " AND date = '" + dateStr + "'";
                    db.execSQL(deleteQuery);
                    Toast.makeText(SkillDetail.this, "Odebrán záznam pro " + dateStr, Toast.LENGTH_SHORT).show();
                } else {
                    // kdyz nove klikneme na den tak tady se prida zaznam do db
                    String insertQuery = "INSERT INTO progress (skill_id, date, note) VALUES (" + skillId + ", '" + dateStr + "', 'Ruční označení')";
                    db.execSQL(insertQuery);
                    Toast.makeText(SkillDetail.this, "Přidán záznam pro " + dateStr, Toast.LENGTH_SHORT).show();
                }
                c.close();
                loadCalendarHighlights(); // aktualizace kalendare
            }
        });

        // button na mazani celeho skillu / habbitu
        Button deleteBtn = findViewById(R.id.deleteSkill);
        deleteBtn.setBackgroundColor(Color.RED);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteBtn.setBackgroundColor(Color.RED);
                db.execSQL("DELETE FROM progress WHERE skill_id = " + skillId);
                db.execSQL("DELETE FROM skills WHERE id = " + skillId);
                finish(); // zavře aktivitu
            }
        });

        // some notifikace bullshid - z toho co chapu, tak se tohle stara o povoleni notifikaci
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    // nazev skill/habbi z db
    private void loadSkillName() {
        String query = "SELECT name FROM skills WHERE id = " + skillId;
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            String name = c.getString(0);
            skillName.setText(name);
        }
        c.close();
    }

    // vrati seznam kdy se ma "zateckovat" do kalendarte
    private List<CalendarDay> getProgressDates(int skillId) {
        List<CalendarDay> dates = new ArrayList<>();
        String query = "SELECT date FROM progress WHERE skill_id = " + skillId;
        Cursor c = db.rawQuery(query, null);
        while (c.moveToNext()) {
            String dateString = c.getString(0);
            try {
                String[] parts = dateString.substring(0, 10).split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                CalendarDay cal = CalendarDay.from(year, month - 1, day);
                dates.add(cal);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        c.close();
        return dates;
    }

    // obnova kalendare s uz tracknutejma dnama
    private void loadCalendarHighlights() {
        MaterialCalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.removeDecorators();
        List<CalendarDay> progressDates = getProgressDates(skillId);
        Decor decorator = new Decor(androidx.core.content.ContextCompat.getColor(this, R.color.colorPrimary), progressDates);
        calendarView.addDecorator(decorator);
    }
}
