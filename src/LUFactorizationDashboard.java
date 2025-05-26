import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class LUFactorizationDashboard {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LUFactorizationDashboard().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("LU Factorization Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        JLabel title = new JLabel("LU Factorization Dashboard", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        frame.add(title, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        JLabel executionLabel = new JLabel("Execution Mode:");
        String[] executionOptions = {"Sequential (false)", "Parallel (true)"};
        JComboBox<String> executionDropdown = new JComboBox<>(executionOptions);
        inputPanel.add(executionLabel);
        inputPanel.add(executionDropdown);

        JLabel matrixInputLabel = new JLabel("Matrix Input (Comma Separated):");
        JTextArea matrixInputArea = new JTextArea(5, 20);
        JScrollPane matrixScrollPane = new JScrollPane(matrixInputArea);
        inputPanel.add(matrixInputLabel);
        inputPanel.add(matrixScrollPane);

        JButton submitButton = new JButton("Submit");
        JButton clearButton = new JButton("Clear");
        inputPanel.add(submitButton);
        inputPanel.add(clearButton);

        frame.add(inputPanel, BorderLayout.CENTER);

        JTextArea resultArea = new JTextArea(8, 40);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        frame.add(resultScrollPane, BorderLayout.SOUTH);

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isParallel = executionDropdown.getSelectedIndex() == 1;
                String matrixInput = matrixInputArea.getText().trim();
                if (matrixInput.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Matrix input cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // write input.txt
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("input.txt"))) {
                    String[] rows = matrixInput.split("\n");
                    for (String row : rows) {
                        writer.write(row.trim().replaceAll(",", " "));
                        writer.newLine();
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error writing to input.txt: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // write config.txt
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("config.txt"))) {
                    writer.write("parallel_execution=" + isParallel);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error writing to config.txt: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // run LUFactorization
                try {
                    ProcessBuilder pb = new ProcessBuilder("java", "-cp", ".", "LUFactorization");
                    pb.redirectErrorStream(true);
                    Process proc = pb.start();
                    int exit = proc.waitFor();
                    resultArea.setText("");
                    try (BufferedReader fileReader = new BufferedReader(new FileReader("output.txt"))) {
                        String line;
                        while ((line = fileReader.readLine()) != null) {
                            resultArea.append(line + "\n");
                        }
                    }
                    if (exit != 0) {
                        resultArea.append("\nProcess ended with error. Exit code: " + exit);
                    }
                } catch (IOException | InterruptedException ex) {
                    JOptionPane.showMessageDialog(frame, "Error running LU Factorization: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        clearButton.addActionListener(e -> matrixInputArea.setText(""));

        frame.setVisible(true);
    }
}