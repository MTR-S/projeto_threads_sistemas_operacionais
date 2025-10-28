package com.projetotrem.sync;

import com.projetotrem.model.Empacotador;
import com.projetotrem.model.Trem;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
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
        this.statusDoTrem = "Esperando carregamento";
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

    public void carregaTrem() throws InterruptedException {
        setStatusDoTrem("Esperando carregamento!");
        quantidadeDeCaixasNoDeposito.acquire(capacidadeDoTrem);
        mutex.acquire();
        try {
            _unsafe_setStatusDoTrem("Entregando");

            quantidadeDeCaixasDepositadas -= capacidadeDoTrem;

            System.out.println("[+] Trem saiu para entrega");
        } finally {
            mutex.release();
        }

        espacosDisponiveisNoDeposito.release(capacidadeDoTrem);
    }


    public void setStatusDoTrem(String status) {
        try {
            mutex.acquire();

            _unsafe_setStatusDoTrem(status);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }

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

    private void _unsafe_setStatusDoTrem(String status) {
        this.statusDoTrem = status;
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

    public String getStatusDoTrem() {
        String statusDoTremCopia;
        try {
            mutex.acquire();
            statusDoTremCopia = this.statusDoTrem;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            statusDoTremCopia = "";
        } finally {
            mutex.release();
        }
        return statusDoTremCopia;
    }

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

    public void createEmpacotador(int idEmpacotador, Scanner sc) {
        System.out.println("Qual o tempo de empacotamento em segundos deste empacotador?");
        long tempoEmpacotamento = sc.nextLong();
        Empacotador empacotador = new Empacotador(this, idEmpacotador, tempoEmpacotamento);
        Thread t =  new Thread(empacotador);
        t.start();
    }

    public void createTrem(int idTrem, Scanner sc) {
        System.out.println("Qual o tempo de viagem em segundos do trem? Tempo de ir do A para o B");
        int tempoViagem =  sc.nextInt();
        Trem trem = new Trem(this, idTrem, tempoViagem);
        Thread t =  new Thread(trem);
        t.start();
    }

    public void getStatusSistema() {
        while (true) {
            for (Map.Entry<Integer, String> entry : getStatusDosEmpacotadores().entrySet()) {
                Integer id = entry.getKey();
                String status = entry.getValue();
                System.out.println("Empacotador ID: " + id + " | Status: " + status);
            }

            System.out.println("Status do Trem: " + getStatusDoTrem());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
