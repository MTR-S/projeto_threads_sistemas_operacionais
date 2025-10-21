package com.projetotrem.model;

import com.projetotrem.sync.GerenteDaEstacao;

public class Trem implements Runnable {

    private GerenteDaEstacao gerenteDaEstacao;
    private int quantidadeTransportada;
    private int tempoViagem;

    public Trem(GerenteDaEstacao gerenteDaEstacao, int quantidadeTransportada,int tempoViagem) {
        this.gerenteDaEstacao = gerenteDaEstacao;
        this.quantidadeTransportada = quantidadeTransportada;
        this.tempoViagem = tempoViagem;
    }

    @Override
    public void run() {

    }
}
