package com.projetotrem.sync;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class GerenteDaEstacao {
    private final int capacidadeDoTrem;
    private final int capacidadeDoDeposito;

    private int quantidadeDeCaixasDepositadas;
    private String statusDoTrem;
    private final Map<Integer, String> statusDosEmpacotadores;

    // Semáforos
    private final Semaphore mutex;
    private final Semaphore espacosDisponiveisNoDeposito;
    private final Semaphore quantidadeDeCaixasNoDeposito;

    public GerenteDaEstacao(int capacidadeDoTrem, int capacidadeDoDeposito) {
        this.capacidadeDoTrem = capacidadeDoTrem;
        this.capacidadeDoDeposito = capacidadeDoDeposito;

        this.quantidadeDeCaixasDepositadas = 0;
        this.statusDoTrem = "Parado";
        this.statusDosEmpacotadores = new HashMap<>();

        this.mutex = new Semaphore(1);
        this.espacosDisponiveisNoDeposito = new Semaphore(capacidadeDoDeposito);
        this.quantidadeDeCaixasNoDeposito = new Semaphore(0);
    }

    public void storeBox(int packerId) throws InterruptedException {return;}

    public void loadTrain() throws InterruptedException {return;}

    // Métodos para UI e Status

    public void setTrainStatus(String status) {return;}

    public int getBoxCount() {return 1;}

    public String getTrainStatus() {return "";}

    public Map<Integer, String> getPackerStatusMap() {return null;}
}
