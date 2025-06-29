package taluzek.apps.habbit;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;

public class Decor implements DayViewDecorator {

    private final int color;                      // barva kolecka
    private final HashSet<CalendarDay> dates;     // dny kam se maji dat

    // prijme barvu a dny kam se to ma dat
    public Decor(int color, Collection<CalendarDay> dates) {
        this.color = color;
        // hashset je prej rychlejsi nez list protoze vyhledani je 0 a 1
        this.dates = new HashSet<>(dates);
    }

    // pokud ma mit kolecko tak vrati true
    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day); // pokud je den v seznamu, bude označen
    }

    // prida kolecko ke dnu
    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(8, color)); // velikost tečky = 8, barva = z konstruktoru
    }
}