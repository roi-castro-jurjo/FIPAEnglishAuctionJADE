package Seller;

import jade.core.AID;

import java.util.Objects;

public class Auction {
    private String _book;
    private Integer _bidIncrement;
    private Integer _actualPrice = 0;

    private AID winner;

    private boolean active = true;


    Auction(String book, Integer bidIncrement){
        this._book = book;
        this._bidIncrement = bidIncrement;
    }

    public String get_book() {
        return _book;
    }

    public void set_book(String _book) {
        this._book = _book;
    }

    public Integer get_bidIncrement() {
        return _bidIncrement;
    }

    public void set_bidIncrement(Integer _bidIncrement) {
        this._bidIncrement = _bidIncrement;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Auction{" +
                "_book='" + _book + '\'' +
                ", _bidIncrement=" + _bidIncrement +
                '}';
    }
}
