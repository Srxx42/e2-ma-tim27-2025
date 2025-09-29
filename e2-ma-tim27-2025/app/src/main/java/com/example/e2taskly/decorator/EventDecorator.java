package com.example.e2taskly.decorator;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan; // Možete obrisati
import android.text.style.LineBackgroundSpan; // Dodajte ovaj import
import java.util.HashSet;

public class EventDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> dates;
    private final LineBackgroundSpan span; // Dekorater sada drži referencu na SPAN

    public EventDecorator(HashSet<CalendarDay> dates, LineBackgroundSpan span) {
        this.dates = dates;
        this.span = span;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        // Samo primeni span koji smo mu prosledili
        view.addSpan(span);
    }
}