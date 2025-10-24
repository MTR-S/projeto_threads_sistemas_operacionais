package com.projetotrem.model;

import com.projetotrem.sync.GerenteDaEstacao;

public class Empacotador implements Runnable {

    private GerenteDaEstacao gerenteDaEstacao;
    private int id;
    private Long tempoEmpacotamento;

    public Empacotador(GerenteDaEstacao gerenteDaEstacao, int id, Long tempoEmpacotamento) {
        this.gerenteDaEstacao = gerenteDaEstacao;
        this.id = id;
        this.tempoEmpacotamento = tempoEmpacotamento;

        this.gerenteDaEstacao.setStatusDosEmpacotadores(this.id, "Iniciando");
    }

    @Override
    public void run() {
        try {
            while(true) {
                gerenteDaEstacao.setStatusDosEmpacotadores(this.id, "Empacotando");

                processoAleatorioCpuBound();

                gerenteDaEstacao.depositaAsCaixas(this.id);
            }
        }catch (Exception e) {
            gerenteDaEstacao.setStatusDosEmpacotadores(this.id, "Interrompido");
            Thread.currentThread().interrupt();
        }
    }

    private void processoAleatorioCpuBound() {

        long tempo = this.tempoEmpacotamento;

        System.out.println("Iniciando CPU-Bound por " + tempo + "ms...");

        long endTime = System.currentTimeMillis() + tempo;

        double soma = 0;

        while (System.currentTimeMillis() < endTime) {

            soma = soma + Math.sin(Math.random()) + Math.cos(Math.random());
        }

        System.out.println("...Trabalho CPU-Bound concluído.");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTempoEmpacotamento(Long tempoEmpacotamento) {
        this.tempoEmpacotamento = tempoEmpacotamento;
    }
}
