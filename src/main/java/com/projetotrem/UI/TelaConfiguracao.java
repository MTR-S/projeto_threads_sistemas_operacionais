package com.projetotrem.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TelaConfiguracao extends JFrame {

    private JTextField txtCapacidadeDepositoM;
    private JTextField txtCapacidadeTremN;
    // private JTextField txtNumEmpacotadores; // [REMOVIDO]
    private JTextField txtTempoViagemTrem;
    private JTextField txtTempoEmpacotamento; // [REVISADO] - Vamos manter, mas usar como "default"

    private JButton btnIniciarSimulacao;

    public TelaConfiguracao() {

        setTitle("Configuração da Simulação - Trem de Carga");
        setSize(400, 220); // Reduzido um pouco a altura
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel painelFormulario = new JPanel(new GridLayout(0, 2, 5, 5));

        painelFormulario.add(new JLabel("Capacidade Depósito (M):"));
        txtCapacidadeDepositoM = new JTextField("10");
        painelFormulario.add(txtCapacidadeDepositoM);

        painelFormulario.add(new JLabel("Capacidade Trem (N):"));
        txtCapacidadeTremN = new JTextField("5");
        painelFormulario.add(txtCapacidadeTremN);

        // [REMOVIDO] Campo de Nº de Empacotadores

        painelFormulario.add(new JLabel("Tempo Viagem Trem (ms):"));
        txtTempoViagemTrem = new JTextField("5000");
        painelFormulario.add(txtTempoViagemTrem);

        painelFormulario.add(new JLabel("Tempo Padrão Pacote (ms):")); // Renomeado para clareza
        txtTempoEmpacotamento = new JTextField("2000");
        painelFormulario.add(txtTempoEmpacotamento);

        btnIniciarSimulacao = new JButton("Iniciar Simulação");

        painelPrincipal.add(painelFormulario, BorderLayout.CENTER);
        painelPrincipal.add(btnIniciarSimulacao, BorderLayout.SOUTH);

        add(painelPrincipal);

        btnIniciarSimulacao.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Chama o método que vai ler e validar os dados
                iniciarSimulacao();
            }
        });
    }

    /**
     * Este método é chamado quando o botão "Iniciar" é clicado.
     */
    private void iniciarSimulacao() {
        try {
            int m = Integer.parseInt(txtCapacidadeDepositoM.getText());
            int n = Integer.parseInt(txtCapacidadeTremN.getText());

            // int numEmpacotadores = Integer.parseInt(txtNumEmpacotadores.getText()); // [REMOVIDO]
            long tempoViagem = Long.parseLong(txtTempoViagemTrem.getText());
            long tempoPacotePadrao = Long.parseLong(txtTempoEmpacotamento.getText()); // Pega o tempo padrão

            if (m < n) {
                // Mostra uma popup de erro (JOptionPane)
                JOptionPane.showMessageDialog(this,
                        "Erro: A Capacidade do Depósito (M) deve ser >= que a do Trem (N).",
                        "Erro de Validação",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. (Por enquanto) Apenas imprimir no console
            System.out.println("--- Configuração Pronta ---");
            System.out.println("Depósito (M): " + m);
            System.out.println("Trem (N): " + n);
            // System.out.println("Empacotadores: " + numEmpacotadores); // [REMOVIDO]
            System.out.println("Tempo Viagem: " + tempoViagem);
            System.out.println("Tempo Pacote Padrão: " + tempoPacotePadrao);
            System.out.println("---------------------------");


            // [MODIFICADO] A chamada do construtor não passa mais 'numEmpacotadores'
            // Passamos o tempoPacotePadrao para que ele seja a sugestão na popup
            TelaPrincipalOriginal telaVisual = new TelaPrincipalOriginal(m, n, tempoViagem, tempoPacotePadrao);
            telaVisual.setVisible(true);

            // 4. Fechar e destruir esta janela de configuração
            this.dispose();

        } catch (NumberFormatException ex) {
            // Captura erro se o usuário digitar "abc" em vez de "123"
            JOptionPane.showMessageDialog(this,
                    "Por favor, insira apenas números válidos nos campos.",
                    "Erro de Formato",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Método Main para executar esta janela de forma independente.
     */
    public static void main(String[] args) {
        // Todas as aplicações Swing DEVEM iniciar desta forma:
        // Isso coloca a UI na "Event Dispatch Thread" (EDT),
        // que é a thread oficial de UI do Swing.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TelaConfiguracao().setVisible(true);
            }
        });
    }
}