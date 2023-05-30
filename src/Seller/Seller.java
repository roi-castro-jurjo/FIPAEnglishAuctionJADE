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
    private AID[] _buyers;
    SellerGUI gui;

    private ArrayList<Auction> _auctions = new ArrayList<>();

    private final String[] books = new String[5];

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

        addBehaviour(new GettingQueries());
        addBehaviour(new GettingBids());
        addBehaviour(new HandlingBids(this, _bidDelay));

    }

    private class GettingQueries extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate queryTemplate = MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF);
            ACLMessage query = receive(queryTemplate);
            if (query != null) {
                System.out.println(getLocalName() + " >> Query recibido del agente " + query.getSender().getLocalName());
                System.out.println(getLocalName() + " >> Contenido del query: " + query.getContent());

                ArrayList<String> requestedBooks = new ArrayList<>();
                Collections.addAll(requestedBooks, query.getContent().split(","));

                for (Auction auction : _auctions){
                    if (requestedBooks.contains(auction.get_book()) && auction.isActive()){
                        ACLMessage replyQuery =  query.createReply();
                        replyQuery.setPerformative(ACLMessage.INFORM);
                        replyQuery.setContent(auction.get_book());

                        send(replyQuery);
                    }
                }

            } else {
                block();
            }

            _auctions = gui.get_auctions();
        }
    }

    private class GettingBids extends CyclicBehaviour{

        @Override
        public void action() {
            MessageTemplate bidTemplate = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage bid = receive(bidTemplate);
            if (bid != null){
                System.out.println(getLocalName() + " >> Got a bid of " + bid.getContent() + " from " + bid.getSender().getLocalName());
                gui.modificarFilaTabla("primero", "b1", 111);
            } else {
                block();
            }
        }
    }

    private class HandlingBids extends TickerBehaviour {
        public HandlingBids(Agent agent, long time){
            super(agent, time);
        }

        @Override
        protected void onTick() {

        }
    }
}
