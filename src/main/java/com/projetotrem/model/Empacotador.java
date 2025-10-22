package com.projetotrem.model;

import com.projetotrem.sync.GerenteDaEstacao;

public class Empacotador implements Runnable {

    private GerenteDaEstacao gerenteDaEstacao;
    private int id;
    private int tempoEmpacotamento;

    public Empacotador(GerenteDaEstacao gerenteDaEstacao, int id, int tempoEmpacotamento) {
        this.gerenteDaEstacao = gerenteDaEstacao;
        this.id = id;
        this.tempoEmpacotamento = tempoEmpacotamento;
    }

    @Override
    public void run() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTempoEmpacotamento() {
        return tempoEmpacotamento;
    }

    public void setTempoEmpacotamento(int tempoEmpacotamento) {
        this.tempoEmpacotamento = tempoEmpacotamento;
    }
}
