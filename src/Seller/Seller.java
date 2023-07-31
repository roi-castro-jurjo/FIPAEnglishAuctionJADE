package Seller;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.*;

import java.util.*;

public class Seller extends Agent {
    SellerGUI gui;

    private ArrayList<Auction> _auctions = new ArrayList<>();

    private final Integer _bidDelay = 10000;


    @Override
    protected void setup() {
        DFAgentDescription sellerDescription = new DFAgentDescription();
        sellerDescription.setName(this.getAID());

        ServiceDescription sellerService = new ServiceDescription();
        sellerService.setType("seller");
        sellerService.setName("Seller");

        sellerDescription.addServices(sellerService);

        try {
            DFService.register(this, sellerDescription);
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }

        gui = new SellerGUI();
        gui.set_sellerName(this.getLocalName());
        gui.setTitle(this.getLocalName());
        gui.setVisible(true);

        addBehaviour(new GettingQueries(this, 500));
        addBehaviour(new GettingBids(this, 500));
        addBehaviour(new RemovingBidders(this, 500));
        addBehaviour(new HandlingBids(this, _bidDelay));

    }

    private class GettingQueries extends TickerBehaviour {

        public GettingQueries(Agent a, long period) {
            super(a, period);
        }

        @Override
        public void onTick() {
            //System.out.println("GettingQueries");
            MessageTemplate queryTemplate = MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF);
            ACLMessage query = receive(queryTemplate);
            if (query != null) {
                //System.out.println(getLocalName() + " >> Query recibido del agente " + query.getSender().getLocalName());
                //System.out.println(getLocalName() + " >> Contenido del query: " + query.getContent());

                ArrayList<String> requestedBooks = new ArrayList<>();
                Collections.addAll(requestedBooks, query.getContent().split(","));

                for (Auction auction : _auctions) {
                    if (requestedBooks.contains(auction.get_book()) && auction.isActive() && !auction.get_bidders().contains(query.getSender())){
                        ACLMessage replyQuery = query.createReply();
                        replyQuery.setPerformative(ACLMessage.INFORM);
                        replyQuery.setContent(auction.get_book() + "," + auction.get_actualPrice() + "," + auction.get_bidIncrement());
                        send(replyQuery);
                    }
                }

            }
            _auctions = gui.get_auctions();
            block();
        }
    }

    private class GettingBids extends TickerBehaviour{

        public GettingBids(Agent a, long period) {
            super(a, period);
        }

        @Override
        public void onTick() {
            //System.out.println("GettingBids");

            MessageTemplate bidTemplate = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage bid = receive(bidTemplate);
            if (bid != null){
                String[] newBid = bid.getContent().split(",");
                System.out.println(getLocalName() + " >> Got a bid of " + newBid[1] + " for " + newBid[0] + " from " + bid.getSender().getLocalName());

                for (Auction auction : _auctions){
                    if (auction.get_book().equals(newBid[0]) && auction.isActive()){
                        ArrayList<AID> actualBidders = auction.get_bidders();
                        if (!actualBidders.contains(bid.getSender())){
                            actualBidders.add(bid.getSender());
                        }
                        auction.set_bidders(actualBidders);
                        auction.set_highestBid(Integer.parseInt(newBid[1]));
                    }
                }
            }

            block();
        }
    }

    private class RemovingBidders extends TickerBehaviour{

        public RemovingBidders(Agent a, long period) {
            super(a, period);
        }

        @Override
        public void onTick() {
            //System.out.println("RemovingBidders");
            MessageTemplate bidTemplate = MessageTemplate.MatchPerformative(ACLMessage.REFUSE);
            ACLMessage refusal = receive(bidTemplate);
            if (refusal != null){
                System.out.println(getLocalName() + " >> " + refusal.getSender().getLocalName() + " got out of " + refusal.getContent());

                for (Auction auction : _auctions){
                    if (auction.get_book().equals(refusal.getContent()) && auction.isActive()){
                        //System.out.println("Salimos");
                        ArrayList<AID> actualBidders = auction.get_bidders();
                        //System.out.println("adios");
                        actualBidders.remove(refusal.getSender());
                        auction.set_bidders(actualBidders);
                    }
                }
            }
            block();
        }
    }

    private class HandlingBids extends TickerBehaviour {
        public HandlingBids(Agent agent, long time){
            super(agent, time);
        }

        @Override
        protected void onTick() {
            System.out.println("Ejecutando bidding");
            for (Auction auction : _auctions){
                ArrayList<AID> actualBidders = auction.get_bidders();
                if (!actualBidders.isEmpty() && auction.isActive()){
                    if (actualBidders.size() == 1){
                        auction.setWinner(actualBidders.get(0));
                        auction.set_actualPrice(auction.get_highestBid());
                        gui.modificarFilaTabla(auction.get_book(), auction.getWinner().getLocalName(), auction.get_actualPrice());


                        auction.setActive(false);

                        // ENviar mensaje de que ha ganado

                        ACLMessage winningMessage = new ACLMessage(ACLMessage.AGREE);
                        winningMessage.addReceiver(actualBidders.get(0));
                        winningMessage.setContent(auction.get_book() + "," + (auction.get_actualPrice()));

                        send(winningMessage);

                        gui.moveRowToFinishedAuctions(auction.get_book());

                    } else {
                        auction.setWinner(actualBidders.get(0));

                        auction.set_actualPrice(auction.get_highestBid());

                        gui.modificarFilaTabla(auction.get_book(), auction.getWinner().getLocalName(), auction.get_actualPrice());

                        ACLMessage newBid = new ACLMessage(ACLMessage.REFUSE);

                        for (AID bidder : actualBidders){
                            newBid.addReceiver(bidder);
                        }
                        newBid.setContent(auction.get_book() + "," + auction.get_actualPrice() + "," + auction.get_bidIncrement());
                        send(newBid);
                        gui.restarValorColumnaTiempo(auction.get_book());
                    }

                    ACLMessage message = receive();
                    while (message != null){
                        message = receive();
                    }

                    //auction.set_bidders(new ArrayList<>());
                }
            }
            block();
        }
    }
}
