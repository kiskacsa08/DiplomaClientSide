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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
            String sql = "SELECT Matches.ID, Matches.RESULT, Predictions.PRED_RESULT,"
                + " Predictions.ALGORITHM, Predictions.\"DATA\""
                + " FROM DIPLOMA.MATCHES AS Matches, DIPLOMA.PREDICTIONS AS Predictions"
                + " WHERE Matches.RESULT <> 'N/A' AND Predictions.MATCH_ID = Matches.ID"
                + " ORDER BY Predictions.ALGORITHM, Predictions.\"DATA\", Matches.ID";
            String algorithmsNumSql = "SELECT COUNT (*) FROM (SELECT DISTINCT ALGORITHM FROM (" + sql + ") AS tmp) AS tmp2";
            String datasNumSql = "SELECT COUNT (*) FROM (SELECT DISTINCT \"DATA\" FROM (" + sql + ") AS tmp) AS tmp2";
            String matchesNumSql = "SELECT COUNT (*) FROM (SELECT DISTINCT ID FROM (" + sql + ") AS tmp) AS tmp2";
            ResultSet rs = dc.executeCommand(sql);
            ResultSet algorithmsNumRS = dc.executeCommand(algorithmsNumSql);
            ResultSet datasNumRS = dc.executeCommand(datasNumSql);
            ResultSet matchesNumRS = dc.executeCommand(matchesNumSql);
            algorithmsNumRS.first();
            int algorithmsNum = algorithmsNumRS.getInt(1);
            datasNumRS.first();
            int datasNum = datasNumRS.getInt(1);
            matchesNumRS.first();
            int matchesNum = matchesNumRS.getInt(1);
            Vector<Vector<Object>> bestAndWorst = calcAccuracy(rs, algorithmsNum, datasNum, matchesNum);
            table_Stat.setModel(buildTableModel(bestAndWorst.get(0), bestAndWorst.get(1)));
            resizeColumnWidth(table_Stat);
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
        //DefaultTableModel model = new DefaultTableModel(data, columnNames);
        DefaultTableModel model = new DefaultTableModel(data, columnNames){
            @Override
            public boolean isCellEditable(int row, int column) {
               //all cells false
               return false;
            }
        };
        return model;
    }
    
    private Vector<Vector<Object>> calcAccuracy(ResultSet rs, int algorithmsNum, int datasNum, int matchesNum) throws SQLException{
        HashMap<String, ArrayList<HashMap<String, Boolean>>> allAccuracies = new HashMap<>();
        ArrayList<HashMap<String, Boolean>> matches = new ArrayList<>();
        Vector<Vector<Object>> bestAndWorst = new Vector<>();
        ArrayList<ArrayList<Object>> accuracies = new ArrayList<>();
        rs.first();
        for (int i = 0; i < algorithmsNum; i++) {
            for (int j = 0; j < datasNum; j++) {
                ArrayList<Object> act = new ArrayList<>();
                act.add(rs.getString("ALGORITHM"));
                act.add(rs.getString("DATA"));
                int correct = 0;
                for (int k = 0; k < matchesNum; k++) {
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
                    rs.next();
                    if (rs.next() == false) {
                        rs.last();
                    }
                    else {
                        rs.previous();
                    }
                }
                int acc = (int)Math.round((double)correct/(double)matchesNum * 100);
                act.add(acc);
                accuracies.add(act);
            }
        }
        accuracies.remove(accuracies.size()-1);
        Vector<Object> best = new Vector<>();
        Vector<Object> worst = new Vector<>();
        int max = 0;
        int min = 100;
        for (ArrayList<Object> accuracy : accuracies) {
            if ((int)accuracy.get(2) > max) {
                best.clear();
                best.setSize(accuracy.size());
                Collections.copy(best, accuracy);
                max = (int)accuracy.get(2);
            }
            if ((int)accuracy.get(2) < min) {
                worst.clear();
                worst.setSize(accuracy.size());
                Collections.copy(worst, accuracy);
                min = (int)accuracy.get(2);
            }
        }
        best.add(0, "Best");
        worst.add(0, "Worst");
        bestAndWorst.add(best);
        bestAndWorst.add(worst);
        return bestAndWorst;
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
