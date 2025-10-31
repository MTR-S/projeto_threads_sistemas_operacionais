package com.projetotrem.UI;

import com.projetotrem.model.Empacotador;
import com.projetotrem.model.Trem;
import com.projetotrem.sync.GerenteDaEstacao;

import javax.swing.*;
import java.awt.*;

public class TelaPrincipalOriginal extends JFrame{
    private GerenteDaEstacao gerenteDaEstacao;
    private CenarioPanel cenarioPanel; // O nosso painel customizado

    public TelaPrincipalOriginal(int m, int n, int numEmpacotadores, long tempoViagem, long tempoPacote) {

        // 1. Iniciar o Backend
        this.gerenteDaEstacao = new GerenteDaEstacao(n, m);

        // 2. Iniciar o Frontend (o Painel de Desenho)
        // Passamos o gerente para ele poder ler os status
        this.cenarioPanel = new CenarioPanel(gerenteDaEstacao);

        // 3. Configurar a Janela (JFrame)
        setTitle("Simulação Trem de Carga - Visual");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Adicionamos nosso painel de desenho à janela
        add(cenarioPanel);

        // Define o tamanho da janela para ser exatamente o tamanho do painel
        // (que definimos dentro do CenarioPanel)
        pack();

        setLocationRelativeTo(null); // Centralizar
        setResizable(false); // Não deixa o usuário redimensionar

        // 4. Iniciar as Threads de Trabalho (Backend)
        iniciarThreadsBackend(numEmpacotadores, tempoViagem, tempoPacote);
    }

    private void iniciarThreadsBackend(int numEmpacotadores, long tempoViagem, long tempoPacote) {
        System.out.println("Iniciando threads da simulação...");

        // Inicia N empacotadores
        for (int i = 0; i < numEmpacotadores; i++) {
            int id = i + 1;
            new Thread(new Empacotador(gerenteDaEstacao, id, tempoPacote)).start();
        }

        // Inicia a Thread do Trem
        new Thread(new Trem(gerenteDaEstacao, tempoViagem)).start();

        System.out.println("Threads iniciadas.");
    }

    // Você precisará atualizar sua 'TelaConfiguracao' para chamar:
    // new TelaPrincipalOrignial(m, n, ...).setVisible(true);
    // ...em vez de TelaSimulacao.
}
