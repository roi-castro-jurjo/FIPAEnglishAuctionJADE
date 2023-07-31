package Buyer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BuyerGUI extends JFrame {
    private JTable subastasTable;
    private JTable librosTable;

    private DefaultTableModel librosModel;

    private DefaultTableModel subastasModel;

    public BuyerGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Crear el modelo de datos para la tabla de subastas
        String[] subastasColumns = {"Libro", "Precio", "Vendedor"};
        subastasModel = new DefaultTableModel(subastasColumns, 0);
        subastasTable = new JTable(subastasModel);
        subastasTable.setEnabled(false);

        // Crear el modelo de datos para la tabla de libros
        String[] librosColumns = {"Titulo", "Precio Maximo", "Estado", "Puja Mínima", "Precio Actual"};
        librosModel = new DefaultTableModel(librosColumns, 0);
        librosTable = new JTable(librosModel);
        librosTable.setEnabled(false);

        // Crear el panel para la tabla de subastas con título
        JPanel subastasPanel = new JPanel(new BorderLayout());
        subastasPanel.setBorder(BorderFactory.createTitledBorder("Libros comprados"));
        subastasPanel.add(new JScrollPane(subastasTable), BorderLayout.CENTER);

        // Crear el panel para la tabla de libros con título
        JPanel librosPanel = new JPanel(new BorderLayout());
        librosPanel.setBorder(BorderFactory.createTitledBorder("Libros buscados"));
        librosPanel.add(new JScrollPane(librosTable), BorderLayout.CENTER);

        // Crear los paneles para las tablas
        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        centerPanel.add(subastasPanel);
        centerPanel.add(librosPanel);

        // Agregar menú desplegable al hacer clic derecho en una fila de la tabla de libros
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem cancelarBusquedaItem = new JMenuItem("Cancelar búsqueda");
        cancelarBusquedaItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = librosTable.getSelectedRow();
                if (selectedRow != -1) {
                    librosModel.setValueAt("Cancelado", selectedRow, 2);
                    moverFilaALaSubasta((String) librosModel.getValueAt(selectedRow, 0), "None");
                }
            }
        });
        popupMenu.add(cancelarBusquedaItem);
        librosTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = librosTable.rowAtPoint(e.getPoint());
                    if (row != -1) {
                        librosTable.setRowSelectionInterval(row, row);
                        popupMenu.show(librosTable, e.getX(), e.getY());
                    }
                }
            }
        });

        // Crear el botón "Buscar Libro"
        JButton buscarLibroButton = new JButton("Buscar Libro");
        buscarLibroButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Crear una ventana de búsqueda de libros
                JFrame buscarLibroFrame = new JFrame("Buscar Libro");
                buscarLibroFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                buscarLibroFrame.setLayout(new FlowLayout());

                // Crear los inputs y el botón de aceptar
                JTextField tituloInput = new JTextField(20);
                JTextField precioMaxInput = new JTextField(10);
                JButton aceptarButton = new JButton("Aceptar");
                aceptarButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Obtener los valores de los inputs
                        String titulo = tituloInput.getText();
                        String precioMax = precioMaxInput.getText();

                        // Comprobar si ya existe una fila con el mismo título
                        int filaExistente = -1;
                        for (int i = 0; i < librosModel.getRowCount(); i++) {
                            String tituloExistente = librosModel.getValueAt(i, 0).toString();
                            if (tituloExistente.equals(titulo)) {
                                filaExistente = i;
                                break;
                            }
                        }

                        // Si existe una fila con el mismo título, sustituir esa fila
                        // de lo contrario, añadir una nueva fila
                        if (filaExistente != -1) {
                            librosModel.setValueAt(precioMax, filaExistente, 1);
                            librosModel.setValueAt("Buscando...", filaExistente, 2);
                        } else {
                            librosModel.addRow(new Object[]{titulo, precioMax, "Buscando...", "", ""});
                        }

                        // Cerrar la ventana de búsqueda de libros
                        buscarLibroFrame.dispose();
                    }
                });

                // Añadir los componentes a la ventana de búsqueda de libros
                buscarLibroFrame.add(new JLabel("Título:"));
                buscarLibroFrame.add(tituloInput);
                buscarLibroFrame.add(new JLabel("Precio Máximo:"));
                buscarLibroFrame.add(precioMaxInput);
                buscarLibroFrame.add(aceptarButton);

                // Configurar la ventana de búsqueda de libros
                buscarLibroFrame.pack();
                buscarLibroFrame.setVisible(true);
            }
        });

        // Añadir los componentes a la ventana principal
        add(centerPanel, BorderLayout.CENTER);
        add(buscarLibroButton, BorderLayout.SOUTH);

        // Configurar la ventana principal
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }


    public void actualizarLibro(String titulo, String valor, String columna) {
        int columnIndex = -1;

        switch (columna) {
            case "minBid" -> columnIndex = 3; // Índice de la columna "Puja Mínima"
            case "actualPrice" -> columnIndex = 4; // Índice de la columna "Precio Actual"
            default -> {
                System.out.println("Columna no válida");
                return;
            }
        }

        for (int rowIndex = 0; rowIndex < librosModel.getRowCount(); rowIndex++) {
            String tituloFila = librosModel.getValueAt(rowIndex, 0).toString(); // Valor de la primera columna (Título)
            if (tituloFila.equals(titulo)) {
                librosModel.setValueAt(valor, rowIndex, columnIndex);
                return;
            }
        }

        System.out.println("Título no encontrado");
    }

    public void cambiarEstadoLibro(String titulo, String nuevoEstado) {
        int rowCount = librosTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String tituloActual = librosTable.getValueAt(i, 0).toString();
            if (tituloActual.equals(titulo)) {
                librosTable.setValueAt(nuevoEstado, i, 2);
                break;
            }
        }
    }


    public HashMap<String, Integer> obtenerLibrosBuscando() {
        HashMap<String, Integer> librosBuscando = new HashMap<>();

        int rowCount = librosTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String estado = librosTable.getValueAt(i, 2).toString();
            if (estado.equals("Buscando...")) {
                String titulo = librosTable.getValueAt(i, 0).toString();
                Integer precioMaximo = Integer.parseInt(librosTable.getValueAt(i, 1).toString());
                librosBuscando.put(titulo, precioMaximo);
            }
        }
        return librosBuscando;
    }

    public void moverFilaALaSubasta(String titulo, String seller) {
        DefaultTableModel subastasTableModel = (DefaultTableModel) subastasTable.getModel();
        DefaultTableModel librosTableModel = (DefaultTableModel) librosTable.getModel();

        int rowCount = librosTableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String currentTitulo = librosTableModel.getValueAt(i, 0).toString();
            if (currentTitulo.equals(titulo)) {
                Object[] rowData = new Object[3];
                rowData[0] = librosTableModel.getValueAt(i, 0);
                rowData[1] = librosTableModel.getValueAt(i, 4);
                rowData[2] = seller;
                subastasTableModel.addRow(rowData);
                librosTableModel.removeRow(i);
                break;
            }
        }
    }

    public ArrayList<String> obtenerValoresPrimeraColumna() {
        ArrayList<String> valores = new ArrayList<>();

        int rowCount = subastasTable.getRowCount();
        int columna = 0; // Índice de la primera columna

        for (int i = 0; i < rowCount; i++) {
            Object valor = subastasTable.getValueAt(i, columna);
            if (valor != null) {
                valores.add(valor.toString());
            }
        }

        return valores;
    }

    public void eliminarValores(HashMap<String, Integer> hashMap, ArrayList<String> valoresEliminar) {
        Iterator<Map.Entry<String, Integer>> iterator = hashMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            String clave = entry.getKey();

            if (valoresEliminar.contains(clave)) {
                iterator.remove();
            }
        }
    }



}

