import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class LUFactorization {

    // readMatrix method specified in assignment
    public static double[][] readMatrix(String filename) throws IOException {
        List<double[]> rows = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty())
                continue; // Skip empty lines
            String[] tokens = line.trim().split("\\s+");
            double[] row = new double[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                row[i] = Double.parseDouble(tokens[i]);
            }
            rows.add(row);
        }
        reader.close();

        int numRows = rows.size();
        if (numRows == 0) {
            throw new IllegalArgumentException("Matrix must be square.");
        }
        for (double[] row : rows) {
            if (row.length != numRows) {
                throw new IllegalArgumentException("Matrix must be square.");
            }
        }

        double[][] matrix = new double[numRows][numRows];
        for (int i = 0; i < numRows; i++) {
            matrix[i] = rows.get(i);
        }
        return matrix;
    }

    // Initializing identity matrix
    public static double[][] initializeIdentityMatrix(int n) {
        double[][] identity = new double[n][n];
        for (int i = 0; i < n; i++) {
            identity[i][i] = 1.0;
        }
        return identity;
    }

    public static void calculateLij(double[][] A, double[][] L, double[][] U, int j, int i, int currentI) {
        double sum = 0.0;
        for (int k = 0; k < i; k++) {
            sum += L[j][k] * U[k][i];
        }
        L[j][i] = (A[j][i] - sum) / U[i][i];
    }
    
    public static void calculateUij(double[][] A, double[][] L, double[][] U, int i, int j) {
        double sum = 0.0;
        for (int k = 0; k < i; k++) {
            sum += L[i][k] * U[k][j];
        }
        U[i][j] = A[i][j] - sum;
    }

    public static void calculateURow(double[][] A, double[][] L, double[][] U, int i, int n) {
        for (int j = i; j < n; j++) {
            double sum = 0.0;
            for (int k = 0; k < i; k++) {
                sum += L[i][k] * U[k][j];
            }
            U[i][j] = A[i][j] - sum;
        }
    }

    public static void calculateLColumn(double[][] A, double[][] L, double[][] U, int i, int n) {
        for (int j = i + 1; j < n; j++) {
            double sum = 0.0;
            for (int k = 0; k < i; k++) {
                sum += L[j][k] * U[k][i];
            }
            L[j][i] = (A[j][i] - sum) / U[i][i];
        }
    }

    public static double[][] multiplyMatrices(double[][] L, double[][] U, int n) {
        double[][] LU = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                LU[i][j] = 0.0;
                for (int k = 0; k < n; k++) {
                    LU[i][j] += L[i][k] * U[k][j];
                }
            }
        }
        return LU;
    }

    // Decompose method outlined in the assignment handout
    public static void decompose(double[][] A, double[][] L, double[][] U, int n, boolean parallel, ExecutorService executor) {
        for (int i = 0; i < n; i++) {
            final int currentI = i; // YOU NEED A FINAL COPY OF I

            if (parallel) {
                // Parallel processing of U[i][j] for j >= i
                List<Future<?>> futures = new ArrayList<>();
                for (int j = i; j < n; j++) {
                    final int row = i;
                    final int col = j;
                    Future<?> future = executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            calculateUij(A, L, U, row, col);
                        }
                    });
                    futures.add(future);
                }
                waitForFutures(futures);
            } else {
                calculateURow(A, L, U, i, n);
            }

            // Check for singularity
            if (U[i][i] == 0.0) {
                throw new ArithmeticException("Matrix is singular, cannot perform decomposition.");
            }

            if (parallel) {
                List<Future<?>> futures = new ArrayList<>();
                for (int j = i + 1; j < n; j++) {
                    final int row = j;
                    final int col = i;
                    Future<?> future = executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            calculateLij(A, L, U, row, col, currentI);
                        }
                    });
                    futures.add(future);
                }
                waitForFutures(futures);
            } else {
                calculateLColumn(A, L, U, i, n);
            }
        }
    }

    public static void waitForFutures(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static double[][] subtractMatrices(double[][] A, double[][] B, int n) {
        double[][] D = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                D[i][j] = A[i][j] - B[i][j];
            }
        }
        return D;
    }

    public static double calculateFrobeniusNorm(double[][] D, int n) {
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sum += D[i][j] * D[i][j];
            }
        }
        return Math.sqrt(sum);
    }

    public static String formatMatrix(double[][] matrix, int decimalPlaces) {
        StringBuilder stringbuilder = new StringBuilder();
        String formatString = "%." + decimalPlaces + "f";
        int n = matrix.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                stringbuilder.append(String.format(formatString, matrix[i][j]));
                if (j < n - 1) {
                    stringbuilder.append(" ");
                }
            }
            stringbuilder.append("\n");
        }
        return stringbuilder.toString();
    }
    
    public static void main(String[] args) {
        String inputFile = "input.txt";
        boolean noInputSpecified = false;

        if (args.length > 0) {
            inputFile = args[0];
        } else {
            noInputSpecified = true;
        }

        String outputFile = "output.txt";

        boolean parallel = false; 
        try {
            BufferedReader reader = new BufferedReader(new FileReader("config.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("parallel_execution")) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        parallel = Boolean.parseBoolean(parts[1].trim());
                    }
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            parallel = false;
        } catch (IOException e) {
            parallel = false;
        }

        ExecutorService executor = null;
        if (parallel) {
            int processors = Runtime.getRuntime().availableProcessors();
            executor = Executors.newFixedThreadPool(processors);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            if (noInputSpecified) {
                writer.write("No input file specified. Using default: input.txt\n");
            }

            writer.write("Input file: " + inputFile + "\n");
            writer.write("Output file: " + outputFile + "\n");
            writer.write("Execution mode: ");
            writer.write(parallel ? "parallel\n\n" : "sequential\n\n");

            double[][] A;
            try {
                A = readMatrix(inputFile);
            } catch (FileNotFoundException e) {
                writer.write("Error: Input file not found.\n");
                return;
            } catch (IllegalArgumentException e) {
                writer.write("Error: " + e.getMessage() + "\n");
                return;
            } catch (IOException e) {
                writer.write("Error reading input file.\n");
                return;
            }

            int n = A.length;
            writer.write("Matrix A:\n");
            writer.write(formatMatrix(A, 1) + "\n");

            double[][] L = initializeIdentityMatrix(n);
            double[][] U = new double[n][n];

            try {
                decompose(A, L, U, n, parallel, executor);
            } catch (ArithmeticException e) {
                writer.write("Error: " + e.getMessage() + "\n");
                return;
            }

            writer.write("Final Matrix L:\n");
            writer.write(formatMatrix(L, 1) + "\n");
            writer.write("Final Matrix U:\n");
            writer.write(formatMatrix(U, 1) + "\n");

            double[][] LU = multiplyMatrices(L, U, n);
            double[][] D = subtractMatrices(A, LU, n);
            writer.write("Difference Matrix (A - LU):\n");
            writer.write(formatMatrix(D, 4) + "\n");

            double tolerance = calculateFrobeniusNorm(D, n);
            writer.write(String.format("Tolerance (difference between A and LU): %.4f\n\n", tolerance));
            writer.write("Decomposition complete. Results written to " + outputFile + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (executor != null) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException ex) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}