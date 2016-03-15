/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diplomaclientside;

import java.awt.Component;
import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author egg
 */
public class StatisticsWindow extends javax.swing.JDialog {

    private DatabaseConnection dc;
    
    /**
     * Creates new form StatisticsWindow
     */
    public StatisticsWindow(java.awt.Frame parent, boolean modal, DatabaseConnection dc, ArrayList<String> algorithms, ArrayList<String> datas) {
        super(parent, modal);
        initComponents();
        this.dc = dc;
        try {
            int best = 0;
            int worst = 100;
            String bestAlgorithm = "";
            String bestData = "";
            String worstAlgorithm = "";
            String worstData = "";
            for (String algorithm : algorithms) {
                for (String data : datas) {
                    int act = calcAccuracy(algorithm, data);
                    if (act > best) {
                        best = act;
                        bestAlgorithm = algorithm;
                        bestData = data;
                    }
                    if (act < worst) {
                        worst = act;
                        worstAlgorithm = algorithm;
                        worstData = data;
                    }
                }
            }
            Vector<Object> bestRow = new Vector<>();
            Vector<Object> worstRow = new Vector<>();
            bestRow.add("Best");
            bestRow.add(bestAlgorithm);
            bestRow.add(bestData);
            bestRow.add(String.valueOf(best) + "%");
            worstRow.add("Worst");
            worstRow.add(worstAlgorithm);
            worstRow.add(worstData);
            worstRow.add(String.valueOf(worst) + "%");
            table_Stat.setModel(buildTableModel(bestRow, worstRow));
            resizeColumnWidth(table_Stat);
//            table_Stat.setPreferredScrollableViewportSize(table_Stat.getPreferredSize());
//            table_Stat.setFillsViewportHeight(true);
//            jScrollPane2.setSize(table_Stat.getPreferredSize());
//            Dimension d = table_Stat.getPreferredSize();
//            jScrollPane2.setPreferredSize(new Dimension(d.width, table_Stat.getRowHeight()*table_Stat.getRowCount()+1));
        } catch (SQLException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private DefaultTableModel buildTableModel(Vector<Object> best, Vector<Object> worst) throws SQLException{
        Vector<Vector<Object>> data = new Vector<>();
        Vector<String> columnNames = new Vector<>();
        columnNames.add("");
        columnNames.add("Algorithm");
        columnNames.add("Dataset");
        columnNames.add("Accuracy");
        data.add(best);
        data.add(worst);
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        return model;
    }
    
    private int calcAccuracy(String algorithm, String data) throws SQLException{
        String sql = "SELECT Matches.RESULT, Predictions.PRED_RESULT,"
                + " Predictions.ALGORITHM, Predictions.\"DATA\""
                + " FROM DIPLOMA.MATCHES AS Matches, DIPLOMA.PREDICTIONS AS Predictions"
                + " WHERE Matches.RESULT <> 'N/A' AND Predictions.ALGORITHM = '" + algorithm + "'"
                + " AND Predictions.\"DATA\" = '" + data + "' AND Predictions.MATCH_ID = Matches.ID";
        String sizeSql = "SELECT COUNT(*) FROM (" + sql + ") AS tmp";
        ResultSet rs = dc.executeCommand(sql);
        ResultSet sizeRS = dc.executeCommand(sizeSql);
        sizeRS.first();
        int size = sizeRS.getInt(1);
        int correct = 0;
        while (rs.next()) {            
            int pred = rs.getInt("PRED_RESULT");
            String outcome = rs.getString("RESULT");
            int out;
            int homeGoals = outcome.charAt(0);
            int awayGoals = outcome.charAt(2);
            if (homeGoals > awayGoals) {
                out = 1;
            }
            else if (awayGoals > homeGoals) {
                out = -1;
            }
            else {
                out = 0;
            }
            if (pred == out) {
                correct++;
            }
        }
        double acc = (double)correct/(double)size;
        acc = acc*100;
        int accuracy = (int)acc;
        
        return accuracy;
    }
    
    private void resizeColumnWidth(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 50; // Min width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width +1 , width);
            }
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        table_Stat = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        btn_OK = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Statistics");
        setResizable(false);

        table_Stat.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "", "Algorithm", "Dataset", "Accuracy"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table_Stat.setFillsViewportHeight(true);
        jScrollPane2.setViewportView(table_Stat);
        if (table_Stat.getColumnModel().getColumnCount() > 0) {
            table_Stat.getColumnModel().getColumn(0).setResizable(false);
        }

        getContentPane().add(jScrollPane2, java.awt.BorderLayout.CENTER);

        btn_OK.setText("OK");
        btn_OK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_OKActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(310, Short.MAX_VALUE)
                .addComponent(btn_OK, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btn_OK)
                .addContainerGap())
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_OKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_OKActionPerformed
        this.dispose();
    }//GEN-LAST:event_btn_OKActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_OK;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable table_Stat;
    // End of variables declaration//GEN-END:variables
}
