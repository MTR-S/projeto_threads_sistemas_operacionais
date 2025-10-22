package com.projetotrem.sync;

import com.projetotrem.model.Empacotador;

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

    public void depositaAsCaixas(int idDoEmpacotador) throws InterruptedException {return;}

    public void carregaTrem() throws InterruptedException {return;}

    // Métodos para UI e Status

    public void setStatusDoTrem(String status) {return;}

    public void setStatusDosEmpacotadores(int idEmpacotador, String status) {
        try {
            mutex.acquire();
            this.statusDosEmpacotadores.put(idEmpacotador, status);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }

    public int getQuantidadeDeCaixasDepositadas() {
        int count = 0;

        try {
            mutex.acquire();
            count = this.quantidadeDeCaixasDepositadas;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
        return count;
    }

    public String getStatusDoTrem() {return "";}

    public Map<Integer, String> getStatusDosEmpacotadores() {
        Map<Integer, String> statusDosEmpacotadoresCopia;
        try {
            mutex.acquire();

            statusDosEmpacotadoresCopia = new HashMap<>(this.statusDosEmpacotadores);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new HashMap<>();
        } finally {
            mutex.release();
        }
        return statusDosEmpacotadoresCopia;
    }

}
