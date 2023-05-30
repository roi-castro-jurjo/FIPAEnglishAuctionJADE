package Seller;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class SellerGUI extends JFrame{
    private JLabel _sellerName;
    private JTable _auctionTable;
    private JButton _addAuctionButton;
    private ArrayList<Auction> _auctions = new ArrayList<>();

    public SellerGUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 300);
        setLayout(new BorderLayout());

        // Creación de los componentes
        _sellerName = new JLabel("Texto de ejemplo");
        _auctionTable = new JTable(new DefaultTableModel(new Object[]{"Subasta", "Precio Actual", "Ganador", "Estado", "Time remaining"}, 0));        _addAuctionButton = new JButton("Abrir ventana");
        _auctionTable.setEnabled(false);
        // Configuración del botón para abrir la ventana
        _addAuctionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _openAuctionCreator();
            }
        });

        // Agregar los componentes a la ventana principal
        add(_sellerName, BorderLayout.NORTH);
        add(new JScrollPane(_auctionTable), BorderLayout.CENTER);
        add(_addAuctionButton, BorderLayout.SOUTH);

        restarValorColumnaTiempo();
    }

    private void _openAuctionCreator() {
        // Creación de la ventana secundaria
        JFrame ventanaSecundaria = new JFrame("Ventana secundaria");
        ventanaSecundaria.setSize(300, 200);
        ventanaSecundaria.setLayout(new FlowLayout());

        // Creación de los componentes de la ventana secundaria
        JTextField textoInput = new JTextField(20);
        JTextField numeroInput = new JTextField(10);
        JButton aceptarButton = new JButton("Aceptar");

        // Configuración del botón de aceptar
        aceptarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Acción a realizar al presionar el botón aceptar
                String texto = textoInput.getText();
                int numero = Integer.parseInt(numeroInput.getText());

                // Agregar una nueva fila a la tabla con los valores ingresados
                DefaultTableModel model = (DefaultTableModel) _auctionTable.getModel();
                model.addRow(new Object[]{texto, "0", "None", "Active", "10s"});

                _auctions.add(new Auction(texto, numero));

                ventanaSecundaria.dispose();
            }
        });

        // Agregar los componentes a la ventana secundaria
        ventanaSecundaria.add(new JLabel("Texto:"));
        ventanaSecundaria.add(textoInput);
        ventanaSecundaria.add(new JLabel("Número:"));
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
                model.setValueAt(nuevoGanador, i, 2);
                model.setValueAt(nuevoPrecio, i, 1);
                break;
            }
        }
    }

    private void restarValorColumnaTiempo() {
        javax.swing.Timer timer = new javax.swing.Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel model = (DefaultTableModel) _auctionTable.getModel();
                int columnaTiempo = 4; // Índice de la quinta columna ("Time Remaining")
                int rowCount = model.getRowCount();
                for (int fila = 0; fila < rowCount; fila++) {
                    String tiempoActual = (String) model.getValueAt(fila, columnaTiempo);

                    int segundosRestantes = obtenerSegundosRestantes(tiempoActual);
                    segundosRestantes--;

                    if (segundosRestantes >= 0) {
                        String nuevoTiempo = formatearTiempoRestante(segundosRestantes);
                        model.setValueAt(nuevoTiempo, fila, columnaTiempo);
                    }
                }
            }
        });

        timer.start();
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
