package com.projetotrem.model;

import com.projetotrem.sync.GerenteDaEstacao;

public class Trem implements Runnable {

    private GerenteDaEstacao gerenteDaEstacao;
    private long tempoViagem;

    public Trem(GerenteDaEstacao gerenteDaEstacao, long tempoViagem) {
        this.gerenteDaEstacao = gerenteDaEstacao;
        this.tempoViagem = tempoViagem;

        this.gerenteDaEstacao.setStatusDoTrem("Iniciando");
    }

    @Override
    public void run() {
        try {
            while(true) {
                gerenteDaEstacao.carregaTrem();

                gerenteDaEstacao.setStatusDoTrem("Viajando (A -> B)");
                processoAleatorioCpuBound();

                gerenteDaEstacao.setStatusDoTrem("Retornando (B -> A)");
                processoAleatorioCpuBound();

            }
        }catch (Exception e) {
            gerenteDaEstacao.setStatusDoTrem("Interrompido");
            Thread.currentThread().interrupt();
        }
    }

    private void processoAleatorioCpuBound() {
        long tempo = this.tempoViagem;

        System.out.println("Iniciando CPU-Bound (Trem) por " + tempo + "ms...");

        long endTime = System.currentTimeMillis() + tempo;

        double soma = 0;
        while (System.currentTimeMillis() < endTime) {
            soma = soma + Math.sin(Math.random()) + Math.cos(Math.random());
        }
        System.out.println("...Trabalho CPU-Bound (Trem) concluído.");
    }
}
