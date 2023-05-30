package Buyer;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
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

    private final String[] books = new String[5];



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

        books[0] = "primero";
        books[1] = "segundo";
        books[2] = "tercero";
        books[3] = "cuarto";
        books[4] = "quinto";

        Random random = new Random();

        for (int i = 0; i < 2; i++) {
            int randomIndex = random.nextInt(books.length);
            _wishList.put(books[randomIndex], random.nextInt(901) + 100);
        }
        for (HashMap.Entry<String, Integer> entry : _wishList.entrySet()) {
            String book = entry.getKey();
            int value = entry.getValue();
            System.out.println(getLocalName() + " >> Libro: " + book + ", Valor: " + value);
        }


        this.addBehaviour(new Scanning());
        this.addBehaviour(new ReceivingReply());

    }


    private class Scanning extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage buyQuery  = new ACLMessage(ACLMessage.QUERY_REF);

            String queryContent = String.join(",", _wishList.keySet());

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
                block();

            } catch (FIPAException e) {
                throw new RuntimeException(e);
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

                        ACLMessage reply = message.createReply();
                        reply.setPerformative(ACLMessage.PROPOSE);
                        reply.setContent("10");

                        send(reply);
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