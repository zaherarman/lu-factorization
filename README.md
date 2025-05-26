# LU Factorization Dashboard

## Overview
The **LU Factorization Dashboard** is a simple graphical user interface (GUI) for performing LU factorization on a matrix. Users can choose between sequential or parallel execution, provide a matrix as input, and view the result in an output window after the computation. The matrix is provided through a text area, and the execution mode is chosen using a dropdown.

This project demonstrates how to:
- Implement matrix input and LU factorization.
- Handle user input and process external files (`input.txt`, `config.txt`, `output.txt`).
- Provide a responsive UI to display matrix results after the process finishes.

## Project Organization

```
lu-factorization/
├── .gitignore
├── README.md
├── input.txt
├── config.txt
└── src/
    ├── LUFactorization.java
    └── LUFactorizationDashboard.java
```

## Features
- **Execution Mode Selection**: Users can choose whether the LU factorization runs in **sequential** or **parallel** mode.
- **Matrix Input**: The input matrix is entered as comma-separated values and is saved to `input.txt`.
- **Execution Feedback**: The dashboard runs the LU factorization program, displays the results in a text area, and shows potential errors.
- **Error Handling**: Includes error handling for matrix input and process execution errors.
- **UI Framework**: The user interface is powered by **Java Swing** and **FlatLaf** (a modern look-and-feel library for Swing applications).
