package Seller;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SellerGUI extends JFrame{
    private JLabel _sellerName;
    private JTable _auctionTable;
    private JTable finishedAuctionsTable;
    private JButton _addAuctionButton;
    private ArrayList<Auction> _auctions = new ArrayList<>();

    public SellerGUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLayout(new BorderLayout());
        setResizable(false);

        // Creación de los componentes
        _sellerName = new JLabel("Nombre del Vendedor");

        _auctionTable = new JTable(new DefaultTableModel(new Object[]{"Subasta", "Precio Actual", "Incremento", "Ganador", "Estado", "Tiempo Restante"}, 0));
        _addAuctionButton = new JButton("Nueva Subasta");


        // Configuración del botón para abrir la ventana
        _addAuctionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _openAuctionCreator();
            }
        });

        // Crear un panel para contener la tabla de subastas activas y el título correspondiente
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Subastas Activas"));
        tablePanel.add(new JScrollPane(_auctionTable), BorderLayout.CENTER);

        // Creación de la segunda tabla y el texto encima
        finishedAuctionsTable = new JTable(new DefaultTableModel(new Object[]{"Subasta", "Precio Final", "Ganador"}, 0));


        JPanel finishedAuctionsPanel = new JPanel(new BorderLayout());
        finishedAuctionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        finishedAuctionsPanel.setBorder(BorderFactory.createTitledBorder("Subastas Finalizadas"));
        finishedAuctionsPanel.add(new JScrollPane(finishedAuctionsTable), BorderLayout.CENTER);

        // Crear un panel para contener ambas tablas y el título correspondiente
        JPanel tablesPanel = new JPanel(new GridLayout(2, 1));
        tablesPanel.add(tablePanel);
        tablesPanel.add(finishedAuctionsPanel);



        // Agregar los componentes al contenedor principal
        add(_sellerName, BorderLayout.NORTH);
        add(tablesPanel, BorderLayout.CENTER);
        add(_addAuctionButton, BorderLayout.SOUTH);
    }


    private void _openAuctionCreator() {
        JFrame ventanaSecundaria = new JFrame("Crear Subasta");
        ventanaSecundaria.setSize(300, 200);
        ventanaSecundaria.setLayout(new FlowLayout());

        JTextField textoInput = new JTextField(20);
        JTextField numeroInput = new JTextField(10);
        JButton aceptarButton = new JButton("Aceptar");

        aceptarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Acción a realizar al presionar el botón aceptar
                String texto = textoInput.getText();
                int numero = Integer.parseInt(numeroInput.getText());

                // Agregar una nueva fila a la tabla con los valores ingresados
                DefaultTableModel model = (DefaultTableModel) _auctionTable.getModel();
                model.addRow(new Object[]{texto, "0", numero, "None", "Active", "10s"});

                _auctions.add(new Auction(texto, numero));

                ventanaSecundaria.dispose();
            }
        });

        // Agregar los componentes a la ventana secundaria
        ventanaSecundaria.add(new JLabel("Titulo:"));
        ventanaSecundaria.add(textoInput);
        ventanaSecundaria.add(new JLabel("Puja Minima:"));
        ventanaSecundaria.add(numeroInput);
        ventanaSecundaria.add(aceptarButton);

        // Mostrar la ventana secundaria
        ventanaSecundaria.setVisible(true);
    }

    public void modificarFilaTabla(String subasta, String nuevoGanador, int nuevoPrecio) {
        DefaultTableModel model = (DefaultTableModel) _auctionTable.getModel();
        int rowCount = model.getRowCount();

        for (int i = 0; i < rowCount; i++) {
            String subastaEnTabla = (String) model.getValueAt(i, 0);
            if (subastaEnTabla.equals(subasta)) {
                model.setValueAt(nuevoGanador, i, 3);
                model.setValueAt(nuevoPrecio, i, 1);
                break;
            }
        }
    }

    private Map<String, Timer> timers = new HashMap<>();

    public void restarValorColumnaTiempo(String auction) {
        // Stop the existing timer for the specific auction if it is running
        Timer existingTimer = timers.get(auction);
        if (existingTimer != null && existingTimer.isRunning()) {
            existingTimer.stop();
        }

        DefaultTableModel model = (DefaultTableModel) _auctionTable.getModel();
        int rowCount = model.getRowCount();
        int columnaTiempo = 5;

        for (int fila = 0; fila < rowCount; fila++) {
            String auctionValue = (String) model.getValueAt(fila, 0);
            if (auctionValue.equals(auction)) {
                model.setValueAt("10s", fila, columnaTiempo);

                // Create a new timer for the specific auction
                int finalFila = fila;
                Timer newTimer = new Timer(1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String tiempoActual = (String) model.getValueAt(finalFila, columnaTiempo);
                        int segundosRestantes = obtenerSegundosRestantes(tiempoActual);
                        segundosRestantes--;

                        if (segundosRestantes >= 0) {
                            String nuevoTiempo = formatearTiempoRestante(segundosRestantes);
                            model.setValueAt(nuevoTiempo, finalFila, columnaTiempo);
                        } else {
                            // Stop the timer when the time reaches zero
                            Timer timerToRemove = timers.get(auction);
                            if (timerToRemove != null) {
                                timerToRemove.stop();
                                timers.remove(auction);
                            }
                        }
                    }
                });

                // Start the new timer for the specific auction
                newTimer.start();
                timers.put(auction, newTimer);
            }
        }
    }

    public void moveRowToFinishedAuctions(String auctionName) {
        DefaultTableModel auctionTableModel = (DefaultTableModel) _auctionTable.getModel();
        DefaultTableModel finishedAuctionsTableModel = (DefaultTableModel) finishedAuctionsTable.getModel();

        Timer timerToRemove = timers.get(auctionName);
        if (timerToRemove != null && timerToRemove.isRunning()) {
            timerToRemove.stop();
            timers.remove(auctionName);
        }

        int rowCount = auctionTableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String currentAuctionName = (String) auctionTableModel.getValueAt(i, 0);
            if (currentAuctionName.equals(auctionName)) {
                Object[] rowData = new Object[3];
                rowData[0] = currentAuctionName;
                rowData[1] = auctionTableModel.getValueAt(i, 1);
                rowData[2] = auctionTableModel.getValueAt(i, 3);
                finishedAuctionsTableModel.addRow(rowData);
                auctionTableModel.removeRow(i);
                break;
            }
        }
    }






    private int obtenerSegundosRestantes(String tiempo) {
        return Integer.parseInt(tiempo.substring(0, tiempo.length() - 1));
    }

    private String formatearTiempoRestante(int segundosRestantes) {
        return segundosRestantes + "s";
    }



    public JLabel get_sellerName() {
        return _sellerName;
    }

    public void set_sellerName(String _sellerName) {
        this._sellerName.setText(_sellerName);
    }

    public JTable get_auctionTable() {
        return _auctionTable;
    }

    public void set_auctionTable(JTable _auctionTable) {
        this._auctionTable = _auctionTable;
    }

    public ArrayList<Auction> get_auctions() {
        return _auctions;
    }

    public void set_auctions(ArrayList<Auction> _auctions) {
        this._auctions = _auctions;
    }
}
