package nl.utwente.ing.model;

public class History {
    private double open;
    private double close;
    private double high;
    private double low;
    private double volume;

    public History(double open, double close, double high, double low, double volume) {
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.volume = volume;
    }

    public double getClose() {
        return close;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getOpen() {
        return open;
    }

    public double getVolume() {
        return volume;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }
}
