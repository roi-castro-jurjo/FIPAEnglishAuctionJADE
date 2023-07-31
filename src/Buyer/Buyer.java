package Buyer;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;


public class Buyer extends Agent {
    //private ArrayList<AID> _sellers = new ArrayList<>();
    private HashMap<String, Integer> _wishList;
    private ArrayList<String> _sentBids = new ArrayList<>();

    BuyerGUI gui;



    @Override
    protected void setup() {
        DFAgentDescription buyerDescription = new DFAgentDescription();
        buyerDescription.setName(this.getAID());
        
        ServiceDescription buyerService = new ServiceDescription();
        buyerService.setType("buyer");
        buyerService.setName("Buyer");

        buyerDescription.addServices(buyerService);

        try {
            DFService.register(this, buyerDescription);
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }

        _wishList = new HashMap<>();

        gui = new BuyerGUI();
        gui.setTitle(getLocalName());
        gui.setVisible(true);


        this.addBehaviour(new Scanning(this, 1000));
        this.addBehaviour(new ReceivingReply());

    }


    private class Scanning extends TickerBehaviour {

        public Scanning(Agent a, long period) {
            super(a, period);
        }

        @Override
        public void onTick() {
            ArrayList<String> foundBooks = gui.obtenerValoresPrimeraColumna();
            gui.eliminarValores(_wishList, foundBooks);

            HashMap<String, Integer> notFoundBooks = gui.obtenerLibrosBuscando();
            _wishList.putAll(notFoundBooks);

            if (!notFoundBooks.isEmpty()){
                ACLMessage buyQuery  = new ACLMessage(ACLMessage.QUERY_REF);

                String queryContent = String.join(",", notFoundBooks.keySet());

                buyQuery.setContent(queryContent);

                DFAgentDescription emisorDesc = new DFAgentDescription();
                ServiceDescription servicioDesc = new ServiceDescription();
                servicioDesc.setType("seller");
                emisorDesc.addServices(servicioDesc);

                try {
                    DFAgentDescription[] sellers = DFService.search(myAgent, emisorDesc);

                    for (DFAgentDescription seller : sellers){
                        //_sellers.add(seller.getName());
                        buyQuery.addReceiver(seller.getName());
                    }

                    send(buyQuery);
                    //System.out.println(getLocalName() + " >> " + "Enviadas querys.");
                    block();

                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private class ReceivingReply extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage message = receive();

            if (message != null){
                switch (message.getPerformative()){
                    case ACLMessage.INFORM -> {
                        System.out.println(getLocalName() + " >> Recibido inform de " + message.getSender().getLocalName() + ": " + message.getContent());
                        String[] proposition = message.getContent().split(",");

                        if (_wishList.get(proposition[0]) != null && (Integer.parseInt(proposition[1]) + Integer.parseInt(proposition[2])) <= _wishList.get(proposition[0])) {

                            int newPrice = Integer.parseInt(proposition[1]) + Integer.parseInt(proposition[2]);

                            ACLMessage reply = message.createReply();
                            reply.setPerformative(ACLMessage.PROPOSE);
                            reply.setContent(proposition[0] + "," + newPrice);

                            send(reply);

                            gui.cambiarEstadoLibro(proposition[0], "Puja enviada");
                            gui.actualizarLibro(proposition[0], proposition[1], "actualPrice");
                            gui.actualizarLibro(proposition[0], proposition[2], "minBid");

                            //_sentBids.add(proposition[0] + "," +_wishList.get(proposition[0]));
                        }
                    }

                    case ACLMessage.REFUSE -> {
                        System.out.println(getLocalName() + " >> Recibido refuse de " + message.getSender().getLocalName() + ": " + message.getContent());
                        String[] proposition = message.getContent().split(",");

                        if (_wishList.get(proposition[0]) != null && (Integer.parseInt(proposition[1]) + Integer.parseInt(proposition[2])) <= _wishList.get(proposition[0])) {
                            System.out.println(getLocalName() + " >> " + proposition[1] + " + " + proposition[2] + " < " + _wishList.get(proposition[0]));

                            int newPrice = Integer.parseInt(proposition[1]) + Integer.parseInt(proposition[2]);
                            System.out.println(getLocalName() + " >> NewPrice: " + newPrice);

                            ACLMessage reply = message.createReply();
                            reply.setPerformative(ACLMessage.PROPOSE);
                            reply.setContent(proposition[0] + "," + newPrice);

                            send(reply);

                            //_sentBids.add(proposition[0]);
                            gui.cambiarEstadoLibro(proposition[0], "Contra-puja enviada");
                            gui.actualizarLibro(proposition[0], proposition[1], "actualPrice");

                        } else {
                            ACLMessage reply = message.createReply();
                            reply.setPerformative(ACLMessage.REFUSE);
                            reply.setContent(proposition[0]);

                            send(reply);

                            gui.cambiarEstadoLibro(proposition[0], "Buscando...");
                            gui.actualizarLibro(proposition[0], null, "actualPrice");
                            gui.actualizarLibro(proposition[0], null, "minBid");
                        }
                    }

                    case ACLMessage.AGREE -> {
                        System.out.println(getLocalName() + " >> Recibido agree de " + message.getSender().getLocalName());
                        String[] agreement = message.getContent().split(",");
                        gui.cambiarEstadoLibro(agreement[0], "Comprado.");
                        gui.actualizarLibro(agreement[0], agreement[1], "actualPrice");
                        gui.moverFilaALaSubasta(agreement[0], message.getSender().getLocalName());
                    }

                    default -> {

                    }
                }
            } else {
                block();
            }
        }
    }

}