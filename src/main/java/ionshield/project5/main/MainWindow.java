package ionshield.project5.main;

import com.bulenkov.darcula.DarculaLaf;
import ionshield.project5.graphics.GraphDisplay;
import ionshield.project5.graphics.PointDisplay;
import ionshield.project5.math.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLookAndFeel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class MainWindow {
    private JPanel rootPanel;
    private JTextArea log;
    private JButton calculateButton;
    private JTextField seedField;
    private JTextField multiField;
    private JTextField modField;
    private JTextField avgOutField1;
    private JTextField devOutField1;
    private JTextArea textArea1;
    private JTextField nField;
    private JTextField zField;
    private JTextField kField;
    private JTextArea textArea2;
    private JTextField m0Field;
    private JTextField s0Field;
    private JTextField a0Field;
    private JTextField avgOutField2;
    private JTextField devOutField2;
    private JTextArea textArea3;
    private GraphDisplay graph;
    private JTextField alphaOutField;
    private JTextField errAvgField;
    private JTextField errDevField;
    private JTextField errAlphaField;
    private GraphDisplay pointDisplay1;
    private GraphDisplay pointDisplay2;
    private JCheckBox continiousGraphCheckBox;
    private JTabbedPane tabbedPane1;
    
    public static final String TITLE = "Project-6";
    
    private MainWindow() {
        initComponents();
    }
    
    private void initComponents() {
        calculateButton.addActionListener(e -> calculate());
    }
    
    
    
    private void calculate() {
        try {
            log.setText("");
    
            textArea1.setText("");
            textArea2.setText("");
            textArea3.setText("");
    
            pointDisplay1.setInterpolators(null);
            pointDisplay1.setPoints(null);
            pointDisplay1.setInterpolatorsHighligthed(null);
            pointDisplay1.setPointsHighligthed(null);
            pointDisplay2.setInterpolators(null);
            pointDisplay2.setPoints(null);
            pointDisplay2.setInterpolatorsHighligthed(null);
            pointDisplay2.setPointsHighligthed(null);
            
            long seed = Long.parseLong(seedField.getText());
            long multi = Long.parseLong(multiField.getText());
            long mod = Long.parseLong(modField.getText());
    
            int n = Integer.parseInt(nField.getText());
            int z = Integer.parseInt(zField.getText());
            int k = Integer.parseInt(kField.getText());
    
            RNG<Double> rng = new CongruentialRNG(seed, multi, mod);
            List<Double> rowX = new ArrayList<>();
            List<PointDouble> rowX2 = new ArrayList<>();
    
            for (int i = 0; i < n; i++) {
                double val = rng.getInRange(-0.5, 0.5);
                rowX.add(val);
                rowX2.add(new PointDouble(i, val));
            }
    
            for (Double aDouble : rowX) {
                textArea1.append(aDouble + System.lineSeparator());
            }
            
            double avgX = rowX.stream().mapToDouble(x -> x).average().orElse(0);
            double devX = rowX.stream().mapToDouble(x -> (x - avgX) * (x - avgX)).average().orElse(0);
            
            avgOutField1.setText(new BigDecimal(avgX).setScale(3, BigDecimal.ROUND_HALF_UP).toString());
            devOutField1.setText(new BigDecimal(devX).setScale(3, BigDecimal.ROUND_HALF_UP).toString());
    
            double m0 = Double.parseDouble(m0Field.getText());
            double s0 = Double.parseDouble(s0Field.getText());
            double a0 = Double.parseDouble(a0Field.getText());
    
            RandomProcessGenerator rpg = new RandomProcessGenerator(m0, s0, a0, rowX);
            while (rowX.size() < z * rpg.getnS()) {
                rowX.add(rng.getInRange(-0.5, 0.5));
            }
            List<Double> rowZ = new ArrayList<>();
            List<PointDouble> rowZ2 = new ArrayList<>();
            for (int i = 0; i < z; i++) {
                double val = rpg.getZ(i + 1);
                rowZ.add(val);
                rowZ2.add(new PointDouble(i, val));
            }
    
            for (Double aDouble : rowZ) {
                textArea2.append(aDouble + System.lineSeparator());
            }
    
            double avgZ = rowZ.stream().mapToDouble(x -> x).average().orElse(0);
            double devZ = rowZ.stream().mapToDouble(x -> (x - avgZ) * (x - avgZ)).average().orElse(0);
    
            avgOutField2.setText(new BigDecimal(avgZ).setScale(3, BigDecimal.ROUND_HALF_EVEN).toString());
            devOutField2.setText(new BigDecimal(devZ).setScale(3, BigDecimal.ROUND_HALF_EVEN).toString());
    
            List<Double> rowK = new ArrayList<>();
            List<PointDouble> rowK2 = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                double val = k(i, rowZ);
                rowK.add(val);
                rowK2.add(new PointDouble(i + 1, val));
            }
    
            double avgK = rowK.stream().mapToDouble(x -> x).average().orElse(0);
            double devK = rowK.stream().mapToDouble(x -> (x - avgK) * (x - avgK)).average().orElse(0);
            
            double res = SolverUtils.findMin(x -> distMetric(rowK, x, devK), 1, 0);
            
            List<PointDouble> rowKa = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                rowKa.add(new PointDouble(i + 1, k(i, res, devK)));
            }
    
            for (Double aDouble : rowK) {
                textArea3.append(aDouble + System.lineSeparator());
            }
            alphaOutField.setText(new BigDecimal(res).setScale(3, BigDecimal.ROUND_HALF_EVEN).toString());
            
            Interpolator ip1 = new LinearInterpolator(rowK2);
            Interpolator ip2 = new LinearInterpolator(rowKa);
            graph.setInterpolatorsHighligthed(Collections.singletonList(ip1));
            graph.setInterpolators(Collections.singletonList(ip2));
            graph.repaint();
            
            if (continiousGraphCheckBox.isSelected()) {
                pointDisplay1.setInterpolators(Collections.singletonList(new LinearInterpolator(rowX2)));
                pointDisplay2.setInterpolators(Collections.singletonList(new LinearInterpolator(rowZ2)));
            }
            else {
                pointDisplay1.setPoints(rowX2);
                pointDisplay2.setPoints(rowZ2);
            }
            pointDisplay1.repaint();
            pointDisplay2.repaint();
            
            double errM = Math.abs(m0 - avgZ);
            double errS = Math.abs(s0 - devZ);
            double errA = Math.abs(a0 - res);
            
            errAvgField.setText(new BigDecimal(errM).setScale(3, BigDecimal.ROUND_HALF_EVEN) + " (" + new BigDecimal(100 * errM / m0).setScale(3, BigDecimal.ROUND_HALF_EVEN) + "%)");
            errDevField.setText(new BigDecimal(errS).setScale(3, BigDecimal.ROUND_HALF_EVEN) + " (" + new BigDecimal(100 * errS / s0).setScale(3, BigDecimal.ROUND_HALF_EVEN) + "%)");
            errAlphaField.setText(new BigDecimal(errA).setScale(3, BigDecimal.ROUND_HALF_EVEN) + " (" + new BigDecimal(100 * errA / a0).setScale(3, BigDecimal.ROUND_HALF_EVEN) + "%)");
        }
        catch (NumberFormatException e) {
            log.append("\nInvalid input format");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public double distMetric(List<Double> row, double alpha, double sigmaSq) {
        
        double res = 0;
        for (int i = 1; i <= row.size(); i++) {
            res += Math.pow(row.get(i - 1) - k(i, alpha, sigmaSq), 2);
        }
        return res;
    }
    
    public double k(double in, double alpha, double sigmaSq) {
        return sigmaSq * Math.exp(-alpha * Math.abs(in));
    }
    
    public double k(int in, List<Double> row) {
        double res = 0;
        int l = row.size() - in;
        double avg = row.stream().mapToDouble(x -> x).average().orElse(0);
        
        for (int i = 0; i < l; i++) {
            res += (row.get(i) - avg) * (row.get(in + i) - avg);
        }
        
        res /= l;
        return res;
    }
    
    public static void main(String[] args) {
        BasicLookAndFeel darcula = new DarculaLaf();
        try {
            UIManager.setLookAndFeel(darcula);
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    
        JFrame frame = new JFrame(TITLE);
        MainWindow gui = new MainWindow();
        frame.setContentPane(gui.rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    
    private void createUIComponents() {
        pointDisplay1 = new GraphDisplay();
        pointDisplay1.setGridX(4);
        pointDisplay1.setGridY(3);
        pointDisplay2 = new GraphDisplay();
        pointDisplay2.setGridX(4);
        pointDisplay2.setGridY(3);
        graph = new GraphDisplay();
        graph.setGridX(4);
        graph.setGridY(3);
    }
}
