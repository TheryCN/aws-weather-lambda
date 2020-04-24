package com.github.therycn;

/**
 * Monthly Weather.
 */
public class MonthlyWeather {

    private String year;

    private String month;

    private String count;

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "MonthlyWeather{" +
                "year='" + year + '\'' +
                ", month='" + month + '\'' +
                ", count='" + count + '\'' +
                '}';
    }
}
