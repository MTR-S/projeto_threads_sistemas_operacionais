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
    private CenarioPanel cenarioPanel;

    private int proximoIdEmpacotador = 1;

    public TelaPrincipalOriginal(int m, int n, long tempoViagem) {

        this.gerenteDaEstacao = new GerenteDaEstacao(n, m);

        this.cenarioPanel = new CenarioPanel(gerenteDaEstacao, tempoViagem);

        setTitle("Simulação Trem de Carga - Visual");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        add(cenarioPanel, BorderLayout.CENTER);

        JPanel painelControle = new JPanel();
        painelControle.setBackground(Color.DARK_GRAY);

        JButton btnCriarEmpacotador = new JButton("Criar Empacotador");
        btnCriarEmpacotador.setFont(new Font("Arial", Font.BOLD, 14));
        btnCriarEmpacotador.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                criarNovoEmpacotador();
            }
        });

        painelControle.add(btnCriarEmpacotador);

        add(painelControle, BorderLayout.NORTH);

        pack();

        setLocationRelativeTo(null);
        setResizable(false);

        iniciarThreadsBackend(tempoViagem);
    }

    private void iniciarThreadsBackend(long tempoViagem) {
        System.out.println("Iniciando threads da simulação...");

        new Thread(new Trem(gerenteDaEstacao, tempoViagem)).start();

        System.out.println("Thread do Trem iniciada. Aguardando criação de empacotadores...");
    }

    private void criarNovoEmpacotador() {
        String resposta = JOptionPane.showInputDialog(
                this,
                "Qual o tempo de empacotamento (em segundos)?",
                "Criar Novo Empacotador",
                JOptionPane.QUESTION_MESSAGE
        );

        if (resposta == null || resposta.trim().isEmpty()) {
            return; // Não faz nada
        }

        try {
            double segundos = Double.parseDouble(resposta.trim());
            long tempoPacoteMs = (long) (segundos * 1000);

            if (tempoPacoteMs <= 0) {
                JOptionPane.showMessageDialog(this, "O tempo deve ser um número positivo.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int id = this.proximoIdEmpacotador;
            this.proximoIdEmpacotador++;

            System.out.println(String.format("Criando Empacotador [%d] com tempo de %d ms", id, tempoPacoteMs));
            Thread t = new Thread(new Empacotador(gerenteDaEstacao, id, tempoPacoteMs));
            t.start();


        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido. Por favor, insira um número (ex: 2.5).", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}