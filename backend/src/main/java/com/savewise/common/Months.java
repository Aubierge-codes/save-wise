package com.savewise.common;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;

public final class Months {

    private Months() {
    }

    /** The requested month, or the current one when the client did not ask for a specific month. */
    public static YearMonth orCurrent(YearMonth month, Clock clock) {
        return month != null ? month : YearMonth.now(clock);
    }

    public static LocalDate first(YearMonth month) {
        return month.atDay(1);
    }

    public static LocalDate last(YearMonth month) {
        return month.atEndOfMonth();
    }
}
