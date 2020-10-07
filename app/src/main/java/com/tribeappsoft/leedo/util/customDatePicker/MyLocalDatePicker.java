package com.tribeappsoft.leedo.util.customDatePicker;

import android.content.Context;
import android.util.AttributeSet;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;


/*
 * Created by ${ROHAN} on 20/6/19.
 */
public class MyLocalDatePicker extends MyDatePicker {

    private LocalDate minDate;
    private LocalDate maxDate;
    private OnLocalDatePickListener onLocalDatePickListener;
    private OnLocalDateSelectedListener onLocalDateSelectedListener;

    //region CONSTRUCTORS
    public MyLocalDatePicker(Context context) {
        this(context, null);
    }

    public MyLocalDatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyLocalDatePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    //endregion

    //region PROTECTED METHOD
    @Override
    protected void onDatePick() {
        if (onLocalDatePickListener != null) {
            onLocalDatePickListener.onLocalDatePick(getLocalDate());
        }
    }

    @Override
    protected void onDateSelected() {
        if (onLocalDateSelectedListener != null) {
            onLocalDateSelectedListener.onLocalDateSelected(getLocalDate() != null);
        }
    }

    @Override
    protected boolean minDateIsNotNull() {
        return minDate != null;
    }

    @Override
    protected boolean maxDateIsNotNull() {
        return maxDate != null;
    }

    @Override
    protected boolean checkMinDate(StringBuilder dateToCheckTmp) {
        LocalDate realDateToCheckTmp = stringToLocalDate(dateToCheckTmp.toString(), dateFormat.getValue());
        return realDateToCheckTmp == null || realDateToCheckTmp.isBefore(minDate);
    }

    @Override
    protected boolean checkMaxDate(StringBuilder dateToCheckTmp) {
        LocalDate realDateToCheckTmp = stringToLocalDate(dateToCheckTmp.toString(), dateFormat.getValue());
        return realDateToCheckTmp == null || realDateToCheckTmp.isAfter(maxDate);
    }

    @Override
    protected boolean checkSameDate(StringBuilder dateToCheckTmp) {
        LocalDate realDateToCheckTmp = stringToLocalDate(dateToCheckTmp.toString(), dateFormat.getValue());
        return dateToString(realDateToCheckTmp, dateFormat.getValue()).equals(dateToCheckTmp.toString());
    }
    //endregion

    //region PUBLIC METHOD
    public LocalDate getLocalDate() {
        if (date.length() == LENGTH_DATE_COMPLETE) {
            return stringToLocalDate(date, dateFormat.getValue());
        }
        return null;
    }

    public boolean setLocalDate(LocalDate newDate) {
        String tmpDate = dateToString(newDate, dateFormat.getValue());

        if (tmpDate.length() != LENGTH_DATE_COMPLETE
                || (minDate != null && newDate.isBefore(minDate))
                || (maxDate != null && newDate.isAfter(maxDate))) {
            return false;
        }

        this.date = tmpDate;

        fillDate();

        return true;
    }

    public void setMinLocalDate(LocalDate minDate) {
        this.minDate = minDate;
        clear();
    }

    public void setMaxLocalDate(LocalDate maxDate) {
        this.maxDate = maxDate;
        clear();
    }

    public void setOnLocalDatePickListener(OnLocalDatePickListener onLocalDatePickListener) {
        this.onLocalDatePickListener = onLocalDatePickListener;
    }

    public void setOnLocalDateSelectedListener(OnLocalDateSelectedListener onLocalDateSelectedListener) {
        this.onLocalDateSelectedListener = onLocalDateSelectedListener;
    }
    //endregion

    //region UTILS
    public static String dateToString(LocalDate date, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(date);
    }

    public static LocalDate stringToLocalDate(String date, String format) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(format));
    }
    //endregion

    public interface OnLocalDatePickListener {
        void onLocalDatePick(LocalDate dateSelected);
    }

    public interface OnLocalDateSelectedListener {
        void onLocalDateSelected(Boolean dateSelected);
    }

}
