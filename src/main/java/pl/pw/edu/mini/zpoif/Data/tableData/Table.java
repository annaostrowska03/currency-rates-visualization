package pl.pw.edu.mini.zpoif.Data.tableData;


public class Table {
    private String currency;
    private String date;
    private double rate;

    public Table(String currency, String date, double rate) {
        this.currency = currency;
        this.rate = rate;
        this.date = date;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }


}
