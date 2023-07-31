package Seller;

import jade.core.AID;

import java.util.ArrayList;
import java.util.Objects;

public class Auction {
    private String _book;
    private Integer _bidIncrement;
    private Integer _actualPrice = 0;

    private Integer _highestBid = 0;

    private AID winner;
    private ArrayList<AID> _bidders = new ArrayList<>();

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

    public Integer get_actualPrice() {
        return _actualPrice;
    }

    public void set_actualPrice(Integer _actualPrice) {
        this._actualPrice = _actualPrice;
    }

    public AID getWinner() {
        return winner;
    }

    public void setWinner(AID winner) {
        this.winner = winner;
    }

    public ArrayList<AID> get_bidders() {
        return _bidders;
    }

    public void set_bidders(ArrayList<AID> _bidders) {
        this._bidders = _bidders;
    }

    public Integer get_highestBid() {
        return _highestBid;
    }

    public void set_highestBid(Integer _highestBid) {
        this._highestBid = _highestBid;
    }

    @Override
    public String toString() {
        return "Auction{" +
                "_book='" + _book + '\'' +
                ", _bidIncrement=" + _bidIncrement +
                '}';
    }
}
