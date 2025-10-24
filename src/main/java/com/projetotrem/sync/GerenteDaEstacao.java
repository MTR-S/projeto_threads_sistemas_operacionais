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


    public void depositaAsCaixas(int idDoEmpacotador) throws InterruptedException {
        setStatusDosEmpacotadores(idDoEmpacotador, "Esperando vaga");
        espacosDisponiveisNoDeposito.acquire();
        mutex.acquire();
        try {
            _unsafe_setStatusDosEmpacotadores(idDoEmpacotador, "Armazenando no Deposito");

            quantidadeDeCaixasDepositadas++;

            System.out.println("[+] Empacotador " + idDoEmpacotador + " guardou. Total: " + quantidadeDeCaixasDepositadas);
        } finally {
            mutex.release();
        }

       quantidadeDeCaixasNoDeposito.release();
    }

    public void carregaTrem() throws InterruptedException {return;}


    public void setStatusDoTrem(String status) {return;}

    public void setStatusDosEmpacotadores(int idEmpacotador, String status) {
        try {
            mutex.acquire();

            _unsafe_setStatusDosEmpacotadores(idEmpacotador, status);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }

    private void _unsafe_setStatusDosEmpacotadores(int idEmpacotador, String status) {
        this.statusDosEmpacotadores.put(idEmpacotador, status);
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
