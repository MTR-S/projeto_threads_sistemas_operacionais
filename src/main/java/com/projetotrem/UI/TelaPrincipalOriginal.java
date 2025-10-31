package com.projetotrem.UI;

import com.projetotrem.model.Empacotador;
import com.projetotrem.model.Trem;
import com.projetotrem.sync.GerenteDaEstacao;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TelaPrincipalOriginal extends JFrame {
    private GerenteDaEstacao gerenteDaEstacao;
    private CenarioPanel cenarioPanel; // O nosso painel customizado

    // --- (NOVO) ---
    private long tempoPacotePadrao;
    private int proximoIdEmpacotador = 1; // Contador para IDs únicos

    // [CONSTRUTOR MODIFICADO]
    public TelaPrincipalOriginal(int m, int n, long tempoViagem, long tempoPacote) {

        // 1. Iniciar o Backend
        this.gerenteDaEstacao = new GerenteDaEstacao(n, m);
        this.tempoPacotePadrao = tempoPacote; // Salva o tempo padrão

        // 2. Iniciar o Frontend (o Painel de Desenho)
        this.cenarioPanel = new CenarioPanel(gerenteDaEstacao);

        // 3. Configurar a Janela (JFrame)
        setTitle("Simulação Trem de Carga - Visual");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // (NOVO) Usar BorderLayout para adicionar o botão abaixo
        setLayout(new BorderLayout());

        // Adicionamos nosso painel de desenho ao CENTRO
        add(cenarioPanel, BorderLayout.CENTER);

        // --- (NOVO) Criar o Painel de Controle com o Botão ---
        JPanel painelControle = new JPanel();
        painelControle.setBackground(Color.DARK_GRAY);

        JButton btnCriarEmpacotador = new JButton("Criar Empacotador");
        btnCriarEmpacotador.setFont(new Font("Arial", Font.BOLD, 14));
        btnCriarEmpacotador.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Chama a função para criar um novo empacotador
                criarNovoEmpacotador();
            }
        });

        painelControle.add(btnCriarEmpacotador);

        // #############################################################
        // ### ÚNICA ALTERAÇÃO AQUI ###
        // #############################################################
        // O painel de controle agora vai na parte NORTE (em cima)
        add(painelControle, BorderLayout.NORTH);
        // #############################################################

        // Define o tamanho da janela para ser exatamente o tamanho do painel
        pack(); // O pack() agora vai considerar o CenarioPanel E o painelControle

        setLocationRelativeTo(null); // Centralizar
        setResizable(false); // Não deixa o usuário redimensionar

        // 4. Iniciar as Threads de Trabalho (Backend)
        // [MODIFICADO] Passa o tempo de viagem, mas não o 'numEmpacotadores'
        iniciarThreadsBackend(tempoViagem);
    }

    // [MÉTODO MODIFICADO]
    private void iniciarThreadsBackend(long tempoViagem) {
        System.out.println("Iniciando threads da simulação...");

        // [REMOVIDO] Loop de criação de empacotadores
        // for (int i = 0; i < numEmpacotadores; i++) { ... }

        // Inicia a Thread do Trem (isso continua igual)
        new Thread(new Trem(gerenteDaEstacao, tempoViagem)).start();

        System.out.println("Thread do Trem iniciada. Aguardando criação de empacotadores...");
    }

    // --- (NOVO) Método para criar um empacotador ---
    private void criarNovoEmpacotador() {
        // 1. Pergunta o tempo ao usuário
        String resposta = JOptionPane.showInputDialog(
                this,
                "Qual o tempo de empacotamento (em segundos)?",
                "Criar Novo Empacotador",
                JOptionPane.QUESTION_MESSAGE
        );

        // Se o usuário clicou "Cancelar" ou fechou a janela, 'resposta' será null
        if (resposta == null || resposta.trim().isEmpty()) {
            return; // Não faz nada
        }

        try {
            // 2. Converte a resposta (de segundos para milissegundos)
            double segundos = Double.parseDouble(resposta.trim());
            long tempoPacoteMs = (long) (segundos * 1000);

            if (tempoPacoteMs <= 0) {
                JOptionPane.showMessageDialog(this, "O tempo deve ser um número positivo.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. Pega o próximo ID
            int id = this.proximoIdEmpacotador;
            this.proximoIdEmpacotador++; // Incrementa para o próximo

            // 4. Cria e inicia a nova thread do Empacotador
            System.out.println(String.format("Criando Empacotador [%d] com tempo de %d ms", id, tempoPacoteMs));
            Thread t = new Thread(new Empacotador(gerenteDaEstacao, id, tempoPacoteMs));
            t.start();

            // 5. O CenarioPanel vai atualizar sozinho!

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido. Por favor, insira um número (ex: 2.5).", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}